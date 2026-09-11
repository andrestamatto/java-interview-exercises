package com.example.lookup;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("acceptance")
class MembershipLookupAcceptanceTest {
  @Test
  void preservesRequestOrderAndDuplicates() {
    List<String> allowed = new MembershipLookup().allowed(List.of("a", "b", "a"), List.of("a"));
    assertThat(allowed).containsExactly("a", "a");
  }

  @Test
  void avoidsRepeatedMembershipComparisons() {
    List<String> requests =
        java.util.stream.IntStream.range(0, 20).mapToObj(index -> "request-" + index).toList();
    List<String> grants =
        java.util.stream.IntStream.range(0, 20).mapToObj(index -> "grant-" + index).toList();
    ComparisonCounter counter = new ComparisonCounter();

    assertThat(new MembershipLookup().allowed(requests, grants, counter)).isEmpty();
    assertThat(counter.comparisons()).isLessThan(20);
  }
}
