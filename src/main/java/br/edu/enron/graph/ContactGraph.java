package br.edu.enron.graph;

import br.edu.enron.model.Edge;
import br.edu.enron.model.Vertex;

import java.io.Serializable;
import java.util.*;

/**
 * Grafo de contatos direcionado, ponderado e rotulado construído a partir do Enron Email Dataset.
 *
 * <h2>Representação interna</h2>
 * <p>O grafo usa um mapa de adjacência de mapas:
 * <pre>
 *   adjacency : Map&lt;Vertex, Map&lt;Vertex, Edge&gt;&gt;
 * </pre>
 * A chave do mapa externo é o vértice de <em>origem</em>. A chave do mapa interno é o 
 * vértice de <em>destino</em>, e seu valor é a {@link Edge} entre eles. 
 * Esta estrutura fornece busca em tempo médio O(1) tanto para "a aresta A→B existe?" 
 * quanto para "quais são todos os vizinhos de A?", o que é essencial para grafos grandes.</p>
 *
 * <h2>Semântica</h2>
 * <ul>
 *   <li>Cada vértice é um endereço de e-mail único (o rótulo do grafo).</li>
 *   <li>Cada aresta A→B registra o número de mensagens enviadas de A para B (o peso). 
 *       Adicionar a mesma aresta novamente incrementa seu peso em vez de duplicá-la.</li>
 *   <li>O grau (de entrada ou de saída) é contado como o número de vizinhos <em>distintos</em>, 
 *       não como a soma dos pesos.</li>
 * </ul>
 *
 * <p>Esta classe implementa {@link Serializable} para que o grafo construído possa ser 
 * persistido em um arquivo de cache binário e recarregado em execuções subsequentes 
 * sem a necessidade de reanalisar todo o dataset.</p>
 */
public class ContactGraph implements Serializable {

    @java.io.Serial
    private static final long serialVersionUID = 2L;

    /**
     * Índice de todos os vértices indexados por seu endereço de e-mail normalizado.
     * Fornece busca de vértice O(1) pela string do e-mail.
     */
    private final Map<String, Vertex> vertexIndex;

    /**
     * Estrutura de adjacência: origem → (destino → aresta).
     * Todo vértice presente no grafo tem uma entrada aqui, mesmo que não tenha 
     * arestas de saída (seu mapa interno estará simplesmente vazio).
     */
    private final Map<Vertex, Map<Vertex, Edge>> adjacency;

    /**
     * Endereços de e-mail dos 150 proprietários de caixas de correio da Enron — ou seja, 
     * os remetentes encontrados nas pastas {@code sent} / {@code _sent_mail}. Usado para 
     * preencher a caixa de combinação "De" apenas com usuários reais do dataset, 
     * e não endereços externos.
     */
    private final Set<String> ownerEmails;

    /**
     * Constrói um grafo de contatos vazio.
     */
    public ContactGraph() {
        this.vertexIndex  = new HashMap<>();
        this.adjacency    = new HashMap<>();
        this.ownerEmails  = new TreeSet<>(); // ordenado para ordem previsível no combo box
    }

    /**
     * Registra um endereço de e-mail como proprietário de uma caixa de correio 
     * (remetente de uma pasta enviada). Apenas esses endereços aparecem na caixa 
     * de combinação "De" do painel de busca.
     *
     * @param email o endereço de e-mail normalizado do proprietário.
     */
    public void addOwner(String email) {
        if (email != null && !email.isBlank()) {
            ownerEmails.add(email.trim().toLowerCase());
        }
    }

    /**
     * Retorna um conjunto imutável e ordenado alfabeticamente de e-mails de proprietários 
     * de caixas de correio. No modo dataset, isso corresponde aos 150 usuários da Enron 
     * cujas pastas de mensagens enviadas foram processadas. No modo demonstração, 
     * contém os remetentes criados manualmente.
     *
     * @return conjunto ordenado de endereços de e-mail de proprietários; nunca {@code null}.
     */
    public Set<String> getOwnerEmails() {
        return Collections.unmodifiableSet(ownerEmails);
    }

