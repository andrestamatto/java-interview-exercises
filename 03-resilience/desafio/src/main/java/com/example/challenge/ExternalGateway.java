package com.example.challenge;

public class ExternalGateway {
    private int callCount = 0;

    // PROBLEMA: Se a API externa estiver fora ou lenta, este método vai travar a thread
    // indefinidamente e lançar exceções sem um fallback estruturado.
    public String callExternalService() {
        callCount++;
        if (callCount <= 3) {
            throw new RuntimeException("API Externa Indisponível (503 Service Unavailable)");
        }
        return "Dados da Nuvem com sucesso!";
    }
}
