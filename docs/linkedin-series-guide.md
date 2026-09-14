# LinkedIn Evidence Guide

Each exercise is designed to leave credible technical evidence for a future
post. This repository does not generate posts or claim results on a learner's
behalf.

Before drafting anything, preserve the facts you can substantiate:

- the incident and invariant from the exercise README;
- the deterministic reproducer or failure matrix you ran;
- the design decision and its stated boundary;
- actual test, benchmark, plan, trace, or log output; and
- one trade-off or unanswered production concern.

Do not turn local test output into a claim about production throughput,
availability, cloud behavior, or distributed exactly-once delivery. State the
emulator and process-local limits from the README. Benchmark posts should name
the machine, JVM, workload, warmup/fork setup, and uncertainty; they must not
present a single number as a universal winner.

Useful screenshot opportunities are a deterministic failing acceptance test,
a passing invariant test, a bounded query/request counter, a JMH JSON report,
or a trace/metric graph with secrets and tenant identifiers redacted. Never
publish access tokens, customer-like identifiers, connection strings, or raw
production data.

Suggested neutral series label: `50 Production-Grade Java Challenges —
Exercise NN`. The learner owns the voice, conclusions, and publication timing.
