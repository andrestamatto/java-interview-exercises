package com.example.payments;

import static org.assertj.core.api.Assertions.assertThat;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Tag("integration")
@Testcontainers(disabledWithoutDocker = true)
@SuppressWarnings(
    "deprecation") // Testcontainers 2 keeps this container type for source compatibility.
class JdbcTransactionPoolIntegrationTest {
  @Container
  private static final PostgreSQLContainer<?> POSTGRES =
      new PostgreSQLContainer<>("postgres:17-alpine");

  private HikariDataSource dataSource;

  @BeforeEach
  void setUp() {
    HikariConfig configuration = new HikariConfig();
    configuration.setJdbcUrl(POSTGRES.getJdbcUrl());
    configuration.setUsername(POSTGRES.getUsername());
    configuration.setPassword(POSTGRES.getPassword());
    configuration.setMaximumPoolSize(2);
    configuration.setMinimumIdle(0);
    configuration.setConnectionTimeout(250);
    dataSource = new HikariDataSource(configuration);
    try (java.sql.Connection connection = dataSource.getConnection();
        java.sql.Statement statement = connection.createStatement()) {
      statement.execute("drop table if exists payment_captures");
      statement.execute("create table payment_captures (capture_id uuid primary key)");
    } catch (java.sql.SQLException exception) {
      throw new IllegalStateException("could not prepare PostgreSQL fixture", exception);
    }
  }

  @AfterEach
  void tearDown() {
    dataSource.close();
  }

  @Test
  void rollsBackReleasesTheHikariConnectionAndAllowsTheNextCapture() throws Exception {
    JdbcTransactionPool jdbcPool = new JdbcTransactionPool(dataSource);
    TransactionPool firstRecordFails = new FailingFirstRecordPool(jdbcPool);
    CaptureService service = new CaptureService(2, firstRecordFails, ignored -> {});

    assertThat(service.capture(UUID.fromString("00000000-0000-0000-0000-000000000020")))
        .isEqualTo(CaptureResult.RETRYABLE_BEFORE_COMMIT);
    assertThat(dataSource.getHikariPoolMXBean().getActiveConnections()).isZero();

    assertThat(service.capture(UUID.fromString("00000000-0000-0000-0000-000000000021")))
        .isEqualTo(CaptureResult.COMMITTED);
    assertThat(dataSource.getHikariPoolMXBean().getActiveConnections()).isZero();
    assertThat(rowCount()).isEqualTo(1);
  }

  private int rowCount() throws java.sql.SQLException {
    try (java.sql.Connection connection = dataSource.getConnection();
        java.sql.Statement statement = connection.createStatement();
        java.sql.ResultSet resultSet =
            statement.executeQuery("select count(*) from payment_captures")) {
      resultSet.next();
      return resultSet.getInt(1);
    }
  }

  private static final class FailingFirstRecordPool implements TransactionPool {
    private final TransactionPool delegate;
    private boolean first = true;

    private FailingFirstRecordPool(TransactionPool delegate) {
      this.delegate = delegate;
    }

    @Override
    public Transaction acquire() {
      Transaction transaction = delegate.acquire();
      if (!first) {
        return transaction;
      }
      first = false;
      return new RecordFailingTransaction(transaction);
    }
  }

  private static final class RecordFailingTransaction implements Transaction {
    private final Transaction delegate;

    private RecordFailingTransaction(Transaction delegate) {
      this.delegate = delegate;
    }

    @Override
    public void begin() {
      delegate.begin();
    }

    @Override
    public void recordCapture(UUID captureId) {
      throw new IllegalStateException("simulated pre-commit connection loss");
    }

    @Override
    public void commit() {
      delegate.commit();
    }

    @Override
    public void rollback() {
      delegate.rollback();
    }

    @Override
    public void close() {
      delegate.close();
    }
  }
}
