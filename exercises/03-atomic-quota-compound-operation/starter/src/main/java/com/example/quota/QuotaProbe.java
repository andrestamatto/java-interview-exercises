package com.example.quota;

@FunctionalInterface
interface QuotaProbe {
  QuotaProbe NO_OP = observedPermits -> {};

  void afterAvailabilityObserved(int observedPermits);
}
