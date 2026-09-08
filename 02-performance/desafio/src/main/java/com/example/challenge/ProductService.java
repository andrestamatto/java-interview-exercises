package com.example.challenge;

import java.util.ArrayList;
import java.util.List;

public class ProductService {
    // Simula uma tabela/banco de dados in-memory
    private final List<Product> database = List.of(
        new Product(1, "Laptop", "Eletronicos"),
        new Product(2, "Teclado", "Eletronicos"),
        new Product(3, "Cadeira", "Moveis")
    );

    // PROBLEMA: Complexidade O(N^2) simulando loops aninhados ineficientes (N+1 queries conceituais)
    public List<String> getProductDetailsBad(List<Integer> ids) {
        List<String> names = new ArrayList<>();
        for (Integer id : ids) {
            for (Product p : database) {
                if (p.getId() == id) { // Simula varredura linear ou consulta repetida ao banco
                    names.add(p.getName().toUpperCase());
                }
            }
        }
        return names;
    }
}

class Product {
    private int id;
    private String name;
    private String category;

    public Product(int id, String name, String category) {
        this.id = id;
        this.name = name;
        this.category = category;
    }
    public int getId() { return id; }
    public String getName() { return name; }
}
