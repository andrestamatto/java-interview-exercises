package com.example.audit;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class EventFeedService {
  private static final int MAX_PAGE_SIZE = 100;

  private final AuditEventStore store;

  public EventFeedService(AuditEventStore store) {
    this.store = Objects.requireNonNull(store, "store");
  }

  public EventPage page(UUID tenantId, int pageSize, Optional<String> cursor) {
    Objects.requireNonNull(tenantId, "tenantId");
    Objects.requireNonNull(cursor, "cursor");
    validatePageSize(pageSize);

    int offset = cursor.map(Integer::parseInt).orElse(0);
    List<AuditEvent> candidates = store.loadOffsetPage(tenantId, offset, pageSize + 1);
    boolean hasMore = candidates.size() > pageSize;
    List<AuditEvent> events = candidates.stream().limit(pageSize).toList();
    Optional<String> nextCursor =
        hasMore ? Optional.of(Integer.toString(offset + pageSize)) : Optional.empty();
    return new EventPage(events, nextCursor);
  }

  private static void validatePageSize(int pageSize) {
    if (pageSize <= 0 || pageSize > MAX_PAGE_SIZE) {
      throw new IllegalArgumentException("pageSize must be between 1 and " + MAX_PAGE_SIZE);
    }
  }
}
