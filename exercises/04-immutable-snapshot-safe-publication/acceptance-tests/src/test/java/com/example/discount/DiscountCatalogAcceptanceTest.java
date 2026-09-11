package com.example.discount;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("acceptance")
class DiscountCatalogAcceptanceTest {
  @Test
  void publishedSnapshotIsNotChangedByLaterCallerMutation() throws Exception {
    DiscountCatalog catalog = new DiscountCatalog();
    Map<String, Integer> draft = new HashMap<>(Map.of("book", 10));
    catalog.publish(draft);

    CountDownLatch mutationComplete = new CountDownLatch(1);
    ExecutorService reader = Executors.newSingleThreadExecutor();
    try {
      Future<Integer> observedDiscount = reader.submit(() -> {
        await(mutationComplete);
        return catalog.discountFor("book");
      });
      draft.put("book", 90);
      mutationComplete.countDown();

      assertThat(observedDiscount.get()).isEqualTo(10);
    } finally {
      mutationComplete.countDown();
      reader.shutdownNow();
      assertThat(reader.awaitTermination(2, TimeUnit.SECONDS)).isTrue();
    }
  }

  private static void await(CountDownLatch latch) {
    try {
      latch.await();
    } catch (InterruptedException exception) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException("reader interrupted", exception);
    }
  }
}
