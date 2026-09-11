package com.example.stock;

import java.util.Objects;

public final class ReservableStock {
  private int remainingUnits;
  private final ReservationProbe probe;

  public ReservableStock(int initialUnits) {
    this(initialUnits, ReservationProbe.NO_OP);
  }

  ReservableStock(int initialUnits, ReservationProbe probe) {
    if (initialUnits < 0) {
      throw new IllegalArgumentException("initialUnits must not be negative");
    }
    this.remainingUnits = initialUnits;
    this.probe = Objects.requireNonNull(probe, "probe");
  }

  public ReservationOutcome reserve(int requestedUnits) {
    requirePositive(requestedUnits);
    int observedUnits = remainingUnits;
    probe.afterAvailabilityObserved(observedUnits);
    if (requestedUnits > observedUnits) {
      return ReservationOutcome.INSUFFICIENT_STOCK;
    }
    remainingUnits = observedUnits - requestedUnits;
    return ReservationOutcome.RESERVED;
  }

  public int remainingUnits() {
    return remainingUnits;
  }

  private static void requirePositive(int requestedUnits) {
    if (requestedUnits <= 0) {
      throw new IllegalArgumentException("requestedUnits must be positive");
    }
  }
}
