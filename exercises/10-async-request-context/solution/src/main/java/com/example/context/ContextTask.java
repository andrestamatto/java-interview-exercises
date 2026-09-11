package com.example.context;

public final class ContextTask {
  private ContextTask() {}

  public static Runnable wrap(Runnable task) {
    String capturedTenant = RequestContext.tenant();
    return () -> {
      try {
        RequestContext.setTenant(capturedTenant);
        task.run();
      } finally {
        RequestContext.clear();
      }
    };
  }
}
