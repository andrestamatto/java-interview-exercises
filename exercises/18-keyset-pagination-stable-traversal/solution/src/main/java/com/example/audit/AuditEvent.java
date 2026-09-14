package com.example.audit;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record AuditEvent(
    UUID tenantId, long id, Instant createdAt, long ingestSequence, String payload) {
  public AuditEvent {
    Objects.requireNonNull(tenantId, "tenantId");
    if (id <= 0) {
      throw new IllegalArgumentException("id must be positive");
    }
    Objects.requireNonNull(createdAt, "createdAt");
    if (ingestSequence <= 0) {
      throw new IllegalArgumentException("ingestSequence must be positive");
    }
    if (payload == null || payload.isBlank()) {
      throw new IllegalArgumentException("payload must not be blank");
    }
  }
}
