package com.example.orders;

import java.util.concurrent.atomic.AtomicInteger;
import org.hibernate.resource.jdbc.spi.StatementInspector;

public final class CountingStatementInspector implements StatementInspector {
  private static final long serialVersionUID = 1L;
  private static final AtomicInteger SELECTS = new AtomicInteger();

  @Override
  public String inspect(String sql) {
    if (sql.stripLeading().startsWith("select")) {
      SELECTS.incrementAndGet();
    }
    return sql;
  }

  static void reset() {
    SELECTS.set(0);
  }

  static int selectCount() {
    return SELECTS.get();
  }
}
