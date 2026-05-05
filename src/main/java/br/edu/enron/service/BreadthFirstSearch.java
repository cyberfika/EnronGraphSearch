package br.edu.enron.service;

import br.edu.enron.graph.ContactGraph;
import br.edu.enron.model.Edge;
import br.edu.enron.model.PathResult;
import br.edu.enron.model.Vertex;

import java.util.*;

/**
 * Busca em largura (BFS) sobre um {@link ContactGraph}.
 *
 * <h2>Algoritmo</h2>
 * <p>A BFS usa uma fila FIFO {@link ArrayDeque} e expande os vértices nível por nível 
 * (uma aresta de cada vez). Isso garante que a primeira vez que o destino for alcançado, 
 * o caminho encontrado terá o número mínimo de arestas — o caminho mais curto não ponderado 
 * entre a origem e o destino.</p>
 *
 * <p>Passos:</p>
 * <ol>
 *   <li>Enfileira o vértice de origem e marca-o como visitado.</li>
 *   <li>Desenfileira o vértice da frente.</li>
 *   <li>Se for igual ao destino, reconstrói e retorna o caminho.</li>
 *   <li>Enfileira todos os vizinhos de saída não visitados e registra seu antecessor.</li>
 *   <li>Repete até que a fila esteja vazia ou o destino seja encontrado.</li>
 * </ol>
 *
 * <h2>Tratamento de ciclos</h2>
 * <p>Um vértice é adicionado ao conjunto de visitados assim que é <em>enfileirado</em> 
 * (não quando é desenfileirado), para que o mesmo vértice não possa ser adicionado à fila 
 * mais de uma vez, mesmo que múltiplos caminhos levem a ele. Isso evita corretamente tanto 
 * ciclos quanto processamento redundante.</p>
 *
 * <h2>Diferença da DFS</h2>
 * <p>A DFS usa uma pilha (LIFO) e pode retornar um caminho mais longo. A BFS usa uma fila 
 * (FIFO) e sempre retorna o caminho com o menor número de arestas. Nenhuma é melhor em geral; 
 * a escolha depende do caso de uso.</p>
 */
public class BreadthFirstSearch {

    /**
     * Procura pelo caminho mais curto (com menos arestas) de {@code originEmail} para
     * {@code destinationEmail} usando o percurso de busca em largura.
     *
     * @param graph            o grafo de contatos para pesquisar; não deve ser {@code null}.
     * @param originEmail      o e-mail do vértice de partida; não deve ser {@code null}.
     * @param destinationEmail o e-mail do vértice de destino; não deve ser {@code null}.
     * @return um {@link PathResult} com o caminho de arestas mínimas se encontrado, ou um resultado 
     *         vazio se não existir caminho ou se qualquer um dos vértices estiver ausente no grafo.
     * @throws IllegalArgumentException se qualquer argumento for {@code null} ou vazio.
     */
    public PathResult search(ContactGraph graph, String originEmail, String destinationEmail) {
        validateArgs(graph, originEmail, destinationEmail);

        Optional<Vertex> srcOpt = graph.findVertex(originEmail);
        Optional<Vertex> dstOpt = graph.findVertex(destinationEmail);
        if (srcOpt.isEmpty() || dstOpt.isEmpty()) return new PathResult(List.of());

        Vertex source      = srcOpt.get();
        Vertex destination = dstOpt.get();

        if (source.equals(destination)) return new PathResult(List.of(source));

        // Conjunto de visitados: os vértices são marcados quando enfileirados, não quando desenfileirados.
        // Isso é crucial para a correção da BFS em grafos cíclicos.
        Set<Vertex>         visited     = new HashSet<>();
        Map<Vertex, Vertex> predecessor = new HashMap<>();
        Deque<Vertex>       queue       = new ArrayDeque<>();

        queue.add(source);
        visited.add(source);
        predecessor.put(source, null);

        while (!queue.isEmpty()) {
            Vertex current = queue.poll();

            if (current.equals(destination)) {
                return new PathResult(reconstructPath(predecessor, source, destination));
            }

            for (Edge edge : graph.getOutEdges(current)) {
                Vertex neighbour = edge.getDestination();
                if (!visited.contains(neighbour)) {
                    visited.add(neighbour);
                    predecessor.put(neighbour, current);
                    queue.add(neighbour);
                }
            }
        }

        return new PathResult(List.of()); // destino não alcançável
    }

    // -------------------------------------------------------------------------
    // Auxiliares privados
    // -------------------------------------------------------------------------

    /**
     * Reconstrói o caminho de {@code source} para {@code destination} usando o 
     * mapa de antecessores preenchido durante o percurso BFS.
     *
     * @param predecessor mapa de cada vértice para o vértice do qual ele foi alcançado.
     * @param source      o vértice de origem.
     * @param destination o vértice de destino.
     * @return lista ordenada da origem ao destino.
     */
    private List<Vertex> reconstructPath(Map<Vertex, Vertex> predecessor,
                                          Vertex source, Vertex destination) {
        LinkedList<Vertex> path = new LinkedList<>();
        Vertex current = destination;
        while (current != null) {
            path.addFirst(current);
            current = predecessor.get(current);
        }
        if (path.isEmpty() || !path.getFirst().equals(source)) return List.of();
        return path;
    }

    /**
     * Valida que nenhum dos argumentos seja {@code null} ou vazio.
     *
     * @param graph            o argumento do grafo.
     * @param originEmail      o argumento do e-mail de origem.
     * @param destinationEmail o argumento do e-mail de destino.
     * @throws IllegalArgumentException em caso de entrada inválida.
     */
    private void validateArgs(ContactGraph graph, String originEmail, String destinationEmail) {
        if (graph == null) throw new IllegalArgumentException("Graph must not be null.");
        if (originEmail == null || originEmail.isBlank())
            throw new IllegalArgumentException("Origin email must not be null or blank.");
        if (destinationEmail == null || destinationEmail.isBlank())
            throw new IllegalArgumentException("Destination email must not be null or blank.");
    }
}
