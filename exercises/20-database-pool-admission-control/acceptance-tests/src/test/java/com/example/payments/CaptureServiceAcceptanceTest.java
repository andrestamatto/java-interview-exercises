package com.example.payments;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("acceptance")
class CaptureServiceAcceptanceTest {
  @Test
  void rejectsTheThirdRequestBeforeItCanWaitForAConnection() throws Exception {
    ControlledTransactionPool transactions = new ControlledTransactionPool(2);
    BlockingRiskGateway riskGateway = new BlockingRiskGateway(2);
    CaptureService service = new CaptureService(2, transactions, riskGateway);
    ExecutorService workers = Executors.newFixedThreadPool(3);
    try {
      Future<CaptureResult> first = workers.submit(() -> service.capture(id(1)));
      Future<CaptureResult> second = workers.submit(() -> service.capture(id(2)));
      assertThat(riskGateway.awaitEntered()).isTrue();

      assertThat(transactions.activeConnections()).isZero();
      Future<CaptureResult> third = workers.submit(() -> service.capture(id(3)));
      assertThat(third.get(500, TimeUnit.MILLISECONDS)).isEqualTo(CaptureResult.CAPACITY_REJECTED);

      riskGateway.release();
      assertThat(first.get(1, TimeUnit.SECONDS)).isEqualTo(CaptureResult.COMMITTED);
      assertThat(second.get(1, TimeUnit.SECONDS)).isEqualTo(CaptureResult.COMMITTED);
      assertThat(transactions.maximumActiveConnections()).isLessThanOrEqualTo(2);
    } finally {
      riskGateway.release();
      workers.shutdownNow();
      assertThat(workers.awaitTermination(1, TimeUnit.SECONDS)).isTrue();
    }
  }

  @Test
  void rollsBackClosesAndRecoversAfterAPreCommitFailure() {
    ControlledTransactionPool transactions = new ControlledTransactionPool(1);
    transactions.failNextRecord();
    CaptureService service = new CaptureService(1, transactions, ignored -> {});

    assertThat(service.capture(id(4))).isEqualTo(CaptureResult.RETRYABLE_BEFORE_COMMIT);
    assertThat(transactions.rollbacks()).isEqualTo(1);
    assertThat(transactions.closedConnections()).isEqualTo(1);
    assertThat(transactions.activeConnections()).isZero();

    assertThat(service.capture(id(5))).isEqualTo(CaptureResult.COMMITTED);
    assertThat(transactions.commits()).isEqualTo(1);
    assertThat(transactions.closedConnections()).isEqualTo(2);
  }

  @Test
  void neverRetriesAnAmbiguousCommit() {
    ControlledTransactionPool transactions = new ControlledTransactionPool(1);
    transactions.failNextCommitWithUnknownOutcome();
    CaptureService service = new CaptureService(1, transactions, ignored -> {});

    CaptureResult result = service.capture(id(6));

    assertThat(result).isEqualTo(CaptureResult.COMMIT_OUTCOME_UNKNOWN);
    assertThat(result.isSafeToRetry()).isFalse();
    assertThat(transactions.recordAttempts()).isEqualTo(1);
    assertThat(transactions.commits()).isEqualTo(1);
    assertThat(transactions.rollbacks()).isZero();
    assertThat(transactions.closedConnections()).isEqualTo(1);
  }

  private static UUID id(long value) {
    return new UUID(0, value);
  }

  private static final class BlockingRiskGateway implements RiskGateway {
    private final CountDownLatch entered;
    private final CountDownLatch release = new CountDownLatch(1);

    private BlockingRiskGateway(int requestsToBlock) {
      this.entered = new CountDownLatch(requestsToBlock);
    }

    @Override
    public void approve(UUID captureId) {
      entered.countDown();
      try {
        if (!release.await(1, TimeUnit.SECONDS)) {
          throw new IllegalStateException("test did not release risk gateway");
        }
      } catch (InterruptedException exception) {
        Thread.currentThread().interrupt();
        throw new IllegalStateException("risk gateway was interrupted", exception);
      }
    }

    boolean awaitEntered() throws InterruptedException {
      return entered.await(1, TimeUnit.SECONDS);
    }

    void release() {
      release.countDown();
    }
  }

  private static final class ControlledTransactionPool implements TransactionPool {
    private final java.util.concurrent.Semaphore connections;
    private final AtomicInteger activeConnections = new AtomicInteger();
    private final AtomicInteger maximumActiveConnections = new AtomicInteger();
    private final AtomicInteger closedConnections = new AtomicInteger();
    private final AtomicInteger rollbacks = new AtomicInteger();
    private final AtomicInteger commits = new AtomicInteger();
    private final AtomicInteger recordAttempts = new AtomicInteger();
    private final AtomicBoolean failNextRecord = new AtomicBoolean();
    private final AtomicBoolean failNextCommitUnknown = new AtomicBoolean();

    private ControlledTransactionPool(int size) {
      this.connections = new java.util.concurrent.Semaphore(size);
    }

    @Override
    public Transaction acquire() {
      connections.acquireUninterruptibly();
      int active = activeConnections.incrementAndGet();
      maximumActiveConnections.accumulateAndGet(active, Math::max);
      return new ControlledTransaction(this);
    }

    void failNextRecord() {
      failNextRecord.set(true);
    }

    void failNextCommitWithUnknownOutcome() {
      failNextCommitUnknown.set(true);
    }

    int activeConnections() {
      return activeConnections.get();
    }

    int maximumActiveConnections() {
      return maximumActiveConnections.get();
    }

    int closedConnections() {
      return closedConnections.get();
    }

    int rollbacks() {
      return rollbacks.get();
    }

    int commits() {
      return commits.get();
    }

    int recordAttempts() {
      return recordAttempts.get();
    }
  }

  private static final class ControlledTransaction implements Transaction {
    private final ControlledTransactionPool pool;
    private boolean closed;

    private ControlledTransaction(ControlledTransactionPool pool) {
      this.pool = pool;
    }

    @Override
    public void begin() {}

    @Override
    public void recordCapture(UUID captureId) {
      pool.recordAttempts.incrementAndGet();
      if (pool.failNextRecord.compareAndSet(true, false)) {
        throw new IllegalStateException("simulated connection loss before commit");
      }
    }

    @Override
    public void commit() {
      pool.commits.incrementAndGet();
      if (pool.failNextCommitUnknown.compareAndSet(true, false)) {
        throw new CommitOutcomeUnknownException("simulated connection loss during commit");
      }
    }

    @Override
    public void rollback() {
      pool.rollbacks.incrementAndGet();
    }

    @Override
    public void close() {
      if (!closed) {
        closed = true;
        pool.closedConnections.incrementAndGet();
        pool.activeConnections.decrementAndGet();
        pool.connections.release();
      }
    }
  }
}
