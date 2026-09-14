package com.example.payments;

/** A definitive remote business rejection. */
public final class RiskRejectedException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  public RiskRejectedException(String message) {
    super(message);
  }
}
