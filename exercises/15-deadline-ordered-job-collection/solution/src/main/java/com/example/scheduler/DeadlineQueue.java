package com.example.scheduler;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

public final class DeadlineQueue {
  private final Map<String, ScheduledJob> activeById = new HashMap<>();
  private final PriorityQueue<ScheduledJob> jobs =
      new PriorityQueue<>(
          Comparator.comparingLong(ScheduledJob::deadlineMillis).thenComparing(ScheduledJob::id));

  public void schedule(ScheduledJob job) {
    activeById.put(job.id(), job);
    jobs.add(job);
  }

  public boolean cancel(String id) {
    return activeById.remove(id) != null;
  }

  public ScheduledJob poll() {
    return pollActiveJob();
  }

  ScheduledJob poll(ScanProbe probe) {
    return pollActiveJob();
  }

  private ScheduledJob pollActiveJob() {
    while (!jobs.isEmpty()) {
      ScheduledJob job = jobs.poll();
      if (activeById.remove(job.id(), job)) {
        return job;
      }
    }
    return null;
  }
}
