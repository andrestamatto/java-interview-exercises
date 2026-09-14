package com.example.catalog;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.BatchGetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.KeysAndAttributes;

/** AWS SDK v2 adapter supplied to make the starter executable against DynamoDB Local. */
public final class AwsDynamoDbCatalogReadClient implements CatalogReadClient {
  private static final String SKU_ATTRIBUTE = "sku";
  private static final String NAME_ATTRIBUTE = "name";
  private static final String UNIT_PRICE_CENTS_ATTRIBUTE = "unitPriceCents";

  private final DynamoDbClient dynamoDb;
  private final String tableName;

  public AwsDynamoDbCatalogReadClient(DynamoDbClient dynamoDb, String tableName) {
    this.dynamoDb = Objects.requireNonNull(dynamoDb, "dynamoDb");
    this.tableName = Objects.requireNonNull(tableName, "tableName");
    if (tableName.isBlank()) {
      throw new IllegalArgumentException("tableName must not be blank");
    }
  }

  @Override
  public Optional<CatalogItem> getItem(Sku sku) {
    Objects.requireNonNull(sku, "sku");
    return Optional.ofNullable(
            dynamoDb
                .getItem(GetItemRequest.builder().tableName(tableName).key(key(sku)).build())
                .item())
        .filter(item -> !item.isEmpty())
        .map(AwsDynamoDbCatalogReadClient::toCatalogItem);
  }

  @Override
  public BatchReadResponse batchGetItems(List<Sku> skus) {
    Objects.requireNonNull(skus, "skus");
    if (skus.isEmpty() || skus.size() > 100) {
      throw new IllegalArgumentException("a batch must contain between 1 and 100 SKUs");
    }
    List<Map<String, AttributeValue>> keys =
        skus.stream().map(AwsDynamoDbCatalogReadClient::key).toList();
    var response =
        dynamoDb.batchGetItem(
            BatchGetItemRequest.builder()
                .requestItems(Map.of(tableName, KeysAndAttributes.builder().keys(keys).build()))
                .build());
    Map<Sku, CatalogItem> items = new LinkedHashMap<>();
    response.responses().getOrDefault(tableName, List.of()).stream()
        .map(AwsDynamoDbCatalogReadClient::toCatalogItem)
        .forEach(item -> items.put(item.sku(), item));
    Set<Sku> unprocessed = new LinkedHashSet<>();
    response
        .unprocessedKeys()
        .getOrDefault(tableName, KeysAndAttributes.builder().build())
        .keys()
        .forEach(key -> unprocessed.add(new Sku(requiredString(key, SKU_ATTRIBUTE))));
    return new BatchReadResponse(items, unprocessed);
  }

  private static Map<String, AttributeValue> key(Sku sku) {
    return Map.of(SKU_ATTRIBUTE, AttributeValue.builder().s(sku.value()).build());
  }

  private static CatalogItem toCatalogItem(Map<String, AttributeValue> item) {
    return new CatalogItem(
        new Sku(requiredString(item, SKU_ATTRIBUTE)),
        requiredString(item, NAME_ATTRIBUTE),
        Long.parseLong(requiredString(item, UNIT_PRICE_CENTS_ATTRIBUTE)));
  }

  private static String requiredString(
      Map<String, AttributeValue> attributes, String attributeName) {
    AttributeValue value = attributes.get(attributeName);
    if (value == null || value.s() == null && value.n() == null) {
      throw new IllegalStateException("DynamoDB item is missing " + attributeName);
    }
    return value.s() != null ? value.s() : value.n();
  }
}
