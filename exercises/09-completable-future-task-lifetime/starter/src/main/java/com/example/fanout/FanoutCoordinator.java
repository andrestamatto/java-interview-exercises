package com.example.fanout;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public final class FanoutCoordinator {
  private final DeadlineScheduler deadlineScheduler;

  public FanoutCoordinator(DeadlineScheduler deadlineScheduler) {
    this.deadlineScheduler = Objects.requireNonNull(deadlineScheduler);
  }

  public CompletableFuture<String> first(AsyncLookup left, AsyncLookup right, Duration timeout) {
    Objects.requireNonNull(left);
    Objects.requireNonNull(right);
    Objects.requireNonNull(timeout);
    return left.start().applyToEither(right.start(), value -> value);
  }
}
