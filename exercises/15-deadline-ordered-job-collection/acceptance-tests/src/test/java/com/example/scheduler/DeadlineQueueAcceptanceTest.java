package com.example.scheduler;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("acceptance")
class DeadlineQueueAcceptanceTest {
  @Test
  void pollsInDeadlineThenIdentifierOrder() {
    DeadlineQueue queue = new DeadlineQueue();
    queue.schedule(new ScheduledJob("b", 10));
    queue.schedule(new ScheduledJob("a", 10));
    queue.schedule(new ScheduledJob("late", 20));

    assertThat(queue.poll()).isEqualTo(new ScheduledJob("a", 10));
    assertThat(queue.poll()).isEqualTo(new ScheduledJob("b", 10));
    assertThat(queue.poll()).isEqualTo(new ScheduledJob("late", 20));
    assertThat(queue.poll()).isNull();
  }

  @Test
  void makesCancellationIdempotentAndRemovesTheScheduledJob() {
    DeadlineQueue queue = new DeadlineQueue();
    queue.schedule(new ScheduledJob("cancelled", 1));
    queue.schedule(new ScheduledJob("next", 2));

    assertThat(queue.cancel("cancelled")).isTrue();
    assertThat(queue.cancel("cancelled")).isFalse();
    assertThat(queue.poll()).isEqualTo(new ScheduledJob("next", 2));
  }

  @Test
  void avoidsScanningEveryScheduledJobWhenPolling() {
    DeadlineQueue queue = new DeadlineQueue();
    for (int index = 0; index < 20; index++) {
      queue.schedule(new ScheduledJob("job-" + index, index));
    }
    ScanProbe probe = new ScanProbe();

    assertThat(queue.poll(probe)).isEqualTo(new ScheduledJob("job-0", 0));
    assertThat(probe.inspectedJobs()).isZero();
  }
}
