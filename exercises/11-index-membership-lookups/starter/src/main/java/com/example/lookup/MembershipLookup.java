package com.example.lookup;

import java.util.ArrayList;
import java.util.List;

public final class MembershipLookup {
  public List<String> allowed(List<String> requests, List<String> grants) {
    List<String> result = new ArrayList<>();
    for (String request : requests) {
      for (String grant : grants) {
        if (request.equals(grant)) {
          result.add(request);
          break;
        }
      }
    }
    return result;
  }
}
