package com.example.payments;

/** A caller-visible retry boundary for one capture attempt. */
public enum CaptureResult {
  COMMITTED,
  CAPACITY_REJECTED,
  RISK_REJECTED,
  RETRYABLE_BEFORE_COMMIT,
  COMMIT_OUTCOME_UNKNOWN;

  public boolean isSafeToRetry() {
    return this == RETRYABLE_BEFORE_COMMIT;
  }
}
