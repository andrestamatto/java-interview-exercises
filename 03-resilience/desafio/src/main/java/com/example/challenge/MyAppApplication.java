package com.example.challenge;

public class MyAppApplication {
    public static void main(String[] args) {
        System.out.println("--- Executando Desafio Resiliência (Sem Proteção) ---");
        ExternalGateway gateway = new ExternalGateway();
        
        try {
            System.out.println(gateway.callExternalService());
        } catch (Exception e) {
            System.err.println("A aplicação quebrou porque o serviço parceiro falhou: " + e.getMessage());
        }
    }
}
