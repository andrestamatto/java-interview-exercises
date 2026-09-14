package com.example.catalog;

import java.util.Objects;

public record Sku(String value) {
  public Sku {
    Objects.requireNonNull(value, "value");
    if (value.isBlank()) {
      throw new IllegalArgumentException("value must not be blank");
    }
  }
}
