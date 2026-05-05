package br.edu.enron.service;

import br.edu.enron.graph.ContactGraph;
import br.edu.enron.model.Edge;
import br.edu.enron.model.PathResult;
import br.edu.enron.model.Vertex;

import java.util.*;

/**
 * Computes the approximate critical path of information flow between two
 * individuals using an adapted Dijkstra algorithm.
 *
 * <h2>Motivation</h2>
 * <p>In the contact graph, a high edge weight means the two individuals
 * communicated frequently — a strong dependency. The "critical path" is defined
 * here as the path that maximises accumulated communication dependency between
 * the source and the target.</p>
 *
 * <h2>Dijkstra adaptation</h2>
 * <p>Standard Dijkstra minimises cost. To maximise dependency we apply the
 * transformation:
 * <pre>
 *     cost(edge) = 1.0 / weight
 * </pre>
 * A high-weight (strong) edge becomes a low-cost edge. Dijkstra therefore
 * naturally selects the path whose edges have the highest total weight — the
 * most communication-dense route.</p>
 *
 * <p>This is an <em>approximation</em> of the true critical path because the
 * inverse-cost optimisation is not equivalent to directly maximising the sum of
 * weights (which would require a longest-path algorithm, an NP-hard problem for
 * general graphs). The approximation is nevertheless well-motivated and
 * academically appropriate for this project.</p>
 *
 * <h2>Output</h2>
 * <ul>
 *   <li>The sequence of vertices on the path.</li>
 *   <li>The total inverse cost (internal Dijkstra metric).</li>
 *   <li>The accumulated dependency: the sum of the original weights of the edges
 *       on the chosen path, presented as the "strength" of the information flow.</li>
 * </ul>
 *
 * <h2>Cycle safety</h2>
 * <p>Dijkstra inherently avoids revisiting vertices: once a vertex is settled
 * (extracted from the priority queue with the minimum known distance), it is never
 * relaxed again. A settled set enforces this guarantee.</p>
 */
public class CriticalPathService {

    /**
     * Computes the critical (maximum-dependency) path from {@code originEmail} to
     * {@code destinationEmail} using Dijkstra with inverse-weight costs.
     *
     * @param graph            the contact graph; must not be {@code null}.
     * @param originEmail      the source vertex's email; must not be {@code null} or blank.
     * @param destinationEmail the target vertex's email; must not be {@code null} or blank.
     * @return a {@link PathResult} containing the path, total inverse cost and
     *         accumulated dependency; empty if no path exists or a vertex is absent.
     * @throws IllegalArgumentException if any argument is {@code null} or blank.
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

        // dist[v] = minimum accumulated inverse cost from source to v
        Map<Vertex, Double> dist        = new HashMap<>();
        Map<Vertex, Vertex> predecessor = new HashMap<>();
        Set<Vertex>         settled     = new HashSet<>();

        // Priority queue ordered by accumulated inverse cost (ascending — lower is better)
        PriorityQueue<VertexEntry> pq = new PriorityQueue<>(Comparator.comparingDouble(e -> e.cost));

        // Initialise: all distances are infinity except the source
        for (Vertex v : graph.getVertices()) {
            dist.put(v, Double.MAX_VALUE);
        }
        dist.put(source, 0.0);
        predecessor.put(source, null);
        pq.offer(new VertexEntry(source, 0.0));

        while (!pq.isEmpty()) {
            VertexEntry entry   = pq.poll();
            Vertex      current = entry.vertex;

            // Skip if already settled (stale queue entry)
            if (!settled.add(current)) continue;

            // Early exit once the destination is settled
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

        if (!settled.contains(destination)) return new PathResult(List.of()); // unreachable

        List<Vertex> path             = reconstructPath(predecessor, source, destination);
        double       totalInverseCost = dist.get(destination);
        double       accDependency    = computeAccumulatedDependency(graph, path);

        return new PathResult(path, totalInverseCost, accDependency);
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /**
     * Reconstructs the ordered vertex path by tracing the predecessor map from
     * destination back to source.
     *
     * @param predecessor map from each vertex to the vertex it was reached from.
     * @param source      the origin vertex.
     * @param destination the settled destination.
     * @return ordered list from source to destination.
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
     * Computes the accumulated dependency of a path: the sum of the original
     * (non-inverted) weights of the edges along the path.
     *
     * @param graph the graph containing the edges.
     * @param path  the ordered list of vertices forming the path.
     * @return sum of original edge weights; {@code 0.0} for paths of length &lt; 2.
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
     * Validates method arguments.
     *
     * @param graph            the graph argument.
     * @param originEmail      the origin email argument.
     * @param destinationEmail the destination email argument.
     * @throws IllegalArgumentException on invalid input.
     */
    private void validateArgs(ContactGraph graph, String originEmail, String destinationEmail) {
        if (graph == null) throw new IllegalArgumentException("Graph must not be null.");
        if (originEmail == null || originEmail.isBlank())
            throw new IllegalArgumentException("Origin email must not be null or blank.");
        if (destinationEmail == null || destinationEmail.isBlank())
            throw new IllegalArgumentException("Destination email must not be null or blank.");
    }

    // -------------------------------------------------------------------------
    // Internal data class
    // -------------------------------------------------------------------------

    /**
     * Simple container pairing a vertex with its current accumulated cost,
     * used as entries in the priority queue.
     */
    private record VertexEntry(Vertex vertex, double cost) {}
}
