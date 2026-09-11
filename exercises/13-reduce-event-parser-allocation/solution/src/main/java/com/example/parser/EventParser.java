package com.example.parser;

public final class EventParser {
  public Event parse(String input) {
    return parse(input, new ParserAllocationProbe());
  }

  Event parse(String input, ParserAllocationProbe probe) {
    if (input == null) {
      throw new IllegalArgumentException("input must not be null");
    }
    int first = input.indexOf('|');
    int second = input.indexOf('|', first + 1);
    if (first <= 0 || second <= first + 1 || input.indexOf('|', second + 1) >= 0) {
      throw new IllegalArgumentException("input must contain type, customer id, and amount");
    }
    try {
      return new Event(
          input.substring(0, first),
          input.substring(first + 1, second),
          Long.parseLong(input.substring(second + 1)));
    } catch (NumberFormatException exception) {
      throw new IllegalArgumentException("amount must be a signed long", exception);
    }
  }
}
