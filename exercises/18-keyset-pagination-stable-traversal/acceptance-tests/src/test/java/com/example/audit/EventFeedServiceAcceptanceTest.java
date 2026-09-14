package com.example.audit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("acceptance")
class EventFeedServiceAcceptanceTest {
  private static final UUID TENANT = UUID.fromString("00000000-0000-0000-0000-000000000018");
  private static final UUID OTHER_TENANT = UUID.fromString("00000000-0000-0000-0000-000000000019");

  @Test
  void traversesTheOriginalFeedExactlyOnceWhenANewerEventArrivesBetweenPages() {
    RecordingStore store = new RecordingStore(eventsFor(TENANT));
    EventFeedService service = new EventFeedService(store);

    EventPage first = service.page(TENANT, 2, Optional.empty());
    store.insert(event(TENANT, 6, "2026-01-01T00:00:06Z", 6));
    EventPage second = service.page(TENANT, 2, first.nextCursor());
    EventPage third = service.page(TENANT, 2, second.nextCursor());

    assertThat(ids(first, second, third)).containsExactly(5L, 4L, 3L, 2L, 1L);
    assertThat(ids(first, second, third)).doesNotContain(6L);
    assertThat(third.nextCursor()).isEmpty();
    assertThat(store.highWatermarkLoads).isEqualTo(1);
    assertThat(store.firstPageLoads).isEqualTo(1);
    assertThat(store.nextPageLoads).isEqualTo(2);
    assertThat(store.offsetPageLoads).isZero();
  }

  @Test
  void rejectsMalformedAndCrossTenantCursors() {
    RecordingStore store = new RecordingStore(eventsFor(TENANT));
    store.insert(event(OTHER_TENANT, 10, "2026-01-01T00:00:10Z", 10));
    store.insert(event(OTHER_TENANT, 9, "2026-01-01T00:00:09Z", 9));
    EventFeedService service = new EventFeedService(store);

    assertThatIllegalArgumentException().isThrownBy(() -> service.page(TENANT, 2, Optional.of("not-a-cursor")));

    String otherTenantCursor = service.page(OTHER_TENANT, 1, Optional.empty()).nextCursor().orElseThrow();
    assertThatIllegalArgumentException()
        .isThrownBy(() -> service.page(TENANT, 1, Optional.of(otherTenantCursor)));
  }

  @Test
  void rejectsAnOutOfRangePageSize() {
    EventFeedService service = new EventFeedService(new RecordingStore(eventsFor(TENANT)));

    assertThatIllegalArgumentException().isThrownBy(() -> service.page(TENANT, 0, Optional.empty()));
    assertThatIllegalArgumentException().isThrownBy(() -> service.page(TENANT, 101, Optional.empty()));
  }

  private static List<AuditEvent> eventsFor(UUID tenantId) {
    return List.of(
        event(tenantId, 5, "2026-01-01T00:00:05Z", 5),
        event(tenantId, 4, "2026-01-01T00:00:04Z", 4),
        event(tenantId, 3, "2026-01-01T00:00:03Z", 3),
        event(tenantId, 2, "2026-01-01T00:00:02Z", 2),
        event(tenantId, 1, "2026-01-01T00:00:01Z", 1));
  }

  private static AuditEvent event(UUID tenantId, long id, String createdAt, long ingestSequence) {
    return new AuditEvent(tenantId, id, Instant.parse(createdAt), ingestSequence, "event-" + id);
  }

  private static List<Long> ids(EventPage... pages) {
    return java.util.Arrays.stream(pages)
        .flatMap(page -> page.events().stream())
        .map(AuditEvent::id)
        .toList();
  }

  private static final class RecordingStore implements AuditEventStore {
    private static final Comparator<AuditEvent> DESCENDING_POSITION =
        Comparator.comparing(AuditEvent::createdAt)
            .thenComparingLong(AuditEvent::id)
            .reversed();

    private final List<AuditEvent> events;
    private int highWatermarkLoads;
    private int offsetPageLoads;
    private int firstPageLoads;
    private int nextPageLoads;

    private RecordingStore(List<AuditEvent> events) {
      this.events = new ArrayList<>(events);
    }

    private void insert(AuditEvent event) {
      events.add(event);
    }

    @Override
    public long loadHighWatermark(UUID tenantId) {
      highWatermarkLoads++;
      return events.stream()
          .filter(event -> event.tenantId().equals(tenantId))
          .mapToLong(AuditEvent::ingestSequence)
          .max()
          .orElse(0L);
    }

    @Override
    public List<AuditEvent> loadOffsetPage(UUID tenantId, int offset, int limit) {
      offsetPageLoads++;
      return ordered(tenantId).stream().skip(offset).limit(limit).toList();
    }

    @Override
    public List<AuditEvent> loadFirstPage(UUID tenantId, long highWatermark, int limit) {
      firstPageLoads++;
      return ordered(tenantId).stream()
          .filter(event -> event.ingestSequence() <= highWatermark)
          .limit(limit)
          .toList();
    }

    @Override
    public List<AuditEvent> loadNextPage(
        UUID tenantId, long highWatermark, Instant beforeCreatedAt, long beforeId, int limit) {
      nextPageLoads++;
      return ordered(tenantId).stream()
          .filter(event -> event.ingestSequence() <= highWatermark)
          .filter(
              event ->
                  event.createdAt().isBefore(beforeCreatedAt)
                      || (event.createdAt().equals(beforeCreatedAt) && event.id() < beforeId))
          .limit(limit)
          .toList();
    }

    private List<AuditEvent> ordered(UUID tenantId) {
      return events.stream()
          .filter(event -> event.tenantId().equals(tenantId))
          .sorted(DESCENDING_POSITION)
          .toList();
    }
  }
}
