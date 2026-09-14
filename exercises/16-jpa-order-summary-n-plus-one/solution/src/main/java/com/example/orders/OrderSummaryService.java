package com.example.orders;

import java.util.List;
import java.util.Objects;

public final class OrderSummaryService {
  private final OrderSummaryStore store;

  public OrderSummaryService(OrderSummaryStore store) {
    this.store = Objects.requireNonNull(store);
  }

  public List<OrderSummary> summaries() {
    return store.loadSummaries();
  }
}
