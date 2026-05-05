package br.edu.enron.model;

import java.util.List;
import java.util.StringJoiner;

/**
 * Representa um caminho encontrado por um algoritmo de percurso de grafo (DFS, BFS ou Dijkstra).
 *
 * <p>Esta classe é compartilhada pelos três casos de uso:</p>
 * <ul>
 *   <li><strong>DFS / BFS</strong> — o campo {@code totalCost} é {@code 0.0} 
 *       porque esses algoritmos verificam apenas a alcançabilidade, não o custo ponderado.</li>
 *   <li><strong>Caminho crítico (Dijkstra adaptado)</strong> — {@code totalCost} contém 
 *       a soma dos pesos inversos das arestas ao longo do caminho; o chamador também calcula 
 *       e armazena a dependência acumulada (soma dos pesos originais) separadamente via 
 *       {@link #getAccumulatedDependency()}.</li>
 * </ul>
 *
 * <p>A lista de vértices é sempre retornada como uma cópia imutável para evitar que 
 * código externo altere um resultado após ele ter sido produzido.</p>
 */
public final class PathResult {

    /** Sequência ordenada de vértices da origem ao destino. Vazia quando não existe caminho. */
    private final List<Vertex> vertices;

    /**
     * Soma dos custos das arestas ao longo do caminho. Para DFS/BFS, isso é {@code 0.0}.
     * Para o caminho crítico, é a soma dos valores {@code 1.0 / peso}.
     */
    private final double totalCost;

    /**
     * Soma dos pesos originais das arestas ao longo do caminho.
     * Relevante apenas para o resultado do caminho crítico; {@code 0.0} para DFS/BFS.
     */
    private final double accumulatedDependency;

    /**
     * Constrói um {@code PathResult} para DFS ou BFS (sem semântica de custo).
     *
     * @param vertices lista ordenada de vértices no caminho; não deve ser {@code null}.
     */
    public PathResult(List<Vertex> vertices) {
        this(vertices, 0.0, 0.0);
    }

    /**
     * Constrói um {@code PathResult} para o algoritmo de caminho crítico.
     *
     * @param vertices              lista de vértices ordenada; não deve ser {@code null}.
     * @param totalCost             soma dos custos de peso inverso ao longo do caminho.
     * @param accumulatedDependency soma dos pesos originais ao longo do caminho.
     */
    public PathResult(List<Vertex> vertices, double totalCost, double accumulatedDependency) {
        if (vertices == null) {
            throw new IllegalArgumentException("Vertex list must not be null.");
        }
        this.vertices = List.copyOf(vertices);
        this.totalCost = totalCost;
        this.accumulatedDependency = accumulatedDependency;
    }

    /**
     * Retorna a lista ordenada de vértices que formam o caminho.
     *
     * @return lista de vértices imutável; vazia se nenhum caminho foi encontrado.
     */
    public List<Vertex> getVertices() {
        return vertices;
    }

    /**
     * Retorna o custo total de peso inverso do caminho, significativo apenas para 
     * o resultado do Dijkstra adaptado.
     *
     * @return custo total; {@code 0.0} para resultados DFS/BFS.
     */
    public double getTotalCost() {
        return totalCost;
    }

    /**
     * Retorna a dependência acumulada, definida como a soma dos pesos originais das arestas 
     * ao longo do caminho crítico. Relevante apenas para o resultado do Dijkstra.
     *
     * @return soma dos pesos originais; {@code 0.0} para resultados DFS/BFS.
     */
    public double getAccumulatedDependency() {
        return accumulatedDependency;
    }

    /**
     * Retorna {@code true} se um caminho foi realmente encontrado (a lista de vértices não está vazia).
     *
     * @return {@code false} quando não existe caminho entre os vértices consultados.
     */
    public boolean exists() {
        return !vertices.isEmpty();
    }

    /**
     * Retorna uma representação legível do caminho como uma cadeia de endereços 
     * de e-mail separados por {@code ->}.
     *
     * @return string do caminho formatada, ou {@code "(no path)"} se estiver vazia.
     */
    @Override
    public String toString() {
        if (!exists()) return "(no path)";
        StringJoiner sj = new StringJoiner(" -> ");
        for (Vertex v : vertices) {
            sj.add(v.getEmail());
        }
        return sj.toString();
    }
}
