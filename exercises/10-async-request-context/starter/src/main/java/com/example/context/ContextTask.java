package com.example.context;

public final class ContextTask {
  private ContextTask() {}

  public static Runnable wrap(Runnable task) {
    return task;
  }
}
