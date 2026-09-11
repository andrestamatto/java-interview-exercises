package com.example.lookup;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class MembershipLookup {
  public List<String> allowed(List<String> requests, List<String> grants) {
    return allowed(requests, grants, new ComparisonCounter());
  }

  List<String> allowed(
      List<String> requests, List<String> grants, ComparisonCounter comparisonCounter) {
    Set<String> indexedGrants = new HashSet<>(grants);
    List<String> result = new ArrayList<>();
    for (String request : requests) {
      if (indexedGrants.contains(request)) {
        result.add(request);
      }
    }
    return result;
  }
}
