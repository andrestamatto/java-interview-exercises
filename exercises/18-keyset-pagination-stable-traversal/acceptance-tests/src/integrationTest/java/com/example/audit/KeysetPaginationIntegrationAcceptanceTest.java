package com.example.audit;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Tag("acceptance")
@Tag("integration")
@SuppressWarnings("deprecation") // Testcontainers 2 keeps this container type for source compatibility.
@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(classes = JdbcIntegrationApplication.class)
@Testcontainers(disabledWithoutDocker = true)
class KeysetPaginationIntegrationAcceptanceTest {
  private static final UUID TENANT = UUID.fromString("00000000-0000-0000-0000-000000000018");

  @Container
  private static final PostgreSQLContainer<?> POSTGRES =
      new PostgreSQLContainer<>("postgres:17-alpine");

  @Autowired private JdbcTemplate jdbcTemplate;
  @Autowired private EventFeedService service;

  @DynamicPropertySource
  static void databaseProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
    registry.add("spring.datasource.username", POSTGRES::getUsername);
    registry.add("spring.datasource.password", POSTGRES::getPassword);
  }

  @BeforeEach
  void createSchemaAndSeedData() {
    jdbcTemplate.execute("drop table if exists audit_events");
    jdbcTemplate.execute(
        """
        create table audit_events (
          tenant_id uuid not null,
          id bigint not null,
          created_at timestamp with time zone not null,
          ingest_sequence bigint not null,
          payload text not null,
          primary key (tenant_id, id)
        )
        """);
    jdbcTemplate.execute(
        "create index audit_events_tenant_created_id_idx on audit_events (tenant_id, created_at desc, id desc)");
    for (long id = 1; id <= 5; id++) {
      insert(id, Instant.parse("2026-01-01T00:00:0" + id + "Z"), id);
    }
  }

  @Test
  void excludesEventsIngestedAfterTheFirstPageWatermark() {
    EventPage first = service.page(TENANT, 2, Optional.empty());
    insert(6, Instant.parse("2026-01-01T00:00:06Z"), 6);
    EventPage second = service.page(TENANT, 2, first.nextCursor());
    EventPage third = service.page(TENANT, 2, second.nextCursor());

    assertThat(ids(first, second, third)).containsExactly(5L, 4L, 3L, 2L, 1L);
    assertThat(ids(first, second, third)).doesNotContain(6L);
  }

  @Test
  void hasAnIndexCompatibleKeysetQueryPlan() {
    jdbcTemplate.execute("set local enable_seqscan = off");

    String plan =
        jdbcTemplate.queryForObject(
            """
            explain (format json, costs off)
            select tenant_id, id, created_at, ingest_sequence, payload
            from audit_events
            where tenant_id = ?
              and ingest_sequence <= ?
              and (created_at < ? or (created_at = ? and id < ?))
            order by created_at desc, id desc
            limit ?
            """,
            String.class,
            TENANT,
            5L,
            Instant.parse("2026-01-01T00:00:05Z"),
            Instant.parse("2026-01-01T00:00:05Z"),
            5L,
            3);

    assertThat(plan).contains("audit_events_tenant_created_id_idx");
    assertThat(plan).containsAnyOf("Index Scan", "Index Only Scan");
  }

  private void insert(long id, Instant createdAt, long ingestSequence) {
    jdbcTemplate.update(
        "insert into audit_events (tenant_id, id, created_at, ingest_sequence, payload) values (?, ?, ?, ?, ?)",
        TENANT,
        id,
        createdAt,
        ingestSequence,
        "event-" + id);
  }

  private static List<Long> ids(EventPage... pages) {
    return java.util.Arrays.stream(pages)
        .flatMap(page -> page.events().stream())
        .map(AuditEvent::id)
        .toList();
  }
}
