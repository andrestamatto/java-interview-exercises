# Guia de Preparação Técnica - Java / Spring Boot para Virtasant

Este repositório contém 3 pastas com cenários críticos que costumam ser cobrados em testes de coding e arquitetura focados em eficiência de nuvem:

1. **01-concurrency**: Mostra como sair de concorrência perigosa e threads pesadas para um padrão thread-safe performático (Virtual Threads/Java Moderno).
2. **02-performance**: Aborda a refatoração de código com loops pesados e DTOs mutáveis para a utilização eficiente de Streams, Mapas (Busca O(1)) e Java Records.
3. **03-resilience**: Demonstra o conceito de tratamento de falhas e padrões de estabilidade (Retry/Fallback) vitais para sistemas distribuídos de nuvem.

### Como rodar:
Cada subpasta possui duas vertentes independentes:
* `/desafio`: O código problemático que você deve tentar refatorar.
* `/solucao`: A arquitetura corrigida de acordo com os padrões seniores exigidos.

Basta executar o método `main` contido em qualquer arquivo `MyAppApplication.java` para ver o comportamento na prática!
