package com.example.challenge;

import java.util.List;
import java.util.function.Function;
import java.util.stream.IntStream;

public class MyAppApplication {

    private static final OrderProcessor.ThreadMode THREAD_MODE = OrderProcessor.ThreadMode.VIRTUAL;

    record Scenario(
            String title,
            List<String> orders,
            Function<List<String>, List<String>> process,
            boolean mayHaveRaceCondition
    ) {}

    public static void main(String[] args) {
        final int limit = 15000;
        var processor = new OrderProcessor(THREAD_MODE);

        var scenarios = List.of(
                new Scenario("ArrayList → Problem!",
                        IntStream.range(0, limit).mapToObj(i -> "ArrayListOrder-" + i).toList(),
                        processor::processLargePayloadArrayList,
                        true),

                new Scenario("CopyOnWriteArrayList → Solution 1.",
                        IntStream.range(0, limit).mapToObj(i -> "CopyOnWriteOrder-" + i).toList(),
                        processor::processLargePayloadCopyOnWriteArrayList,
                        false),

                new Scenario("SynchronizedList → Solution 2.",
                        IntStream.range(0, limit).mapToObj(i -> "SynchronizedOrder-" + i).toList(),
                        processor::processLargePayloadSynchronizedList,
                        false),

                new Scenario("ConcurrentLinkedQueue → Solution 3.",
                        IntStream.range(0, limit).mapToObj(i -> "ConcurrentLinkedQueueOrder-" + i).toList(),
                        processor::processLargePayloadConcurrentLinkedQueue,
                        false)
        );

        System.out.println("=== Executing Concurrency Challenge ===");
        System.out.println("Thread mode: " + THREAD_MODE + "\n");
        scenarios.forEach(scenario -> executeScenario(scenario, limit));
        System.out.println("=== Finished. ===");
    }

    private static void executeScenario(Scenario scenario, int expectedTotal) {
        System.out.println("--- Executing Concurrency Challenge with " + scenario.title());

        long start = System.nanoTime();
        List<String> result = scenario.process().apply(scenario.orders());
        long elapsedMs = (System.nanoTime() - start) / 1_000_000;

        System.out.println("Elapsed time: " + elapsedMs + "ms");
        System.out.printf("Expected total: %d, Processed total: %d%n",
                expectedTotal, result.size());

        String warning = scenario.mayHaveRaceCondition()
                ? "Warning: A race condition may occur, and the final size could be less than " + expectedTotal + "!"
                : "Warning: No race condition. Final size must be " + expectedTotal + "!";

        System.out.println(warning);
        System.out.println("--- End ---\n");
    }
}
