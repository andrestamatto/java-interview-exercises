package com.example.catalog;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public record BatchReadResponse(Map<Sku, CatalogItem> items, Set<Sku> unprocessedSkus) {
  public BatchReadResponse {
    Objects.requireNonNull(items, "items");
    Objects.requireNonNull(unprocessedSkus, "unprocessedSkus");
    items = Map.copyOf(new LinkedHashMap<>(items));
    unprocessedSkus = Set.copyOf(new LinkedHashSet<>(unprocessedSkus));
    if (items.entrySet().stream()
        .anyMatch(entry -> !entry.getKey().equals(entry.getValue().sku()))) {
      throw new IllegalArgumentException("item map keys must match item SKUs");
    }
  }
}
