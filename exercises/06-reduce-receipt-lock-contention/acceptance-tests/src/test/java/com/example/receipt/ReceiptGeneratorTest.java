package com.example.receipt;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ReceiptGeneratorTest {
  @Test
  void assignsUniqueSequencesAndKeepsTheirSignaturesPaired() throws Exception {
    ReceiptGenerator generator = new ReceiptGenerator((sequence, document) -> sequence + ":" + document);

    Receipt first = generator.issue("first");
    Receipt second = generator.issue("second");

    assertThat(first).isEqualTo(new Receipt(1, "1:first"));
    assertThat(second).isEqualTo(new Receipt(2, "2:second"));
  }
}
