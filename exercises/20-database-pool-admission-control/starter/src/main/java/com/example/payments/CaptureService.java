package com.example.payments;

import java.util.Objects;
import java.util.UUID;

/**
 * Starter defect: the scarce database connection is acquired before the remote risk call. Under
 * load, remote latency occupies every pool connection.
 */
public final class CaptureService {
  private final TransactionPool transactions;
  private final RiskGateway riskGateway;

  public CaptureService(
      int maximumInFlight, TransactionPool transactions, RiskGateway riskGateway) {
    if (maximumInFlight <= 0) {
      throw new IllegalArgumentException("maximumInFlight must be positive");
    }
    this.transactions = Objects.requireNonNull(transactions, "transactions");
    this.riskGateway = Objects.requireNonNull(riskGateway, "riskGateway");
  }

  public CaptureResult capture(UUID captureId) {
    Objects.requireNonNull(captureId, "captureId");
    try (Transaction transaction = transactions.acquire()) {
      boolean transactionStarted = false;
      try {
        transaction.begin();
        transactionStarted = true;
        riskGateway.approve(captureId);
        transaction.recordCapture(captureId);
        transaction.commit();
        return CaptureResult.COMMITTED;
      } catch (RiskRejectedException exception) {
        if (transactionStarted) {
          transaction.rollback();
        }
        return CaptureResult.RISK_REJECTED;
      } catch (CommitOutcomeUnknownException exception) {
        return CaptureResult.COMMIT_OUTCOME_UNKNOWN;
      } catch (RuntimeException exception) {
        if (transactionStarted) {
          transaction.rollback();
        }
        return CaptureResult.RETRYABLE_BEFORE_COMMIT;
      }
    }
  }
}
