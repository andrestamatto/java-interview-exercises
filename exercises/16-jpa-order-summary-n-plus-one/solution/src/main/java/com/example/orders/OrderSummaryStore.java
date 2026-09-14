package com.example.orders;

import java.util.List;
import java.util.UUID;

public interface OrderSummaryStore {
  List<PurchaseOrder> loadOrders();

  List<OrderLine> loadLines(UUID orderId);

  List<OrderSummary> loadSummaries();
}
