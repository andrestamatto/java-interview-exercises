package com.example.orders;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record Order(UUID id, Instant createdAt, long totalCents) {
  public Order {
    Objects.requireNonNull(id, "id must not be null");
    Objects.requireNonNull(createdAt, "createdAt must not be null");
    if (totalCents < 0) {
      throw new IllegalArgumentException("totalCents must not be negative");
    }
  }
}
