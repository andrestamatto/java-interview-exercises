package com.example.quota;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;
import net.jqwik.api.constraints.IntRange;

class RequestQuotaProperties {
  @Property
  void sequentialAcquisitionsMatchTheReferenceStateMachine(
      @ForAll @IntRange(min = 0, max = 100) int initialPermits,
      @ForAll("attemptCounts") List<Integer> attemptCounts) {
    RequestQuota quota = new RequestQuota(initialPermits);
    int remaining = initialPermits;
    for (int ignored : attemptCounts) {
      boolean expected = remaining > 0;
      if (expected) {
        remaining--;
      }
      assertThat(quota.tryAcquire()).isEqualTo(expected);
      assertThat(quota.availablePermits()).isEqualTo(remaining);
    }
  }

  @Provide
  Arbitrary<List<Integer>> attemptCounts() {
    return Arbitraries.integers().between(1, 50).list().ofMaxSize(100);
  }
}
