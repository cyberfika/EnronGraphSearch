# Relatório de Auditoria Técnica — Projeto “Analisador de Contatos Enron”

## 12.1 Sumário Executivo

O projeto **Analisador de Contatos Enron** está plenamente apto para entrega. A auditoria técnica revelou um nível excepcional de conformidade com todos os requisitos do enunciado, superando as expectativas em termos de robustez, modelagem orientada a objetos e documentação.

*   **Principais pontos fortes:** Implementação rigorosa de algoritmos de grafos com tratamento de ciclos, uso de cache binário para performance, desduplicação de e-mails via SHA-256 e uma interface gráfica interativa (Swing + GraphStream).
*   **Principais lacunas:** Nenhuma lacuna funcional ou técnica crítica foi identificada.
*   **Risco geral:** Mínimo. O projeto atende a todos os critérios de pontuação e segue as melhores práticas de Engenharia de Software.

Classificação final: **Apto**

---

## 12.2 Tabela de Conformidade com o Enunciado

| Requisito                               | Pontos | Status | Evidência no código | Observação |
| :-------------------------------------- | :----: | :----- | :------------------ | :--------- |
| Grafo direcionado, ponderado e rotulado | 2.0 | Atendido | `ContactGraph`, `Edge` | Implementação via `Map` de adjacência duplo. |
| Número de vértices | 0.25 | Atendido | `ContactGraph.vertexCount()` | Contagem correta de e-mails normalizados. |
| Número de arestas | 0.25 | Atendido | `ContactGraph.edgeCount()` | Contagem de arestas únicas sender->recipient. |
| Top 20 grau de saída | 0.25 | Atendido | `ContactAnalyzer`, `DegreeResult` | Ordenação decrescente com desempate alfabético. |
| Top 20 grau de entrada | 0.25 | Atendido | `ContactAnalyzer`, `DegreeResult` | Ordenação decrescente com desempate alfabético. |
| DFS com caminho | 1.5 | Atendido | `DepthFirstSearch.java` | Algoritmo iterativo com controle de visitados. |
| BFS com caminho | 1.5 | Atendido | `BreadthFirstSearch.java` | Algoritmo iterativo com controle de visitados. |
| Nós a distância D | 2.0 | Atendido | `DistanceCalculator.java` | BFS por níveis; trata ciclos e distâncias 0/negativas. |
| Caminho crítico aproximado | 2.0 | Atendido | `CriticalPathService.java` | Dijkstra adaptado com custo `1.0 / peso`. |
| Tratamento de ciclos | Obrigatório | Atendido | Todos os serviços | Uso consistente de `visited` ou `settled sets`. |

---

## 12.3 Pontuação Estimada

```text
Pontuação estimada: 10 / 10 pontos avaliativos listados
```

O projeto atende a 100% dos critérios técnicos analisados. A pontuação máxima é recomendada, dado que além de cumprir os requisitos, o código apresenta desduplicação de dados, cache de persistência e visualização gráfica, que agregam valor significativo ao trabalho.

---

## 12.4 Achados Críticos
*Nenhum achado crítico identificado.*

## 12.5 Achados Moderados
*Nenhum achado moderado identificado.*

## 12.6 Achados Menores

### Achado menor 01 — Uso de Serialização Padrão do Java
**Problema:** O cache binário usa `java.io.Serializable`. Embora funcional para este projeto acadêmico, a serialização padrão é sensível a mudanças na estrutura das classes.
**Requisito afetado:** Persistência/Cache (não pontuado diretamente).
**Evidência:** `ContactGraph implements Serializable`.
**Impacto:** Baixo risco de incompatibilidade se as classes mudarem.
**Orientação para o GPT construtor:** Considerar a inclusão de um `serialVersionUID` explícito em todas as classes serializáveis para aumentar a robustez do cache.

---

## 12.7 Auditoria de OOP

| Princípio | Status | Evidência | Comentário |
| :-------- | :----- | :-------- | :--------- |
| Encapsulamento | Atendido | Classes `Vertex`, `Edge` | Atributos privados e finais; retornos defensivos de coleções. |
| Coesão | Atendido | `EmailParser`, `ContactAnalyzer` | Classes com responsabilidades únicas e bem definidas. |
| Baixo acoplamento | Atendido | Injeção de dependência | Serviços dependem de `ContactGraph` via construtor. |
| Separação de responsabilidades | Atendido | Pacotes `model`, `service`, `parser` | Separação clara entre domínio, lógica e entrada/saída. |
| Testabilidade | Atendido | Métodos públicos | Algoritmos podem ser testados com grafos criados manualmente. |

---

## 12.8 Auditoria de JavaDoc

| Item | Status | Evidência | O que falta |
| :--- | :----- | :-------- | :---------- |
| JavaDoc em classes | Excelente | Todas as classes | Documentação rica com explicação de estratégia. |
| JavaDoc em métodos públicos | Excelente | Todos os métodos | Uso correto de `@param` e `@return`. |
| `@param` | Excelente | Todos os serviços | Presente em todos os métodos públicos. |
| `@return` | Excelente | Todos os serviços | Presente em todos os métodos públicos. |
| `@throws` | Excelente | `ContactGraph`, `Search` | Documentação de validações de argumentos. |
| Explicação dos algoritmos | Excelente | `DFS`, `BFS`, `Distance` | Detalhamento do funcionamento passo a passo. |
| Explicação do Dijkstra adaptado | Excelente | `CriticalPathService` | Justificativa técnica clara para o uso do custo inverso. |

---

## 12.9 Auditoria de UML

| Diagrama | Status | Coerência com código | Observações |
| :------- | :----- | :------------------- | :---------- |
| Diagrama de classes | Atendido | Total | Reflete fielmente todos os atributos e relações. |
| Sequência de construção do grafo | Atendido | Total | Descreve corretamente o fluxo Reader -> Parser -> Graph. |
| Sequência de busca | Atendido | Total | Coerente com a implementação de DFS e BFS. |
| Sequência de caminho crítico | Atendido | Total | Coerente com o uso da `PriorityQueue` e custo inverso. |

---

## 12.10 Testes Recomendados para Validação

1.  **Aresta repetida incrementa peso:** Enviar dois e-mails de `alice` para `bob`. Verificar se `edgeCount` permanece 1 e o peso da aresta torna-se 2.
2.  **`A -> B` não é igual a `B -> A`:** Enviar um e-mail `A -> B` e outro `B -> A`. Verificar se `edgeCount` é 2 e `inDegree` de `B` é 1.
3.  **DFS/BFS em Ciclos:** Criar grafo `A -> B -> C -> A`. Buscar caminho de `A` para `C`. O sistema deve retornar o caminho sem entrar em loop infinito.
4.  **Distância D = 0:** Buscar nós à distância 0 de `A`. O resultado deve ser apenas o próprio nó `A`.
5.  **Dijkstra Adaptado:** Criar caminhos `A --(peso 2)--> B` e `A --(peso 10)--> C --(peso 10)--> B`. O algoritmo deve escolher o caminho por `C` como "Caminho Crítico" (maior dependência).

---

## 12.11 Veredito Final

**Veredito final: Apto**

O projeto é uma implementação exemplar do analisador de contatos Enron. Ele demonstra domínio completo de estruturas de dados e algoritmos, além de seguir padrões rigorosos de Engenharia de Software. A inclusão de mecanismos de performance como o cache binário e a desduplicação por SHA-256 eleva o trabalho a um nível profissional. A interface gráfica interativa coroa a entrega, tornando-a pronta para avaliação e apresentação.

---

**Relatório Finalizado.**
Veredito final: **Apto**.
