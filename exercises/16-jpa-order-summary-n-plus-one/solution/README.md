# Reference Solution: Explicit Summary Fetch

`OrderSummaryService` delegates the response-shaped read to
`OrderSummaryStore.loadSummaries()` rather than iterating headers and loading
lines separately. `JpaOrderSummaryStore` uses one scoped JPQL fetch join and
maps the loaded entities to immutable summaries. The association remains lazy
by default, so unrelated paths are not silently made eager.

The fetch join makes one select for this unpaged, small-to-moderate summary
shape. Mapping lines is O(total lines returned) and memory is proportional to
the joined graph. A cancel-heavy or very large response should use a projection
or a separate paging strategy; this is not a universal fetch policy.

Fixtures flush and clear the persistence context before resetting a Hibernate
`StatementInspector`. The PostgreSQL integration test therefore counts the read
path, not fixture inserts or first-level-cache hits. It proves a bounded query
shape, not latency, RDS failover, or every endpoint's database behavior.

`EAGER`, OSIV, and a cache are rejected because they hide or relocate the
problem. A production endpoint also needs authorization before query execution,
pagination/result limits, query/pool observability with bounded-cardinality
labels, and careful redaction of order/customer data. Multiple application
instances share the same database load; this local code change does not create
distributed coordination or cache consistency.

- [Hibernate fetching](https://docs.hibernate.org/orm/current/introduction/html_single/Hibernate_Introduction.html#fetching)
- [JPA `EntityManager`](https://jakarta.ee/specifications/persistence/)
