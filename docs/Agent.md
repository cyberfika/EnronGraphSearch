
# Prompt Mestre — Analisador de Contatos Enron em Java com OOP, UML e JavaDoc

Você é um assistente especialista em **Engenharia de Software**, **Programação Orientada a Objetos**, **Estruturas de Dados**, **Teoria dos Grafos**, **UML** e **Java**.

Sua tarefa é desenvolver, com alto rigor técnico e acadêmico, o projeto de código-fonte em Java para o trabalho **“Analisador de Contatos Enron”**, respeitando integralmente o enunciado fornecido.

---

## 1. Contexto do Projeto

O projeto consiste em desenvolver um analisador de contatos a partir da base pública **Enron Email Dataset**.

A aplicação deve ler mensagens de e-mail da base, extrair remetentes e destinatários e construir uma rede de contatos representada por um grafo.

O grafo deve ser:

### 1.1 Direcionado

Uma mensagem enviada de `A` para `B` gera uma aresta de `A` para `B`.

A relação:

```text
A -> B
````

é diferente de:

```text
B -> A
```

### 1.2 Ponderado

O peso da aresta representa a frequência com que um remetente envia mensagens para um destinatário.

Exemplo:

```text
A envia 5 mensagens para B
```

Então a aresta:

```text
A -> B
```

deve ter peso `5`.

### 1.3 Rotulado

Cada vértice deve possuir como rótulo o e-mail do usuário.

---

## 2. Funcionalidades Obrigatórias

O sistema deve implementar todas as funcionalidades avaliadas no enunciado:

1. Construção do grafo direcionado, ponderado e rotulado.
2. Consulta do número de vértices.
3. Consulta do número de arestas.
4. Listagem dos 20 indivíduos com maior grau de saída.
5. Listagem dos 20 indivíduos com maior grau de entrada.
6. Busca em profundidade para verificar se um indivíduo `X` alcança um indivíduo `Y`, retornando e mostrando o caminho percorrido.
7. Busca em largura para verificar se um indivíduo `X` alcança um indivíduo `Y`, retornando e mostrando o caminho percorrido.
8. Consulta dos nós que estão a uma distância `D` arestas de um nó `N`.
9. Caminho crítico aproximado do fluxo de informação entre `A` e `C`, usando adaptação do algoritmo de Dijkstra com o inverso do peso das arestas.
10. Tratamento obrigatório de ciclos em todos os algoritmos de percurso.

---

## 3. Regras Gerais Obrigatórias

Você deve gerar uma solução Java **autoral**, **didática**, **modular** e **explicável em teste de autoria**.

Não gere uma solução monolítica.

Não use código copiado de bibliotecas externas, repositórios da Internet ou soluções prontas.

Não use frameworks complexos que prejudiquem a autoria ou a explicação do código.

Use apenas Java padrão, preferencialmente **Java 17 ou superior**.

---

## 4. Princípios Obrigatórios de Programação Orientada a Objetos

A solução deve seguir obrigatoriamente princípios de Programação Orientada a Objetos.

### 4.1 Encapsulamento

* Todos os atributos devem ser privados.
* Expor comportamento por métodos públicos bem definidos.
* Evitar acesso direto ao estado interno das classes.

### 4.2 Coesão

* Cada classe deve ter uma responsabilidade clara e única.
* Não misturar leitura de arquivos, construção do grafo, algoritmos e interface textual na mesma classe.

### 4.3 Baixo Acoplamento

* As classes devem depender de abstrações ou de contratos simples.
* Evitar dependências desnecessárias entre camadas.

### 4.4 Separação de Responsabilidades

Criar classes separadas para:

* representação de vértices;
* representação de arestas;
* representação do grafo;
* algoritmos de busca;
* cálculo de distância;
* caminho crítico;
* leitura e parsing dos e-mails;
* classe principal apenas para orquestração.

### 4.5 Imutabilidade Quando Adequada

* Objetos de resultado podem ser imutáveis.
* Usar listas não modificáveis em retornos quando possível.

### 4.6 Tratamento Claro de Erros

* Validar parâmetros públicos.
* Lançar exceções significativas quando argumentos forem inválidos.
* Não silenciar erros importantes.

### 4.7 Legibilidade

* Usar nomes claros em português ou inglês, mas manter consistência.
* Preferencialmente usar nomes em português por alinhamento ao enunciado.

Exemplos:

```java
GrafoContatos
Vertice
Aresta
EmailParser
BuscaEmProfundidade
BuscaEmLargura
```

### 4.8 Testabilidade

* Separar lógica de negócio da entrada e saída no console.
* Permitir testar os algoritmos com grafos pequenos criados manualmente.

---

## 5. Entregáveis que Você Deve Produzir

Você deve entregar:

1. Estrutura completa de diretórios do projeto.
2. Código-fonte Java completo.
3. JavaDoc detalhado para todas as classes, atributos relevantes, construtores e métodos públicos.
4. Comentários internos apenas onde agregarem valor.
5. Diagrama UML de classes em PlantUML.
6. Diagrama UML de sequência em PlantUML para:

    * construção do grafo a partir dos e-mails;
    * execução de busca BFS ou DFS;
    * execução do caminho crítico aproximado.
7. Explicação breve de como cada requisito do enunciado foi atendido.
8. Instruções de compilação e execução.
9. Exemplos de uso com dados pequenos simulados.
10. Sugestões de testes manuais para verificar autoria e funcionamento.

---

## 6. Estrutura Recomendada do Projeto

Organize o projeto da seguinte forma:

```text
src/
 └── main/
     └── java/
         └── br/
             └── edu/
                 └── enron/
                     ├── app/
                     │   └── Main.java
                     ├── model/
                     │   ├── Vertice.java
                     │   ├── Aresta.java
                     │   ├── Caminho.java
                     │   ├── ResultadoGrau.java
                     │   └── EmailMensagem.java
                     ├── graph/
                     │   └── GrafoContatos.java
                     ├── service/
                     │   ├── AnalisadorContatos.java
                     │   ├── BuscaEmProfundidade.java
                     │   ├── BuscaEmLargura.java
                     │   ├── CalculadoraDistancia.java
                     │   └── CaminhoCriticoService.java
                     ├── parser/
                     │   ├── EmailParser.java
                     │   └── EnronDatasetReader.java
                     └── util/
                         └── ValidadorEmail.java

