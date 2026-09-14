package com.example.catalog;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public record CatalogReadResult(
    Map<Sku, CatalogItem> foundItems, Set<Sku> missingSkus, Set<Sku> unresolvedSkus) {
  public CatalogReadResult {
    Objects.requireNonNull(foundItems, "foundItems");
    Objects.requireNonNull(missingSkus, "missingSkus");
    Objects.requireNonNull(unresolvedSkus, "unresolvedSkus");
    foundItems = Map.copyOf(new LinkedHashMap<>(foundItems));
    missingSkus = Set.copyOf(new LinkedHashSet<>(missingSkus));
    unresolvedSkus = Set.copyOf(new LinkedHashSet<>(unresolvedSkus));
    if (!java.util.Collections.disjoint(foundItems.keySet(), missingSkus)
        || !java.util.Collections.disjoint(foundItems.keySet(), unresolvedSkus)
        || !java.util.Collections.disjoint(missingSkus, unresolvedSkus)) {
      throw new IllegalArgumentException("a SKU cannot have more than one outcome");
    }
  }
}
