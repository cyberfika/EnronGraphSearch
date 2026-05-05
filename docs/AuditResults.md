trabalho# Relatório de Auditoria Técnica — Analisador de Contatos Enron

**Data da auditoria:** 2026-05-05
**Auditor:** Claude (IA — auditoria automatizada)
**Referência:** Enunciado "Enron Contact Analyzer - 2026.txt" e prompt "Auditor.md"

---

## 12.1 Sumário Executivo

O projeto está **apto** para entrega. A arquitetura é bem organizada em 7 pacotes (`app`, `model`, `graph`, `service`, `parser`, `util`, `view`) com separação clara de responsabilidades. Todos os 6 requisitos avaliativos do enunciado estão implementados: grafo direcionado/ponderado/rotulado, contagens de vértices e arestas, top 20 grau de saída e entrada, DFS com caminho, BFS com caminho, nós a distância D, e caminho crítico via Dijkstra adaptado com custo inverso. Todos os algoritmos tratam ciclos explicitamente. A documentação JavaDoc é excelente nas classes core (model, graph, service, parser) e adequada nas classes de view. Os 10 diagramas UML estão presentes e coerentes com o código. O README é completo e inclui instruções de compilação, execução e exemplos de demonstração.

**Principais pontos fortes:** qualidade do JavaDoc, separação de responsabilidades exemplar, tratamento correto de ciclos em todos os algoritmos, deduplicação SHA-256, interface gráfica rica com design system temático.

**Principais lacunas:** nenhuma crítica que comprometa a nota. Observações menores relacionadas a DistancePanel usando JTextArea em vez de visual rico, e o método `forceDarkThemeOnUIManager()` em Main.java é código morto (nunca chamado).

**Classificação final: Apto**

---

## 12.2 Tabela de Conformidade com o Enunciado

| Requisito | Pontos | Status | Evidência no código | Observação |
| --- | ---: | --- | --- | --- |
| Grafo direcionado, ponderado e rotulado | 2.0 | **Atendido** | `ContactGraph.java` — `Map<Vertex, Map<Vertex, Edge>>`. `Edge.incrementWeight()` para peso. `Vertex.email` como rótulo. `addEdge()` ignora self-loops, incrementa peso em arestas existentes. | A→B distinto de B→A pela estrutura de adjacência. Múltiplos destinatários geram múltiplas arestas em `EnronDatasetReader:217`. |
| Número de vértices | 0.25 | **Atendido** | `ContactGraph.vertexCount()` → `vertexIndex.size()`. Delegado via `ContactAnalyzer.getVertexCount()`. | Vértices duplicados impossíveis: `computeIfAbsent` no `vertexIndex` com email normalizado. |
| Número de arestas | 0.25 | **Atendido** | `ContactGraph.edgeCount()` — soma de `outMap.size()` para cada vértice. `ContactAnalyzer.getEdgeCount()`. | Conta arestas distintas (não pesos). Conforme enunciado. |
| Top 20 grau de saída | 0.25 | **Atendido** | `ContactAnalyzer.getTop20OutDegree()` → `topN(20, true)` usando `graph.outDegree(v)` que retorna `outMap.size()`. `DegreeResult.compareTo()` desempata por email. | Grau = destinos distintos, não soma de pesos. Ordenação decrescente com desempate alfabético. |
| Top 20 grau de entrada | 0.25 | **Atendido** | `ContactAnalyzer.getTop20InDegree()` → `topN(20, false)` usando `graph.inDegree(v)` que conta origens distintas via scan. | Mesma lógica do grau de saída, invertida. |
| DFS com caminho | 1.5 | **Atendido** | `DepthFirstSearch.search()` — iterativa com `ArrayDeque` como pilha. `HashSet<Vertex> visited`. `Map<Vertex, Vertex> predecessor` para reconstrução. Retorna `PathResult`. | Trata origem/destino inexistente (retorna empty). Trata origem == destino. Pilha (LIFO) confirma DFS real. |
| BFS com caminho | 1.5 | **Atendido** | `BreadthFirstSearch.search()` — `ArrayDeque` como fila FIFO. Visitados marcados no **enfileiramento** (correto). `predecessor` para reconstrução. | Garante caminho com menor número de arestas. Tratamento de ciclos correto. |
| Nós a distância D | 2.0 | **Atendido** | `DistanceCalculator.getVerticesAtDistance()` — BFS por níveis. Retorna apenas nível exato D. Trata D=0 (retorna só o nó), D<0 (lança exceção), nó inexistente (retorna vazio). | Resultado ordenado alfabeticamente. `visited.add()` retorna boolean para eficiência. |
| Caminho crítico aproximado | 2.0 | **Atendido** | `CriticalPathService.computeCriticalPath()` — Dijkstra com `cost = 1.0/weight` (`Edge.getInverseCost()`). `PriorityQueue` com `Comparator.comparingDouble`. `settled` set para ciclos. Retorna caminho + custo inverso + dependência acumulada. | Adaptação coerente e documentada. Early exit ao settlar destino. |
| Tratamento de ciclos | Obrigatório | **Atendido** | DFS: `HashSet<Vertex> visited` (l.64). BFS: `HashSet<Vertex> visited` marcado no enfileiramento (l.66-71). DistanceCalculator: `visited.add()` (l.77). CriticalPathService: `HashSet<Vertex> settled` (l.79, 97). | Todos os 4 algoritmos previnem laços infinitos. Nenhum vértice é processado mais de uma vez. |

