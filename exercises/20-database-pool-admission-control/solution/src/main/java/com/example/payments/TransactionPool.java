package com.example.payments;

@FunctionalInterface
public interface TransactionPool {
  Transaction acquire();
}
