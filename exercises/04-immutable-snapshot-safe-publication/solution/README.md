# Reference solution: ownership transfer at publication

`DiscountSnapshot` validates and copies the incoming map with `Map.copyOf`.
The catalog then publishes that complete immutable value with `AtomicReference`.
The copy is made once per update, so later caller mutations cannot alter the
snapshot and readers need neither a lock nor a defensive copy.

This does not coordinate replicas or provide an audit trail. A production pricing
system still needs versioning, authorization, rollout policy, durable storage,
and cross-instance propagation.
