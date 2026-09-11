package com.example.bulkhead;

import java.util.Objects;

public final class BulkheadGateway {
  private final DownstreamClient downstream;

  public BulkheadGateway(DownstreamClient downstream, int maximumInFlight) {
    this.downstream = Objects.requireNonNull(downstream);
  }

  public String fetch(String request) throws InterruptedException {
    return downstream.fetch(request);
  }
}