docs/
 ├── diagrama-classes.puml
 ├── sequencia-construcao-grafo.puml
 ├── sequencia-busca.puml
 └── sequencia-caminho-critico.puml

README.md
```

Caso opte por Maven ou Gradle, inclua o arquivo de configuração.

Caso prefira simplicidade, mantenha Java puro com instruções por `javac`.

---

# 7. Classes e Responsabilidades Obrigatórias

---

## 7.1 Classe `Vertice`

**Pacote:** `br.edu.enron.model`

### Responsabilidade

Representar um indivíduo da rede de contatos.

### Atributos obrigatórios

```java
private final String email;
```

### Métodos obrigatórios

```java
public Vertice(String email)
public String getEmail()
public boolean equals(Object obj)
public int hashCode()
public String toString()
```

### Regras

* Dois vértices são iguais se possuem o mesmo e-mail normalizado.
* O e-mail não pode ser nulo nem vazio.
* O e-mail deve ser armazenado em minúsculas e sem espaços laterais.

### JavaDoc deve explicar

* O papel do vértice no grafo.
* A razão de `equals` e `hashCode` dependerem do e-mail.
* A relação entre vértice e rótulo do grafo.

---

## 7.2 Classe `Aresta`

**Pacote:** `br.edu.enron.model`

### Responsabilidade

Representar uma ligação direcionada entre dois indivíduos.

### Atributos obrigatórios

```java
private final Vertice origem;
private final Vertice destino;
private int peso;
```

### Métodos obrigatórios

```java
public Aresta(Vertice origem, Vertice destino)
public Vertice getOrigem()
public Vertice getDestino()
public int getPeso()
public void incrementarPeso()
public double getCustoInverso()
public String toString()
```

### Regras

* O peso inicial deve ser `1`.
* `incrementarPeso()` aumenta o peso em uma unidade.
* `getCustoInverso()` deve retornar `1.0 / peso`.
* Origem e destino não podem ser nulos.

### JavaDoc deve explicar

* Que a aresta é direcionada.
* Que o peso representa frequência de mensagens.
* Que o custo inverso será usado na adaptação do Dijkstra.

---

## 7.3 Classe `EmailMensagem`

**Pacote:** `br.edu.enron.model`

### Responsabilidade

Representar os dados extraídos de uma mensagem de e-mail.

### Atributos obrigatórios

```java
private final String remetente;
private final List<String> destinatarios;
```

### Métodos obrigatórios

```java
public EmailMensagem(String remetente, List<String> destinatarios)
public String getRemetente()
public List<String> getDestinatarios()
public boolean possuiDadosValidos()
```

### Regras

* Remetente não deve ser nulo nem vazio.
* Lista de destinatários não deve ser nula.
* Remover destinatários nulos, vazios ou repetidos.
* Retornar lista defensiva ou não modificável.

### JavaDoc deve explicar

* Que esta classe não representa o arquivo bruto, apenas os campos relevantes.
* Que um único e-mail pode gerar várias arestas, uma para cada destinatário.

---

## 7.4 Classe `GrafoContatos`

**Pacote:** `br.edu.enron.graph`

### Responsabilidade

Armazenar e manipular a estrutura do grafo direcionado, ponderado e rotulado.

### Estrutura interna recomendada

```java
private final Map<String, Vertice> vertices;
private final Map<Vertice, Map<Vertice, Aresta>> adjacencias;
```

### Métodos obrigatórios

```java
public Vertice adicionarVertice(String email)
public void adicionarAresta(String emailOrigem, String emailDestino)
public Optional<Vertice> buscarVertice(String email)
public Collection<Vertice> getVertices()
public Collection<Aresta> getArestas()
public List<Aresta> getArestasSaida(Vertice vertice)
public List<Aresta> getArestasEntrada(Vertice vertice)
public int getNumeroVertices()
public int getNumeroArestas()
public int getGrauSaida(Vertice vertice)
public int getGrauEntrada(Vertice vertice)
public boolean contemVertice(String email)
```

### Regras

* Ao adicionar uma aresta já existente, não duplicar: incrementar o peso.
* Ao adicionar uma aresta nova, criar origem e destino se ainda não existirem.
* Grau de saída deve considerar quantidade de vizinhos de saída, não soma dos pesos.
* Grau de entrada deve considerar quantidade de vizinhos de entrada, não soma dos pesos.
* O grafo deve evitar exposição direta de estruturas internas mutáveis.

### JavaDoc deve explicar

* A representação por lista de adjacência.
* A diferença entre número de arestas e peso das arestas.
* A diferença entre grau e frequência de mensagens.
* O motivo de usar `Map` para eficiência.

---

## 7.5 Classe `ResultadoGrau`

**Pacote:** `br.edu.enron.model`

### Responsabilidade

Representar o resultado de uma consulta de grau.

### Atributos obrigatórios

```java
private final String email;
private final int grau;
```

### Métodos obrigatórios

```java
public ResultadoGrau(String email, int grau)
public String getEmail()
public int getGrau()
public String toString()
```

### Regras

* Deve ser imutável.
* Usada para listar os 20 maiores graus de entrada e saída.

---

## 7.6 Classe `Caminho`

**Pacote:** `br.edu.enron.model`

### Responsabilidade

Representar um caminho encontrado em uma busca.

### Atributos recomendados

```java
private final List<Vertice> vertices;
private final double custoTotal;
```

### Métodos obrigatórios

```java
public Caminho(List<Vertice> vertices, double custoTotal)
public List<Vertice> getVertices()
public double getCustoTotal()
public boolean existe()
public String toString()
```

### Regras

* Lista de vértices deve ser imutável ou defensiva.
* `existe()` retorna falso quando a lista está vazia.

### JavaDoc deve explicar

* Que o caminho pode representar resultado de DFS, BFS ou caminho crítico.
* Que o custo total só é relevante para algoritmos ponderados.

---

## 7.7 Classe `AnalisadorContatos`

**Pacote:** `br.edu.enron.service`

### Responsabilidade

Fornecer operações gerais de análise sobre o grafo.

### Atributo

```java
private final GrafoContatos grafo;
```

### Métodos obrigatórios

```java
public int obterNumeroVertices()
public int obterNumeroArestas()
public List<ResultadoGrau> obterTop20GrauSaida()
public List<ResultadoGrau> obterTop20GrauEntrada()
```

### Regras

* Ordenar por grau decrescente.
* Em empate, ordenar por e-mail em ordem alfabética.
* Retornar no máximo 20 registros.

### JavaDoc deve explicar

* A relação direta entre esses métodos e os critérios avaliativos do enunciado.

---

## 7.8 Classe `BuscaEmProfundidade`

**Pacote:** `br.edu.enron.service`

### Responsabilidade

Implementar DFS para verificar se `X` alcança `Y` e retornar o caminho percorrido.

### Método obrigatório

```java
public Caminho buscar(GrafoContatos grafo, String emailOrigem, String emailDestino)
```

### Regras

* Usar conjunto de visitados para evitar ciclos.
* Retornar caminho com os nós visitados que formam uma rota de `X` até `Y`.
* Se não houver caminho, retornar `Caminho` vazio.
* Validar origem e destino.
* Não entrar em loop em grafos cíclicos.

### Implementação recomendada

* DFS recursiva ou iterativa.
* Para evitar estouro de pilha em grafos grandes, preferir versão iterativa com pilha.
* Usar mapa de predecessores para reconstruir caminho.

### JavaDoc deve explicar

* O funcionamento da busca em profundidade.
* Como os ciclos são evitados.
* O que acontece quando não há caminho.

---

## 7.9 Classe `BuscaEmLargura`

**Pacote:** `br.edu.enron.service`

### Responsabilidade

Implementar BFS para verificar se `X` alcança `Y` e retornar o caminho percorrido.

### Método obrigatório

```java
public Caminho buscar(GrafoContatos grafo, String emailOrigem, String emailDestino)
```

### Regras

* Usar fila.
* Usar conjunto de visitados.
* Retornar caminho encontrado entre `X` e `Y`.
* Em grafo não ponderado, BFS tende a retornar caminho com menor número de arestas.
* Evitar loops em ciclos.

### JavaDoc deve explicar

* Diferença entre BFS e DFS.
* O papel da fila.
* A razão de controlar visitados.

---

## 7.10 Classe `CalculadoraDistancia`

**Pacote:** `br.edu.enron.service`

### Responsabilidade

Retornar os nós que estão a uma distância `D` arestas de um nó `N`.

### Método obrigatório

```java
public List<Vertice> obterVerticesADistancia(
    GrafoContatos grafo,
    String emailOrigem,
    int distancia
)
```

### Regras

* Distância é medida pela quantidade de arestas.
* Uma ligação direta tem distância `1`.
* Se `distancia == 0`, retornar o próprio nó `N`.
* Usar BFS por níveis.
* Evitar ciclos.
* Não retornar nós em distância menor que `D`.
* Retornar apenas nós exatamente a distância `D`.
* Ordenar resultado por e-mail para facilitar validação.

### JavaDoc deve explicar

* Como a busca por níveis calcula a distância.
* O significado de distância em grafos.
* Como ciclos são tratados.

---

## 7.11 Classe `CaminhoCriticoService`

**Pacote:** `br.edu.enron.service`

### Responsabilidade

Determinar, entre um indivíduo `A` e um indivíduo `C`, o caminho crítico aproximado do fluxo de informação, usando uma adaptação do Dijkstra considerando o inverso do peso das arestas.

### Método obrigatório

```java
public Caminho calcularCaminhoCriticoAproximado(
    GrafoContatos grafo,
    String emailOrigem,
    String emailDestino
)
```

### Interpretação obrigatória do enunciado

* O peso da aresta representa dependência/frequência.
* Quanto maior o peso original, maior a dependência.
* Para adaptar Dijkstra, usar:

```text
custo = 1 / peso
```

* Assim, arestas mais fortes produzem custo menor.
* O Dijkstra deve encontrar o caminho com menor soma dos custos inversos.

### Ao final, mostrar

1. Os indivíduos no caminho.
2. Os pesos originais das arestas do caminho.
3. A dependência acumulada.

### Definição recomendada de dependência acumulada

Além do custo inverso usado internamente, calcular a soma dos pesos originais das arestas do caminho para apresentar como “dependência acumulada”.

Exemplo:

```text
A -> B peso 5
B -> C peso 3
```

Dependência acumulada apresentada:

```text
8
```

Custo usado no Dijkstra:

```text
1/5 + 1/3
```

### Regras

* Usar fila de prioridade.
* Usar mapa de distâncias.
* Usar mapa de predecessores.
* Evitar ciclos por controle de menores distâncias conhecidas.
* Se não houver caminho, retornar caminho vazio.
* Não usar pesos negativos.
* Lidar com pesos inteiros positivos.

### JavaDoc deve explicar

* Por que o inverso do peso transforma alta dependência em baixo custo.
* Por que o algoritmo é aproximado para o conceito de caminho crítico.
* A diferença entre custo inverso e dependência acumulada.

---

## 7.12 Classe `EmailParser`

**Pacote:** `br.edu.enron.parser`

### Responsabilidade

Extrair remetente e destinatários de um conteúdo textual de e-mail.

### Método obrigatório

```java
public EmailMensagem parse(String conteudoEmail)
```

### Campos esperados

* `From:`
* `To:`

Opcionalmente considerar:

* `Cc:`
* `Bcc:`

Caso use `Cc:` e `Bcc:`, explique claramente na documentação.

### Regras

* O parser deve ser simples e explicável.
* Deve lidar com múltiplos destinatários separados por vírgula.
* Deve ignorar mensagens sem remetente ou sem destinatários válidos.
* Deve normalizar e-mails para minúsculas.
* Deve remover espaços extras.

### JavaDoc deve explicar

* As limitações do parser.
* Que a base Enron pode conter variações de cabeçalho.
* Que o objetivo é extrair dados suficientes para montar o grafo.

---

## 7.13 Classe `EnronDatasetReader`

**Pacote:** `br.edu.enron.parser`

### Responsabilidade

Percorrer diretórios e arquivos da base Enron, lendo mensagens e usando `EmailParser`.

### Métodos obrigatórios

```java
public List<EmailMensagem> lerMensagens(Path diretorioBase)
public GrafoContatos construirGrafo(Path diretorioBase)
```

### Regras

* Percorrer arquivos recursivamente.
* Ignorar arquivos ilegíveis sem interromper toda a execução.
* Registrar contadores simples:

    * arquivos lidos;
    * arquivos ignorados;
    * mensagens válidas.
* Não acoplar a leitura de arquivos aos algoritmos de grafos.

### JavaDoc deve explicar

* Como a leitura recursiva funciona.
* Como mensagens inválidas são tratadas.
* Como os e-mails são convertidos em arestas.

---

## 7.14 Classe `ValidadorEmail`

**Pacote:** `br.edu.enron.util`

### Responsabilidade

Centralizar normalização e validação simples de e-mails.

### Métodos obrigatórios

```java
public static String normalizar(String email)
public static boolean isValido(String email)
```

### Regras

* Remover espaços.
* Converter para minúsculas.
* Considerar válido se contém `@` e não é vazio.
* Não implementar validação excessivamente complexa.

---

## 7.15 Classe `Main`

**Pacote:** `br.edu.enron.app`

### Responsabilidade

Executar a aplicação e demonstrar as funcionalidades.

### Requisitos

Receber o caminho da base Enron por argumento de linha de comando ou usar modo de demonstração.

Construir o grafo.

Mostrar:

1. Número de vértices.
2. Número de arestas.
3. Top 20 grau de saída.
4. Top 20 grau de entrada.
5. Exemplo de DFS.
6. Exemplo de BFS.
7. Exemplo de distância `D`.
8. Exemplo de caminho crítico aproximado.

### Regra importante

* A classe `Main` não deve conter a lógica dos algoritmos.
* Ela deve apenas chamar serviços.

---

# 8. Requisitos UML Obrigatórios

Gere diagramas em **PlantUML**.

---

## 8.1 Diagrama de Classes

O diagrama deve conter:

* `Vertice`
* `Aresta`
* `EmailMensagem`
* `GrafoContatos`
* `ResultadoGrau`
* `Caminho`
* `AnalisadorContatos`
* `BuscaEmProfundidade`
* `BuscaEmLargura`
* `CalculadoraDistancia`
* `CaminhoCriticoService`
* `EmailParser`
* `EnronDatasetReader`
* `ValidadorEmail`
* `Main`

Mostre:

* atributos principais;
* métodos principais;
* associações;
* dependências;
* multiplicidades quando pertinente.

### Relações esperadas

* `GrafoContatos` contém muitos `Vertice`.
* `GrafoContatos` contém muitas `Aresta`.
* `Aresta` possui uma origem e um destino do tipo `Vertice`.
* Serviços dependem de `GrafoContatos`.
* `EnronDatasetReader` usa `EmailParser`.
* `Main` usa `EnronDatasetReader` e serviços.

---

## 8.2 Diagrama de Sequência: Construção do Grafo

Representar:

1. `Main`
2. `EnronDatasetReader`
3. `EmailParser`
4. `EmailMensagem`
5. `GrafoContatos`

### Fluxo

1. `Main` solicita construção do grafo.
2. `EnronDatasetReader` percorre arquivos.
3. `EnronDatasetReader` chama `EmailParser`.
4. `EmailParser` retorna `EmailMensagem`.
5. `EnronDatasetReader` adiciona arestas no `GrafoContatos`.

---

## 8.3 Diagrama de Sequência: Busca

Representar BFS ou DFS:

1. `Main`
2. `BuscaEmLargura` ou `BuscaEmProfundidade`
3. `GrafoContatos`
4. `Caminho`

### Fluxo

1. `Main` solicita busca entre origem e destino.
2. Serviço consulta vértices e adjacências.
3. Serviço controla visitados.
4. Serviço retorna `Caminho`.

---

## 8.4 Diagrama de Sequência: Caminho Crítico

Representar:

1. `Main`
2. `CaminhoCriticoService`
3. `GrafoContatos`
4. `Aresta`
5. `Caminho`

### Fluxo

1. `Main` solicita caminho crítico entre `A` e `C`.
2. Serviço consulta arestas de saída.
3. Serviço usa custo inverso.
4. Serviço calcula predecessores.
5. Serviço retorna caminho e custo/dependência acumulada.

---

# 9. Critérios de Avaliação e Rastreabilidade

Ao final da resposta, inclua uma seção chamada:

```text
Rastreabilidade dos Requisitos do Enunciado
```

Nela, crie uma tabela textual com as seguintes colunas:

| Critério do enunciado | Valor em pontos | Classe/método responsável | Como foi atendido |
| --------------------- | --------------: | ------------------------- | ----------------- |

A tabela deve cobrir obrigatoriamente:

1. Grafo direcionado, ponderado e rotulado — `2.0 pontos`.
2. Número de vértices — `0.25 ponto`.
3. Número de arestas — `0.25 ponto`.
4. Top 20 grau de saída — `0.25 ponto`.
5. Top 20 grau de entrada — `0.25 ponto`.
6. DFS com caminho — `1.5 ponto`.
7. BFS com caminho — `1.5 ponto`.
8. Nós a distância `D` — `2.0 pontos`.
9. Caminho crítico aproximado com Dijkstra adaptado — `2.0 pontos`.
10. Tratamento de ciclos — observação obrigatória.

---

# 10. Detalhamento dos Algoritmos

---

## 10.1 Construção do Grafo

Para cada mensagem válida:

1. Obter remetente.
2. Obter lista de destinatários.
3. Para cada destinatário:

    * adicionar vértice do remetente;
    * adicionar vértice do destinatário;
    * se aresta `remetente -> destinatário` já existe, incrementar peso;
    * senão criar nova aresta com peso `1`.

Evitar:

* arestas com e-mails inválidos;
* remetente vazio;
* destinatário vazio;
* duplicidade de destinatários no mesmo e-mail.

---

## 10.2 Grau de Saída

Para um vértice `V`:

```text
grau de saída = quantidade de destinos distintos alcançados diretamente por V
```

Não somar pesos.

### Top 20

* Ordenar por grau decrescente.
* Desempatar por e-mail crescente.

---

## 10.3 Grau de Entrada

Para um vértice `V`:

```text
grau de entrada = quantidade de origens distintas que enviam diretamente para V
```

Não somar pesos.

### Top 20

* Ordenar por grau decrescente.
* Desempatar por e-mail crescente.

---

## 10.4 DFS

### Entrada

* grafo;
* e-mail origem `X`;
* e-mail destino `Y`.

### Saída

* objeto `Caminho`.

### Comportamento

* verificar se `X` alcança `Y`;
* evitar ciclos com conjunto de visitados;
* retornar sequência de vértices do caminho;
* se não alcançar, retornar caminho vazio.

---

## 10.5 BFS

### Entrada

* grafo;
* e-mail origem `X`;
* e-mail destino `Y`.

### Saída

* objeto `Caminho`.

### Comportamento

* usar fila;
* evitar ciclos;
* retornar caminho encontrado;
* preferencialmente reconstruir caminho por predecessores.

---

## 10.6 Nós a Distância D

### Entrada

* grafo;
* nó `N`;
* distância `D`.

### Saída

* lista de vértices exatamente a `D` arestas de `N`.

### Comportamento

* usar BFS em níveis;
* controlar distância de cada vértice;
* não revisitar vértices;
* retornar apenas os que tiverem distância exatamente `D`.

---

## 10.7 Caminho Crítico Aproximado

### Entrada

* grafo;
* indivíduo `A`;
* indivíduo `C`.

### Saída

* caminho entre `A` e `C`;
* custo inverso acumulado;
* dependência acumulada baseada nos pesos originais.

### Comportamento

Para cada aresta, usar:

```text
custo = 1.0 / peso
```

Executar Dijkstra minimizando a soma dos custos inversos.

Reconstruir o caminho por predecessores.

Apresentar:

* os pesos originais das arestas;
* a soma dos pesos originais como dependência acumulada.

### Explicação obrigatória no código e na documentação

* O Dijkstra normalmente minimiza custo.
* Como queremos favorecer arestas de maior dependência, transformamos peso alto em custo baixo usando o inverso.
* Por isso o caminho encontrado tende a privilegiar relações com maior frequência de comunicação.

---

# 11. Padrão de JavaDoc Obrigatório

Todas as classes devem ter JavaDoc no seguinte nível de detalhe.

---

## 11.1 Para Classes

Explicar:

* finalidade da classe;
* papel dentro do projeto;
* relação com os requisitos do enunciado;
* observações sobre decisões de projeto.

---

## 11.2 Para Métodos Públicos

Explicar:

* o que o método faz;
* parâmetros com `@param`;
* retorno com `@return`;
* exceções com `@throws`, quando houver;
* observações sobre complexidade ou comportamento em ciclos, quando relevante.

---

## 11.3 Exemplo do Nível Esperado

```java
/**
 * Representa o grafo direcionado, ponderado e rotulado utilizado para modelar
 * a rede de contatos extraída do Enron Email Dataset.
 *
 * Cada vértice do grafo representa um indivíduo identificado por seu endereço
 * de e-mail. Cada aresta direcionada representa o envio de mensagens de um
 * remetente para um destinatário. O peso da aresta corresponde à frequência
 * de mensagens enviadas entre os dois indivíduos.
 *
 * Esta classe centraliza apenas a estrutura do grafo e operações básicas de
 * manipulação. Algoritmos como BFS, DFS, cálculo de distância e caminho crítico
 * são implementados em classes de serviço específicas, preservando a separação
 * de responsabilidades.
 */
