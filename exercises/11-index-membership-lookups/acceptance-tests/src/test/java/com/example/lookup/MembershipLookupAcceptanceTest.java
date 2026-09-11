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
}
