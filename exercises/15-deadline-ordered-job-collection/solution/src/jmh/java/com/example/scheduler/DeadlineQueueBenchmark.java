package com.example.scheduler;

import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(2)
public class DeadlineQueueBenchmark {
  @Param({"100", "10000"})
  public int activeJobs;

  private DeadlineQueue queue;
  private long nextDeadline;
  private long nextPriority;

  @Setup
  public void setUp() {
    queue = new DeadlineQueue();
    for (int index = 0; index < activeJobs; index++) {
      queue.schedule(new ScheduledJob("job-" + index, index));
    }
    nextDeadline = activeJobs;
    nextPriority = Long.MIN_VALUE;
  }

  @Benchmark
  public ScheduledJob pollAndReschedule() {
    ScheduledJob job = queue.poll();
    queue.schedule(new ScheduledJob(job.id(), nextDeadline++));
    return job;
  }

  @Benchmark
  public ScheduledJob cancelScheduleAndPoll() {
    queue.cancel("job-0");
    queue.schedule(new ScheduledJob("job-0", nextPriority++));
    ScheduledJob rescheduled = queue.poll();
    ScheduledJob next = queue.poll();
    queue.schedule(rescheduled);
    queue.schedule(new ScheduledJob(next.id(), nextDeadline++));
    return next;
  }
}
