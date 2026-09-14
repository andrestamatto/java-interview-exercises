package com.example.catalog;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/** Deliberately issues one remote operation for every distinct SKU. */
public final class CatalogLookupService {
  private final CatalogReadClient client;

  public CatalogLookupService(
      CatalogReadClient client, RetryBackoff retryBackoff, int maxAttempts) {
    this.client = Objects.requireNonNull(client, "client");
    Objects.requireNonNull(retryBackoff, "retryBackoff");
    if (maxAttempts < 1) {
      throw new IllegalArgumentException("maxAttempts must be at least one");
    }
  }

  public CatalogReadResult lookup(Collection<Sku> requestedSkus) {
    Objects.requireNonNull(requestedSkus, "requestedSkus");
    Set<Sku> requested = new LinkedHashSet<>(requestedSkus);
    if (requested.contains(null)) {
      throw new IllegalArgumentException("requestedSkus must not contain null");
    }
    Map<Sku, CatalogItem> found = new LinkedHashMap<>();
    Set<Sku> missing = new LinkedHashSet<>();
    for (Sku sku : requested) {
      client.getItem(sku).ifPresentOrElse(item -> found.put(sku, item), () -> missing.add(sku));
    }
    return new CatalogReadResult(found, missing, Set.of());
  }
}
