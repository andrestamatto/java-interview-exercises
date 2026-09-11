package com.example.parser;

public final class EventParser {
  public Event parse(String input) {
    return parse(input, new ParserAllocationProbe());
  }

  Event parse(String input, ParserAllocationProbe probe) {
    if (input == null) {
      throw new IllegalArgumentException("input must not be null");
    }
    String[] fields = input.split("\\|", -1);
    probe.recordIntermediateArray();
    if (fields.length != 3 || fields[0].isEmpty() || fields[1].isEmpty()) {
      throw new IllegalArgumentException("input must contain type, customer id, and amount");
    }
    try {
      return new Event(fields[0], fields[1], Long.parseLong(fields[2]));
    } catch (NumberFormatException exception) {
      throw new IllegalArgumentException("amount must be a signed long", exception);
    }
  }
}
