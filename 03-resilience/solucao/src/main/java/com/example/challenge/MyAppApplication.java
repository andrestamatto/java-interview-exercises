package com.example.challenge;

public class MyAppApplication {
    public static void main(String[] args) {
        System.out.println("--- Executando Solução Resiliência (Retry/Fallback Ativo) ---");
        ResilientGatewaySolution gateway = new ResilientGatewaySolution();
        
        String response = gateway.callWithResilience();
        System.out.println("Resposta final resiliente: " + response);
    }
}
