package com.example.challenge;

import java.util.List;

public class MyAppApplication {
    public static void main(String[] args) {
        System.out.println("--- Executando Desafio Performance (Loops Ineficientes) ---");
        ProductService service = new ProductService();
        List<String> details = service.getProductDetailsBad(List.of(1, 2, 3));
        System.out.println("Resultado: " + details);
    }
}
