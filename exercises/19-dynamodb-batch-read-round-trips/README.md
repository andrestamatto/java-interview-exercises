# Batch DynamoDB Catalog Reads Without Excessive Round Trips

**Category:** Performance, data access, and resilience  
**Difficulty:** Specialist  
**Estimated time:** 120 minutes

## Scenario, symptoms, and business impact

An order-pricing workflow needs catalog data for hundreds of SKUs. Its current loop makes one
remote `GetItem` call per SKU. Network latency and SDK overhead consume the request deadline long
before DynamoDB has a chance to return the data.

## Learning objectives and prerequisites

Use AWS SDK for Java v2's low-level `BatchGetItem` API, honor its 100-key limit, interpret
`UnprocessedKeys`, and make retry behavior deterministic under test. Complete exercises 07 and 11.

## Underlying cause without revealing the solution

Independent reads are easy to write but turn N requested items into N network round trips. A batch
operation has a service limit and may deliberately return only a partial result, so batching alone
cannot be treated as success.

## System invariants

- Each distinct requested SKU has exactly one terminal outcome: found, missing, or unresolved.
- No `BatchGetItem` request contains more than 100 keys.
- Only `UnprocessedKeys` are retried; a missing item is not retried.
- Retries are bounded and an exhausted key remains visible in `unresolvedSkus`.

## Delivery and consistency guarantees

This is a point-in-time collection of individual DynamoDB reads, not a transaction or a consistent
snapshot across all SKU keys. An unresolved key means no successful read was obtained, not that the
item is absent.

## Process-local versus distributed guarantees

The chunking, retry bound, and result classification are local application behavior. DynamoDB Local
checks SDK request wiring but cannot reproduce AWS IAM, managed throttling, partitions, capacity
allocation, latency, or regional failure behavior.

## Functional requirements

Implement `CatalogLookupService.lookup` using the supplied `CatalogReadClient`. Preserve the public
result types, split distinct requested SKUs into batches of at most 100, retry only unprocessed keys
up to `maxAttempts` (including the first call), invoke the injected `RetryBackoff` before each retry,
and report unresolved keys without converting them to missing items.

## Non-functional requirements and resource limits

Do not add an HTTP server, cache, queue, or framework. The application service and the supplied AWS
SDK v2 adapter are enough. The acceptance suite counts calls and requested keys rather than using a
latency threshold.

## Constraints and forbidden shortcuts

- Do not call `getItem` from the completed solution path.
- Do not send more than 100 keys in one `BatchGetItem` request.
- Do not retry every original key after a partial response.
- Do not retry forever or silently label an unprocessed key as absent.
- Do not use DynamoDB Local throttling as deterministic retry evidence.

## Architecture

```text
requested SKUs -> chunks of <=100 -> BatchGetItem -> found + UnprocessedKeys
                                             ^                    |
                                             |---- bounded retry --|
                                                         |
                                                injected backoff policy
```

## Deterministic failure reproduction

Run `starterAcceptanceTest`. The scripted client returns all requested items, but the starter makes
201 single-item calls and zero batches. A second fixture returns partial responses to demonstrate why
the reference must retry only pending keys and retain a final unresolved SKU.

## Task and automated acceptance criteria

Make the starter acceptance suite green without changing public APIs. It verifies 201 keys become
three batches of 100, 100, and 1; no single-item call occurs; retries receive `[1, 2]`; retry input
shrinks to only unprocessed keys; missing and unresolved outcomes remain distinct.

## Exact build, test, and run commands

```shell
./gradlew :exercises:19-dynamodb-batch-read-round-trips:starter:starterAcceptanceTest
./gradlew verifyExercise19
./gradlew verifyIntegrationExercise19
```

The final command requires Docker and starts DynamoDB Local through Testcontainers.

## Progressive hints

1. Count `getItem` invocations for 201 distinct SKUs.
2. Read the `BatchGetItem` key limit and inspect `UnprocessedKeys`.
3. Treat the first request as attempt one; retry only the remaining keys.

## Common wrong solutions and why they fail

- One `GetItem` per SKU has correct values but keeps the N-round-trip incident.
- A batch of 101 violates the API limit.
- Retrying the original batch repeats completed reads and wastes capacity.
- Returning a successful-looking map while dropping unprocessed keys hides partial failure.
- An unbounded loop turns a degraded dependency into unbounded work.

## Production and emulator boundaries

The tagged integration test validates client endpoint, table key mapping, and 101-key chunking against
DynamoDB Local. It does not validate cloud authorization, adaptive capacity, retry quotas, endpoint
resolution, cross-region behavior, or latency. Production needs deadline-aware retry policy, metrics
for request size/retries/unresolved keys, and domain-specific handling for incomplete catalog data.

## Specialist extension

Add an absolute request deadline that caps both SDK attempts and backoff. Compare eventual and
strongly consistent reads, including their read-capacity cost and why neither produces an atomic
multi-item snapshot.

## Interview follow-up questions

- Why is `UnprocessedKeys` different from a missing item?
- Where should retry ownership live when the AWS SDK also retries transport failures?
- How would you preserve a caller's requested order while keeping this batch protocol?
- Which metrics reveal a batch-size regression or a hot partition?

## Reflection and future-post evidence

Share the 201-to-3 request counter, the shrinking retry inputs, and the explicit unresolved result.
Do not claim that a DynamoDB Local run proves AWS throttling or production latency.

## Primary references

- [AWS SDK for Java v2 Gradle setup](https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/setup-project-gradle.html)
- [DynamoDB BatchGetItem API](https://docs.aws.amazon.com/amazondynamodb/latest/APIReference/API_BatchGetItem.html)
- [AWS SDK for Java v2 DynamoDB examples](https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/examples-dynamodb.html)
