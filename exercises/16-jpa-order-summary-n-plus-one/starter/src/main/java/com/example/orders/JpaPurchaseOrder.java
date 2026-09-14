package com.example.orders;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity(name = "purchase_orders")
final class JpaPurchaseOrder {
  @Id private UUID id;
  private String customer;

  @OneToMany(mappedBy = "order")
  private List<JpaOrderLine> lines = new ArrayList<>();

  protected JpaPurchaseOrder() {}

  JpaPurchaseOrder(UUID id, String customer) {
    this.id = id;
    this.customer = customer;
  }

  UUID id() {
    return id;
  }

  String customer() {
    return customer;
  }
}