---

## 12.3 Pontuação Estimada

```
Pontuação estimada: 10.0 / 10.0 pontos avaliativos listados
```

| Requisito | Máximo | Estimado | Observação |
| --- | ---: | ---: | --- |
| Grafo dir/pond/rot | 2.0 | 2.0 | Completo |
| Vértices | 0.25 | 0.25 | Completo |
| Arestas | 0.25 | 0.25 | Completo |
| Top 20 out-degree | 0.25 | 0.25 | Completo |
| Top 20 in-degree | 0.25 | 0.25 | Completo |
| DFS | 1.5 | 1.5 | Completo |
| BFS | 1.5 | 1.5 | Completo |
| Distância D | 2.0 | 2.0 | Completo |
| Caminho crítico | 2.0 | 2.0 | Completo |

Nenhuma perda de pontos identificada nos critérios avaliativos. A nota final depende da avaliação presencial do professor (teste de autoria).

---

## 12.4 Achados Críticos

Nenhum achado crítico identificado. Todos os requisitos do enunciado estão implementados corretamente.

---

## 12.5 Achados Moderados

### Achado moderado 1 — Método morto `forceDarkThemeOnUIManager()` em Main.java

**Problema:** O método `forceDarkThemeOnUIManager()` (Main.java:389-421) nunca é chamado. A funcionalidade equivalente já existe em `DesignSystem.applyToUIManager()` que é chamado na linha 44.

**Requisito afetado:** Nenhum requisito avaliativo, mas é código morto que pode gerar confusão em teste de autoria.

**Evidência:** `Main.java:389-421` — método privado sem nenhuma referência.

**Impacto:** Risco menor em teste de autoria: o professor pode questionar por que existe código duplicado/não utilizado.

**Orientação para o GPT construtor:** Remover o método `forceDarkThemeOnUIManager()` de `Main.java`, já que `DesignSystem.applyToUIManager()` cumpre a mesma função e é efetivamente chamado.

---

### Achado moderado 2 — DistancePanel usa JTextArea genérica

**Problema:** `DistancePanel.java` exibe resultados em um `JTextArea` monoespaçado genérico (via `resultArea()` de SwingHelper), enquanto `DfsBfsPanel` e `CriticalPathPanel` já usam visualização rica com chain/rail/microBar.

**Requisito afetado:** Não afeta pontuação, mas cria inconsistência visual entre as abas.

**Evidência:** `DistancePanel.java:60-61` — `resultArea = resultArea(); resultScroll(resultArea)`.

**Impacto:** Menor — a funcionalidade está correta, apenas a apresentação difere das outras abas.

**Orientação para o GPT construtor:** Considerar substituir o JTextArea por uma visualização em lista similar às outras abas, usando BoxLayout com linhas formatadas para cada vértice encontrado.

---

### Achado moderado 3 — README.md desatualizado em relação à estrutura real

**Problema:** O README (docs/README.md) lista apenas `GraphVisualizer.java` e `SearchPanel.java` na camada `view`, mas a estrutura real contém 10+ arquivos em `view/`, `view/panel/`, `view/component/`, e `design/`. A árvore de diretórios (seção 3) também está desatualizada.

