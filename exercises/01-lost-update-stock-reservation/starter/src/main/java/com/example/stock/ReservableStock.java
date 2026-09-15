package com.example.stock;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public final class ReservableStock {
  private final AtomicInteger remainingUnits;
  private final ReservationProbe probe;

  public ReservableStock(final int initialUnits) {
    this(initialUnits, ReservationProbe.NO_OP);
  }

  ReservableStock(final int initialUnits, ReservationProbe probe) {
    if (initialUnits < 0) {
      throw new IllegalArgumentException("initialUnits must not be negative");
    }
    this.remainingUnits = new AtomicInteger(initialUnits);
    this.probe = Objects.requireNonNull(probe, "probe");
  }

  public ReservationOutcome reserve(int requestedUnits) {
    requirePositive(requestedUnits);
    int observedUnits = remainingUnits.get();
    probe.afterAvailabilityObserved(observedUnits);

    while (requestedUnits <= observedUnits) {
      int updatedUnits = observedUnits - requestedUnits;
      if (remainingUnits.compareAndSet(observedUnits, updatedUnits)) {
        return ReservationOutcome.RESERVED;
      }
      observedUnits = remainingUnits.get();
    }

    return ReservationOutcome.INSUFFICIENT_STOCK;
  }

  public int remainingUnits() {
    return remainingUnits.get();
  }

  private static void requirePositive(int requestedUnits) {
    if (requestedUnits <= 0) {
      throw new IllegalArgumentException("requestedUnits must be positive");
    }
  }
}
