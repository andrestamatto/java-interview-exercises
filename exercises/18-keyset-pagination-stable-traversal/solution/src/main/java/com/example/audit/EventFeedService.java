package com.example.audit;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class EventFeedService {
  private static final int MAX_PAGE_SIZE = 100;
  private static final String CURSOR_VERSION = "v1";

  private final AuditEventStore store;

  public EventFeedService(AuditEventStore store) {
    this.store = Objects.requireNonNull(store, "store");
  }

  public EventPage page(UUID tenantId, int pageSize, Optional<String> cursor) {
    Objects.requireNonNull(tenantId, "tenantId");
    Objects.requireNonNull(cursor, "cursor");
    validatePageSize(pageSize);

    if (cursor.isEmpty()) {
      long highWatermark = store.loadHighWatermark(tenantId);
      return pageFrom(
          store.loadFirstPage(tenantId, highWatermark, pageSize + 1),
          pageSize,
          tenantId,
          highWatermark);
    }

    Cursor decoded = decode(cursor.orElseThrow(), tenantId);
    return pageFrom(
        store.loadNextPage(
            tenantId,
            decoded.highWatermark(),
            decoded.lastCreatedAt(),
            decoded.lastId(),
            pageSize + 1),
        pageSize,
        tenantId,
        decoded.highWatermark());
  }

  private static EventPage pageFrom(
      List<AuditEvent> candidates, int pageSize, UUID tenantId, long highWatermark) {
    boolean hasMore = candidates.size() > pageSize;
    List<AuditEvent> events = candidates.stream().limit(pageSize).toList();
    Optional<String> nextCursor =
        hasMore ? Optional.of(encode(tenantId, highWatermark, events.getLast())) : Optional.empty();
    return new EventPage(events, nextCursor);
  }

  private static String encode(UUID tenantId, long highWatermark, AuditEvent last) {
    String contents =
        String.join(
            "|",
            CURSOR_VERSION,
            tenantId.toString(),
            Long.toString(highWatermark),
            Long.toString(last.createdAt().getEpochSecond()),
            Integer.toString(last.createdAt().getNano()),
            Long.toString(last.id()));
    return Base64.getUrlEncoder()
        .withoutPadding()
        .encodeToString(contents.getBytes(StandardCharsets.UTF_8));
  }

  private static Cursor decode(String encoded, UUID expectedTenantId) {
    try {
      String[] parts =
          new String(Base64.getUrlDecoder().decode(encoded), StandardCharsets.UTF_8)
              .split("\\|", -1);
      if (parts.length != 6 || !CURSOR_VERSION.equals(parts[0])) {
        throw new IllegalArgumentException("cursor has an unsupported shape");
      }
      UUID cursorTenantId = UUID.fromString(parts[1]);
      if (!expectedTenantId.equals(cursorTenantId)) {
        throw new IllegalArgumentException("cursor belongs to another tenant");
      }
      long highWatermark = Long.parseLong(parts[2]);
      Instant lastCreatedAt =
          Instant.ofEpochSecond(Long.parseLong(parts[3]), Long.parseLong(parts[4]));
      long lastId = Long.parseLong(parts[5]);
      if (highWatermark < 0 || lastId <= 0) {
        throw new IllegalArgumentException("cursor contains an invalid position");
      }
      return new Cursor(highWatermark, lastCreatedAt, lastId);
    } catch (RuntimeException exception) {
      throw new IllegalArgumentException("cursor is invalid", exception);
    }
  }

  private static void validatePageSize(int pageSize) {
    if (pageSize <= 0 || pageSize > MAX_PAGE_SIZE) {
      throw new IllegalArgumentException("pageSize must be between 1 and " + MAX_PAGE_SIZE);
    }
  }

  private record Cursor(long highWatermark, Instant lastCreatedAt, long lastId) {}
}
