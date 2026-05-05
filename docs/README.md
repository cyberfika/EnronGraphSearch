# Enron Graph Analyzer

Analisador de contatos construído sobre o **Enron Email Dataset** público.
A aplicação lê mensagens de e-mail, extrai remetentes e destinatários e constrói uma **rede de contatos representada por um grafo direcionado, ponderado e rotulado**, implementando sobre ele buscas em profundidade (DFS), largura (BFS), consulta de distância e caminho crítico via Dijkstra adaptado.

---

## Sumário

1. [Visão geral](#1-visão-geral)
2. [Requisitos de sistema](#2-requisitos-de-sistema)
3. [Estrutura do projeto](#3-estrutura-do-projeto)
4. [Como compilar](#4-como-compilar)
5. [Como executar](#5-como-executar)
   - [5.1 Interface gráfica (recomendado)](#51-interface-gráfica-recomendado)
   - [5.2 Linha de comando (avançado)](#52-linha-de-comando-avançado)
   - [5.3 Top 20 via CLI — saída completa](#53-top-20-via-cli--saída-completa)
6. [Funcionalidades](#6-funcionalidades)
   - [6.1 Construção do grafo](#61-construção-do-grafo)
   - [6.2 Cache binário](#62-cache-binário)
   - [6.3 Estatísticas básicas](#63-estatísticas-básicas)
   - [6.4 Top 20 grau de saída](#64-top-20-grau-de-saída)
   - [6.5 Top 20 grau de entrada](#65-top-20-grau-de-entrada)
   - [6.6 Busca em profundidade — DFS](#66-busca-em-profundidade--dfs)
   - [6.7 Busca em largura — BFS](#67-busca-em-largura--bfs)
   - [6.8 Nós a distância D](#68-nós-a-distância-d)
   - [6.9 Caminho crítico — Dijkstra adaptado](#69-caminho-crítico--dijkstra-adaptado)
7. [Interface gráfica — tela de boas-vindas](#7-interface-gráfica--tela-de-boas-vindas)
8. [Interface gráfica — painel de buscas](#8-interface-gráfica--painel-de-buscas)
9. [Visualização com GraphStream](#9-visualização-com-graphstream)
10. [Deduplicação de e-mails](#10-deduplicação-de-e-mails)
11. [Regras de parsing](#11-regras-de-parsing)
12. [Arquitetura e classes](#12-arquitetura-e-classes)
13. [Diagramas UML](#13-diagramas-uml)
14. [Modo demonstração](#14-modo-demonstração)
15. [Rastreabilidade dos requisitos](#15-rastreabilidade-dos-requisitos)
16. [Sugestões de testes manuais](#16-sugestões-de-testes-manuais)

---

## 1. Visão geral

O **Enron Email Dataset** é uma base pública com aproximadamente 500 mil e-mails de funcionários da empresa Enron, organizada em pastas por usuário. Este projeto processa esse dataset para construir um grafo de comunicação onde:

| Elemento | Significado |
|----------|-------------|
| **Vértice** | Um endereço de e-mail único (rótulo do nó) |
| **Aresta A → B** | A enviou ao menos uma mensagem para B |
| **Peso da aresta** | Quantidade total de mensagens enviadas de A para B |
| **Direção** | A → B é diferente de B → A |

Sobre esse grafo são implementados, **do zero e sem bibliotecas externas de algoritmos**:

- DFS e BFS com reconstrução de caminho e tratamento de ciclos
- Consulta de nós a exatamente D arestas de distância
- Caminho crítico aproximado via Dijkstra com custo inverso ao peso

A visualização interativa usa a biblioteca **GraphStream** e é apresentada em uma interface Swing com seletores alfabéticos de vértices.

---

## 2. Requisitos de sistema

| Componente | Versão mínima |
|------------|--------------|
| Java (JDK) | 17 |
| Maven | 3.8+ |
| Sistema operacional | Windows, macOS ou Linux |
| Memória RAM recomendada | 4 GB (para o dataset completo) |

> **Maven não instalado?**
> Baixe e extraia o Maven para sua pasta home e adicione ao PATH:
> ```bash
> curl -sL https://archive.apache.org/dist/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.tar.gz \
>   | tar xz -C $HOME
> export PATH="$HOME/apache-maven-3.9.6/bin:$PATH"
> ```

---

## 3. Estrutura do projeto

```
Enron_GraphAnalyzer/
├── pom.xml                          # Configuração Maven (Java 17, GraphStream, assembly)
├── graph.bin                        # Cache binário (gerado na primeira execução)
├── data/
│   └── maildir/                     # Dataset Enron (150 subpastas de usuários)
│       ├── allen-p/
│       │   ├── sent/                # E-mails enviados — usados pelo sistema
│       │   └── _sent_mail/          # Idem (nome alternativo)
│       └── ...
├── docs/
│   ├── README.md                    # Este arquivo
│   ├── diagrama-classes.puml        # Diagrama de classes (PlantUML)
│   ├── sequencia-construcao-grafo.puml
│   ├── sequencia-busca.puml
│   └── sequencia-caminho-critico.puml
└── src/
    └── main/
        └── java/
            └── br/edu/enron/
                ├── app/
                │   └── Main.java                  # Ponto de entrada
                ├── model/
                │   ├── Vertex.java                # Nó do grafo (e-mail)
                │   ├── Edge.java                  # Aresta direcionada e ponderada
                │   ├── EmailMessage.java           # Dados extraídos de um e-mail
                │   ├── DegreeResult.java           # Resultado de consulta de grau
                │   └── PathResult.java             # Caminho retornado por algoritmos
                ├── graph/
                │   └── ContactGraph.java           # Estrutura do grafo (lista de adjacência)
                ├── service/
                │   ├── ContactAnalyzer.java        # Estatísticas e rankings
                │   ├── DepthFirstSearch.java       # DFS iterativa
                │   ├── BreadthFirstSearch.java     # BFS com fila
                │   ├── DistanceCalculator.java     # BFS por níveis
                │   └── CriticalPathService.java    # Dijkstra com custo inverso
                ├── parser/
                │   ├── EmailParser.java            # Parser do cabeçalho RFC 2822
                │   └── EnronDatasetReader.java     # Varredura do dataset + cache
                ├── util/
                │   └── EmailValidator.java         # Normalização e validação de e-mails
                └── view/
                    ├── GraphVisualizer.java        # Renderização GraphStream
                    └── SearchPanel.java            # Interface Swing com dropdowns
```

---

## 4. Como compilar

Na raiz do projeto (`Enron_GraphAnalyzer/`):

```bash
mvn compile
```

Para gerar o JAR executável com todas as dependências embutidas:

```bash
mvn package
```

O arquivo gerado será `target/enron-graph-analyzer.jar`.

---

## 5. Como executar

### 5.1 Interface gráfica (recomendado)

**Basta executar sem nenhum argumento.** O sistema abre automaticamente a tela de boas-vindas:

```bash
mvn exec:java
```

Ou pelo IntelliJ IDEA: `Run → Run 'Main'` (sem configurar argumentos).

Na tela de boas-vindas:

| Botão / Campo | O que faz |
|---------------|-----------|
| Campo "Dataset folder" | Pré-preenchido automaticamente se `data/maildir` for encontrado |
| **Browse…** | Abre um seletor de pasta para localizar o `maildir` manualmente |
| **Load Enron Dataset** | Carrega os 150 usuários e abre o painel de análise |
| **Demo Mode** | Usa 5 e-mails fictícios — ideal para testar sem o dataset |
| Checkbox "Force cache rebuild" | Força nova varredura mesmo que o cache `graph.bin` exista |

> Na primeira execução com o dataset completo, a varredura de todos os arquivos pode levar de 1 a 3 minutos dependendo do hardware. Nas execuções seguintes o grafo é carregado do cache `graph.bin` em segundos.

---

### 5.2 Linha de comando (avançado)

Passar o caminho do dataset como argumento pula a tela de boas-vindas:

```bash
# Carrega o dataset (usa cache se existir)
mvn exec:java -Dexec.args="data/maildir"

# Força reconstrução do cache
mvn exec:java -Dexec.args="data/maildir --rebuild"

# Via JAR (após mvn package)
java -jar target/enron-graph-analyzer.jar data/maildir
java -jar target/enron-graph-analyzer.jar data/maildir --rebuild
```

---

### 5.3 Top 20 via CLI — saída completa

Ao iniciar com o dataset real, o console imprime automaticamente os dois rankings
antes de abrir a interface gráfica. Exemplo de saída esperada (endereços ilustrativos):

```
--- 2. TOP 20 OUT-DEGREE (most active senders) ---
   1. username1@enron.com (degree=N)
   2. username2@enron.com (degree=N)
   ...
  20. username20@enron.com (degree=N)

--- 3. TOP 20 IN-DEGREE (most contacted recipients) ---
   1. username1@enron.com (degree=N)
   2. username2@enron.com (degree=N)
   ...
  20. username20@enron.com (degree=N)
```

**Grau de saída** = número de destinatários distintos para os quais o usuário enviou ao menos uma mensagem.

**Grau de entrada** = número de remetentes distintos que enviaram ao menos uma mensagem para o usuário.

> Os valores de grau contam **relacionamentos únicos** (arestas), não a soma das mensagens (pesos). Um usuário que enviou 100 mensagens para a mesma pessoa tem grau de saída 1 para esse par.

---

## 6. Funcionalidades

### 6.1 Construção do grafo

O grafo é construído lendo os arquivos de e-mail do dataset. Para cada mensagem válida:

1. Extrai o remetente do campo `From:`.
2. Extrai os destinatários do campo `To:` (múltiplos, separados por vírgula).
3. Para cada par `(remetente, destinatário)`:
   - Se a aresta já existe → incrementa o peso em 1.
   - Se a aresta não existe → cria nova aresta com peso 1 e cria os vértices se necessário.
4. Auto-loops (remetente == destinatário) são ignorados.
5. E-mails sem `@` ou com formato inválido são descartados.

**Apenas as subpastas `sent` e `_sent_mail`** de cada usuário são processadas. As demais pastas (inbox, trash, etc.) são ignoradas.

**Representação interna:**

```
adjacency: Map<Vertex, Map<Vertex, Edge>>
```

A chave externa é o vértice de origem; a chave interna é o vértice de destino. Isso permite busca de aresta em O(1) médio e listagem de vizinhos de saída em O(grau).

---

### 6.2 Cache binário

Para evitar reprocessar centenas de milhares de arquivos a cada execução, o grafo construído é serializado em `graph.bin` (Java Object Serialization) ao lado do diretório do dataset.

| Situação | Comportamento |
|----------|---------------|
| `graph.bin` não existe | Varre o dataset, salva o cache |
| `graph.bin` existe | Carrega o cache diretamente |
| Flag `--rebuild` presente | Ignora o cache e refaz a varredura |

---

### 6.3 Estatísticas básicas

Exibidas no console e na barra de título da interface:

- **Número de vértices**: quantidade de endereços de e-mail únicos no grafo.
- **Número de arestas**: quantidade de pares (remetente, destinatário) distintos com ao menos uma mensagem trocada. Não é a soma dos pesos — é o número de relacionamentos únicos.

---

### 6.4 Top 20 grau de saída

Lista os 20 vértices com maior **grau de saída**, definido como o número de destinatários distintos para os quais o vértice enviou ao menos uma mensagem (não a soma dos pesos).

Ordenação: decrescente por grau; empates desempatados por e-mail em ordem alfabética crescente.

Exibido no console ao iniciar a aplicação.

---

### 6.5 Top 20 grau de entrada

Lista os 20 vértices com maior **grau de entrada**, definido como o número de remetentes distintos que enviaram ao menos uma mensagem para o vértice.

Mesma ordenação do grau de saída.

---

### 6.6 Busca em profundidade — DFS

**Objetivo:** verificar se um vértice X consegue alcançar um vértice Y e retornar o caminho percorrido.

**Algoritmo (iterativo com pilha):**

1. Empilha o vértice de origem.
2. Desempilha o vértice do topo.
3. Se já foi visitado, ignora (tratamento de ciclos).
4. Marca como visitado e registra o predecessor.
5. Se é o destino, reconstrói o caminho pelos predecessores e retorna.
6. Empilha os vizinhos de saída ainda não visitados.
7. Repete até encontrar o destino ou esvaziar a pilha.

**Tratamento de ciclos:** conjunto `visited` garante que nenhum vértice é processado mais de uma vez, eliminando loops infinitos em grafos cíclicos.

**Resultado:** sequência de vértices do caminho encontrado. Não é necessariamente o caminho mais curto — DFS explora o mais fundo possível antes de retroceder.

---

### 6.7 Busca em largura — BFS

**Objetivo:** encontrar o caminho com o **menor número de arestas** entre X e Y.

**Algoritmo (com fila FIFO):**

1. Enfileira o vértice de origem e marca como visitado.
2. Desenfileira o vértice da frente.
3. Se é o destino, reconstrói e retorna o caminho.
4. Enfileira todos os vizinhos de saída ainda não visitados, marcando-os como visitados **no momento em que são enfileirados** (não quando são desenfileirados).
5. Repete até encontrar o destino ou esvaziar a fila.

**Tratamento de ciclos:** o vértice é marcado como visitado ao ser enfileirado, impedindo que seja adicionado à fila mais de uma vez mesmo que haja múltiplos caminhos chegando até ele.

**Diferença em relação ao DFS:** BFS garante o menor número de arestas (caminho mais curto não ponderado). DFS pode retornar um caminho mais longo dependendo da ordem de exploração.

---

### 6.8 Nós a distância D

**Objetivo:** listar todos os vértices que estão a **exatamente D arestas** de distância do vértice N.

**Algoritmo (BFS por níveis):**

1. Começa com o nível 0 contendo apenas N.
2. Para cada nível de 1 até D, expande todos os vizinhos de saída dos vértices do nível atual que ainda não foram visitados.
3. O nível resultante após D expansões contém exatamente os vértices a distância D.

**Casos especiais:**
- D = 0 → retorna apenas o próprio vértice N.
- Se o grafo se esgotar antes de atingir D → retorna lista vazia.

**Resultado:** lista ordenada alfabeticamente por e-mail.

---

### 6.9 Caminho crítico — Dijkstra adaptado

**Objetivo:** encontrar o caminho de **máxima dependência de comunicação** entre A e C.

**Motivação:** peso alto = muitas mensagens = forte dependência. Queremos o caminho que passa pelos relacionamentos mais intensos.

**Adaptação do Dijkstra:**

O Dijkstra padrão minimiza custo. Para favorecer arestas pesadas, aplicamos a transformação:

```
custo(aresta) = 1.0 / peso
```

Uma aresta com peso 10 passa a ter custo 0,1. Uma aresta com peso 1 tem custo 1,0. O Dijkstra, minimizando a soma dos custos inversos, naturalmente escolhe o caminho que passa pelos relacionamentos mais frequentes.

**Algoritmo:**

1. Inicializa distâncias de todos os vértices como ∞, exceto a origem (distância = 0).
2. Usa uma `PriorityQueue` ordenada pelo custo acumulado.
3. A cada iteração, extrai o vértice de menor custo conhecido.
4. Para cada vizinho de saída, calcula o novo custo acumulado e atualiza se for menor.
5. Um conjunto `settled` evita reprocessar vértices já finalizados (tratamento de ciclos).
6. Ao finalizar o destino, reconstrói o caminho pelos predecessores.

**Saída apresentada:**

| Campo | Descrição |
|-------|-----------|
| **Caminho** | Sequência de vértices de A até C |
| **Custo inverso total** | Soma de `1/peso` de cada aresta do caminho |
| **Dependência acumulada** | Soma dos pesos originais das arestas do caminho |

**Exemplo:**

```
A -> B  (peso 5)
B -> C  (peso 3)

Custo inverso total    = 1/5 + 1/3 = 0,5333
Dependência acumulada  = 5 + 3 = 8
```

> Este é um algoritmo **aproximado** para o conceito de caminho crítico, pois a maximização exata da soma de pesos em grafos gerais é um problema NP-difícil. A aproximação via custo inverso é academicamente adequada e bem motivada.

---

## 7. Interface gráfica — tela de boas-vindas

Ao iniciar sem argumentos de linha de comando, a aplicação exibe uma **tela de boas-vindas visual** com dois cartões de ação clicáveis:

| Cartão | Cor | O que faz |
|--------|-----|-----------|
| **Load Enron Dataset** | Azul | Carrega os 150 usuários do `maildir` |
| **Demo Mode** | Verde | Abre o painel com 5 nós fictícios, sem dataset |

O campo "Dataset folder" é **pré-preenchido automaticamente** se `data/maildir` for encontrado na pasta do projeto. O botão **Browse…** permite navegar até outra pasta. O checkbox "Force cache rebuild" refaz a varredura completa mesmo que `graph.bin` já exista.

Não é necessário configurar argumentos no IntelliJ ou no terminal — basta clicar no cartão desejado.

---

## 8. Interface gráfica — painel de buscas

Após o carregamento do grafo, a janela principal `SearchPanel` é exibida:

### Barra de cabeçalho
- Título da aplicação
- Estatísticas do grafo: número de vértices e arestas

### Painel de seleção de vértices

Dois `JComboBox` com **agrupamento alfabético**. Cada grupo de e-mails iniciados pela mesma letra é precedido por um separador visual (ex.: `── A ──`) com fundo azul escuro. Os separadores não são selecionáveis.

Também há um spinner para selecionar a distância D (0 a 20).

### Botões de ação

| Botão | Cor | Ação |
|-------|-----|------|
| **DFS** | Azul | Busca em profundidade de "From" até "To" |
| **BFS** | Verde | Busca em largura de "From" até "To" |
| **Distance D** | Roxo | Lista nós a D arestas de "From" |
| **Critical Path** | Vermelho | Dijkstra adaptado de "From" até "To" |
| **Show Full Graph** | Cinza escuro | Abre janela GraphStream com o grafo completo |

### Área de resultados
- Tema escuro (fundo preto, texto verde monoespaçado)
- Resultados mais recentes aparecem no topo
- Mostra: caminho completo, número de saltos, custo inverso e dependência acumulada

---

## 9. Visualização com GraphStream

Cada execução de algoritmo que encontra um caminho abre uma **janela GraphStream** separada com:

- **Nós padrão**: azul com borda escura, rótulo com o e-mail
- **Nós no caminho (intermediários)**: vermelho, tamanho maior
- **Nós de origem e destino**: verde, tamanho ainda maior
- **Arestas no caminho**: vermelho, mais espessas
- **Rótulos das arestas**: peso original da aresta

**Subgrafo inteligente:** quando o grafo tem mais de 500 nós, a visualização exibe automaticamente um subgrafo com os 500 vértices de maior grau de saída e suas conexões entre si, mantendo a janela responsiva. Para caminhos específicos, o subgrafo inclui os vértices do caminho e seus vizinhos diretos para contexto.

---

## 10. Deduplicação de e-mails

Alguns usuários possuem tanto a pasta `sent` quanto `_sent_mail` com mensagens sobrepostas. Para evitar que o mesmo e-mail seja contado duas vezes (o que inflaria artificialmente os pesos das arestas):

1. Para cada usuário, os arquivos de ambas as pastas são listados.
2. O conteúdo de cada arquivo é hasheado com **SHA-256**.
3. Se o mesmo hash aparecer em ambas as pastas, apenas a primeira ocorrência é processada.
4. A segunda ocorrência é descartada e contabilizada no relatório como "duplicata removida".

E-mails sem duplicata em pastas distintas são sempre processados normalmente.

---

## 11. Regras de parsing

O parser segue o formato **RFC 2822** (cabeçalho + linha em branco + corpo):

**Campos extraídos:**
- `From:` — remetente (um único endereço)
- `To:` — destinatários (um ou mais, separados por vírgula)

**Campos ignorados intencionalmente:**
- `Cc:` e `Bcc:` — uma cópia não representa o mesmo tipo de dependência de comunicação direta
- Todos os demais campos de cabeçalho (`Subject:`, `Date:`, `Message-ID:`, `X-*`, etc.)

**Regra de thread:** o parser lê **apenas o primeiro bloco de cabeçalho** — tudo antes da primeira linha em branco. Qualquer cabeçalho embutido no corpo da mensagem (e-mails encaminhados, respostas citadas com `> From:` ou `-----Original Message-----`) é completamente ignorado. Isso evita que uma thread com cinco mensagens gere arestas a partir de remetentes de conversas anteriores.

**Normalização:**
- E-mails são convertidos para minúsculas e têm espaços removidos.
- Destinatários duplicados dentro do mesmo e-mail são removidos.
- Endereços no formato `"Nome Completo <email@dominio.com>"` são extraídos corretamente.

---

## 12. Arquitetura e classes

### Camada `model` — dados puros

| Classe | Responsabilidade |
|--------|-----------------|
| `Vertex` | Nó do grafo. Imutável. `equals`/`hashCode` baseados no e-mail normalizado. |
| `Edge` | Aresta direcionada com peso mutável. Oferece `incrementWeight()` e `getInverseCost()`. |
| `EmailMessage` | Dados extraídos de um arquivo de e-mail. Lista de destinatários deduplicada e imutável. |
| `DegreeResult` | Par imutável (email, grau). Implementa `Comparable` para ordenação natural. |
| `PathResult` | Caminho retornado pelos algoritmos. Carrega vértices, custo inverso e dependência acumulada. |

### Camada `graph` — estrutura do grafo

| Classe | Responsabilidade |
|--------|-----------------|
| `ContactGraph` | Grafo direcionado, ponderado e rotulado. Representação por lista de adjacência (`Map<Vertex, Map<Vertex, Edge>>`). Implementa `Serializable` para cache. |

### Camada `service` — algoritmos

| Classe | Responsabilidade |
|--------|-----------------|
| `ContactAnalyzer` | Contagem de vértices/arestas e rankings top-20. |
| `DepthFirstSearch` | DFS iterativa com pilha. Retorna `PathResult`. |
| `BreadthFirstSearch` | BFS com fila FIFO. Retorna o caminho de menor número de arestas. |
| `DistanceCalculator` | BFS por níveis. Retorna vértices exatamente a D hops. |
| `CriticalPathService` | Dijkstra com custo = 1/peso. Retorna caminho crítico aproximado. |

### Camada `parser` — leitura do dataset

| Classe | Responsabilidade |
|--------|-----------------|
| `EmailParser` | Extrai `From:` e `To:` do primeiro bloco de cabeçalho de um e-mail bruto. |
| `EnronDatasetReader` | Varre as 150 pastas de usuários, aplica deduplicação SHA-256, usa `EmailParser` e constrói o `ContactGraph`. Gerencia cache binário. |

### Camada `util`

| Classe | Responsabilidade |
|--------|-----------------|
| `EmailValidator` | `normalize(email)` e `isValid(email)` — validação mínima com verificação de `@`. |

### Camada `view` — interface

| Classe | Responsabilidade |
|--------|-----------------|
| `GraphVisualizer` | Converte `ContactGraph` em grafo GraphStream. Aplica estilos CSS e destaca caminhos. |
| `SearchPanel` | Janela Swing com dropdowns alfabéticos, spinner de distância e botões de ação. |

### Camada `app`

| Classe | Responsabilidade |
|--------|-----------------|
| `Main` | Ponto de entrada. Detecta modo (dataset / demo), carrega ou constrói o grafo, imprime estatísticas no console e abre o `SearchPanel`. Não contém lógica de algoritmos. |

---

## 13. Diagramas UML

Os diagramas estão na pasta `docs/` em formato **PlantUML** (`.puml`). Para renderizá-los:

```bash
# Com PlantUML instalado
plantuml docs/*.puml

# Via plugin IntelliJ IDEA: instale o plugin "PlantUML Integration"
# Via VS Code: instale a extensão "PlantUML"
# Online: https://www.plantuml.com/plantuml/uml/
```

| Arquivo | Conteúdo |
|---------|----------|
| `diagrama-classes.puml` | Todas as classes, atributos, métodos e relacionamentos |
| `sequencia-construcao-grafo.puml` | Fluxo de leitura do dataset e construção do grafo |
| `sequencia-busca.puml` | Fluxo de execução do DFS e BFS |
| `sequencia-caminho-critico.puml` | Fluxo do Dijkstra adaptado |

---

## 14. Modo demonstração

Quando executado sem argumentos, o sistema constrói o seguinte grafo pequeno:

```
alice@company.com  -->  bob@company.com    (peso 2)
alice@company.com  -->  carol@company.com  (peso 1)
bob@company.com    -->  dave@company.com   (peso 1)
carol@company.com  -->  dave@company.com   (peso 1)
dave@company.com   -->  eve@company.com    (peso 3)
bob@company.com    -->  eve@company.com    (peso 1)
```

Este grafo permite demonstrar todos os algoritmos:

| Consulta | Exemplo de resultado |
|----------|---------------------|
| Vértices | 5 |
| Arestas | 6 |
| Top out-degree | alice (2), bob (2), carol (1), dave (1) |
| DFS alice → eve | alice → bob → dave → eve |
| BFS alice → eve | alice → bob → eve (2 saltos) |
| Distância 2 de alice | dave@company.com |
| Caminho crítico alice → eve | alice → bob → eve (dependência = 3) |

---

## 15. Rastreabilidade dos requisitos

| Critério | Pontos | Classe responsável | Como é atendido |
|----------|-------:|-------------------|-----------------|
| Grafo direcionado, ponderado e rotulado | 2,0 | `ContactGraph`, `Vertex`, `Edge` | Adjacência `Map<Vertex, Map<Vertex, Edge>>`. Aresta tem `weight` e `getInverseCost()`. Vértice tem rótulo de e-mail. |
| Número de vértices | 0,25 | `ContactAnalyzer.getVertexCount()` | Delega para `ContactGraph.vertexCount()`. |
| Número de arestas | 0,25 | `ContactAnalyzer.getEdgeCount()` | Delega para `ContactGraph.edgeCount()`. |
| Top 20 grau de saída | 0,25 | `ContactAnalyzer.getTop20OutDegree()` | Ordena por grau desc, desempata por email asc, limita a 20. |
| Top 20 grau de entrada | 0,25 | `ContactAnalyzer.getTop20InDegree()` | Idem, usando grau de entrada. |
| DFS com caminho | 1,5 | `DepthFirstSearch.search()` | Iterativa com pilha e conjunto de visitados. Reconstrói caminho por predecessores. |
| BFS com caminho | 1,5 | `BreadthFirstSearch.search()` | Fila FIFO, visitados marcados no enfileiramento. Reconstrói caminho por predecessores. |
| Nós a distância D | 2,0 | `DistanceCalculator.getVerticesAtDistance()` | BFS por níveis. Retorna somente os vértices no nível exato D. |
| Caminho crítico (Dijkstra) | 2,0 | `CriticalPathService.computeCriticalPath()` | Dijkstra com `custo = 1/peso`. Fila de prioridade. Retorna caminho, custo inverso e dependência acumulada. |
| Tratamento de ciclos | — | Todos os algoritmos de percurso | `Set<Vertex> visited` / `Set<Vertex> settled` em cada algoritmo. Nenhum vértice é processado mais de uma vez. |

---

## 16. Sugestões de testes manuais

### Verificar autoria do grafo

1. Execute em modo demo: `mvn exec:java`
2. Selecione `alice@company.com` → `eve@company.com`
3. Clique em **BFS** → resultado esperado: `alice → bob → eve` (2 saltos)
4. Clique em **DFS** → resultado pode variar mas deve terminar em `eve`
5. Selecione distância D = 2 com origem `alice@company.com` → resultado esperado: `dave@company.com`
6. Clique em **Critical Path** → resultado esperado: `alice → bob → eve`, dependência acumulada = 3

### Verificar deduplicação

1. Execute com o dataset: `mvn exec:java -Dexec.args="data/maildir"`
2. No relatório impresso no console, verifique a linha `Duplicates dropped` — deve ser maior que zero para confirmar que o sistema identificou e removeu duplicatas reais.

### Verificar cache

1. Execute pela primeira vez com o dataset — note o tempo de varredura.
2. Execute novamente — deve carregar instantaneamente com a mensagem `Graph loaded from cache`.
3. Execute com `--rebuild` — deve refazer a varredura completa.

### Verificar tratamento de ciclos

1. Execute DFS ou BFS no dataset completo entre dois vértices quaisquer.
2. O sistema deve retornar um resultado (caminho ou "no path") sem travar, mesmo que haja ciclos no grafo — o que é esperado em redes de comunicação reais.

### Verificar caminho inexistente

1. Selecione dois endereços externos ao Enron (que não existem no grafo).
2. O sistema deve exibir `(no path found)` sem lançar exceção.

---

*Projeto desenvolvido para a disciplina de Estruturas de Dados e Teoria dos Grafos.*
*Código 100% autoral em Java 17 puro, com visualização via GraphStream e interface Swing.*
