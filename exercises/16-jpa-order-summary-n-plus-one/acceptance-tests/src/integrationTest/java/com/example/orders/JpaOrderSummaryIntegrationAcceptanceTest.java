package com.example.orders;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Tag("acceptance")
@Tag("integration")
@SuppressWarnings("deprecation") // Testcontainers 2 keeps this container type for source compatibility.
@DataJpaTest(
    properties = {
      "spring.jpa.hibernate.ddl-auto=create-drop",
      "spring.jpa.properties.hibernate.session_factory.statement_inspector=com.example.orders.CountingStatementInspector"
    })
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(classes = JpaIntegrationApplication.class)
@Testcontainers(disabledWithoutDocker = true)
class JpaOrderSummaryIntegrationAcceptanceTest {
  @Container
  private static final PostgreSQLContainer<?> POSTGRES =
      new PostgreSQLContainer<>("postgres:17-alpine");

  @Autowired private EntityManager entityManager;
  @Autowired private OrderSummaryService service;

  @DynamicPropertySource
  static void databaseProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
    registry.add("spring.datasource.username", POSTGRES::getUsername);
    registry.add("spring.datasource.password", POSTGRES::getPassword);
  }

  @BeforeEach
  void seedOrders() {
    JpaPurchaseOrder first =
        new JpaPurchaseOrder(UUID.fromString("00000000-0000-0000-0000-000000000010"), "Ada");
    JpaPurchaseOrder second =
        new JpaPurchaseOrder(UUID.fromString("00000000-0000-0000-0000-000000000020"), "Grace");
    entityManager.persist(first);
    entityManager.persist(second);
    entityManager.persist(new JpaOrderLine(first, "keyboard", 2, 3_500));
    entityManager.persist(new JpaOrderLine(second, "monitor", 1, 20_000));
    entityManager.persist(new JpaOrderLine(second, "cable", 3, 500));
    entityManager.flush();
    entityManager.clear();
    CountingStatementInspector.reset();
  }

  @Test
  void fetchesEverySummaryWithABoundedSelectBudget() {
    List<OrderSummary> summaries = service.summaries();

    assertThat(summaries)
        .containsExactly(
            new OrderSummary(UUID.fromString("00000000-0000-0000-0000-000000000010"), "Ada", 7_000),
            new OrderSummary(
                UUID.fromString("00000000-0000-0000-0000-000000000020"), "Grace", 21_500));
    assertThat(CountingStatementInspector.selectCount()).isLessThanOrEqualTo(1);
  }
}
