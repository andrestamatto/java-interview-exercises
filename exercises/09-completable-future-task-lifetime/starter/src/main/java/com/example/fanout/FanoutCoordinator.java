package com.example.fanout;

import java.util.concurrent.CompletableFuture;

public final class FanoutCoordinator {
  public CompletableFuture<String> first(AsyncLookup left, AsyncLookup right) {
    return left.start().applyToEither(right.start(), value -> value);
  }
}
