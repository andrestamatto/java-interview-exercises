package com.example.challenge;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.function.Consumer;

public class OrderProcessor {

    private static final long EXTERNAL_IO_DELAY_MS = 100;
    private final ThreadMode threadMode;

    public enum ThreadMode {
        VIRTUAL,
        PLATFORM
    }

    public OrderProcessor() {
        this(ThreadMode.VIRTUAL);
    }

    public OrderProcessor(ThreadMode threadMode) {
        this.threadMode = Objects.requireNonNull(threadMode, "threadMode must not be null");
    }

    public List<String> processLargePayloadArrayList(List<String> orders) {
        // Concurrency problem!
        var processedOrders = new ArrayList<String>();
        process(orders, processedOrders::add);
        return processedOrders;
    }

    public List<String> processLargePayloadCopyOnWriteArrayList(List<String> orders) {
        // Solution 1!
        var processedOrders = new CopyOnWriteArrayList<String>();
        process(orders, processedOrders::add);
        return processedOrders;
    }

    public List<String> processLargePayloadSynchronizedList(List<String> orders) {
        // Solution 2!
        var processedOrders = Collections.synchronizedList(new ArrayList<String>());
        process(orders, processedOrders::add);
        return processedOrders;
    }

    public List<String> processLargePayloadConcurrentLinkedQueue(List<String> orders) {
        // Solution 3!
        Queue<String> processedOrders = new ConcurrentLinkedQueue<>();
        process(orders, processedOrders::add);
        return new ArrayList<>(processedOrders);
    }

    private void process(List<String> orders, Consumer<String> addProcessedOrder) {
        try (ExecutorService executor = createExecutor()) {
            List<Future<?>> tasks = new ArrayList<>(orders.size());
            for (String order : orders) {
                tasks.add(executor.submit(() -> {
                    simulateExternalIo();
                    addProcessedOrder.accept("PROCESSED: " + order);
                }));
            }

            waitFor(tasks);
        }
    }

    private ExecutorService createExecutor() {
        return switch (threadMode) {
            case VIRTUAL -> Executors.newVirtualThreadPerTaskExecutor();
            case PLATFORM -> Executors.newThreadPerTaskExecutor(Thread.ofPlatform().factory());
        };
    }

    private void simulateExternalIo() {
        try {
            Thread.sleep(EXTERNAL_IO_DELAY_MS);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Order processing was interrupted", exception);
        }
    }

    private void waitFor(List<Future<?>> tasks) {
        try {
            for (Future<?> task : tasks) {
                task.get();
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Waiting for order processing was interrupted", exception);
        } catch (ExecutionException exception) {
            throw new IllegalStateException("Order processing failed", exception.getCause());
        }
    }

}
