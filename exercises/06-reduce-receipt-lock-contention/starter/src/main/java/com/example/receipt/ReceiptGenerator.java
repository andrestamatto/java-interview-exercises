package com.example.receipt;

import java.util.Objects;

public final class ReceiptGenerator {
  private final ReceiptSigner signer;
  private long nextSequence = 1;

  public ReceiptGenerator(ReceiptSigner signer) {
    this.signer = Objects.requireNonNull(signer, "signer");
  }

  public synchronized Receipt issue(String document) throws InterruptedException {
    long sequence = nextSequence++;
    return new Receipt(sequence, signer.sign(sequence, document));
  }
}
