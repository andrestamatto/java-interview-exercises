package com.example.quota;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("acceptance")
class RequestQuotaAcceptanceTest {
  @Test
  void concurrentAcquisitionCannotConsumeTheLastPermitTwice() throws Exception {
    CountDownLatch observations = new CountDownLatch(2);
    CountDownLatch release = new CountDownLatch(1);
    QuotaProbe probe = ignored -> {
      observations.countDown();
      await(release);
    };
    RequestQuota quota = new RequestQuota(1, probe);
    ExecutorService workers = Executors.newFixedThreadPool(2);
    try {
      Future<Boolean> first = workers.submit(quota::tryAcquire);
      Future<Boolean> second = workers.submit(quota::tryAcquire);
      assertThat(observations.await(2, TimeUnit.SECONDS)).isTrue();
      release.countDown();

      assertThat(List.of(first.get(), second.get())).containsExactlyInAnyOrder(true, false);
      assertThat(quota.availablePermits()).isZero();
    } finally {
      release.countDown();
      workers.shutdownNow();
      assertThat(workers.awaitTermination(2, TimeUnit.SECONDS)).isTrue();
    }
  }

  private static void await(CountDownLatch latch) {
    try {
      latch.await();
    } catch (InterruptedException exception) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException("quota probe interrupted", exception);
    }
  }
}
