package com.example.transfer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import org.junit.jupiter.api.Test;

class AccountTransferTest {
  @Test
  void transfersFundsAndPreservesTheTotal() throws Exception {
    Account source = new Account("source", 100);
    Account destination = new Account("destination", 20);

    boolean transferred = new AccountTransferService().transfer(source, destination, 30);

    assertThat(transferred).isTrue();
    assertThat(source.balance()).isEqualTo(70);
    assertThat(destination.balance()).isEqualTo(50);
  }

  @Test
  void rejectsInvalidTransferRequests() {
    Account account = new Account("account", 100);
    AccountTransferService service = new AccountTransferService();

    assertThatIllegalArgumentException().isThrownBy(() -> service.transfer(account, account, 1));
    assertThatIllegalArgumentException().isThrownBy(
        () -> service.transfer(account, new Account("other", 0), 0));
  }

  @Test
  void rejectsATransferThatWouldOverflowTheDestinationBalance() throws Exception {
    Account source = new Account("source", 1);
    Account destination = new Account("destination", Long.MAX_VALUE);

    boolean transferred = new AccountTransferService().transfer(source, destination, 1);

    assertThat(transferred).isFalse();
    assertThat(source.balance()).isEqualTo(1);
    assertThat(destination.balance()).isEqualTo(Long.MAX_VALUE);
  }
}