**Requisito afetado:** Auditoria de execução (seção 10) — instruções de compilação e execução estão corretas, mas a documentação da arquitetura está incompleta.

**Evidência:** `docs/README.md:124-127` (seção view) vs `src/main/java/br/edu/enron/view/` que contém Sidebar, StatusBar, PanelHeader, QueryRow, SwingHelper, TweaksPanel, OverviewPanel, DfsBfsPanel, DistancePanel, CriticalPathPanel.

**Impacto:** Não afeta a nota diretamente, mas pode causar confusão durante o teste de autoria se o professor consultar o README.

**Orientação para o GPT construtor:** Atualizar o README seções 3 e 12 para incluir todas as classes atuais: `design/DesignSystem`, `view/component/*`, `view/panel/*`, `view/TweaksPanel`, `util/FontManager`.

---

## 12.6 Achados Menores

### Achado menor 1 — Welcome screen usa cores DARK_* hardcoded

**Problema:** O método `showWelcomeScreen()` em `Main.java` referencia diretamente `DesignSystem.DARK_BG`, `DARK_SURFACE`, etc., em vez de usar os métodos dinâmicos `bg()`, `surface()`. A tela de boas-vindas sempre renderiza em tema escuro independentemente da configuração.

**Evidência:** `Main.java:86-221` — todas as referências de cor usam `DARK_*`.

**Impacto:** Nenhum impacto funcional significativo; a tela de boas-vindas aparece antes da escolha de tema.

**Orientação para o GPT construtor:** Substituir `DesignSystem.DARK_*` por chamadas a `DesignSystem.bg()`, `surface()`, etc., para consistência.

---

### Achado menor 2 — DFS pode sobrescrever predecessor em certas topologias

**Problema:** Em `DepthFirstSearch.java:85`, a condição `if (!predecessor.containsKey(neighbour))` impede atualização do predecessor após a primeira descoberta. Isso é correto para DFS (primeiro a chegar define o caminho), mas um vizinho pode ser empilhado múltiplas vezes com predecessores diferentes antes de ser visitado.

**Evidência:** `DepthFirstSearch.java:82-89`.

