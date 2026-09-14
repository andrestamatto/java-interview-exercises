package com.example.payments;

import java.util.UUID;

@FunctionalInterface
public interface RiskGateway {
  void approve(UUID captureId);
}
