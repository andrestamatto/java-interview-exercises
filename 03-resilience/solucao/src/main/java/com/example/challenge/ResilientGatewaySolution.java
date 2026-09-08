package com.example.challenge;

public class ResilientGatewaySolution {
    private int callCount = 0;

    public String callExternalServiceRaw() {
        callCount++;
        if (callCount <= 2) {
            throw new RuntimeException("Erro temporário na Nuvem");
        }
        return "Dados da Nuvem recuperados com sucesso!";
    }

    // SOLUÇÃO: Implementação conceitual do padrão Retry / Circuit Breaker.
    // Em um app Spring Boot real, você utilizaria a anotação @CircuitBreaker(name="external", fallbackMethod="fallback") do Resilience4j.
    public String callWithResilience() {
        int maxAttempts = 3;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return callExternalServiceRaw();
            } catch (RuntimeException e) {
                System.out.println("Tentativa " + attempt + " falhou. Aplicando lógica de Retry...");
                if (attempt == maxAttempts) {
                    return fallbackValue();
                }
            }
        }
        return fallbackValue();
    }

    private String fallbackValue() {
        return "Dados Estáticos em Cache (Fallback Ativado de forma segura)";
    }
}
