package com.example.discount;

import java.util.Map;
import java.util.Objects;

public record DiscountSnapshot(Map<String, Integer> percentageByProduct) {
  public DiscountSnapshot {
    percentageByProduct = Objects.requireNonNull(percentageByProduct, "percentageByProduct");
    percentageByProduct.forEach(
        (productId, percentage) -> {
          if (productId.isBlank() || percentage < 0 || percentage > 100) {
            throw new IllegalArgumentException(
                "a discount must have a product id and a percentage from 0 to 100");
          }
        });
  }
}
