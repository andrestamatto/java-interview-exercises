package com.example.catalog;

import java.util.Objects;

public record CatalogItem(Sku sku, String name, long unitPriceCents) {
  public CatalogItem {
    Objects.requireNonNull(sku, "sku");
    Objects.requireNonNull(name, "name");
    if (name.isBlank() || unitPriceCents < 0) {
      throw new IllegalArgumentException("item fields are invalid");
    }
  }
}
