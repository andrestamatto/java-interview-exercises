package com.example.scheduler;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Combinators;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;

class DeadlineQueueProperties {
  @Property
  void mixedOperationsMatchTheActiveJobReferenceModel(
      @ForAll("operations") List<Operation> operations) {
    DeadlineQueue queue = new DeadlineQueue();
    Map<String, ScheduledJob> activeJobs = new HashMap<>();

    for (Operation operation : operations) {
      switch (operation) {
        case Schedule schedule -> {
          ScheduledJob job = new ScheduledJob(schedule.id(), schedule.deadlineMillis());
          queue.schedule(job);
          activeJobs.put(job.id(), job);
        }
        case Cancel cancel ->
            assertThat(queue.cancel(cancel.id())).isEqualTo(activeJobs.remove(cancel.id()) != null);
        case Poll ignored -> {
          ScheduledJob expected =
              activeJobs.values().stream()
                  .min(
                      Comparator.comparingLong(ScheduledJob::deadlineMillis)
                          .thenComparing(ScheduledJob::id))
                  .orElse(null);
          assertThat(queue.poll()).isEqualTo(expected);
          if (expected != null) {
            activeJobs.remove(expected.id());
          }
        }
      }
    }
  }

  @Provide
  Arbitrary<List<Operation>> operations() {
    Arbitrary<String> ids = Arbitraries.integers().between(0, 7).map(index -> "job-" + index);
    Arbitrary<Operation> schedule =
        Combinators.combine(ids, Arbitraries.longs().between(0, 100))
            .as((id, deadline) -> new Schedule(id, deadline));
    Arbitrary<Operation> cancel = ids.map(Cancel::new);
    Arbitrary<Operation> poll = Arbitraries.just(new Poll());
    return Arbitraries.frequencyOf(
            net.jqwik.api.Tuple.of(5, schedule),
            net.jqwik.api.Tuple.of(3, cancel),
            net.jqwik.api.Tuple.of(2, poll))
        .list()
        .ofMaxSize(200);
  }

  private sealed interface Operation permits Schedule, Cancel, Poll {}

  private record Schedule(String id, long deadlineMillis) implements Operation {}

  private record Cancel(String id) implements Operation {}

  private record Poll() implements Operation {}
}
