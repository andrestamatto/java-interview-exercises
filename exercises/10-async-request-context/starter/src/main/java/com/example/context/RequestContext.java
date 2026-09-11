package com.example.context;

public final class RequestContext {
  private static final ThreadLocal<String> TENANT = new ThreadLocal<>();

  private RequestContext() {}

  public static void setTenant(String tenant) {
    TENANT.set(tenant);
  }

  public static String tenant() {
    return TENANT.get();
  }

  public static void clear() {
    TENANT.remove();
  }
}
