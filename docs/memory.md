# Enron Graph Analyzer — Session Memory

> Salvo automaticamente para reabertura de sessão. Atualizado a cada sessão com Claude Code.

---

## Resumo do Projeto

Aplicação Java 17 + Maven que lê o dataset Enron Email (150 mailboxes), constrói um grafo dirigido ponderado rotulado e implementa DFS, BFS, distância D e Dijkstra adaptado (caminho crítico). Interface gráfica Swing com design system forense/escuro baseado em `design_template/`.

---

## Arquitetura

**Package root:** `br.edu.enron`

| Arquivo | Função |
|---|---|
| `app/Main.java` | Entry point, tela de boas-vindas, demo mode |
| `model/Vertex.java` | Imutável, email lowercase/trimmed |
| `model/Edge.java` | origin/destination/weight; `getInverseCost()=1/w` |
| `model/EmailMessage.java` | sender + recipients (Set<String>) |
| `model/PathResult.java` | vertices/totalCost/accumulatedDependency |
| `model/DegreeResult.java` | email + degree; Comparable (desc. degree) |
| `graph/ContactGraph.java` | Adjacency map; `ownerEmails` TreeSet; serialVersionUID=2L |
| `service/ContactAnalyzer.java` | vertexCount/edgeCount/top20Out/top20In |
| `service/DepthFirstSearch.java` | Iterativo com ArrayDeque stack |
| `service/BreadthFirstSearch.java` | FIFO ArrayDeque queue |
| `service/DistanceCalculator.java` | BFS por nível, retorna vértices em exatamente distância D |
| `service/CriticalPathService.java` | Dijkstra adaptado: custo=1/peso |
| `parser/EmailParser.java` | Só o primeiro header block (regra de thread) |
| `parser/EnronDatasetReader.java` | Só `sent` e `_sent_mail`; SHA-256 dedup; cache `graph.bin` |
| `util/EmailValidator.java` | normalize + isValid |
| `view/SearchPanel.java` | UI principal — design system forense escuro (v2) |
| `view/GraphVisualizer.java` | GraphStream 2.0 (MultiGraph) |

---

## Decisões de Design

- **Linguagem**: Java 17, `--release 17` (não `-source/-target`)
- **Build**: Maven; fat-jar via assembly plugin; exec-maven-plugin
- **Visualização**: GraphStream 2.0 (`gs-core`, `gs-ui-swing`); `MultiGraph` (não `MultiDiGraph`)
- **Código**: TODOS os nomes em inglês
- **Cache**: `data/graph.bin` (ao lado de `maildir`); `serialVersionUID = 2L`
- **From combo**: apenas os ~150 owners do Enron (`graph.getOwnerEmails()`)
- **To combo**: todos os emails do grafo
- **Separadores**: `"── A ──"` nos combos; não-selecionável

---

## Design System (UI)

A UI em `SearchPanel.java` replica o template em `design_template/` (dark forensic):

**Cores:**
- Background: `rgb(13, 21, 32)` — navy escuro
- Sidebar: `rgb(10, 17, 27)`
- Card: `rgb(18, 30, 46)`
- Topbar: `rgb(8, 15, 25)`
- Status bar: `rgb(5, 10, 17)`
- Accent (âmbar): `rgb(212, 170, 37)`
- Texto primário: `rgb(220, 228, 240)`
- Texto secundário: `rgb(90, 112, 138)`
- Borda: `rgb(24, 38, 56)`
- Verde (ATIVO): `rgb(38, 185, 88)`

**Layout:**
```
JFrame (BorderLayout)
├── NORTH: Topbar (44px) — logo + tabs + label direito
├── WEST:  Sidebar (264px) — FONTE | RESUMO | DIRETÓRIO
├── CENTER: CardLayout (4 abas)
│   ├── "overview" — Visão Geral: heroBlock + statCards + leaderboard
│   ├── "dfsbfs"   — DFS/BFS: combos From/To + botões + resultArea
│   ├── "distance" — Distância D: combo From + spinner D + resultArea
│   └── "critpath" — Caminho Crítico: combos From/To + resultArea
└── SOUTH: Statusbar (24px) — status live + relógio
```

**Abas:** botões no topbar com `MatteBorder(0,0,2,0,ACCENT)` na ativa; `CardLayout` no centro.

**Sidebar:** 3 seções; DIRETÓRIO = owners ordenados por out-degree; hover effects.

**Stat cards (Visão Geral):** 4 cards com número grande + unidade + sub; DENSIDADE usa cor âmbar.

**Leaderboard:** 2 colunas; cada linha tem rank + email + número âmbar + barra proporcional (desenhada via `paintComponent`).

---

## Regras de Parsing

- Apenas `sent` e `_sent_mail` por user
- SHA-256 por conteúdo para deduplicação entre as duas pastas
- Apenas o **primeiro bloco de header** de cada email (para antes da primeira linha em branco)
- Extrai apenas `From:` e `To:` (ignora Cc:, Bcc:)
- Trata headers multilinha (folded)
- Extrai endereço bare de `"Nome <email>"`

---

## Erros Corrigidos (histórico)

| Erro | Fix |
|---|---|
| `SequencedCollection` — Java 21 | Substituído por `Set<String>` |
| `MultiDiGraph` — não existe em GraphStream 2.0 | Substituído por `MultiGraph` |
| `-source/-target 17` não bloqueia Java 21+ | Trocado para `--release 17` |
| `toCombo` selecionando separador | Busca backward por último item real |
| `selectedEmail()` retornando `"── A ──"` | Walk forward do índice selecionado |
| Demo mode sem tela gráfica | Welcome screen com `SwingUtilities.invokeAndWait` |
| From combo mostrando todos os emails | `ownerEmails TreeSet` em ContactGraph; `serialVersionUID` 1L→2L |

---

## Pendente / Próximos Passos

- [ ] Filtro de distância no GraphStream (`GraphVisualizer`) para sumir arestas mais distantes e reduzir o "hairball" — o user pediu isso como segunda prioridade após o design
- [ ] Testar a UI nova com o dataset real para verificar performance

---

## Caminho do Projeto

```
/Users/jafte/Documents/GitHub/Grafos/Enron_GraphAnalyzer/
├── data/
│   ├── maildir/          ← dataset Enron (150 pastas de usuário)
│   └── graph.bin         ← cache binário (gerado na 1ª execução)
├── design_template/
│   ├── styles.css        ← design system de referência (HTML/CSS)
│   └── example.png       ← screenshot de referência
├── docs/
│   ├── README.md
│   ├── memory.md         ← este arquivo
│   └── *.puml            ← diagramas UML
└── src/main/java/br/edu/enron/
    ├── app/Main.java
    ├── graph/ContactGraph.java
    ├── model/*.java
    ├── parser/*.java
    ├── service/*.java
    ├── util/EmailValidator.java
    └── view/SearchPanel.java   ← redesenhado na última sessão
```
