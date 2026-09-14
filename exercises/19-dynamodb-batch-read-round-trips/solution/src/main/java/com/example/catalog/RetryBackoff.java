package com.example.catalog;

public interface RetryBackoff {
  void awaitBeforeRetry(int retryNumber);
}
