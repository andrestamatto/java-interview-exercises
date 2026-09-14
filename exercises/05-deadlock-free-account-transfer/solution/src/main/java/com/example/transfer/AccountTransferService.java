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
    Account first = source.id().compareTo(destination.id()) < 0 ? source : destination;
    Account second = first == source ? destination : source;

    first.lock().lockInterruptibly();
    try {
      observer.afterFirstLock();
      second.lock().lockInterruptibly();
      try {
        if (!source.canDebit(amount) || !destination.canCredit(amount)) {
          return false;
        }
        source.debit(amount);
        destination.credit(amount);
        return true;
      } finally {
        second.lock().unlock();
      }
    } finally {
      first.lock().unlock();
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
