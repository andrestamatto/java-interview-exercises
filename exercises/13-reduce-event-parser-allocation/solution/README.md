# Reference Solution

## Rationale

The solution finds the first and second `|` with `indexOf`, rejects a missing, leading, adjacent, or third delimiter, and only then extracts the fields and parses the numeric suffix. That removes the `String[]` created by `split` while preserving the accepted grammar and `IllegalArgumentException` boundary.

The public `parse(String)` calls the parsing helper directly. It no longer allocates a `ParserAllocationProbe` merely to execute production code. The package-local overload remains for the acceptance test, but it delegates to the same helper and records no intermediate array. The returned `Event` retains the type and customer-ID strings; converting the amount via the current `String` overload also uses a temporary suffix string, so the solution makes the targeted allocation reduction rather than claiming zero allocation.

## Resources and complexity

Delimiter discovery and validation are linear in payload length; the third-delimiter check is another bounded scan of the same input. The parser stores only the returned event and its two textual fields after successful parsing, plus transient parsing objects. Its memory use is therefore `O(n)` in the size of retained text for one event. There is no payload-length limit or buffer reuse policy in this small exercise.

## Failure handling and recovery

`null`, wrong delimiter shape, empty type/customer ID, and an unparsable amount are reported as `IllegalArgumentException`. An invalid event must not be silently repaired: a production consumer would count it, redact its diagnostic context, and route it according to a documented dead-letter or rejection policy. Retrying the same malformed bytes is not recovery. A process restart does not preserve any parsing state because the parser is stateless.

## Alternatives and trade-offs

- `String.split` is concise but performs regex-oriented work and creates the fields array targeted by this exercise.
- `Long.parseLong(CharSequence, begin, end, radix)` can avoid constructing the numeric suffix on Java 21, at the cost of a more detailed conversion path.
- A generated/schema-aware parser can provide richer evolution and validation rules, but increases build and deployment complexity.
- Streaming byte parsing can reduce decoding and copying for very high-volume inputs, but must specify character encoding, bounds, and buffer ownership.

## Multi-instance boundary

Every consumer instance must use the same versioned grammar and error policy. Partition ordering, retries, deduplication, schema rollout, and poison-message handling are broker/workflow concerns outside this in-process parser. Allocation improvements on one node do not provide a distributed throughput or latency guarantee.

## Observability and security

Track valid, malformed, and size-distribution counts; use JFR or the JMH GC profiler to inspect allocation with representative payloads. Do not log raw payloads by default: type and customer IDs may be sensitive, and attacker-controlled payloads can cause log injection or cardinality explosions. At an ingress boundary, enforce a maximum payload size, authenticate the producer, and validate schema/version before relying on event fields.

## Limits

The grammar has no whitespace normalization, escaping, schema-version field, or maximum length. It accepts exactly the `Long.parseLong` numeric syntax and has no semantic validation of type or customer ID. The parser is stateless but that does not make the upstream source, event model, or error routing thread-safe or distributed.

## Primary references

- [String API](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/String.html)
- [Long API](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Long.html)
- [JMH samples](https://github.com/openjdk/jmh/tree/master/jmh-samples/src/main/java/org/openjdk/jmh/samples)
- [Java Flight Recorder API](https://docs.oracle.com/en/java/javase/21/jfapi/)
