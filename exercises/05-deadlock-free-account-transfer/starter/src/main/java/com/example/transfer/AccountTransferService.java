package com.example.transfer;

import java.util.Objects;

public final class AccountTransferService {
  public boolean transfer(Account source, Account destination, long amount)
      throws InterruptedException {
    return transfer(source, destination, amount, LockObserver.NONE);
  }

  boolean transfer(Account source, Account destination, long amount, LockObserver observer)
      throws InterruptedException {
    validate(source, destination, amount);
    source.lock().lockInterruptibly();
    try {
      observer.afterFirstLock();
      destination.lock().lockInterruptibly();
      try {
        if (!source.canDebit(amount)) {
          return false;
        }
        source.debit(amount);
        destination.credit(amount);
        return true;
      } finally {
        destination.lock().unlock();
      }
    } finally {
      source.lock().unlock();
    }
  }

  private static void validate(Account source, Account destination, long amount) {
    Objects.requireNonNull(source, "source");
    Objects.requireNonNull(destination, "destination");
    if (source == destination || source.id().equals(destination.id()) || amount <= 0) {
      throw new IllegalArgumentException("accounts must be distinct and amount must be positive");
    }
  }
}
