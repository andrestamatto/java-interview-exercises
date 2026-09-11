package com.example.importer;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class ImportExecutor implements AutoCloseable {
  private final ExecutorService executor = Executors.newFixedThreadPool(2);

  public boolean submit(Runnable importTask) {
    executor.submit(importTask);
    return true;
  }

  @Override
  public void close() {
    executor.shutdownNow();
  }
}
