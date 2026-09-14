package com.example.orders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import org.junit.jupiter.api.Test;

class OrderDateRangeContractTest {
  @Test
  void modelsTheNewYorkSpringForwardDayAsOneHalfOpenInstantRange() {
    OrderDateRange range =
        OrderDateRange.forLocalDate(LocalDate.of(2024, 3, 10), ZoneId.of("America/New_York"));

    assertThat(range.startInclusive()).isEqualTo(Instant.parse("2024-03-10T05:00:00Z"));
    assertThat(range.endExclusive()).isEqualTo(Instant.parse("2024-03-11T04:00:00Z"));
    assertThat(range.endExclusive()).isEqualTo(range.startInclusive().plusSeconds(23 * 60 * 60));
  }

  @Test
  void rejectsAnEmptyOrReversedRange() {
    Instant instant = Instant.parse("2024-03-10T05:00:00Z");

    assertThatIllegalArgumentException()
        .isThrownBy(
            () ->
                new OrderDateRange(
                    LocalDate.of(2024, 3, 10), ZoneId.of("America/New_York"), instant, instant));
  }
}
