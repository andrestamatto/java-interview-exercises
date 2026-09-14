package com.example.orders;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity(name = "order_lines")
final class JpaOrderLine {
  @Id @GeneratedValue private Long id;
  private String sku;
  private int quantity;
  private long unitPriceCents;

  @ManyToOne(optional = false)
  private JpaPurchaseOrder order;

  protected JpaOrderLine() {}

  JpaOrderLine(JpaPurchaseOrder order, String sku, int quantity, long unitPriceCents) {
    this.order = order;
    this.sku = sku;
    this.quantity = quantity;
    this.unitPriceCents = unitPriceCents;
  }

  String sku() {
    return sku;
  }

  int quantity() {
    return quantity;
  }

  long unitPriceCents() {
    return unitPriceCents;
  }
}