    // -------------------------------------------------------------------------
    // Mutação
    // -------------------------------------------------------------------------

    /**
     * Garante que um vértice com o e-mail fornecido exista no grafo, criando-o se 
     * necessário, e o retorna.
     *
     * @param email o endereço de e-mail; não deve ser {@code null} ou vazio.
     * @return o {@link Vertex} existente ou recém-criado.
     * @throws IllegalArgumentException se o {@code email} for {@code null} ou vazio.
     */
    public Vertex addVertex(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email must not be null or blank.");
        }
        String key = email.trim().toLowerCase();
        return vertexIndex.computeIfAbsent(key, k -> {
            Vertex v = new Vertex(k);
            adjacency.put(v, new HashMap<>());
            return v;
        });
    }

    /**
     * Adiciona uma aresta direcionada de {@code originEmail} para {@code destinationEmail}.
     *
     * <p>Se a aresta já existir, seu peso é incrementado em um. Se algum dos vértices 
     * ainda não existir, ele será criado automaticamente. Loops próprios 
     * (origem == destino) são ignorados silenciosamente, pois não carregam 
     * informações significativas para a rede de contatos.</p>
     *
     * @param originEmail      o endereço de e-mail do remetente.
     * @param destinationEmail o endereço de e-mail do destinatário.
     * @throws IllegalArgumentException se qualquer um dos endereços for {@code null} ou vazio.
     */
    public void addEdge(String originEmail, String destinationEmail) {
        if (originEmail == null || originEmail.isBlank()) {
            throw new IllegalArgumentException("Origin email must not be null or blank.");
        }
        if (destinationEmail == null || destinationEmail.isBlank()) {
            throw new IllegalArgumentException("Destination email must not be null or blank.");
        }

        String normOrigin = originEmail.trim().toLowerCase();
        String normDest   = destinationEmail.trim().toLowerCase();

        // Ignorar loops próprios (self-loops)
        if (normOrigin.equals(normDest)) return;

        Vertex origin      = addVertex(normOrigin);
        Vertex destination = addVertex(normDest);

        Map<Vertex, Edge> outEdges = adjacency.get(origin);
        Edge existing = outEdges.get(destination);
        if (existing != null) {
            existing.incrementWeight();
        } else {
            outEdges.put(destination, new Edge(origin, destination));
        }
    }

    // -------------------------------------------------------------------------
    // Consultas
    // -------------------------------------------------------------------------

    /**
     * Procura um vértice pelo seu endereço de e-mail.
     *
     * @param email o e-mail normalizado ou bruto a ser pesquisado.
     * @return um {@link Optional} contendo o vértice, ou vazio se não for encontrado.
     */
    public Optional<Vertex> findVertex(String email) {
        if (email == null || email.isBlank()) return Optional.empty();
        return Optional.ofNullable(vertexIndex.get(email.trim().toLowerCase()));
    }

    /**
     * Retorna {@code true} se um vértice com o e-mail fornecido existir no grafo.
     *
     * @param email o e-mail para verificar.
     * @return {@code true} se encontrado.
     */
    public boolean containsVertex(String email) {
        if (email == null || email.isBlank()) return false;
        return vertexIndex.containsKey(email.trim().toLowerCase());
    }

    /**
     * Retorna uma visão imutável de todos os vértices do grafo.
     *
     * @return coleção de todos os vértices; nunca {@code null}.
     */
    public Collection<Vertex> getVertices() {
        return Collections.unmodifiableCollection(vertexIndex.values());
    }

    /**
     * Retorna uma lista imutável de todas as arestas do grafo.
     *
     * <p>Esta operação é O(V + E), pois achata o mapa de adjacência.</p>
     *
     * @return lista de todas as arestas direcionadas; nunca {@code null}.
     */
    public List<Edge> getEdges() {
        List<Edge> all = new ArrayList<>();
        for (Map<Vertex, Edge> outMap : adjacency.values()) {
            all.addAll(outMap.values());
        }
        return Collections.unmodifiableList(all);
    }

    /**
     * Retorna as arestas de saída do vértice fornecido.
     *
     * @param vertex o vértice de origem; não deve ser {@code null}.
     * @return lista imutável de arestas de saída; vazia se o vértice não tiver nenhuma.
     * @throws IllegalArgumentException se o {@code vertex} for {@code null}.
     */
    public List<Edge> getOutEdges(Vertex vertex) {
        if (vertex == null) throw new IllegalArgumentException("Vertex must not be null.");
        Map<Vertex, Edge> outMap = adjacency.get(vertex);
        if (outMap == null) return Collections.emptyList();
        return Collections.unmodifiableList(new ArrayList<>(outMap.values()));
    }

    /**
     * Retorna as arestas de entrada para o vértice fornecido.
     *
     * <p>Como o mapa de adjacência é indexado pela origem, o cálculo das arestas 
     * de entrada exige uma varredura completa de todos os vértices — O(V + E). 
     * Isso é aceitável para as consultas dos top-20 que rodam uma única vez na inicialização.</p>
     *
     * @param vertex o vértice de destino; não deve ser {@code null}.
     * @return lista imutável de arestas de entrada; vazia se não houver nenhuma.
     * @throws IllegalArgumentException se o {@code vertex} for {@code null}.
     */
    public List<Edge> getInEdges(Vertex vertex) {
        if (vertex == null) throw new IllegalArgumentException("Vertex must not be null.");
        List<Edge> result = new ArrayList<>();
        for (Map<Vertex, Edge> outMap : adjacency.values()) {
            Edge e = outMap.get(vertex);
            if (e != null) result.add(e);
        }
        return Collections.unmodifiableList(result);
    }

    /**
     * Retorna o número total de vértices no grafo.
     *
     * @return contagem de vértices.
     */
    public int vertexCount() {
        return vertexIndex.size();
    }

    /**
     * Retorna o número total de arestas direcionadas no grafo.
     *
     * <p>Nota: isto conta arestas distintas, não a soma de seus pesos.</p>
     *
     * @return contagem de arestas.
     */
    public int edgeCount() {
        int count = 0;
        for (Map<Vertex, Edge> outMap : adjacency.values()) {
            count += outMap.size();
        }
        return count;
    }

    /**
     * Retorna o grau de saída de um vértice: o número de destinatários distintos 
     * para os quais ele enviou pelo menos uma mensagem.
     *
     * @param vertex o vértice a ser consultado; não deve ser {@code null}.
     * @return grau de saída, ou 0 se o vértice não tiver arestas de saída.
     * @throws IllegalArgumentException se o {@code vertex} for {@code null}.
     */
    public int outDegree(Vertex vertex) {
        if (vertex == null) throw new IllegalArgumentException("Vertex must not be null.");
        Map<Vertex, Edge> outMap = adjacency.get(vertex);
        return outMap == null ? 0 : outMap.size();
    }

    /**
     * Retorna o grau de entrada de um vértice: o número de remetentes distintos 
     * que enviaram pelo menos uma mensagem para este vértice.
     *
     * @param vertex o vértice a ser consultado; não deve ser {@code null}.
     * @return grau de entrada, ou 0 se ninguém tiver enviado para este vértice.
     * @throws IllegalArgumentException se o {@code vertex} for {@code null}.
     */
    public int inDegree(Vertex vertex) {
        if (vertex == null) throw new IllegalArgumentException("Vertex must not be null.");
        int count = 0;
        for (Map<Vertex, Edge> outMap : adjacency.values()) {
            if (outMap.containsKey(vertex)) count++;
        }
        return count;
    }
}
