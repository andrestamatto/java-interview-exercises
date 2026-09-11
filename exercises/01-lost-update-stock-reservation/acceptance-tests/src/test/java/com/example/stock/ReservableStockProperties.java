package com.example.stock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;
import net.jqwik.api.constraints.IntRange;
import org.junit.jupiter.api.Test;

class ReservableStockProperties {
    @Property
    void sequentialReservations_matchTheConservationModel(
            @ForAll @IntRange(min = 0, max = 100) int initialUnits,
            @ForAll("requestSequences") List<Integer> requestedUnits) {
        ReservableStock stock = new ReservableStock(initialUnits);
        int modelRemaining = initialUnits;

        for (int requested : requestedUnits) {
            ReservationOutcome expected;
            if (requested <= modelRemaining) {
                modelRemaining -= requested;
                expected = ReservationOutcome.RESERVED;
            } else {
                expected = ReservationOutcome.INSUFFICIENT_STOCK;
            }
            assertThat(stock.reserve(requested)).isEqualTo(expected);
            assertThat(stock.remainingUnits()).isEqualTo(modelRemaining);
        }
    }

    @Provide
    Arbitrary<List<Integer>> requestSequences() {
        return Arbitraries.integers().between(1, 20).list().ofMaxSize(50);
    }

    @Test
    void invalidInputs_areRejectedWithoutMutation() {
        assertThatThrownBy(() -> new ReservableStock(-1))
                .isInstanceOf(IllegalArgumentException.class);

        ReservableStock stock = new ReservableStock(3);
        assertThatThrownBy(() -> stock.reserve(0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> stock.reserve(-1)).isInstanceOf(IllegalArgumentException.class);
        assertThat(stock.remainingUnits()).isEqualTo(3);
    }
}
