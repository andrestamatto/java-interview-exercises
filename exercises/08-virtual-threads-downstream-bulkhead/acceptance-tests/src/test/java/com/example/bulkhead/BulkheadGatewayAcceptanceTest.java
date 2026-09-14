package com.example.bulkhead;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("acceptance")
class BulkheadGatewayAcceptanceTest {
  @Test
  void virtualThreadsDoNotExceedTheDownstreamConcurrencyBudgetAndReleasePermits() throws Exception {
    CountDownLatch entered = new CountDownLatch(2);
    CountDownLatch release = new CountDownLatch(1);
    AtomicInteger inFlight = new AtomicInteger();
    AtomicInteger maximumObserved = new AtomicInteger();
    DownstreamClient downstream = request -> {
      int now = inFlight.incrementAndGet();
      maximumObserved.accumulateAndGet(now, Math::max);
      entered.countDown();
      try {
        release.await();
        return request;
      } finally {
        inFlight.decrementAndGet();
      }
    };
    ExecutorService callers = Executors.newVirtualThreadPerTaskExecutor();
    try {
      BulkheadGateway gateway = new BulkheadGateway(downstream, 2);
      List<java.util.concurrent.Future<String>> futures = new ArrayList<>();
      for (int index = 0; index < 4; index++) {
        futures.add(callers.submit(() -> gateway.fetch("request")));
      }
      assertThat(entered.await(1, TimeUnit.SECONDS)).isTrue();
      assertThat(maximumObserved.get()).isEqualTo(2);
      release.countDown();
      for (java.util.concurrent.Future<String> future : futures) {
        assertThat(future.get(1, TimeUnit.SECONDS)).isEqualTo("request");
      }
      assertThat(maximumObserved.get()).isLessThanOrEqualTo(2);
    } finally {
      release.countDown();
      callers.close();
    }
  }

  @Test
  void rejectsANonPositiveDownstreamBudget() {
    DownstreamClient downstream = request -> request;

    assertThatIllegalArgumentException()
        .isThrownBy(() -> new BulkheadGateway(downstream, 0))
        .withMessage("maximumInFlight must be positive");
  }

  @Test
  void releasesAPermitWhenTheDownstreamCallFails() throws Exception {
    AtomicInteger calls = new AtomicInteger();
    DownstreamClient downstream =
        request -> {
          if (calls.incrementAndGet() == 1) {
            throw new IllegalStateException("dependency failed");
          }
          return request;
        };
    BulkheadGateway gateway = new BulkheadGateway(downstream, 1);

    assertThatThrownBy(() -> gateway.fetch("first"))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("dependency failed");
    assertThat(gateway.fetch("second")).isEqualTo("second");
  }
}
