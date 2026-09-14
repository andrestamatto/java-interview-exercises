package com.example.catalog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class CatalogLookupService {
  public static final int MAX_KEYS_PER_BATCH = 100;

  private final CatalogReadClient client;
  private final RetryBackoff retryBackoff;
  private final int maxAttempts;

  public CatalogLookupService(
      CatalogReadClient client, RetryBackoff retryBackoff, int maxAttempts) {
    this.client = Objects.requireNonNull(client, "client");
    this.retryBackoff = Objects.requireNonNull(retryBackoff, "retryBackoff");
    if (maxAttempts < 1) {
      throw new IllegalArgumentException("maxAttempts must be at least one");
    }
    this.maxAttempts = maxAttempts;
  }

  public CatalogReadResult lookup(Collection<Sku> requestedSkus) {
    Objects.requireNonNull(requestedSkus, "requestedSkus");
    Set<Sku> requested = new LinkedHashSet<>(requestedSkus);
    if (requested.contains(null)) {
      throw new IllegalArgumentException("requestedSkus must not contain null");
    }

    Map<Sku, CatalogItem> found = new LinkedHashMap<>();
    Set<Sku> unresolved = new LinkedHashSet<>();
    List<Sku> request = new ArrayList<>(requested);
    for (int start = 0; start < request.size(); start += MAX_KEYS_PER_BATCH) {
      List<Sku> batch =
          request.subList(start, Math.min(start + MAX_KEYS_PER_BATCH, request.size()));
      unresolved.addAll(readBatch(batch, found));
    }
    Set<Sku> missing = new LinkedHashSet<>(requested);
    missing.removeAll(found.keySet());
    missing.removeAll(unresolved);
    return new CatalogReadResult(found, missing, unresolved);
  }

  private Set<Sku> readBatch(List<Sku> batch, Map<Sku, CatalogItem> found) {
    Set<Sku> pending = new LinkedHashSet<>(batch);
    for (int attempt = 1; attempt <= maxAttempts; attempt++) {
      BatchReadResponse response = client.batchGetItems(List.copyOf(pending));
      validateResponse(batch, response);
      found.putAll(response.items());
      pending.retainAll(response.unprocessedSkus());
      if (pending.isEmpty()) {
        return Set.of();
      }
      if (attempt < maxAttempts) {
        retryBackoff.awaitBeforeRetry(attempt);
      }
    }
    return Set.copyOf(pending);
  }

  private static void validateResponse(List<Sku> batch, BatchReadResponse response) {
    Set<Sku> requested = Set.copyOf(batch);
    if (!requested.containsAll(response.items().keySet())
        || !requested.containsAll(response.unprocessedSkus())) {
      throw new IllegalStateException("catalog client returned a SKU outside the requested batch");
    }
    if (!java.util.Collections.disjoint(response.items().keySet(), response.unprocessedSkus())) {
      throw new IllegalStateException("a SKU cannot be returned and unprocessed in one response");
    }
  }
}
