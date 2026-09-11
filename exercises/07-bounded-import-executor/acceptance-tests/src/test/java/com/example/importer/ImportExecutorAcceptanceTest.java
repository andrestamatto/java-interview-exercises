package com.example.importer;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("acceptance")
class ImportExecutorAcceptanceTest {
  @Test
  void rejectsWorkAfterWorkersAndTheBoundedQueueAreFull() throws Exception {
    CountDownLatch workersStarted = new CountDownLatch(2);
    CountDownLatch releaseWorkers = new CountDownLatch(1);
    Runnable blockedImport = () -> {
      workersStarted.countDown();
      await(releaseWorkers);
    };
    try (ImportExecutor imports = new ImportExecutor()) {
      assertThat(imports.submit(blockedImport)).isTrue();
      assertThat(imports.submit(blockedImport)).isTrue();
      assertThat(workersStarted.await(1, TimeUnit.SECONDS)).isTrue();
      assertThat(imports.submit(() -> {})).isTrue();
      assertThat(imports.submit(() -> {})).isFalse();
    } finally {
      releaseWorkers.countDown();
    }
  }

  private static void await(CountDownLatch latch) {
    try {
      latch.await();
    } catch (InterruptedException exception) {
      Thread.currentThread().interrupt();
    }
  }
}
