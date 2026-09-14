package com.example.catalog;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.interceptor.Context;
import software.amazon.awssdk.core.interceptor.ExecutionAttributes;
import software.amazon.awssdk.core.interceptor.ExecutionInterceptor;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeDefinition;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.BatchGetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.CreateTableRequest;
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement;
import software.amazon.awssdk.services.dynamodb.model.KeyType;
import software.amazon.awssdk.services.dynamodb.model.ProvisionedThroughput;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.ScalarAttributeType;

@Tag("acceptance")
@Tag("integration")
@Testcontainers(disabledWithoutDocker = true)
class DynamoDbLocalIntegrationAcceptanceTest {
  private static final String TABLE = "catalog-items";

  @Container
  private static final GenericContainer<?> DYNAMODB_LOCAL =
      new GenericContainer<>("amazon/dynamodb-local:3.0.0").withExposedPorts(8000);

  private static DynamoDbClient dynamoDb;
  private static BatchRequestCounter requestCounter;

  @BeforeAll
  static void startClientAndCreateTable() {
    requestCounter = new BatchRequestCounter();
    dynamoDb =
        DynamoDbClient.builder()
            .endpointOverride(
                URI.create("http://" + DYNAMODB_LOCAL.getHost() + ":" + DYNAMODB_LOCAL.getMappedPort(8000)))
            .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create("test", "test")))
            .region(Region.US_EAST_1)
            .httpClientBuilder(UrlConnectionHttpClient.builder())
            .overrideConfiguration(builder -> builder.addExecutionInterceptor(requestCounter))
            .build();
    dynamoDb.createTable(
        CreateTableRequest.builder()
            .tableName(TABLE)
            .keySchema(KeySchemaElement.builder().attributeName("sku").keyType(KeyType.HASH).build())
            .attributeDefinitions(
                AttributeDefinition.builder().attributeName("sku").attributeType(ScalarAttributeType.S).build())
            .provisionedThroughput(
                ProvisionedThroughput.builder().readCapacityUnits(10L).writeCapacityUnits(10L).build())
            .build());
  }

  @AfterAll
  static void closeClient() {
    if (dynamoDb != null) {
      dynamoDb.close();
    }
  }

  @Test
  void readsOneHundredAndOneItemsWithTwoBatchGetRequests() {
    List<Sku> requested = new ArrayList<>();
    for (int index = 0; index < 101; index++) {
      Sku sku = new Sku("sku-" + index);
      requested.add(sku);
      dynamoDb.putItem(
          PutItemRequest.builder()
              .tableName(TABLE)
              .item(
                  Map.of(
                      "sku", AttributeValue.builder().s(sku.value()).build(),
                      "name", AttributeValue.builder().s("item-" + index).build(),
                      "unitPriceCents", AttributeValue.builder().n("100").build()))
              .build());
    }
    requestCounter.reset();

    CatalogReadResult result =
        new CatalogLookupService(new AwsDynamoDbCatalogReadClient(dynamoDb, TABLE), retry -> {}, 3)
            .lookup(requested);

    assertThat(result.foundItems()).hasSize(101);
    assertThat(result.missingSkus()).isEmpty();
    assertThat(result.unresolvedSkus()).isEmpty();
    assertThat(requestCounter.batchGetCalls.get()).isEqualTo(2);
    assertThat(requestCounter.largestKeyCount.get()).isLessThanOrEqualTo(100);
  }

  private static final class BatchRequestCounter implements ExecutionInterceptor {
    private final AtomicInteger batchGetCalls = new AtomicInteger();
    private final AtomicInteger largestKeyCount = new AtomicInteger();

    @Override
    public void beforeExecution(Context.BeforeExecution context, ExecutionAttributes executionAttributes) {
      if (context.request() instanceof BatchGetItemRequest request) {
        batchGetCalls.incrementAndGet();
        int keyCount = request.requestItems().values().stream().mapToInt(keys -> keys.keys().size()).sum();
        largestKeyCount.accumulateAndGet(keyCount, Math::max);
      }
    }

    private void reset() {
      batchGetCalls.set(0);
      largestKeyCount.set(0);
    }
  }
}
