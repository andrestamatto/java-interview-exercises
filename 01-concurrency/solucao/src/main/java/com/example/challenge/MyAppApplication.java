package com.example.challenge;

import java.util.List;
import java.util.stream.IntStream;

public class MyAppApplication {
    public static void main(String[] args) throws Exception {
        System.out.println("--- Executando Solução Concorrência (Correto) ---");
        OrderProcessorSolution processor = new OrderProcessorSolution();
        List<String> orders = IntStream.range(0, 100).mapToObj(i -> "Order-" + i).toList();
        
        long start = System.currentTimeMillis();
        List<String> result = processor.processLargePayload(orders);
        long end = System.currentTimeMillis();
        
        System.out.println("Tempo decorrido com Virtual Threads: " + (end - start) + "ms");
        System.out.println("Total esperado: 100, Total processado de forma segura: " + result.size());
    }
}
