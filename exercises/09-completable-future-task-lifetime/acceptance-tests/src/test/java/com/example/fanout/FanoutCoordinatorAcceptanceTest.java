package com.example.fanout;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeoutException;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("acceptance")
class FanoutCoordinatorAcceptanceTest {
  @Test
  void cancelsTheLosingChildAfterTheParentCompletes() {
    CompletableFuture<String> winner = new CompletableFuture<>();
    CompletableFuture<String> loser = new CompletableFuture<>();
    ManualDeadlineScheduler scheduler = new ManualDeadlineScheduler();

    CompletableFuture<String> result =
        new FanoutCoordinator(scheduler).first(() -> winner, () -> loser, Duration.ofSeconds(1));
    winner.complete("winner");

    assertThat(result.join()).isEqualTo("winner");
    assertThat(loser.isCancelled()).isTrue();
    assertThat(scheduler.wasCancelled()).isTrue();
  }

  @Test
  void waitsForAPotentiallySuccessfulSiblingAfterOneFailure() {
    CompletableFuture<String> failed = new CompletableFuture<>();
    CompletableFuture<String> winner = new CompletableFuture<>();
    CompletableFuture<String> result =
        new FanoutCoordinator(new ManualDeadlineScheduler())
            .first(() -> failed, () -> winner, Duration.ofSeconds(1));

    failed.completeExceptionally(new IllegalStateException("left failed"));

    assertThat(result).isNotCompleted();
    assertThat(winner.isCancelled()).isFalse();
    winner.complete("right result");
    assertThat(result.join()).isEqualTo("right result");
  }

  @Test
  void preservesTheFirstDependencyFailureWhenEveryBranchFails() {
    CompletableFuture<String> left = new CompletableFuture<>();
    CompletableFuture<String> right = new CompletableFuture<>();
    CompletableFuture<String> result =
        new FanoutCoordinator(new ManualDeadlineScheduler())
            .first(() -> left, () -> right, Duration.ofSeconds(1));

    left.completeExceptionally(new IllegalArgumentException("invalid left response"));
    right.completeExceptionally(new IllegalStateException("right failed too"));

    assertThatThrownBy(result::join)
        .isInstanceOf(CompletionException.class)
        .hasCauseInstanceOf(IllegalArgumentException.class)
        .hasRootCauseMessage("invalid left response");
  }

  @Test
  void cancelsEveryChildWhenTheSharedDeadlineExpires() {
    CompletableFuture<String> left = new CompletableFuture<>();
    CompletableFuture<String> right = new CompletableFuture<>();
    ManualDeadlineScheduler scheduler = new ManualDeadlineScheduler();
    CompletableFuture<String> result =
        new FanoutCoordinator(scheduler).first(() -> left, () -> right, Duration.ofSeconds(1));

    scheduler.fire();

    assertThatThrownBy(result::join)
        .isInstanceOf(CompletionException.class)
        .hasCauseInstanceOf(TimeoutException.class);
    assertThat(left.isCancelled()).isTrue();
    assertThat(right.isCancelled()).isTrue();
  }

  @Test
  void propagatesParentCancellationToChildrenAndDeadline() {
    CompletableFuture<String> left = new CompletableFuture<>();
    CompletableFuture<String> right = new CompletableFuture<>();
    ManualDeadlineScheduler scheduler = new ManualDeadlineScheduler();
    CompletableFuture<String> result =
        new FanoutCoordinator(scheduler).first(() -> left, () -> right, Duration.ofSeconds(1));

    assertThat(result.cancel(true)).isTrue();

    assertThat(left.isCancelled()).isTrue();
    assertThat(right.isCancelled()).isTrue();
    assertThat(scheduler.wasCancelled()).isTrue();
  }

  @Test
  void rejectsANonPositiveDeadline() {
    FanoutCoordinator coordinator = new FanoutCoordinator(new ManualDeadlineScheduler());

    assertThatIllegalArgumentException()
        .isThrownBy(
            () ->
                coordinator.first(
                    () -> new CompletableFuture<>(), () -> new CompletableFuture<>(), Duration.ZERO))
        .withMessage("timeout must be positive");
  }
}
