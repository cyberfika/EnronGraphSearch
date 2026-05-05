package br.edu.enron.service;

import br.edu.enron.graph.ContactGraph;
import br.edu.enron.model.Edge;
import br.edu.enron.model.PathResult;
import br.edu.enron.model.Vertex;

import java.util.*;

/**
 * Calcula o caminho crítico aproximado do fluxo de informações entre dois indivíduos 
 * usando um algoritmo de Dijkstra adaptado.
 *
 * <h2>Motivação</h2>
 * <p>No grafo de contatos, um peso de aresta alto significa que os dois indivíduos se 
 * comunicaram com frequência — uma dependência forte. O "caminho crítico" é definido aqui 
 * como o caminho que maximiza a dependência de comunicação acumulada entre a origem e o destino.</p>
 *
 * <h2>Adaptação do Dijkstra</h2>
 * <p>O Dijkstra padrão minimiza o custo. Para maximizar a dependência, aplicamos a transformação:
 * <pre>
 *     custo(aresta) = 1.0 / peso
 * </pre>
 * Uma aresta de peso alto (forte) torna-se uma aresta de custo baixo. Portanto, o Dijkstra seleciona 
 * naturalmente o caminho cujas arestas têm o maior peso total — a rota mais densa em comunicação.</p>
 *
 * <p>Esta é uma <em>aproximação</em> do verdadeiro caminho crítico porque a otimização do custo 
 * inverso não é equivalente a maximizar diretamente a soma dos pesos (o que exigiria um algoritmo 
 * de caminho mais longo, um problema NP-difícil para grafos em geral). No entanto, a aproximação 
 * é bem fundamentada e academicamente apropriada para este projeto.</p>
 *
 * <h2>Saída</h2>
 * <ul>
 *   <li>A sequência de vértices no caminho.</li>
 *   <li>O custo inverso total (métrica interna do Dijkstra).</li>
 *   <li>A dependência acumulada: a soma dos pesos originais das arestas no caminho escolhido, 
 *       apresentada como a "força" do fluxo de informações.</li>
 * </ul>
 *
 * <h2>Segurança contra ciclos</h2>
 * <p>O Dijkstra evita inerentemente revisitar vértices: uma vez que um vértice é "resolvido" 
 * (extraído da fila de prioridade com a distância mínima conhecida), ele nunca mais é relaxado. 
 * Um conjunto de resolvidos (settled set) reforça esta garantia.</p>
 */
public class CriticalPathService {

    /**
     * Calcula o caminho crítico (dependência máxima) de {@code originEmail} para
     * {@code destinationEmail} usando Dijkstra com custos de peso inverso.
     *
     * @param graph            o grafo de contatos; não deve ser {@code null}.
     * @param originEmail      o e-mail do vértice de origem; não deve ser {@code null} ou vazio.
     * @param destinationEmail o e-mail do vértice de destino; não deve ser {@code null} ou vazio.
     * @return um {@link PathResult} contendo o caminho, o custo inverso total e a 
     *         dependência acumulada; vazio se não existir caminho ou se um vértice estiver ausente.
     * @throws IllegalArgumentException se qualquer argumento for {@code null} ou vazio.
     */
    public PathResult computeCriticalPath(ContactGraph graph,
                                          String originEmail,
                                          String destinationEmail) {
        validateArgs(graph, originEmail, destinationEmail);

        Optional<Vertex> srcOpt = graph.findVertex(originEmail);
        Optional<Vertex> dstOpt = graph.findVertex(destinationEmail);
        if (srcOpt.isEmpty() || dstOpt.isEmpty()) return new PathResult(List.of());

        Vertex source      = srcOpt.get();
        Vertex destination = dstOpt.get();

        if (source.equals(destination)) return new PathResult(List.of(source), 0.0, 0.0);

        // dist[v] = custo inverso acumulado mínimo da origem até v
        Map<Vertex, Double> dist        = new HashMap<>();
        Map<Vertex, Vertex> predecessor = new HashMap<>();
        Set<Vertex>         settled     = new HashSet<>();

        // Fila de prioridade ordenada pelo custo inverso acumulado (ascendente — menor é melhor)
        PriorityQueue<VertexEntry> pq = new PriorityQueue<>(Comparator.comparingDouble(e -> e.cost));

        // Inicializar: todas as distâncias são infinito exceto a origem
        for (Vertex v : graph.getVertices()) {
            dist.put(v, Double.MAX_VALUE);
        }
        dist.put(source, 0.0);
        predecessor.put(source, null);
        pq.offer(new VertexEntry(source, 0.0));

        while (!pq.isEmpty()) {
            VertexEntry entry   = pq.poll();
            Vertex      current = entry.vertex;

            // Pular se já estiver resolvido (entrada obsoleta na fila)
            if (!settled.add(current)) continue;

            // Saída antecipada assim que o destino for resolvido
            if (current.equals(destination)) break;

            for (Edge edge : graph.getOutEdges(current)) {
                Vertex neighbour = edge.getDestination();
                if (settled.contains(neighbour)) continue;

                double newCost = dist.get(current) + edge.getInverseCost();
                if (newCost < dist.getOrDefault(neighbour, Double.MAX_VALUE)) {
                    dist.put(neighbour, newCost);
                    predecessor.put(neighbour, current);
                    pq.offer(new VertexEntry(neighbour, newCost));
                }
            }
        }

        if (!settled.contains(destination)) return new PathResult(List.of()); // inalcançável

        List<Vertex> path             = reconstructPath(predecessor, source, destination);
        double       totalInverseCost = dist.get(destination);
        double       accDependency    = computeAccumulatedDependency(graph, path);

        return new PathResult(path, totalInverseCost, accDependency);
    }

    // -------------------------------------------------------------------------
    // Auxiliares privados
    // -------------------------------------------------------------------------

    /**
     * Reconstrói o caminho ordenado de vértices rastreando o mapa de antecessores do 
     * destino de volta à origem.
     *
     * @param predecessor mapa de cada vértice para o vértice do qual ele foi alcançado.
     * @param source      o vértice de origem.
     * @param destination o destino resolvido.
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
     * Calcula a dependência acumulada de um caminho: a soma dos pesos originais 
     * (não invertidos) das arestas ao longo do caminho.
     *
     * @param graph o grafo contendo as arestas.
     * @param path  a lista ordenada de vértices que formam o caminho.
     * @return soma dos pesos originais das arestas; {@code 0.0} para caminhos com comprimento &lt; 2.
     */
    private double computeAccumulatedDependency(ContactGraph graph, List<Vertex> path) {
        double total = 0.0;
        for (int i = 0; i < path.size() - 1; i++) {
            Vertex from = path.get(i);
            Vertex to   = path.get(i + 1);
            for (Edge edge : graph.getOutEdges(from)) {
                if (edge.getDestination().equals(to)) {
                    total += edge.getWeight();
                    break;
                }
            }
        }
        return total;
    }

    /**
     * Valida os argumentos do método.
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

    // -------------------------------------------------------------------------
    // Classe de dados interna
    // -------------------------------------------------------------------------

    /**
     * Container simples que emparelha um vértice com seu custo acumulado atual, 
     * usado como entradas na fila de prioridade.
     */
    private record VertexEntry(Vertex vertex, double cost) {}
}
