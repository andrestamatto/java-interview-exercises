package com.example.orders;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Tag("integration")
@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers(disabledWithoutDocker = true)
@SuppressWarnings(
    "deprecation") // Testcontainers 2 keeps this container type for source compatibility.
class JdbcOrderLookupIntegrationTest {
  @Container
  private static final PostgreSQLContainer<?> POSTGRES =
      new PostgreSQLContainer<>("postgres:17-alpine");

  @Autowired private DataSource dataSource;

  private JdbcTemplate jdbcTemplate;

  @DynamicPropertySource
  static void databaseProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
    registry.add("spring.datasource.username", POSTGRES::getUsername);
    registry.add("spring.datasource.password", POSTGRES::getPassword);
  }

  @BeforeEach
  void prepareSchema() {
    jdbcTemplate = new JdbcTemplate(dataSource);
    jdbcTemplate.execute("drop table if exists purchase_orders");
    jdbcTemplate.execute(
        """
        create table purchase_orders (
          id uuid primary key,
          created_at timestamptz not null,
          total_cents bigint not null
        )
        """);
    jdbcTemplate.execute(
        "create index purchase_orders_created_at_id_idx on purchase_orders (created_at, id)");

    for (int hour = -180; hour < 180; hour++) {
      insertOrder(
          UUID.nameUUIDFromBytes(("filler-" + hour).getBytes()),
          Instant.parse("2024-03-10T05:00:00Z").plusSeconds(hour * 3_600L),
          100);
    }
    insertOrder(
        UUID.fromString("00000000-0000-0000-0000-000000000017"),
        Instant.parse("2024-03-10T05:00:00Z"),
        1_700);
    insertOrder(
        UUID.fromString("00000000-0000-0000-0000-000000000018"),
        Instant.parse("2024-03-11T03:59:59Z"),
        1_800);
    insertOrder(
        UUID.fromString("00000000-0000-0000-0000-000000000019"),
        Instant.parse("2024-03-11T04:00:00Z"),
        1_900);
    jdbcTemplate.execute("analyze purchase_orders");
  }

  @Test
  void retrievesTheDstRangeInOneStatementAndUsesItsCompositeIndexPlan() {
    OrderDateRange range =
        OrderDateRange.forLocalDate(LocalDate.of(2024, 3, 10), ZoneId.of("America/New_York"));
    CountingDataSource countingDataSource = new CountingDataSource(dataSource);
    JdbcOrderLookup lookup = new JdbcOrderLookup(new JdbcTemplate(countingDataSource));

    List<Order> orders = lookup.findCreatedOn(range);

    assertThat(orders)
        .extracting(Order::id)
        .contains(
            UUID.fromString("00000000-0000-0000-0000-000000000017"),
            UUID.fromString("00000000-0000-0000-0000-000000000018"))
        .doesNotContain(UUID.fromString("00000000-0000-0000-0000-000000000019"));
    assertThat(countingDataSource.executions()).isEqualTo(1);

    jdbcTemplate.execute("set enable_seqscan = off");
    String plan =
        jdbcTemplate.queryForObject(
            """
            explain (format json)
            select id, created_at, total_cents
            from purchase_orders
            where created_at >= ? and created_at < ?
            order by created_at, id
            """,
            String.class,
            Timestamp.from(range.startInclusive()),
            Timestamp.from(range.endExclusive()));

    assertThat(plan)
        .contains("Index Scan")
        .contains("purchase_orders_created_at_id_idx")
        .doesNotContain("Seq Scan");
  }

  private void insertOrder(UUID id, Instant createdAt, long totalCents) {
    jdbcTemplate.update(
        "insert into purchase_orders (id, created_at, total_cents) values (?, ?, ?)",
        id,
        Timestamp.from(createdAt),
        totalCents);
  }

  private static final class CountingDataSource implements DataSource {
    private final DataSource delegate;
    private final AtomicInteger executions = new AtomicInteger();

    private CountingDataSource(DataSource delegate) {
      this.delegate = delegate;
    }

    int executions() {
      return executions.get();
    }

    @Override
    public Connection getConnection() throws SQLException {
      return instrument(delegate.getConnection());
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
      return instrument(delegate.getConnection(username, password));
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
      return delegate.getLogWriter();
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
      delegate.setLogWriter(out);
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
      delegate.setLoginTimeout(seconds);
    }

    @Override
    public int getLoginTimeout() throws SQLException {
      return delegate.getLoginTimeout();
    }

    @Override
    public Logger getParentLogger() {
      return Logger.getLogger("com.example.orders");
    }

    @Override
    public <T> T unwrap(Class<T> interfaceType) throws SQLException {
      return delegate.unwrap(interfaceType);
    }

    @Override
    public boolean isWrapperFor(Class<?> interfaceType) throws SQLException {
      return delegate.isWrapperFor(interfaceType);
    }

    private Connection instrument(Connection connection) {
      return (Connection)
          Proxy.newProxyInstance(
              connection.getClass().getClassLoader(),
              new Class<?>[] {Connection.class},
              (proxy, method, arguments) -> {
                Object result = invoke(connection, method, arguments);
                if (method.getName().equals("prepareStatement")
                    && result instanceof PreparedStatement statement) {
                  return instrument(statement);
                }
                return result;
              });
    }

    private PreparedStatement instrument(PreparedStatement statement) {
      return (PreparedStatement)
          Proxy.newProxyInstance(
              statement.getClass().getClassLoader(),
              new Class<?>[] {PreparedStatement.class},
              (proxy, method, arguments) -> {
                if (method.getName().startsWith("execute")) {
                  executions.incrementAndGet();
                }
                return invoke(statement, method, arguments);
              });
    }

    private static Object invoke(Object target, java.lang.reflect.Method method, Object[] arguments)
        throws Throwable {
      try {
        return method.invoke(target, arguments);
      } catch (InvocationTargetException exception) {
        throw exception.getCause();
      }
    }
  }
}
