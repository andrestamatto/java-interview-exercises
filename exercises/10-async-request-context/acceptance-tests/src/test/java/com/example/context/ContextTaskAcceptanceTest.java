package com.example.context;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("acceptance")
class ContextTaskAcceptanceTest {
  @Test
  void propagatesAndClearsContextOnAReusedWorker() throws Exception {
    try (ExecutorService worker = Executors.newSingleThreadExecutor()) {
      RequestContext.setTenant("tenant-a");
      AtomicReference<String> observed = new AtomicReference<>();
      worker.submit(ContextTask.wrap(() -> observed.set(RequestContext.tenant()))).get();
      RequestContext.clear();
      String after = worker.submit(RequestContext::tenant).get();
      assertThat(observed.get()).isEqualTo("tenant-a");
      assertThat(after).isNull();
    }
  }

  @Test
  void clearsWorkerContextWhenTheTaskFails() throws Exception {
    try (ExecutorService worker = Executors.newSingleThreadExecutor()) {
      RequestContext.setTenant("tenant-a");
      AtomicReference<String> observed = new AtomicReference<>();

      java.util.concurrent.Future<?> failed =
          worker.submit(
              ContextTask.wrap(
                  () -> {
                    observed.set(RequestContext.tenant());
                    throw new IllegalStateException("simulated failure");
                  }));
      org.assertj.core.api.Assertions.assertThatThrownBy(failed::get)
          .hasCauseInstanceOf(IllegalStateException.class);
      RequestContext.clear();

      assertThat(observed.get()).isEqualTo("tenant-a");
      assertThat(worker.submit(RequestContext::tenant).get()).isNull();
    }
  }

  @Test
  void preservesAnAbsentContextAndTheCallerContext() throws Exception {
    try (ExecutorService worker = Executors.newSingleThreadExecutor()) {
      RequestContext.clear();
      Runnable wrapped = ContextTask.wrap(() -> assertThat(RequestContext.tenant()).isNull());
      RequestContext.setTenant("caller-after-capture");

      worker.submit(wrapped).get();

      assertThat(RequestContext.tenant()).isEqualTo("caller-after-capture");
      RequestContext.clear();
    }
  }
}
