package com.example.orders;

import jakarta.persistence.EntityManager;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Bean;

@SpringBootConfiguration
@EnableAutoConfiguration
@EntityScan(basePackageClasses = JpaPurchaseOrder.class)
class JpaIntegrationApplication {
  @Bean
  OrderSummaryService orderSummaryService(EntityManager entityManager) {
    return new OrderSummaryService(new JpaOrderSummaryStore(entityManager));
  }
}
