package com.example.importer;

import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public final class ImportExecutor implements AutoCloseable {
  private final ThreadPoolExecutor executor =
      new ThreadPoolExecutor(
          2,
          2,
          0,
          TimeUnit.MILLISECONDS,
          new ArrayBlockingQueue<>(1),
          new ThreadPoolExecutor.AbortPolicy());

  public boolean submit(Runnable importTask) {
    try {
      executor.execute(Objects.requireNonNull(importTask, "importTask"));
      return true;
    } catch (java.util.concurrent.RejectedExecutionException exception) {
      return false;
    }
  }

  @Override
  public void close() {
    executor.shutdownNow();
  }
}
