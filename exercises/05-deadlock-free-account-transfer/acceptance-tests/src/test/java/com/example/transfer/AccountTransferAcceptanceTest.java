package com.example.transfer;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("acceptance")
class AccountTransferAcceptanceTest {
  @Test
  void opposingTransfersDoNotCreateALockCycle() throws Exception {
    Account first = new Account("account-a", 100);
    Account second = new Account("account-b", 100);
    AccountTransferService service = new AccountTransferService();
    CountDownLatch firstLocksHeld = new CountDownLatch(2);
    CountDownLatch releaseFirstLocks = new CountDownLatch(1);
    LockObserver observer = () -> {
      firstLocksHeld.countDown();
      releaseFirstLocks.await();
    };
    ExecutorService transfers = Executors.newFixedThreadPool(2);
    try {
      transfers.submit(() -> service.transfer(first, second, 10, observer));
      transfers.submit(() -> service.transfer(second, first, 10, observer));

      boolean twoDifferentFirstLocksWereHeld = firstLocksHeld.await(250, TimeUnit.MILLISECONDS);
      releaseFirstLocks.countDown();

      if (twoDifferentFirstLocksWereHeld) {
        assertThat(awaitDeadlockDetection()).isNull();
      }
    } finally {
      releaseFirstLocks.countDown();
      transfers.shutdownNow();
      assertThat(transfers.awaitTermination(2, TimeUnit.SECONDS)).isTrue();
    }
  }

  private static long[] awaitDeadlockDetection() throws InterruptedException {
    ThreadMXBean threadMxBean = ManagementFactory.getThreadMXBean();
    for (int attempt = 0; attempt < 50; attempt++) {
      long[] deadlockedThreads = threadMxBean.findDeadlockedThreads();
      if (deadlockedThreads != null) {
        return deadlockedThreads;
      }
      Thread.sleep(10);
    }
    return null;
  }
}
