package com.example.orders;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("acceptance")
class OrderSummaryServiceAcceptanceTest {
  @Test
  void preservesTotalsWithoutPerformingOneLineLookupPerOrder() {
    UUID firstId = UUID.fromString("00000000-0000-0000-0000-000000000001");
    UUID secondId = UUID.fromString("00000000-0000-0000-0000-000000000002");
    CountingStore store =
        new CountingStore(
            List.of(
                new PurchaseOrder(firstId, "Ada"),
                new PurchaseOrder(secondId, "Grace"),
                new PurchaseOrder(
                    UUID.fromString("00000000-0000-0000-0000-000000000003"), "Linus")),
            Map.of(
                firstId, List.of(new OrderLine("keyboard", 2, 3_500)),
                secondId, List.of(new OrderLine("monitor", 1, 20_000), new OrderLine("cable", 3, 500))));

    List<OrderSummary> summaries = new OrderSummaryService(store).summaries();

    assertThat(summaries)
        .containsExactly(
            new OrderSummary(firstId, "Ada", 7_000),
            new OrderSummary(secondId, "Grace", 21_500),
            new OrderSummary(
                UUID.fromString("00000000-0000-0000-0000-000000000003"), "Linus", 0));
    assertThat(store.summaryLoads.get()).isEqualTo(1);
    assertThat(store.orderLoads.get()).isZero();
    assertThat(store.lineLoads.get()).isZero();
  }

  private static final class CountingStore implements OrderSummaryStore {
    private final List<PurchaseOrder> orders;
    private final Map<UUID, List<OrderLine>> linesByOrder;
    private final AtomicInteger lineLoads = new AtomicInteger();
    private final AtomicInteger orderLoads = new AtomicInteger();
    private final AtomicInteger summaryLoads = new AtomicInteger();

    private CountingStore(List<PurchaseOrder> orders, Map<UUID, List<OrderLine>> linesByOrder) {
      this.orders = orders;
      this.linesByOrder = linesByOrder;
    }

    @Override
    public List<PurchaseOrder> loadOrders() {
      orderLoads.incrementAndGet();
      return orders;
    }

    @Override
    public List<OrderLine> loadLines(UUID orderId) {
      lineLoads.incrementAndGet();
      return linesByOrder.getOrDefault(orderId, List.of());
    }

    @Override
    public List<OrderSummary> loadSummaries() {
      summaryLoads.incrementAndGet();
      return orders.stream()
          .map(
              order ->
                  new OrderSummary(
                      order.id(),
                      order.customer(),
                      linesByOrder.getOrDefault(order.id(), List.of()).stream()
                          .map(OrderLine::subtotalCents)
                          .reduce(0L, Math::addExact)))
          .toList();
    }
  }
}
