# Reference Solution: Bounded BatchGetItem Continuation

`CatalogLookupService` de-duplicates requested SKUs in insertion order, slices them into groups of
at most 100, and invokes `CatalogReadClient.batchGetItems` for every group. A response's returned
items are final; only its `unprocessedSkus` become the next request. The `maxAttempts` bound includes
the initial request, and `RetryBackoff.awaitBeforeRetry(1)` is called only before the second attempt.
After the bound, remaining keys are returned as `unresolvedSkus`; keys absent from a successful
response and not marked unprocessed are `missingSkus`.

The supplied AWS SDK v2 adapter is intentionally thin. It translates SKUs to the table's `sku` hash
key and translates `responses` and `unprocessedKeys` from `BatchGetItem`. Retry policy remains in the
application service so the deterministic acceptance test can script it. In a production service the
backoff implementation must also obey the caller's overall deadline and coordinate intentionally with
SDK transport retries.

The DynamoDB Local test counts AWS SDK `BatchGetItemRequest` objects with an execution interceptor.
It proves 101 items use two requests and no request exceeds 100 keys. Local DynamoDB is not used to
manufacture throttling; scripted unit responses are the reliable evidence for partial-response logic.

Relevant boundaries: BatchGetItem is not transactional, does not make every key a single snapshot,
and its result order is not a caller ordering guarantee. A real API should expose or explicitly
compensate for incomplete catalog data rather than discarding unresolved SKUs.
