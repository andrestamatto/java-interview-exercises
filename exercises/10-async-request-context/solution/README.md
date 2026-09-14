# Reference Solution

## Rationale

`ContextTask.wrap` reads the tenant on the submitting thread, before any executor interaction. The returned `Runnable` installs that captured value on the worker, runs the delegate, and calls `RequestContext.clear()` in `finally`.

This ordering is the essential fix. A `ThreadLocal` is attached to the physical worker thread, whereas the tenant belongs to the logical request. A shared executor may run unrelated requests on the same worker. `finally` is therefore a correctness boundary: the delegate's exception escapes unchanged, but its diagnostic state cannot escape with it. The wrapper deliberately clears rather than restores a worker's previous value; a pooled worker must not carry request context between tasks.

## Resources and complexity

Wrapping allocates one small `Runnable` and captures one `String` reference: `O(1)` time and space per submission. Execution adds one thread-local write and one removal. It does not create executors, queues, or threads. The scope is one field only; a production context with tracing, logging, locale, and security metadata needs an explicit representation and propagation policy for every field.

## Failure handling and recovery

If the delegate throws, the same exception is propagated by `Runnable.run`; cleanup still runs. If an executor rejects the wrapper before it begins, no worker context was installed and the caller context remains untouched. Cancellation before execution likewise has nothing to clean up on a worker.

This class is not a recovery mechanism. After a process failure, in-flight work and its in-memory context are gone. A durable workflow must reconstruct its request metadata from its durable command or message, not from a thread-local value.

## Alternatives and trade-offs

- A decorating `Executor` can apply the same capture/install/clear rule centrally, which is useful when all submissions use one boundary.
- Framework integrations can propagate logging MDC and OpenTelemetry context together. They must still guarantee cleanup on pooled workers.
- `InheritableThreadLocal` copies state when a new thread is created; it does not correctly model reused executor workers.
- Scoped Values are a lexical alternative in Java versions where the application can use their supported API and propagation model. They do not turn local context into remote identity.

## Multi-instance boundary

The guarantee ends at this JVM and this `Runnable`. A queue, HTTP call, or RPC must serialize the required correlation fields explicitly. The receiving service must authenticate and authorize the caller again; a propagated tenant string is diagnostic metadata, not proof of identity or permission.

## Observability and security

Test reused workers and exceptional paths, not only a successful one-shot task. In production, monitor missing or conflicting trace/tenant fields at executor and RPC boundaries, while avoiding high-cardinality metrics based on raw tenant IDs. Treat tenant and correlation IDs as potentially sensitive: redact them from untrusted logs and never authorize a request solely because a `ThreadLocal` contains a value.

## Limits

The wrapper covers `Runnable` only and assumes the wrapped task is the sole owner of this exercise's `RequestContext` during its execution. It is not a `Callable`, reactive-stream, virtual-thread, MDC, transaction, retry, or distributed-context implementation. It also has no validation rule for a tenant value; the API permits an absent (`null`) tenant.

## Primary references

- [ThreadLocal API](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/ThreadLocal.html)
- [ExecutorService API](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/concurrent/ExecutorService.html)
- [OpenTelemetry context propagation](https://opentelemetry.io/docs/concepts/context-propagation/)
