package com.example.receipt;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

public final class ReceiptGenerator {
  private final ReceiptSigner signer;
  private final AtomicLong nextSequence = new AtomicLong(1);

  public ReceiptGenerator(ReceiptSigner signer) {
    this.signer = Objects.requireNonNull(signer, "signer");
  }

  public Receipt issue(String document) throws InterruptedException {
    long sequence = nextSequence.getAndIncrement();
    return new Receipt(sequence, signer.sign(sequence, document));
  }
}
