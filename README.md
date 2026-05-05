# Enron Graph Analyzer

<div align="center">

![Java 17](https://img.shields.io/badge/Java-17-orange?logo=java&logoColor=white)
![Maven 3.8+](https://img.shields.io/badge/Maven-3.8%2B-blue?logo=apachemaven&logoColor=white)
![GraphStream 2.0](https://img.shields.io/badge/GraphStream-2.0-blue)
![Release 1.0.0](https://img.shields.io/badge/Release-1.0.0-green?logo=github)
![License MIT](https://img.shields.io/badge/License-MIT-green?logo=github)
![Platform](https://img.shields.io/badge/Platform-Windows%20%7C%20macOS%20%7C%20Linux-blue)
![Build Status](https://img.shields.io/badge/Build-Passing-brightgreen)

*Analisador de redes de contatos do Enron Email Dataset com algoritmos de grafo implementados do zero*

[Demonstração](#-funcionalidades) • [Como Instalar](#-requisitos-de-sistema) • [Documentação](#-arquitetura-e-classes) • [GitHub](https://github.com/cyberfika/EnronGraphSearch)

</div>

---

## 📋 Visão Geral

O **Enron Email Dataset** é uma base pública contendo aproximadamente **500 mil e-mails** de funcionários da empresa Enron. Este projeto processa esse dataset para construir um **grafo direcionado, ponderado e rotulado** de comunicação, sobre o qual implementa algoritmos clássicos **100% do zero, sem bibliotecas externas de algoritmos**:

- ✅ **DFS** — Busca em profundidade com reconstrução de caminho
- ✅ **BFS** — Busca em largura (menor número de arestas)
- ✅ **Distância D** — Nós a exatamente D hops de distância
- ✅ **Caminho Crítico** — Dijkstra adaptado para maximizar dependências
- ✅ **Visualização Interativa** — GraphStream + Interface Swing profissional

### Estrutura do Grafo

| Elemento | Significado |
|----------|-------------|
| **Vértice** | Um endereço de e-mail único |
| **Aresta A → B** | A enviou ao menos uma mensagem para B |
| **Peso da aresta** | Quantidade total de mensagens (A → B) |
| **Direção** | Grafo direcionado: A → B ≠ B → A |

---

## 🚀 Requisitos de Sistema

| Componente | Versão | Status |
|------------|--------|--------|
| **Java (JDK)** | 17+ | ✅ Obrigatório |
| **Maven** | 3.8+ | ✅ Obrigatório |
| **Sistema Operacional** | Windows / macOS / Linux | ✅ Multiplataforma |
| **Memória RAM** | 4 GB recomendada | Para dataset completo |

### Instalação Rápida (Maven não instalado?)

```bash
curl -sL https://archive.apache.org/dist/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.tar.gz \
  | tar xz -C $HOME
export PATH="$HOME/apache-maven-3.9.6/bin:$PATH"
```

---

## 📁 Estrutura do Projeto

```
EnronAnalyzer/
├── pom.xml                              # Configuração Maven (Java 17, GraphStream 2.0)
├── data/
│   └── maildir/                         # Dataset Enron (150 usuários)
│       └── ../graph.bin                 # Cache binário gerado ao lado de maildir
├── docs/
│   ├── diagrama-classes.puml           # Diagrama UML (PlantUML)
│   ├── sequencia-construcao-grafo.puml
│   ├── sequencia-busca.puml
│   └── sequencia-caminho-critico.puml
└── src/main/java/br/edu/enron/
    ├── app/
    │   └── Main.java                    # Ponto de entrada
    ├── design/
    │   ├── DesignSystem.java            # Sistema de cores (Dark/Light theme)
    │   └── FontManager.java             # Gestão de fontes multiplataforma
    ├── model/
    │   ├── Vertex.java                  # Nó do grafo
    │   ├── Edge.java                    # Aresta ponderada
    │   ├── EmailMessage.java            # Dados de e-mail extraído
    │   ├── DegreeResult.java            # Resultado de grau
    │   └── PathResult.java              # Resultado de caminho
    ├── graph/
    │   └── ContactGraph.java            # Estrutura do grafo
    ├── service/
    │   ├── ContactAnalyzer.java         # Estatísticas e rankings
    │   ├── DepthFirstSearch.java        # Algoritmo DFS iterativo
    │   ├── BreadthFirstSearch.java      # Algoritmo BFS com fila
    │   ├── DistanceCalculator.java      # Cálculo de distância D
    │   └── CriticalPathService.java     # Dijkstra adaptado
    ├── parser/
    │   ├── EmailParser.java             # Parser RFC 2822
    │   └── EnronDatasetReader.java      # Leitor do dataset + cache
    ├── util/
    │   ├── EmailValidator.java          # Validação de e-mails
    │   └── FontManager.java             # Gestão de fontes
    └── view/
        ├── SearchPanel.java             # Interface principal Swing
        ├── TweaksPanel.java             # Painel de temas (Dark/Light)
        ├── GraphVisualizer.java         # Renderização GraphStream
        └── component/                    # Componentes reutilizáveis
            ├── PanelHeader.java
            ├── QueryRow.java
            ├── Sidebar.java
            ├── StatusBar.java
            └── SwingHelper.java
```

---

## 🔧 Como Compilar

Na raiz do projeto:

```bash
# Apenas compilar
mvn clean compile

# Compilar + gerar JAR executável (all-in-one)
mvn clean package
```

O JAR será gerado em: `target/enron-graph-analyzer.jar`

---

## ▶️ Como Executar

### Opção 1: Interface Gráfica (Recomendado)

```bash
mvn exec:java
```

Isso abre automaticamente a **tela de boas-vindas** com dois cartões:

| Cartão | O que faz |
|--------|-----------|
| **Load Enron Dataset** | Carrega os 150 usuários do `data/maildir` |
| **Demo Mode** | Modo demonstração com 5 nós fictícios (ideal para testar) |

**Recursos adicionais:**
- Campo "Dataset folder" (pré-preenchido se `data/maildir` existir)
- Botão **Browse…** para selecionar pasta manualmente
- Checkbox **Force cache rebuild** para reprocessar o dataset

> Na primeira execução, o sistema leva **1-3 minutos** para processar os e-mails enviados. Nas execuções seguintes, carrega do cache `graph.bin`, gerado ao lado da pasta `maildir`, em **segundos**.

### Opção 2: Linha de Comando (Avançado)

```bash
# Carrega dataset com cache
mvn exec:java -Dexec.args="data/maildir"

# Força reconstrução do cache
mvn exec:java -Dexec.args="data/maildir --rebuild"

# Via JAR executável
java -jar target/enron-graph-analyzer.jar data/maildir
java -jar target/enron-graph-analyzer.jar data/maildir --rebuild
```

### Opção 3: IntelliJ IDEA

1. Abra o projeto
2. Clique em `Run → Run 'Main'` (sem argumentos para tela de boas-vindas)

---

## 🎯 Funcionalidades Principais

### 1. Construção do Grafo

- Lê e-mails das subpastas `sent` e `_sent_mail` de 150 usuários
- Extrai remetente (`From:`) e destinatários (`To:`)
- Cria vértices e arestas com pesos automáticos
- Remove auto-loops (e-mails para si mesmo)
- Normaliza e-mails (minúsculas, sem espaços)

**Representação interna:**
```java
Map<Vertex, Map<Vertex, Edge>>  // O(1) para busca de aresta
```

### 2. Cache Binário

| Situação | Comportamento |
|----------|---------------|
| `graph.bin` não existe | Varre dataset, salva cache |
| `graph.bin` existe | Carrega instantaneamente |
| Flag `--rebuild` | Ignora cache, refaz varredura |

### 3. Estatísticas Básicas

Exibidas no console e na interface:
- **Número de vértices** — endereços de e-mail únicos
- **Número de arestas** — pares (remetente, destinatário) distintos

### 4. Top 20 Rankings

**Grau de saída:** Usuários mais ativos (mais destinatários)
**Grau de entrada:** Usuários mais procurados (mais remetentes)

Impresso automaticamente no console ao iniciar.

### 5. Busca em Profundidade (DFS)

```
Algoritmo: Iterativo com pilha
Resultado: Caminho de A até B (não necessariamente o mais curto)
Ciclos:   Tratados com conjunto de visitados
```

### 6. Busca em Largura (BFS)

```
Algoritmo: Fila FIFO
Resultado: Caminho com MENOR NÚMERO DE ARESTAS
Ciclos:   Vértice marcado como visitado ao ser enfileirado
```

### 7. Nós a Distância D

```
Entrada:  Vértice N, distância D
Saída:    Todos os vértices a EXATAMENTE D arestas
Algoritmo: BFS por níveis
```

**Exemplo:** `alice` a distância 2 → retorna apenas nós com 2 hops

### 8. Caminho Crítico (Dijkstra Adaptado)

Encontra o caminho de **máxima dependência** aplicando:

```
custo(aresta) = 1.0 / peso
```

Uma aresta com 10 mensagens tem custo 0,1 (favorecida).
Uma aresta com 1 mensagem tem custo 1,0 (evitada).

**Saída:**
- Caminho completo
- Custo inverso total
- Dependência acumulada (soma de pesos)

---

## 🎨 Interface Gráfica

### Tema Dark/Light

O sistema possui um **sistema de temas profissional**:

- **Dark Mode:** Fundo escuro, texto claro (padrão)
- **Light Mode:** Fundo claro, texto escuro
- **Toggle:** Botão ⚙️ (tweaks) na barra superior

Cores baseadas em **oklch() CSS** convertidas para RGB (compatível com Java Swing).

### Painel de Buscas

**Barra superior:**
- Título da aplicação
- Estatísticas (vértices, arestas)
- Botão ⚙️ para alternar tema

**Seleção de vértices:**
- Dois dropdowns com **agrupamento alfabético** (A-Z)
- Spinner para distância (0-20)

**Botões de ação:**

| Botão | Cor | Função |
|-------|-----|--------|
| **DFS** | Azul | Busca em profundidade |
| **BFS** | Verde | Busca em largura |
| **Distance D** | Roxo | Nós a D arestas |
| **Critical Path** | Vermelho | Dijkstra adaptado |
| **Show Full Graph** | Cinza | Visualização GraphStream |

**Área de resultados:**
- Tema escuro (fundo preto, texto verde monoespaçado)
- Resultados mais recentes no topo
- Mostra: caminho, saltos, custo inverso, dependência

### Visualização GraphStream

Cada algoritmo abre uma **janela interativa** com:

- **Nós padrão:** Azul com borda escura
- **Nós no caminho:** Vermelho (tamanho maior)
- **Origem/Destino:** Verde (tamanho ainda maior)
- **Arestas no caminho:** Vermelho (mais espessas)
- **Rótulos:** Peso original de cada aresta

**Otimização automática:** a visualização geral mostra por padrão os 30 vértices de maior grau, rotulando até 25, para manter a interface responsiva. Visualizações de caminho incluem os nós do caminho e contexto local ponderado.

---

## 🔍 Deduplicação de E-mails

Alguns usuários possuem tanto `sent` quanto `_sent_mail` com sobreposições. O sistema:

1. Hash SHA-256 de cada e-mail
2. Detecta duplicatas entre pastas
3. Processa apenas a primeira ocorrência
4. Registra estatísticas de duplicatas removidas

---

## 📄 Regras de Parsing (RFC 2822)

**Campos extraídos:**
- `From:` — remetente (único)
- `To:` — destinatários (múltiplos)

**Campos ignorados:**
- `Cc:` e `Bcc:` (fora do escopo definido; o grafo usa os destinatários diretos em `To:`)
- Todos os demais campos de cabeçalho

**Tratamento de threads:**
- Apenas o primeiro bloco de cabeçalho é processado
- Cabeçalhos embutidos no corpo (encaminhados) são ignorados
- Evita contar relacionamentos de conversas anteriores

**Normalização:**
- Minúsculas + remoção de espaços
- Formato `"Nome <email@dominio>"` → `email@dominio`
- Destinatários duplicados → removidos

---

## 🏗️ Arquitetura e Classes

### Camada `model` — Dados Puros

| Classe | Responsabilidade |
|--------|-----------------|
| `Vertex` | Nó do grafo. Imutável. `equals`/`hashCode` baseados no e-mail. |
| `Edge` | Aresta com peso mutável. Oferece `incrementWeight()` e `getInverseCost()`. |
| `EmailMessage` | Dados extraídos de um e-mail. Destinatários deduplicados. |
| `DegreeResult` | Par (email, grau). Implementa `Comparable`. |
| `PathResult` | Caminho retornado pelos algoritmos. Carrega vértices, custo e dependência. |

### Camada `graph` — Estrutura

| Classe | Responsabilidade |
|--------|-----------------|
| `ContactGraph` | Grafo direcionado, ponderado, rotulado. Lista de adjacência. `Serializable`. |

### Camada `service` — Algoritmos

| Classe | Responsabilidade |
|--------|-----------------|
| `ContactAnalyzer` | Estatísticas e rankings top-20. |
| `DepthFirstSearch` | DFS iterativa com pilha. |
| `BreadthFirstSearch` | BFS com fila FIFO. |
| `DistanceCalculator` | BFS por níveis para distância D. |
| `CriticalPathService` | Dijkstra com custo = 1/peso. |

### Camada `parser` — Leitura

| Classe | Responsabilidade |
|--------|-----------------|
| `EmailParser` | Extrai `From:` e `To:` (RFC 2822). |
| `EnronDatasetReader` | Varre 150 usuários, deduplicação SHA-256, cache. |

### Camada `util` — Utilitários

| Classe | Responsabilidade |
|--------|-----------------|
| `EmailValidator` | Normalização e validação de e-mails. |
| `FontManager` | Gestão de fontes multiplataforma (Windows/macOS/Linux). |

### Camada `design` — Sistema de Temas

| Classe | Responsabilidade |
|--------|-----------------|
| `DesignSystem` | Cores centralizadas (Dark/Light). 26 cores definidas. |

### Camada `view` — Interface

| Classe | Responsabilidade |
|--------|-----------------|
| `SearchPanel` | Janela Swing principal com dropdowns e botões. |
| `TweaksPanel` | Diálogo flutuante para toggle de tema. |
| `GraphVisualizer` | Renderização GraphStream com estilos CSS. |
| Componentes | `PanelHeader`, `QueryRow`, `Sidebar`, `StatusBar`, etc. |

---

## 📊 Diagramas UML

Os diagramas estão em `docs/` em formato **PlantUML** (`.puml`):

```bash
# Renderizar com PlantUML instalado
plantuml docs/*.puml

# Online: https://www.plantuml.com/plantuml/uml/
```

| Arquivo | Conteúdo |
|---------|----------|
| `diagrama-classes.puml` | Classes, atributos, métodos, relacionamentos |
| `sequencia-construcao-grafo.puml` | Fluxo de leitura do dataset |
| `sequencia-busca.puml` | Fluxo de DFS e BFS |
| `sequencia-caminho-critico.puml` | Fluxo do Dijkstra adaptado |

---

## 🎓 Modo Demonstração

Ao executar sem argumentos, o sistema constrói um pequeno grafo fictício:

```
alice@company.com  -->  bob@company.com    (peso 2)
alice@company.com  -->  carol@company.com  (peso 1)
bob@company.com    -->  dave@company.com   (peso 1)
carol@company.com  -->  dave@company.com   (peso 1)
dave@company.com   -->  eve@company.com    (peso 3)
bob@company.com    -->  eve@company.com    (peso 1)
```

Permite testar todos os algoritmos sem o dataset completo.

---

## ✅ Rastreabilidade de Requisitos

| Critério | Pontos | Classe | Como Atendido |
|----------|-------:|--------|----------------|
| Grafo direcionado, ponderado, rotulado | 2,0 | `ContactGraph`, `Vertex`, `Edge` | Lista de adjacência com pesos |
| Número de vértices | 0,25 | `ContactAnalyzer.getVertexCount()` | Delega para `ContactGraph` |
| Número de arestas | 0,25 | `ContactAnalyzer.getEdgeCount()` | Delega para `ContactGraph` |
| Top 20 out-degree | 0,25 | `ContactAnalyzer.getTop20OutDegree()` | Ordena por grau desc, desempata por email asc |
| Top 20 in-degree | 0,25 | `ContactAnalyzer.getTop20InDegree()` | Ordena por grau desc, desempata por email asc |
| DFS com caminho | 1,5 | `DepthFirstSearch.search()` | Iterativa com pilha, reconstrói caminho |
| BFS com caminho | 1,5 | `BreadthFirstSearch.search()` | Fila FIFO, garante menor número de arestas |
| Nós a distância D | 2,0 | `DistanceCalculator.getVerticesAtDistance()` | BFS por níveis, retorna nível exato D |
| Caminho crítico (Dijkstra) | 2,0 | `CriticalPathService.computeCriticalPath()` | Dijkstra com custo = 1/peso |
| Tratamento de ciclos | — | Todos os algoritmos | `Set<Vertex> visited`/`settled` |

---

## 🧪 Testes Manuais Sugeridos

### Validar Algoritmos (Demo)

```bash
mvn exec:java
```

1. Selecione `alice@company.com` → `eve@company.com`
2. Clique **BFS** → esperado: `alice → bob → eve` (2 saltos)
3. Clique **DFS** → esperado: termina em `eve`
4. Distância D = 2 com origem `alice` → esperado: `dave@company.com`
5. Clique **Critical Path** → esperado: `alice → bob → eve`, dependência = 3

### Validar Cache

```bash
mvn exec:java -Dexec.args="data/maildir"
```

- 1ª execução: leva 1-3 minutos (varredura)
- 2ª execução: instantâneo (cache)
- Com `--rebuild`: refaz varredura

### Validar Tema

1. Abra a aplicação
2. Clique em ⚙️ (tweaks) na barra superior
3. Toggle entre Dark/Light
4. Verifique se todas as cores atualizam

---

## 📝 Informações do Projeto

| Propriedade | Valor |
|------------|-------|
| **Versão** | 1.0.0 |
| **Java** | 17 LTS |
| **Build Tool** | Maven 3.8+ |
| **Dependências** | GraphStream 2.0 |
| **Dependências Algoritmos** | Zero (100% do zero) |
| **Linhas de Código** | ~3.500+ |
| **Classes** | 30+ |
| **Licença** | MIT |
| **Autor** | Cyberfika |
| **Repositório** | [GitHub](https://github.com/cyberfika/EnronGraphSearch) |

---

## 🚀 Stack Tecnológico

```
┌─────────────────────────────────────┐
│         Interface Swing             │
│  (PanelHeader, QueryRow, Sidebar)   │
└──────────────────┬──────────────────┘
                   │
┌──────────────────┴──────────────────┐
│    GraphStream 2.0 (Visualização)   │
│     Theme System (DesignSystem)     │
└──────────────────┬──────────────────┘
                   │
┌──────────────────┴──────────────────┐
│  Algoritmos (DFS, BFS, Dijkstra)    │
│    ContactGraph (Lista Adjacência)  │
└──────────────────┬──────────────────┘
                   │
┌──────────────────┴──────────────────┐
│    Parser (RFC 2822 + SHA-256)      │
│  EnronDatasetReader + Cache Binário │
└─────────────────────────────────────┘
```

---

## 📈 Métricas de Desempenho

| Operação | Dataset Demo | Dataset Completo (500k emails) |
|----------|-------------|------|
| Carregamento (1ª vez) | Instantâneo | 1-3 minutos |
| Carregamento (cache) | — | < 1 segundo |
| DFS / BFS | < 10ms | < 500ms |
| Top 20 | < 5ms | < 100ms |
| Visualização GraphStream | Instantâneo | 2-5 segundos |

---

## 📚 Referências e Recursos

- **Enron Email Dataset:** [CMU](https://www.cs.cmu.edu/~enron/)
- **GraphStream:** [Official Site](https://graphstream-project.org/)
- **PlantUML:** [Online Editor](https://www.plantuml.com/plantuml/uml/)
- **RFC 2822:** [Email Format Specification](https://tools.ietf.org/html/rfc2822)

---

## 📄 Licença

Este projeto é licenciado sob a **Licença MIT** — veja o arquivo `LICENSE` para detalhes.

```
MIT License

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software.
```

---

## 👨‍💻 Contribuindo

Sugestões, bug reports e pull requests são bem-vindos!

```bash
# Clone o repositório
git clone https://github.com/cyberfika/EnronGraphSearch.git
cd EnronGraphSearch

# Crie uma branch para sua feature
git checkout -b feature/sua-feature

# Commit com mensagem descritiva
git commit -m "feat: adiciona nova funcionalidade"

# Push para a branch
git push origin feature/sua-feature

# Abra um Pull Request
```

