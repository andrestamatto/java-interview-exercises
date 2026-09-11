package com.example.parser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("acceptance")
class EventParserAcceptanceTest {
  private final EventParser parser = new EventParser();

  @Test
  void preservesTheDelimitedEventContract() {
    assertThat(parser.parse("PAYMENT|customer-7|-42"))
        .isEqualTo(new Event("PAYMENT", "customer-7", -42));
  }

  @Test
  void rejectsMalformedEventsConsistently() {
    assertThatIllegalArgumentException().isThrownBy(() -> parser.parse(null));
    assertThatIllegalArgumentException().isThrownBy(() -> parser.parse("PAYMENT|customer-7"));
    assertThatIllegalArgumentException().isThrownBy(() -> parser.parse("|customer-7|5"));
    assertThatIllegalArgumentException().isThrownBy(() -> parser.parse("PAYMENT||5"));
    assertThatIllegalArgumentException().isThrownBy(() -> parser.parse("PAYMENT|customer-7|5|extra"));
    assertThatIllegalArgumentException().isThrownBy(() -> parser.parse("PAYMENT|customer-7|five"));
  }

  @Test
  void avoidsAnIntermediateFieldsArray() {
    ParserAllocationProbe probe = new ParserAllocationProbe();

    assertThat(parser.parse("PAYMENT|customer-7|5", probe))
        .isEqualTo(new Event("PAYMENT", "customer-7", 5));
    assertThat(probe.intermediateArrays()).isZero();
  }
}
