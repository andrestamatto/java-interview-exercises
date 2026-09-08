package com.example.challenge;

import java.util.List;

public class MyAppApplication {
    public static void main(String[] args) {
        System.out.println("--- Executando Solução Performance (Otimizado) ---");
        ProductServiceSolution service = new ProductServiceSolution();
        List<String> details = service.getProductDetailsGood(List.of(1, 2, 3));
        System.out.println("Resultado com O(1) e Records: " + details);
    }
}
