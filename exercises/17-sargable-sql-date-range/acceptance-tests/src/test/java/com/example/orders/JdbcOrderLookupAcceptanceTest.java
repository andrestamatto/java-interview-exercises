package com.example.orders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

@Tag("acceptance")
class JdbcOrderLookupAcceptanceTest {
  @Test
  void bindsThePrecomputedHalfOpenInstantRangeWithoutTransformingTheIndexedColumn() {
    JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
    Order expected =
        new Order(
            UUID.fromString("00000000-0000-0000-0000-000000000017"),
            Instant.parse("2024-03-10T12:00:00Z"),
            1_700);
    doReturn(List.of(expected))
        .when(jdbcTemplate)
        .query(anyString(), JdbcOrderLookupAcceptanceTest.<Order>anyRowMapper(), any(Object[].class));
    OrderDateRange range =
        OrderDateRange.forLocalDate(LocalDate.of(2024, 3, 10), ZoneId.of("America/New_York"));

    List<Order> orders = new JdbcOrderLookup(jdbcTemplate).findCreatedOn(range);

    ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<Object[]> arguments = ArgumentCaptor.forClass(Object[].class);
    verify(jdbcTemplate)
        .query(sql.capture(), JdbcOrderLookupAcceptanceTest.<Order>anyRowMapper(), arguments.capture());

    assertThat(orders).containsExactly(expected);
    assertThat(sql.getValue())
        .containsPattern("(?is)where\\s+created_at\\s*>=\\s*\\?\\s+and\\s+created_at\\s*<\\s*\\?")
        .doesNotContainIgnoringCase("at time zone")
        .doesNotContain("::date");
    assertThat(arguments.getValue())
        .containsExactly(
            Timestamp.from(Instant.parse("2024-03-10T05:00:00Z")),
            Timestamp.from(Instant.parse("2024-03-11T04:00:00Z")));
  }

  @SuppressWarnings("unchecked")
  private static RowMapper<Order> anyRowMapper() {
    return (RowMapper<Order>) any(RowMapper.class);
  }
}
