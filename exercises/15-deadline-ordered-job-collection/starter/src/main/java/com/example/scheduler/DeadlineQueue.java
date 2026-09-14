package com.example.scheduler;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class DeadlineQueue {
  private final List<ScheduledJob> jobs = new ArrayList<>();

  public void schedule(ScheduledJob job) {
    jobs.add(job);
  }

  public boolean cancel(String id) {
    return jobs.removeIf(job -> job.id().equals(id));
  }

  public ScheduledJob poll() {
    return poll(new ScanProbe());
  }

  ScheduledJob poll(ScanProbe probe) {
    jobs.forEach(job -> probe.inspect());
    return jobs.stream()
        .min(Comparator.comparingLong(ScheduledJob::deadlineMillis).thenComparing(ScheduledJob::id))
        .map(
            job -> {
              jobs.remove(job);
              return job;
            })
        .orElse(null);
  }
}
