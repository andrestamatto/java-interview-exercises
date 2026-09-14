package com.example.orders;

import java.util.Objects;

public record OrderLine(String sku, int quantity, long unitPriceCents) {
  public OrderLine {
    if (Objects.requireNonNull(sku, "sku").isBlank() || quantity <= 0 || unitPriceCents < 0) {
      throw new IllegalArgumentException(
          "a line needs a sku, positive quantity, and non-negative price");
    }
  }

  long subtotalCents() {
    return Math.multiplyExact(quantity, unitPriceCents);
  }
}
