# Reference solution: safe publication of a shutdown signal

The solution declares `stopRequested` as `volatile`. The write in `requestStop`
and a read that observes it in `isStopRequested` form the required
happens-before relationship. The field is one-way, so there is no compound
read-modify-write invariant to protect.

This is deliberately not a claim about stopping arbitrary work: blocking I/O,
long calculations, request admission, task cancellation, and multi-instance
shutdown all require additional mechanisms. `volatile` is also not a substitute
for an atomic stock decrement; exercise 03 addresses that distinction.
