package com.example.transfer;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("acceptance")
class AccountTransferAcceptanceTest {
  @Test
  void opposingTransfersCannotHoldConflictingFirstLocks() throws Exception {
    Account first = new Account("account-a", 100);
    Account second = new Account("account-b", 100);
    AccountTransferService service = new AccountTransferService();
    CountDownLatch firstLocksHeld = new CountDownLatch(2);
    CountDownLatch releaseFirstLock = new CountDownLatch(1);
    LockObserver observer =
        () -> {
          firstLocksHeld.countDown();
          releaseFirstLock.await();
        };
    ExecutorService transfers = java.util.concurrent.Executors.newFixedThreadPool(2);
    try {
      Future<Boolean> forward =
          transfers.submit(() -> service.transfer(first, second, 10, observer));
      Future<Boolean> backward =
          transfers.submit(() -> service.transfer(second, first, 10, observer));

      assertThat(firstLocksHeld.await(1, TimeUnit.SECONDS)).isFalse();
      releaseFirstLock.countDown();

      assertThat(forward.get(1, TimeUnit.SECONDS)).isTrue();
      assertThat(backward.get(1, TimeUnit.SECONDS)).isTrue();
      assertThat(first.balance() + second.balance()).isEqualTo(200);
    } finally {
      releaseFirstLock.countDown();
      transfers.shutdownNow();
      assertThat(transfers.awaitTermination(2, TimeUnit.SECONDS)).isTrue();
    }
  }
}
