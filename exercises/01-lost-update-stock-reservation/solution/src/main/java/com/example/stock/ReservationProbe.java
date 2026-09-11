package com.example.stock;

@FunctionalInterface
interface ReservationProbe {
  ReservationProbe NO_OP = observedUnits -> {};

  void afterAvailabilityObserved(int observedUnits);
}
