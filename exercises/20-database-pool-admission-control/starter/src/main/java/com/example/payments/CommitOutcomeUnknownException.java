package com.example.payments;

/** The connection failed while a commit outcome could already have become durable. */
public final class CommitOutcomeUnknownException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  public CommitOutcomeUnknownException(String message) {
    super(message);
  }
}
