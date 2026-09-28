package com.example.quota;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public final class RequestQuota {
  private final AtomicInteger availablePermits;
  private final QuotaProbe probe;

  public RequestQuota(int initialPermits) {
    this(initialPermits, QuotaProbe.NO_OP);
  }

  RequestQuota(int initialPermits, QuotaProbe probe) {
    if (initialPermits < 0) {
      throw new IllegalArgumentException("initialPermits must not be negative");
    }
    availablePermits = new AtomicInteger(initialPermits);
    this.probe = Objects.requireNonNull(probe, "probe");
  }

  public boolean tryAcquire() {
    int observedPermits = availablePermits.get();
    probe.afterAvailabilityObserved(observedPermits);
    while (observedPermits > 0) {
      if (availablePermits.compareAndSet(observedPermits, observedPermits - 1)) {
        return true;
      }
      observedPermits = availablePermits.get();
    }
    return false;
  }

  public int availablePermits() {
    return availablePermits.get();
  }
}
