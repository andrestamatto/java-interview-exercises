package com.example.challenge;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class OrderProcessorSolution {
    // SOLUÇÃO: Usando estrutura thread-safe para evitar Race Condition.
    private final List<String> processedOrders = new CopyOnWriteArrayList<>();

    public List<String> processLargePayload(List<String> orders) throws InterruptedException {
        // SOLUÇÃO: Usando Virtual Threads (Java 21) ideal para tarefas bloqueantes de I/O em nuvem.
        // Alternativamente, para versões anteriores, usaria-se um ThreadPoolTaskExecutor fixo.
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (String order : orders) {
                executor.submit(() -> {
                    try {
                        Thread.sleep(100); // I/O Bloqueante
                        processedOrders.add("PROCESSED: " + order);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
            }
            executor.shutdown();
            executor.awaitTermination(10, TimeUnit.SECONDS);
        }
        return processedOrders;
    }
}
