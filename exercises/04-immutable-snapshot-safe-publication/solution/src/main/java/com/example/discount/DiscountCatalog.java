package com.example.discount;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public final class DiscountCatalog {
  private final AtomicReference<DiscountSnapshot> snapshot =
      new AtomicReference<>(new DiscountSnapshot(Map.of()));

  public void publish(Map<String, Integer> percentageByProduct) {
    snapshot.set(new DiscountSnapshot(percentageByProduct));
  }

  public int discountFor(String productId) {
    return snapshot.get().percentageByProduct().getOrDefault(productId, 0);
  }
}
