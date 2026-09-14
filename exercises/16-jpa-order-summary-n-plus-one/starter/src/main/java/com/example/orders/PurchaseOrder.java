package com.example.orders;

import java.util.Objects;
import java.util.UUID;

public record PurchaseOrder(UUID id, String customer) {
  public PurchaseOrder {
    Objects.requireNonNull(id, "id");
    if (Objects.requireNonNull(customer, "customer").isBlank()) {
      throw new IllegalArgumentException("customer must not be blank");
    }
  }
}
