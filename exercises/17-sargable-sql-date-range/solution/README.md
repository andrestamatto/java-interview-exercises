# Reference Solution: Sargable Order-Date Lookup

`OrderDateRange.forLocalDate` translates the business calendar day once, using
the supplied IANA `ZoneId`. Its `startInclusive` and `endExclusive` values are
instants, so the spring-forward transition is represented naturally: New York
2024-03-10 begins at `05:00Z` and the next local midnight is `04:00Z`.

`JdbcOrderLookup` binds those two values to this predicate:

```sql
where created_at >= ? and created_at < ?
```

The database compares the `timestamptz` column directly with the supplied
boundaries. That preserves the local-date meaning while leaving the leading
`created_at` key available to the `(created_at, id)` B-tree index. The
half-open boundary also avoids precision guesses such as `23:59:59.999` and
prevents an order at the following midnight from appearing twice.

## Why the reference is correct

The normal contract test proves the DST conversion and rejects empty ranges.
The acceptance test captures JDBC's statement and arguments: it checks the
bare range predicate, the absence of a date/time-zone transform on the column,
and both exact timestamps. It deliberately avoids a timing threshold.

The tagged PostgreSQL test uses a `CountingDataSource` only around the lookup,
so the one-statement assertion does not accidentally count fixture setup. It
then asks PostgreSQL for JSON `EXPLAIN` output after disabling sequential scans
in that isolated session. This proves the schema and range predicate are
eligible for the intended index; it does not promise that every production
dataset will choose that exact plan.

## Why the starter fails

The starter calculates the local date correctly but applies `AT TIME ZONE` and
a `::date` cast to `created_at`. That makes each stored timestamp compute a
derived value before comparison. It may return correct rows, but it violates
the supplied query-shape contract and usually prevents a normal range seek on
the timestamp index.

## Operational and design boundaries

This solution is a single read statement. It does not add a transaction,
snapshot across pages, cache, or retry policy. Production plan selection still
depends on statistics, selectivity, parameter values, and competing indexes;
observe query fingerprints, plans, buffer reads, and pool waiting rather than
turning this test into a latency SLO.

A functional index or generated local-date column can be appropriate when the
business permanently queries one reporting zone. Those alternatives encode a
schema-level policy and increase write/index maintenance. The range approach
keeps the timestamp as the source of truth and makes the requested zone explicit.

## Resources

- [PostgreSQL `EXPLAIN`](https://www.postgresql.org/docs/current/using-explain.html)
- [PostgreSQL index ordering](https://www.postgresql.org/docs/current/indexes-ordering.html)
- [Spring JDBC reference](https://docs.spring.io/spring-framework/reference/data-access/jdbc.html)
