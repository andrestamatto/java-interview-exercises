package com.example.payments;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.Semaphore;

/**
 * Bounds local capture admission before work can wait for a database connection. A permit is
 * process-local capacity, not a distributed rate limit.
 */
public final class CaptureService {
  private final Semaphore admission;
  private final TransactionPool transactions;
  private final RiskGateway riskGateway;

  public CaptureService(
      int maximumInFlight, TransactionPool transactions, RiskGateway riskGateway) {
    if (maximumInFlight <= 0) {
      throw new IllegalArgumentException("maximumInFlight must be positive");
    }
    this.admission = new Semaphore(maximumInFlight);
    this.transactions = Objects.requireNonNull(transactions, "transactions");
    this.riskGateway = Objects.requireNonNull(riskGateway, "riskGateway");
  }

  public CaptureResult capture(UUID captureId) {
    Objects.requireNonNull(captureId, "captureId");
    if (!admission.tryAcquire()) {
      return CaptureResult.CAPACITY_REJECTED;
    }
    try {
      try {
        riskGateway.approve(captureId);
      } catch (RiskRejectedException exception) {
        return CaptureResult.RISK_REJECTED;
      } catch (RuntimeException exception) {
        return CaptureResult.RETRYABLE_BEFORE_COMMIT;
      }
      return recordApprovedCapture(captureId);
    } finally {
      admission.release();
    }
  }

  private CaptureResult recordApprovedCapture(UUID captureId) {
    try (Transaction transaction = transactions.acquire()) {
      boolean transactionStarted = false;
      boolean committed = false;
      boolean commitOutcomeUnknown = false;
      try {
        transaction.begin();
        transactionStarted = true;
        transaction.recordCapture(captureId);
        transaction.commit();
        committed = true;
        return CaptureResult.COMMITTED;
      } catch (CommitOutcomeUnknownException exception) {
        commitOutcomeUnknown = true;
        return CaptureResult.COMMIT_OUTCOME_UNKNOWN;
      } catch (RuntimeException exception) {
        return CaptureResult.RETRYABLE_BEFORE_COMMIT;
      } finally {
        if (transactionStarted && !committed && !commitOutcomeUnknown) {
          transaction.rollback();
        }
      }
    }
  }
}
