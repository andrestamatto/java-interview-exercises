package com.example.orders;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

public final class JdbcOrderLookup implements OrderLookup {
  private static final String FIND_BY_LOCAL_DATE =
      """
      select id, created_at, total_cents
      from purchase_orders
      where (created_at at time zone ?)::date = ?
      order by created_at, id
      """;

  private static final RowMapper<Order> ORDER_ROW_MAPPER = JdbcOrderLookup::mapOrder;

  private final JdbcTemplate jdbcTemplate;

  public JdbcOrderLookup(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = Objects.requireNonNull(jdbcTemplate, "jdbcTemplate must not be null");
  }

  @Override
  public List<Order> findCreatedOn(OrderDateRange range) {
    Objects.requireNonNull(range, "range must not be null");
    return jdbcTemplate.query(
        FIND_BY_LOCAL_DATE, ORDER_ROW_MAPPER, range.zone().getId(), range.localDate());
  }

  private static Order mapOrder(ResultSet resultSet, int rowNumber) throws SQLException {
    return new Order(
        resultSet.getObject("id", UUID.class),
        resultSet.getObject("created_at", OffsetDateTime.class).toInstant(),
        resultSet.getLong("total_cents"));
  }
}
