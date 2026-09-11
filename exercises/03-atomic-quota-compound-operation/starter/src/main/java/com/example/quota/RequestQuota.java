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
    if (observedPermits == 0) {
      return false;
    }
    availablePermits.decrementAndGet();
    return true;
  }

  public int availablePermits() {
    return availablePermits.get();
  }
}
