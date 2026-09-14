package com.example.transfer;

import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;

public final class Account {
  private final String id;
  private final ReentrantLock lock = new ReentrantLock();
  private long balance;

  public Account(String id, long openingBalance) {
    this.id = Objects.requireNonNull(id, "id");
    if (id.isBlank() || openingBalance < 0) {
      throw new IllegalArgumentException(
          "id must not be blank and opening balance must be non-negative");
    }
    this.balance = openingBalance;
  }

  public String id() {
    return id;
  }

  public long balance() {
    lock.lock();
    try {
      return balance;
    } finally {
      lock.unlock();
    }
  }

  ReentrantLock lock() {
    return lock;
  }

  void debit(long amount) {
    balance -= amount;
  }

  void credit(long amount) {
    balance += amount;
  }

  boolean canDebit(long amount) {
    return balance >= amount;
  }

  boolean canCredit(long amount) {
    return balance <= Long.MAX_VALUE - amount;
  }
}
