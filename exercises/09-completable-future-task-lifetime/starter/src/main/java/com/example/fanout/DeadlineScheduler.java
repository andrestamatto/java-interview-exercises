package com.example.fanout;

import java.time.Duration;

@FunctionalInterface
public interface DeadlineScheduler {
  ScheduledTask schedule(Duration delay, Runnable action);

  interface ScheduledTask {
    void cancel();
  }
}
