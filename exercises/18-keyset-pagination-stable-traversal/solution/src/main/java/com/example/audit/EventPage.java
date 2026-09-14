package com.example.audit;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public record EventPage(List<AuditEvent> events, Optional<String> nextCursor) {
  public EventPage {
    events = List.copyOf(events);
    nextCursor = Objects.requireNonNull(nextCursor, "nextCursor");
  }
}
