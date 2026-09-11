package com.example.fanout;

import java.util.concurrent.CompletableFuture;

public final class FanoutCoordinator {
  public CompletableFuture<String> first(AsyncLookup left, AsyncLookup right) {
    CompletableFuture<String> leftFuture = left.start();
    CompletableFuture<String> rightFuture = right.start();
    return leftFuture
        .applyToEither(rightFuture, value -> value)
        .whenComplete(
            (value, failure) -> {
              leftFuture.cancel(true);
              rightFuture.cancel(true);
            });
  }
}
