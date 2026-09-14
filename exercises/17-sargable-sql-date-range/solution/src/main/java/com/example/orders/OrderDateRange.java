package com.example.orders;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Objects;

public record OrderDateRange(
    LocalDate localDate, ZoneId zone, Instant startInclusive, Instant endExclusive) {
  public OrderDateRange {
    Objects.requireNonNull(localDate, "localDate must not be null");
    Objects.requireNonNull(zone, "zone must not be null");
    Objects.requireNonNull(startInclusive, "startInclusive must not be null");
    Objects.requireNonNull(endExclusive, "endExclusive must not be null");
    if (!startInclusive.isBefore(endExclusive)) {
      throw new IllegalArgumentException("startInclusive must be before endExclusive");
    }
  }

  public static OrderDateRange forLocalDate(LocalDate localDate, ZoneId zone) {
    Objects.requireNonNull(localDate, "localDate must not be null");
    Objects.requireNonNull(zone, "zone must not be null");
    Instant startInclusive = localDate.atStartOfDay(zone).toInstant();
    Instant endExclusive = localDate.plusDays(1).atStartOfDay(zone).toInstant();
    return new OrderDateRange(localDate, zone, startInclusive, endExclusive);
  }
}
