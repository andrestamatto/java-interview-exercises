package com.example.orders;

import java.util.List;
import java.util.Objects;

public final class OrderSummaryService {
  private final OrderSummaryStore store;

  public OrderSummaryService(OrderSummaryStore store) {
    this.store = Objects.requireNonNull(store);
  }

  public List<OrderSummary> summaries() {
    return store.loadOrders().stream()
        .map(
            order ->
                new OrderSummary(
                    order.id(),
                    order.customer(),
                    store.loadLines(order.id()).stream()
                        .map(OrderLine::subtotalCents)
                        .reduce(0L, Math::addExact)))
        .toList();
  }
}
