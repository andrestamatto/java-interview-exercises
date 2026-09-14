package com.example.audit;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootConfiguration
@EnableAutoConfiguration
class JdbcIntegrationApplication {
  @Bean
  EventFeedService eventFeedService(JdbcTemplate jdbcTemplate) {
    return new EventFeedService(new JdbcAuditEventStore(jdbcTemplate));
  }
}
