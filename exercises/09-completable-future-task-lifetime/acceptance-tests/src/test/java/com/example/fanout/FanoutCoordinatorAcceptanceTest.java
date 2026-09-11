package com.example.fanout;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("acceptance")
class FanoutCoordinatorAcceptanceTest {
  @Test
  void cancelsTheLosingChildAfterTheParentCompletes() {
    CompletableFuture<String> winner = new CompletableFuture<>();
    CompletableFuture<String> loser = new CompletableFuture<>();

    CompletableFuture<String> result = new FanoutCoordinator().first(() -> winner, () -> loser);
    winner.complete("winner");

    assertThat(result.join()).isEqualTo("winner");
    assertThat(loser.isCancelled()).isTrue();
  }
}
