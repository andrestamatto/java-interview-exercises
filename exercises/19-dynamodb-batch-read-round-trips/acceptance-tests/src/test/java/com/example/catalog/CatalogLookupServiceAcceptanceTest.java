package com.example.catalog;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("acceptance")
class CatalogLookupServiceAcceptanceTest {
  @Test
  void chunksAtTheDynamoDbLimitWithoutUsingSingleItemReads() {
    ScriptedCatalogClient client = new ScriptedCatalogClient();
    List<Sku> requested = skus(201);
    client.returnAllRequestedItems();

    CatalogReadResult result = new CatalogLookupService(client, retry -> {}, 3).lookup(requested);

    assertThat(result.foundItems()).hasSize(201);
    assertThat(result.missingSkus()).isEmpty();
    assertThat(result.unresolvedSkus()).isEmpty();
    assertThat(client.singleItemCalls).isZero();
    assertThat(client.batchCalls).hasSize(3);
    assertThat(client.batchCalls).allSatisfy(batch -> assertThat(batch).hasSizeLessThanOrEqualTo(100));
    assertThat(client.batchCalls).extracting(List::size).containsExactly(100, 100, 1);
  }

  @Test
  void retriesOnlyUnprocessedKeysAndReportsKeysStillUnresolvedAfterTheBound() {
    Sku one = new Sku("sku-1");
    Sku two = new Sku("sku-2");
    Sku three = new Sku("sku-3");
    ScriptedCatalogClient client = new ScriptedCatalogClient();
    client.enqueue(new BatchReadResponse(Map.of(one, item(one)), Set.of(two, three)));
    client.enqueue(new BatchReadResponse(Map.of(two, item(two)), Set.of(three)));
    client.enqueue(new BatchReadResponse(Map.of(), Set.of(three)));
    List<Integer> retries = new ArrayList<>();

    CatalogReadResult result =
        new CatalogLookupService(client, retries::add, 3).lookup(List.of(one, two, three));

    assertThat(client.singleItemCalls).isZero();
    assertThat(client.batchCalls)
        .containsExactly(List.of(one, two, three), List.of(two, three), List.of(three));
    assertThat(retries).containsExactly(1, 2);
    assertThat(result.foundItems()).containsKeys(one, two);
    assertThat(result.missingSkus()).isEmpty();
    assertThat(result.unresolvedSkus()).containsExactly(three);
  }

  @Test
  void distinguishesAbsentItemsFromUnprocessedItems() {
    Sku present = new Sku("present");
    Sku absent = new Sku("absent");
    ScriptedCatalogClient client = new ScriptedCatalogClient();
    client.enqueue(new BatchReadResponse(Map.of(present, item(present)), Set.of()));

    CatalogReadResult result = new CatalogLookupService(client, retry -> {}, 1).lookup(List.of(present, absent));

    assertThat(result.foundItems()).containsKey(present);
    assertThat(result.missingSkus()).containsExactly(absent);
    assertThat(result.unresolvedSkus()).isEmpty();
  }

  private static List<Sku> skus(int count) {
    return java.util.stream.IntStream.range(0, count).mapToObj(index -> new Sku("sku-" + index)).toList();
  }

  private static CatalogItem item(Sku sku) {
    return new CatalogItem(sku, "name-" + sku.value(), 100L);
  }

  private static final class ScriptedCatalogClient implements CatalogReadClient {
    private final ArrayDeque<BatchReadResponse> scriptedResponses = new ArrayDeque<>();
    private final List<List<Sku>> batchCalls = new ArrayList<>();
    private int singleItemCalls;
    private boolean returnAllRequestedItems;

    private void enqueue(BatchReadResponse response) {
      scriptedResponses.add(response);
    }

    private void returnAllRequestedItems() {
      returnAllRequestedItems = true;
    }

    @Override
    public Optional<CatalogItem> getItem(Sku sku) {
      singleItemCalls++;
      return Optional.of(item(sku));
    }

    @Override
    public BatchReadResponse batchGetItems(List<Sku> skus) {
      batchCalls.add(List.copyOf(skus));
      if (returnAllRequestedItems) {
        Map<Sku, CatalogItem> items = new LinkedHashMap<>();
        skus.forEach(sku -> items.put(sku, item(sku)));
        return new BatchReadResponse(items, Set.of());
      }
      return Optional.ofNullable(scriptedResponses.poll())
          .orElseThrow(() -> new AssertionError("unexpected batch request: " + skus));
    }
  }
}
