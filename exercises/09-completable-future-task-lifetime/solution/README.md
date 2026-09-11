# Reference Solution

The coordinator retains both child futures and cancels them when the parent is
terminal. This prevents a completed winner from orphaning the loser. A real
adapter must still translate cancellation into its own transport-specific abort.
