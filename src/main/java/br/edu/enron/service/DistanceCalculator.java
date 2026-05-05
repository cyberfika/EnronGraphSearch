package br.edu.enron.service;

import br.edu.enron.graph.ContactGraph;
import br.edu.enron.model.Edge;
import br.edu.enron.model.Vertex;

import java.util.*;

/**
 * Retorna todos os vértices que estão localizados a <em>exatamente</em> {@code D} arestas 
 * direcionadas de distância de um vértice de origem fornecido.
 *
 * <h2>Algoritmo — BFS por níveis</h2>
 * <p>Uma busca em largura (BFS) padrão é executada a partir da origem, mas em vez de parar no
 * destino, ela expande todos os vértices nível por nível. Cada nível corresponde a
 * uma aresta adicional de distância. O algoritmo para assim que expandir totalmente o
 * nível {@code D}; vértices nos níveis {@code 0 … D-1} não são incluídos no resultado.</p>
 *
 * <ul>
 *   <li>Distância {@code 0} → apenas o próprio vértice de origem.</li>
 *   <li>Distância {@code 1} → todos os vizinhos diretos (saída) da origem.</li>
 *   <li>Distância {@code D} → vértices alcançáveis em exatamente {@code D} passos.</li>
 * </ul>
 *
 * <h2>Tratamento de ciclos</h2>
 * <p>Um conjunto de visitados impede que qualquer vértice seja enfileirado mais de uma vez, 
 * portanto, ciclos não causam loops infinitos e a distância de um vértice é sempre a 
 * distância do caminho mais curto a partir da origem (garantia da BFS).</p>
 */
public class DistanceCalculator {

    /**
     * Retorna todos os vértices alcançáveis de {@code originEmail} em exatamente
     * {@code distance} arestas direcionadas.
     *
     * <p>O resultado é ordenado alfabeticamente pelo endereço de e-mail para saída determinística
     * e fácil validação.</p>
     *
     * @param graph       o grafo de contatos; não deve ser {@code null}.
     * @param originEmail o e-mail do vértice de origem; não deve ser {@code null} ou vazio.
     * @param distance    a distância exata de arestas para consulta; deve ser &ge; 0.
     * @return lista ordenada e imutável de vértices a exatamente {@code distance} saltos;
     *         vazia se não existir nenhum ou se a origem estiver ausente.
     * @throws IllegalArgumentException se qualquer argumento for inválido.
     */
    public List<Vertex> getVerticesAtDistance(ContactGraph graph,
                                               String originEmail,
                                               int distance) {
        if (graph == null)
            throw new IllegalArgumentException("Graph must not be null.");
        if (originEmail == null || originEmail.isBlank())
            throw new IllegalArgumentException("Origin email must not be null or blank.");
        if (distance < 0)
            throw new IllegalArgumentException("Distance must be non-negative.");

        Optional<Vertex> srcOpt = graph.findVertex(originEmail);
        if (srcOpt.isEmpty()) return List.of();

        Vertex source = srcOpt.get();

        if (distance == 0) return List.of(source);

        // BFS com rastreamento de nível
        // currentLevel contém todos os vértices na profundidade BFS atual
        Set<Vertex>   visited      = new HashSet<>();
        List<Vertex>  currentLevel = new ArrayList<>();

        visited.add(source);
        currentLevel.add(source);

        for (int level = 1; level <= distance; level++) {
            List<Vertex> nextLevel = new ArrayList<>();
            for (Vertex v : currentLevel) {
                for (Edge edge : graph.getOutEdges(v)) {
                    Vertex neighbour = edge.getDestination();
                    if (visited.add(neighbour)) { // add retorna true se o elemento for novo
                        nextLevel.add(neighbour);
                    }
                }
            }
            if (nextLevel.isEmpty()) return List.of(); // grafo esgotado antes de atingir D
            currentLevel = nextLevel;
        }

        // currentLevel agora contém exatamente os vértices à distância D
        List<Vertex> result = new ArrayList<>(currentLevel);
        result.sort(Comparator.comparing(Vertex::getEmail));
        return Collections.unmodifiableList(result);
    }
}
