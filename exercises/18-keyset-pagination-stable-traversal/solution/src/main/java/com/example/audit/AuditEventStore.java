package com.example.audit;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface AuditEventStore {
  long loadHighWatermark(UUID tenantId);

  List<AuditEvent> loadOffsetPage(UUID tenantId, int offset, int limit);

  List<AuditEvent> loadFirstPage(UUID tenantId, long highWatermark, int limit);

  List<AuditEvent> loadNextPage(
      UUID tenantId, long highWatermark, Instant beforeCreatedAt, long beforeId, int limit);
}
