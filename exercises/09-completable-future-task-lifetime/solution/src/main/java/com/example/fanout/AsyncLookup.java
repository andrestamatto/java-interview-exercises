package com.example.fanout;

import java.util.concurrent.CompletableFuture;

@FunctionalInterface
public interface AsyncLookup {
  CompletableFuture<String> start();
}
