package com.example.orders;

import java.util.Objects;
import java.util.UUID;

public record OrderSummary(UUID orderId, String customer, long totalCents) {
  public OrderSummary {
    Objects.requireNonNull(orderId, "orderId");
    Objects.requireNonNull(customer, "customer");
    if (totalCents < 0) {
      throw new IllegalArgumentException("totalCents must not be negative");
    }
  }
}
