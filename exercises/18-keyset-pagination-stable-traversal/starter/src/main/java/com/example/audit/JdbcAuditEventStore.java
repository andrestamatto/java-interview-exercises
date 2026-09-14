package com.example.audit;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

public final class JdbcAuditEventStore implements AuditEventStore {
  private static final RowMapper<AuditEvent> ROW_MAPPER = JdbcAuditEventStore::mapEvent;

  private final JdbcTemplate jdbcTemplate;

  public JdbcAuditEventStore(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = Objects.requireNonNull(jdbcTemplate, "jdbcTemplate");
  }

  @Override
  public long loadHighWatermark(UUID tenantId) {
    Long watermark =
        jdbcTemplate.queryForObject(
            "select coalesce(max(ingest_sequence), 0) from audit_events where tenant_id = ?",
            Long.class,
            tenantId);
    return Objects.requireNonNull(watermark, "watermark");
  }

  @Override
  public List<AuditEvent> loadOffsetPage(UUID tenantId, int offset, int limit) {
    return jdbcTemplate.query(
        """
        select tenant_id, id, created_at, ingest_sequence, payload
        from audit_events
        where tenant_id = ?
        order by created_at desc, id desc
        limit ? offset ?
        """,
        ROW_MAPPER,
        tenantId,
        limit,
        offset);
  }

  @Override
  public List<AuditEvent> loadFirstPage(UUID tenantId, long highWatermark, int limit) {
    throw new UnsupportedOperationException("Implement the keyset query");
  }

  @Override
  public List<AuditEvent> loadNextPage(
      UUID tenantId, long highWatermark, Instant beforeCreatedAt, long beforeId, int limit) {
    throw new UnsupportedOperationException("Implement the keyset query");
  }

  private static AuditEvent mapEvent(ResultSet resultSet, int rowNumber) throws SQLException {
    return new AuditEvent(
        resultSet.getObject("tenant_id", UUID.class),
        resultSet.getLong("id"),
        resultSet.getTimestamp("created_at").toInstant(),
        resultSet.getLong("ingest_sequence"),
        resultSet.getString("payload"));
  }
}
