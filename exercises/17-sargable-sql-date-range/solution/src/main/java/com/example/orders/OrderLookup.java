package com.example.orders;

import java.util.List;

public interface OrderLookup {
  List<Order> findCreatedOn(OrderDateRange range);
}
