package com.example.challenge;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ProductServiceSolution {
    
    // SOLUÇÃO: Utilizar Records do Java moderno para imutabilidade e DTOs limpos
    public record ProductRecord(int id, String name, String category) {}

    private final Map<Integer, ProductRecord> databaseMap = List.of(
        new ProductRecord(1, "Laptop", "Eletronicos"),
        new ProductRecord(2, "Teclado", "Eletronicos"),
        new ProductRecord(3, "Cadeira", "Moveis")
    ).stream().collect(Collectors.toMap(ProductRecord::id, p -> p));

    // SOLUÇÃO: Otimização para busca em O(1) usando Map e Streams API corporativa
    // Em cenários JPA reais, isso equivaleria a usar um JOIN FETCH ao invés de buscar um a um.
    public List<String> getProductDetailsGood(List<Integer> ids) {
        return ids.stream()
            .map(databaseMap::get)
            .filter(java.util.Objects::nonNull)
            .map(p -> p.name().toUpperCase())
            .toList();
    }
}
