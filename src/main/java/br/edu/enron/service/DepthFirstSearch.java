package br.edu.enron.service;

import br.edu.enron.graph.ContactGraph;
import br.edu.enron.model.Edge;
import br.edu.enron.model.PathResult;
import br.edu.enron.model.Vertex;

import java.util.*;

/**
 * Busca em profundidade (DFS) iterativa sobre um {@link ContactGraph}.
 *
 * <h2>Algoritmo</h2>
 * <p>A abordagem iterativa usa um {@link ArrayDeque} explícito como pilha em vez da pilha de 
 * chamadas do Java, o que evita o erro {@code StackOverflowError} em grafos grandes (o dataset 
 * da Enron pode ter dezenas de milhares de vértices e centenas de milhares de arestas).</p>
 *
 * <p>Passos:</p>
 * <ol>
 *   <li>Empilha o vértice de origem na pilha.</li>
 *   <li>Desempilha o vértice do topo. Se já foi visitado, pula.</li>
 *   <li>Marca-o como visitado.</li>
 *   <li>Se for igual ao destino, reconstrói e retorna o caminho.</li>
 *   <li>Empilha todos os vizinhos não visitados na pilha, registrando seu antecessor.</li>
 *   <li>Repete até que a pilha esteja vazia ou o destino seja encontrado.</li>
 * </ol>
 *
 * <h2>Tratamento de ciclos</h2>
 * <p>Um {@code HashSet<Vertex>} de vértices visitados garante que nenhum vértice seja processado 
 * mais de uma vez. Como um vértice é marcado como visitado antes de seus vizinhos serem empilhados, 
 * os ciclos não podem causar loops infinitos.</p>
 */
public class DepthFirstSearch {

    /**
     * Procura por um caminho de {@code originEmail} para {@code destinationEmail}
     * usando o percurso de busca em profundidade.
     *
     * <p>O caminho retornado é uma sequência de vértices da origem ao destino. 
     * Não é necessariamente o caminho mais curto — a DFS explora o mais profundo possível 
     * antes de retroceder (backtracking), portanto a rota depende da ordem de adjacência.</p>
     *
     * @param graph            o grafo de contatos para pesquisar; não deve ser {@code null}.
     * @param originEmail      o e-mail do vértice de partida; não deve ser {@code null}.
     * @param destinationEmail o e-mail do vértice de destino; não deve ser {@code null}.
     * @return um {@link PathResult} contendo o caminho se encontrado, ou um resultado vazio 
     *         se não existir caminho ou se qualquer um dos vértices estiver ausente no grafo.
     * @throws IllegalArgumentException se qualquer argumento for {@code null} ou vazio.
     */
    public PathResult search(ContactGraph graph, String originEmail, String destinationEmail) {
        validateArgs(graph, originEmail, destinationEmail);

        Optional<Vertex> srcOpt  = graph.findVertex(originEmail);
        Optional<Vertex> dstOpt  = graph.findVertex(destinationEmail);
        if (srcOpt.isEmpty() || dstOpt.isEmpty()) return new PathResult(List.of());

        Vertex source      = srcOpt.get();
        Vertex destination = dstOpt.get();

        if (source.equals(destination)) return new PathResult(List.of(source));

        // conjunto de visitados impede revisitar vértices em grafos cíclicos
        Set<Vertex>        visited     = new HashSet<>();
        // mapa de antecessores permite a reconstrução do caminho sem armazenar o caminho completo por entrada na pilha
        Map<Vertex, Vertex> predecessor = new HashMap<>();
        Deque<Vertex>      stack       = new ArrayDeque<>();

        stack.push(source);
        predecessor.put(source, null);

        while (!stack.isEmpty()) {
            Vertex current = stack.pop();

            if (visited.contains(current)) continue;
            visited.add(current);

            if (current.equals(destination)) {
                return new PathResult(reconstructPath(predecessor, source, destination));
            }

            for (Edge edge : graph.getOutEdges(current)) {
                Vertex neighbour = edge.getDestination();
                if (!visited.contains(neighbour)) {
                    if (!predecessor.containsKey(neighbour)) {
                        predecessor.put(neighbour, current);
                    }
                    stack.push(neighbour);
                }
            }
        }

        return new PathResult(List.of()); // destino não alcançável
    }

    // -------------------------------------------------------------------------
    // Auxiliares privados
    // -------------------------------------------------------------------------

    /**
     * Reconstrói o caminho de {@code source} para {@code destination} seguindo o 
     * mapa de antecessores de trás para frente e invertendo o resultado.
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
        // Verificação de segurança: se a reconstrução não atingir a origem, algo deu errado
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
