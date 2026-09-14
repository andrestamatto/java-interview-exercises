package com.example.orders;

import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class JpaOrderSummaryStore implements OrderSummaryStore {
  private final EntityManager entityManager;

  public JpaOrderSummaryStore(EntityManager entityManager) {
    this.entityManager = Objects.requireNonNull(entityManager);
  }

  @Override
  public List<PurchaseOrder> loadOrders() {
    return entityManager
        .createQuery(
            "select purchaseOrder from purchase_orders purchaseOrder order by purchaseOrder.id",
            JpaPurchaseOrder.class)
        .getResultList()
        .stream()
        .map(order -> new PurchaseOrder(order.id(), order.customer()))
        .toList();
  }

  @Override
  public List<OrderLine> loadLines(UUID orderId) {
    return entityManager
        .createQuery(
            "select line from order_lines line where line.order.id = :orderId", JpaOrderLine.class)
        .setParameter("orderId", orderId)
        .getResultList()
        .stream()
        .map(line -> new OrderLine(line.sku(), line.quantity(), line.unitPriceCents()))
        .toList();
  }

  @Override
  public List<OrderSummary> loadSummaries() {
    return loadOrders().stream()
        .map(
            order ->
                new OrderSummary(
                    order.id(),
                    order.customer(),
                    loadLines(order.id()).stream()
                        .map(OrderLine::subtotalCents)
                        .reduce(0L, Math::addExact)))
        .toList();
  }
}
