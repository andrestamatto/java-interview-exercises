package com.example.payments;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Objects;
import java.util.UUID;
import javax.sql.DataSource;

/** Small JDBC adapter kept outside the admission and retry policy. */
public final class JdbcTransactionPool implements TransactionPool {
  private final DataSource dataSource;

  public JdbcTransactionPool(DataSource dataSource) {
    this.dataSource = Objects.requireNonNull(dataSource, "dataSource");
  }

  @Override
  public Transaction acquire() {
    try {
      return new JdbcTransaction(dataSource.getConnection());
    } catch (SQLException exception) {
      throw new IllegalStateException("could not acquire a database connection", exception);
    }
  }

  private static final class JdbcTransaction implements Transaction {
    private final Connection connection;

    private JdbcTransaction(Connection connection) {
      this.connection = connection;
    }

    @Override
    public void begin() {
      try {
        connection.setAutoCommit(false);
      } catch (SQLException exception) {
        throw new IllegalStateException("could not begin transaction", exception);
      }
    }

    @Override
    public void recordCapture(UUID captureId) {
      try (PreparedStatement statement =
          connection.prepareStatement("insert into payment_captures (capture_id) values (?)")) {
        statement.setObject(1, captureId);
        statement.executeUpdate();
      } catch (SQLException exception) {
        throw new IllegalStateException("could not record capture", exception);
      }
    }

    @Override
    public void commit() {
      try {
        connection.commit();
      } catch (SQLException exception) {
        throw new CommitOutcomeUnknownException(
            "commit outcome is unknown: " + exception.getSQLState());
      }
    }

    @Override
    public void rollback() {
      try {
        connection.rollback();
      } catch (SQLException exception) {
        throw new IllegalStateException("could not roll back transaction", exception);
      }
    }

    @Override
    public void close() {
      try {
        connection.close();
      } catch (SQLException exception) {
        throw new IllegalStateException("could not close database connection", exception);
      }
    }
  }
}
