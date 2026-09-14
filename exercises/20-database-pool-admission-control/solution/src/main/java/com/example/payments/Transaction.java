package com.example.payments;

import java.util.UUID;

public interface Transaction extends AutoCloseable {
  void begin();

  void recordCapture(UUID captureId);

  void commit();

  void rollback();

  @Override
  void close();
}