**Impacto:** Nenhum — o comportamento é correto. O vizinho só é processado uma vez (quando é pop'd e marcado como visitado). O predecessor registrado na primeira descoberta é mantido, que é o comportamento esperado de DFS.

---

### Achado menor 3 — inDegree() em ContactGraph é O(V+E) por chamada

**Problema:** `ContactGraph.inDegree()` faz scan completo da adjacência para cada vértice. Para o top-20, isso é chamado uma vez por vértice (O(V*(V+E)) total).

**Evidência:** `ContactGraph.java:285-292`.

**Impacto:** Funcional — não afeta corretude. Para o dataset Enron (~36k vértices), pode ser lento na primeira execução, mas é calculado apenas uma vez e o cache resolve execuções subsequentes.

---

## 12.7 Auditoria de OOP

| Princípio | Status | Evidência | Comentário |
| --- | --- | --- | --- |
| Encapsulamento | **Atendido** | Todos os campos são `private final` em model. Coleções retornadas via `Collections.unmodifiable*` ou `List.copyOf`. | Excelente — `ContactGraph.getVertices()`, `getEdges()`, `getOutEdges()` todos retornam cópias defensivas. `PathResult.vertices` é `List.copyOf`. |
| Coesão | **Atendido** | Cada classe tem responsabilidade única: `Vertex` = nó, `Edge` = aresta, `ContactGraph` = estrutura, `EmailParser` = parsing, `EnronDatasetReader` = I/O, cada service = um algoritmo. | SRP respeitado rigorosamente. 29 classes, cada uma coesa. |
| Baixo acoplamento | **Atendido** | Services operam sobre `ContactGraph` e retornam `PathResult`/`List<Vertex>`. Sem dependências circulares. | `model` não depende de ninguém. `graph` depende só de `model`. `service` depende de `graph` e `model`. `parser` depende de `model` e `util`. `view` depende de tudo exceto `parser`. |
| Separação de responsabilidades | **Atendido** | Classes separadas para: Vertex, Edge, ContactGraph, EmailMessage, EmailParser, EnronDatasetReader, ContactAnalyzer, DFS, BFS, DistanceCalculator, CriticalPathService, Main. | Conforme checklist do Auditor.md: todas as 12 responsabilidades listadas possuem classe dedicada. |
| Testabilidade | **Atendido** | `ContactGraph` pode ser construído manualmente (sem dataset). Todos os services aceitam `ContactGraph` como parâmetro. `Main.runDemoMode()` demonstra grafos de teste. Métodos retornam dados (`PathResult`, `List<DegreeResult>`), não apenas imprimem. | Demo mode prova testabilidade sem dataset real. |

---

## 12.8 Auditoria de JavaDoc

| Item | Status | Evidência | O que falta |
| --- | --- | --- | --- |
| JavaDoc em classes | **Excelente** | Todas as 29 classes possuem JavaDoc de classe com `<h2>`, `<p>`, `<ul>`, `<li>`. | — |
| JavaDoc em métodos públicos | **Excelente** | Todos os métodos públicos em `model/`, `graph/`, `service/`, `parser/`, `util/` possuem JavaDoc completo. | Classes em `view/panel/` e `view/component/` têm JavaDoc mínimo (apenas classe-level), o que é aceitável para código de UI. |
| `@param` | **Excelente** | Presente em todos os métodos públicos das camadas core. Ex: `DFS.search()`, `CriticalPathService.computeCriticalPath()`, `DistanceCalculator.getVerticesAtDistance()`. | — |
| `@return` | **Excelente** | Presente em todos os métodos com retorno nas camadas core. | — |
| `@throws` | **Adequada** | Presente em métodos que lançam `IllegalArgumentException`. | Nem todos os métodos de view documentam exceções, mas estes são internos ao framework Swing. |
| Explicação dos algoritmos | **Excelente** | DFS: `<h2>Algorithm</h2>` + `<h2>Cycle handling</h2>`. BFS: idem + `<h2>Difference from DFS</h2>`. DistanceCalculator: `<h2>Algorithm — BFS by levels</h2>`. CriticalPathService: `<h2>Motivation</h2>` + `<h2>Dijkstra adaptation</h2>`. | — |
| Explicação do Dijkstra adaptado | **Excelente** | `CriticalPathService.java:10-48` documenta: motivação, fórmula `cost = 1/weight`, por que é aproximação, saída (caminho + custo + dependência), segurança contra ciclos. `Edge.getInverseCost()` documenta a fórmula. `CriticalPathPanel` header mostra "CUSTO = 1 / PESO". | — |

---

## 12.9 Auditoria de UML

| Diagrama | Status | Coerência com código | Observações |
| --- | --- | --- | --- |
| Diagrama de classes | **Atendido** | `diagrama-classes.puml` — todas as 29 classes representadas em 9 pacotes, com atributos, métodos e associações. | Coerente com o código atual. |
| Sequência de construção do grafo | **Atendido** | `sequencia-construcao-grafo.puml` — fluxo Main → welcome screen → EnronDatasetReader → EmailParser → ContactGraph → SearchPanel. | Reflete corretamente o fluxo de inicialização. |
| Sequência de busca | **Atendido** | `sequencia-busca.puml` — fluxo DfsBfsPanel → DepthFirstSearch/BreadthFirstSearch → ContactGraph → PathResult → GraphVisualizer. | Reflete o código de `DfsBfsPanel.run()`. |
| Sequência de caminho crítico | **Atendido** | `sequencia-caminho-critico.puml` — fluxo CriticalPathPanel → CriticalPathService → PriorityQueue → Edge.getInverseCost() → PathResult. | Reflete `CriticalPathService.computeCriticalPath()`. |
| Diagrama de caso de uso | **Atendido** | `diagrama-caso-uso.puml` — 9 casos de uso + 3 includes. | Coerente. |
| Diagrama de componentes | **Atendido** | `diagrama-componentes.puml` — 7 pacotes com dependências. | Coerente. |
| Diagrama de domínio | **Atendido** | `diagrama-dominio.puml` — 6 conceitos de domínio. | Coerente. |
| Diagrama de atividade | **Atendido** | `diagrama-atividade.puml` — fluxo completo. | Coerente. |
| Diagrama de estado | **Atendido** | `diagrama-estado.puml` — estados por aba. | Coerente. |
| Diagrama de comunicação | **Atendido** | `diagrama-comunicacao.puml` — 30 mensagens numeradas. | Coerente. |

---

## 12.10 Testes Recomendados para Validação

### Teste 1 — Aresta repetida incrementa peso

**Objetivo:** Confirmar que múltiplas mensagens entre o mesmo par não criam arestas duplicadas, mas incrementam o peso.
**Entrada sugerida:** Demo mode. Verificar aresta alice→bob.
**Resultado esperado:** Peso = 2 (duas chamadas `addEdge("alice", "bob")` em Main.java:328-329).

### Teste 2 — A→B não é igual a B→A

**Objetivo:** Confirmar que o grafo é direcionado.
**Entrada sugerida:** Demo mode. DFS de eve→alice.
**Resultado esperado:** "Sem caminho" — eve não envia para ninguém no demo.

### Teste 3 — DFS encontra caminho em grafo com ciclo

**Objetivo:** Verificar que DFS não entra em loop infinito.
**Entrada sugerida:** Dataset real. Dois vértices quaisquer com alto grau (ex: os primeiros do top-20 out-degree).
**Resultado esperado:** Caminho retornado ou "sem caminho" em tempo finito. Sem travamento.

### Teste 4 — BFS encontra caminho em grafo com ciclo

**Objetivo:** Verificar BFS com ciclos.
**Entrada sugerida:** Mesma entrada do teste 3.
**Resultado esperado:** Caminho com menor número de arestas. Sem travamento.

### Teste 5 — Distância D = 0

**Objetivo:** Retorna apenas o próprio nó.
**Entrada sugerida:** Demo mode. Origem: alice@company.com. D = 0.
**Resultado esperado:** Lista com apenas "alice@company.com".

### Teste 6 — Distância D = 1

**Objetivo:** Retorna vizinhos diretos.
**Entrada sugerida:** Demo mode. Origem: alice@company.com. D = 1.
**Resultado esperado:** "bob@company.com" e "carol@company.com" (vizinhos de saída de alice).

### Teste 7 — Distância D = 2

**Objetivo:** Retorna vértices a exatamente 2 hops.
**Entrada sugerida:** Demo mode. Origem: alice@company.com. D = 2.
**Resultado esperado:** "dave@company.com" e "eve@company.com" (bob→dave, bob→eve, carol→dave).

### Teste 8 — Nó inexistente

**Objetivo:** Verificar tratamento de entrada inválida.
**Entrada sugerida:** DFS/BFS/Distância com email que não existe no grafo (ex: "naoexiste@x.com").
**Resultado esperado:** "Sem caminho" / lista vazia. Sem exceção.

### Teste 9 — Caminho inexistente

**Objetivo:** Verificar tratamento de par sem conexão.
**Entrada sugerida:** Demo mode. eve→alice (eve não tem arestas de saída).
**Resultado esperado:** PathResult vazio. "Sem caminho" na interface.

### Teste 10 — Dijkstra adaptado favorece arestas de maior peso

**Objetivo:** Confirmar que o caminho crítico prefere arestas pesadas.
**Entrada sugerida:** Demo mode. alice→eve.
**Resultado esperado:** Caminho alice→bob→dave→eve (peso total = 2+1+3 = 6, custo inverso = 0.5+1.0+0.333 = 1.833) OU alice→bob→eve (peso = 2+1 = 3, custo inverso = 0.5+1.0 = 1.5). O Dijkstra minimiza custo inverso, então deve escolher alice→bob→eve (custo 1.5 < 1.833).

---

## 12.11 Veredito Final

```
Veredito final: Apto
```

O projeto atende a todos os requisitos avaliativos do enunciado "Analisador de Contatos Enron — 2026" de forma completa e correta. A arquitetura orientada a objetos é sólida, com separação clara de responsabilidades em 7 pacotes e 29 classes. Os 4 algoritmos de grafo (DFS, BFS, distância D, Dijkstra adaptado) estão implementados corretamente com tratamento explícito de ciclos via conjuntos de visitados. A documentação JavaDoc é exemplar nas classes core, com explicações detalhadas dos algoritmos e suas adaptações. Os 10 diagramas UML são coerentes com o código-fonte. O projeto inclui instruções completas de compilação/execução, modo demonstração funcional, e interface gráfica rica com sistema de design temático. Os únicos pontos a atentar são o README levemente desatualizado e o método morto em Main.java — nenhum dos quais compromete a avaliação.
