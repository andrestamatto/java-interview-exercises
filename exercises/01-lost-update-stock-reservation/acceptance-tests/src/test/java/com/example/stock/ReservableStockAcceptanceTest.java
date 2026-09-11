package com.example.stock;

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
class ReservableStockAcceptanceTest {
    @Test
    void concurrentReservations_preserveStockConservation() throws Exception {
        CountDownLatch observations = new CountDownLatch(2);
        CountDownLatch release = new CountDownLatch(1);
        ReservationProbe probe = ignored -> {
            observations.countDown();
            await(release);
        };
        ReservableStock stock = new ReservableStock(1, probe);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<ReservationOutcome> first = executor.submit(() -> stock.reserve(1));
            Future<ReservationOutcome> second = executor.submit(() -> stock.reserve(1));
            assertThat(observations.await(2, TimeUnit.SECONDS)).isTrue();
            release.countDown();

            List<ReservationOutcome> outcomes = List.of(first.get(), second.get());
            assertThat(outcomes).containsExactlyInAnyOrder(
                    ReservationOutcome.RESERVED, ReservationOutcome.INSUFFICIENT_STOCK);
            assertThat(stock.remainingUnits()).isZero();
        } finally {
            release.countDown();
            executor.shutdownNow();
            assertThat(executor.awaitTermination(2, TimeUnit.SECONDS)).isTrue();
        }
    }

    private static void await(CountDownLatch latch) {
        try {
            latch.await();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("reservation probe interrupted", exception);
        }
    }
}
