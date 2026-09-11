package com.example.discount;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Map;
import org.junit.jupiter.api.Test;

class DiscountSnapshotTest {
  @Test
  void missingProductUsesNoDiscount() {
    DiscountCatalog catalog = new DiscountCatalog();
    catalog.publish(Map.of("book", 10));

    assertThat(catalog.discountFor("unknown")).isZero();
  }

  @Test
  void invalidDiscountIsRejected() {
    assertThatThrownBy(() -> new DiscountSnapshot(Map.of("book", 101)))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
