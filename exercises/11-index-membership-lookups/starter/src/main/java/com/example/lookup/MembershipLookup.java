package com.example.lookup;

import java.util.ArrayList;
import java.util.List;

public final class MembershipLookup {
  public List<String> allowed(List<String> requests, List<String> grants) {
    return allowed(requests, grants, new ComparisonCounter());
  }

  List<String> allowed(
      List<String> requests, List<String> grants, ComparisonCounter comparisonCounter) {
    List<String> result = new ArrayList<>();
    for (String request : requests) {
      for (String grant : grants) {
        comparisonCounter.increment();
        if (request.equals(grant)) {
          result.add(request);
          break;
        }
      }
    }
    return result;
  }
}