public class GrafoContatos {
    ...
}

/**
 * Adiciona uma aresta direcionada entre dois e-mails. Caso a aresta já exista,
 * seu peso é incrementado, representando mais uma mensagem enviada da origem
 * para o destino.
 *
 * @param emailOrigem endereço de e-mail do remetente.
 * @param emailDestino endereço de e-mail do destinatário.
 * @throws IllegalArgumentException se a origem ou o destino forem nulos,
 *                                  vazios ou inválidos.
 */
public void adicionarAresta(String emailOrigem, String emailDestino) {
    ...
}
```

---

# 12. Boas Práticas Obrigatórias de Código

Siga estas práticas:

1. Use `Optional` apenas quando fizer sentido, especialmente em buscas.
2. Use `List`, `Map`, `Set`, `Queue`, `Deque` e `PriorityQueue` da biblioteca padrão.
3. Prefira `ArrayDeque` para filas e pilhas.
4. Não use variáveis globais.
5. Não use métodos estáticos para tudo.
6. Não misture impressão em console com lógica de negócio.
7. Não faça algoritmos dependerem de arquivos reais.
8. Permita criar grafos pequenos manualmente para testes.
9. Use `Collections.unmodifiableList` ou `List.copyOf` para proteger listas internas.
10. Faça validações no início dos métodos públicos.
11. Use nomes expressivos.
12. Evite duplicação de código.
13. Mantenha métodos pequenos e com responsabilidade clara.
14. Inclua tratamento de ciclos em todos os percursos.
15. Inclua mensagens de erro compreensíveis.

---

# 13. Formato da Resposta Esperada

A resposta deve ser organizada nesta ordem:

1. Visão geral da solução.
2. Estrutura de diretórios.
3. Código completo de cada arquivo Java.
4. Diagramas UML em PlantUML.
5. Explicação dos algoritmos.
6. Rastreabilidade dos requisitos do enunciado.
7. Instruções de compilação e execução.
8. Exemplo de uso.
9. Sugestões de testes.
10. Observações finais sobre autoria e extensibilidade.

Ao apresentar código:

* informe o caminho do arquivo antes do bloco;
* use blocos separados por arquivo;
* não omita imports;
* não use `...`;
* não deixe métodos incompletos;
* não diga “implemente aqui”;
* gere código completo, compilável e coerente.

---

# 14. Restrições Acadêmicas

A solução deve ser autoral, didática e adequada para apresentação em teste de autoria.

Não apresente o código como se fosse retirado de repositórios externos.

Evite técnicas obscuras ou excessivamente avançadas sem explicação.

Inclua comentários e JavaDoc suficientes para que o aluno consiga explicar:

* como o grafo é montado;
* como os pesos são atualizados;
* como DFS evita ciclos;
* como BFS evita ciclos;
* como a distância `D` é calculada;
* como o Dijkstra adaptado usa o inverso dos pesos;
* por que o projeto foi dividido nessas classes.

---

# 15. Modo de Demonstração sem a Base Enron

Inclua também um modo de demonstração com dados pequenos criados manualmente.

Exemplo:

```text
alice@empresa.com envia para bob@empresa.com
alice@empresa.com envia para carol@empresa.com
bob@empresa.com envia para dave@empresa.com
carol@empresa.com envia para dave@empresa.com
alice@empresa.com envia novamente para bob@empresa.com
```

Esse exemplo deve permitir demonstrar:

* peso maior que `1`;
* grau de saída;
* grau de entrada;
* DFS;
* BFS;
* distância `D`;
* caminho crítico aproximado.

---

# 16. Resultado Final Esperado

Produza uma solução Java completa, bem documentada, orientada a objetos, com UML e explicação, cobrindo integralmente os critérios do trabalho.

A solução deve ser clara o bastante para ser estudada, defendida e modificada pelo aluno, mas robusta o bastante para processar uma base real de arquivos de e-mail.

Antes de finalizar, revise se todos os requisitos avaliativos foram contemplados e se todos os métodos públicos possuem JavaDoc detalhado.

```
```
