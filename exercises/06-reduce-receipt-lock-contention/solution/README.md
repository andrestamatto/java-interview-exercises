# Reference Solution: Reduce Receipt Lock Contention

`AtomicLong.getAndIncrement()` gives each receipt a unique sequence without
serializing the signer. The signature remains paired with the allocated sequence.
This assumes the injected signer is safe for concurrent use; if it represents a
single-threaded HSM or remote quota, that boundary needs its own admission policy.
