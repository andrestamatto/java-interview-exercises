package com.example.fanout;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public final class FanoutCoordinator {
  private final DeadlineScheduler deadlineScheduler;

  public FanoutCoordinator(DeadlineScheduler deadlineScheduler) {
    this.deadlineScheduler = Objects.requireNonNull(deadlineScheduler);
  }

  public CompletableFuture<String> first(AsyncLookup left, AsyncLookup right, Duration timeout) {
    Objects.requireNonNull(left);
    Objects.requireNonNull(right);
    if (timeout == null || timeout.isNegative() || timeout.isZero()) {
      throw new IllegalArgumentException("timeout must be positive");
    }

    CompletableFuture<String> leftFuture = start(left);
    CompletableFuture<String> rightFuture = start(right);
    CompletableFuture<String> result = new CompletableFuture<>();
    AtomicInteger failedBranches = new AtomicInteger();
    AtomicReference<Throwable> firstFailure = new AtomicReference<>();
    DeadlineScheduler.ScheduledTask deadline =
        deadlineScheduler.schedule(
            timeout,
            () -> result.completeExceptionally(new TimeoutException("fan-out deadline elapsed")));

    observe(leftFuture, result, failedBranches, firstFailure);
    observe(rightFuture, result, failedBranches, firstFailure);
    result.whenComplete(
        (value, failure) -> {
          deadline.cancel();
          leftFuture.cancel(true);
          rightFuture.cancel(true);
        });
    return result;
  }

  private static CompletableFuture<String> start(AsyncLookup lookup) {
    try {
      return Objects.requireNonNull(lookup.start(), "lookup returned null");
    } catch (RuntimeException failure) {
      return CompletableFuture.failedFuture(failure);
    }
  }

  private static void observe(
      CompletableFuture<String> branch,
      CompletableFuture<String> result,
      AtomicInteger failedBranches,
      AtomicReference<Throwable> firstFailure) {
    branch.whenComplete(
        (value, failure) -> {
          if (failure == null) {
            result.complete(value);
            return;
          }
          firstFailure.compareAndSet(null, unwrap(failure));
          if (failedBranches.incrementAndGet() == 2) {
            result.completeExceptionally(firstFailure.get());
          }
        });
  }

  private static Throwable unwrap(Throwable failure) {
    if (failure instanceof CompletionException completion && completion.getCause() != null) {
      return completion.getCause();
    }
    return failure;
  }
}
