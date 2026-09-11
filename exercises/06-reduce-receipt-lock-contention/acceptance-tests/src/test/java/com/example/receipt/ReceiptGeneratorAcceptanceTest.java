package com.example.receipt;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("acceptance")
class ReceiptGeneratorAcceptanceTest {
  @Test
  void signingDoesNotHoldTheSequenceAllocationLock() throws Exception {
    CountDownLatch bothSignersEntered = new CountDownLatch(2);
    CountDownLatch releaseSigners = new CountDownLatch(1);
    ReceiptSigner signer = (sequence, document) -> {
      bothSignersEntered.countDown();
      releaseSigners.await();
      return sequence + ":" + document;
    };
    ExecutorService callers = Executors.newFixedThreadPool(2);
    try {
      ReceiptGenerator generator = new ReceiptGenerator(signer);
      callers.submit(() -> generator.issue("first"));
      callers.submit(() -> generator.issue("second"));

      assertThat(bothSignersEntered.await(300, TimeUnit.MILLISECONDS)).isTrue();
    } finally {
      releaseSigners.countDown();
      callers.shutdownNow();
      assertThat(callers.awaitTermination(2, TimeUnit.SECONDS)).isTrue();
    }
  }
}
