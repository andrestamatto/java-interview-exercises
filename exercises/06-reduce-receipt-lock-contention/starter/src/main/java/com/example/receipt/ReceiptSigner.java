package com.example.receipt;

@FunctionalInterface
public interface ReceiptSigner {
  String sign(long sequence, String document) throws InterruptedException;
}
