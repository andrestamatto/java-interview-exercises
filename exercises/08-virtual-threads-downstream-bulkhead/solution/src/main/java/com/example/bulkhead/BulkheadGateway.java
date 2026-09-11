package com.example.bulkhead;

import java.util.Objects;
import java.util.concurrent.Semaphore;

public final class BulkheadGateway {
  private final DownstreamClient downstream;
  private final Semaphore permits;

  public BulkheadGateway(DownstreamClient downstream, int maximumInFlight) {
    this.downstream = Objects.requireNonNull(downstream);
    this.permits = new Semaphore(maximumInFlight);
  }

  public String fetch(String request) throws InterruptedException {
    permits.acquire();
    try {
      return downstream.fetch(request);
    } finally {
      permits.release();
    }
  }
}
