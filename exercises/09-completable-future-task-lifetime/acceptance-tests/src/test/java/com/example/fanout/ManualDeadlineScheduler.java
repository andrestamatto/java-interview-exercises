package com.example.fanout;

import java.time.Duration;
import java.util.Objects;

final class ManualDeadlineScheduler implements DeadlineScheduler {
  private Runnable action;
  private boolean cancelled;

  @Override
  public ScheduledTask schedule(Duration delay, Runnable scheduledAction) {
    if (delay.isNegative() || delay.isZero()) {
      throw new IllegalArgumentException("delay must be positive");
    }
    action = Objects.requireNonNull(scheduledAction);
    return () -> cancelled = true;
  }

  void fire() {
    if (action == null) {
      throw new IllegalStateException("no deadline was scheduled");
    }
    action.run();
  }

  boolean wasCancelled() {
    return cancelled;
  }
}
