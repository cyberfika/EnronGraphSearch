package br.edu.enron.service;

import br.edu.enron.graph.ContactGraph;
import br.edu.enron.model.Edge;
import br.edu.enron.model.PathResult;
import br.edu.enron.model.Vertex;

import java.util.*;

/**
 * Breadth-first search (BFS) over a {@link ContactGraph}.
 *
 * <h2>Algorithm</h2>
 * <p>BFS uses a FIFO {@link ArrayDeque} as a queue and expands vertices level by
 * level (one edge at a time). This guarantees that the first time the destination
 * is reached, the path found has the minimum number of edges — the shortest
 * unweighted path between origin and destination.</p>
 *
 * <p>Steps:</p>
 * <ol>
 *   <li>Enqueue the origin vertex and mark it as visited.</li>
 *   <li>Dequeue the front vertex.</li>
 *   <li>If it equals the destination, reconstruct and return the path.</li>
 *   <li>Enqueue all unvisited out-neighbours and record their predecessor.</li>
 *   <li>Repeat until the queue is empty or the destination is found.</li>
 * </ol>
 *
 * <h2>Cycle handling</h2>
 * <p>A vertex is added to the visited set as soon as it is <em>enqueued</em> (not
 * when it is dequeued), so that the same vertex cannot be added to the queue more
 * than once even if multiple paths lead to it. This correctly prevents both cycles
 * and redundant processing.</p>
 *
 * <h2>Difference from DFS</h2>
 * <p>DFS uses a stack (LIFO) and may return a longer path. BFS uses a queue (FIFO)
 * and always returns the path with the fewest edges. Neither is better in general;
 * the choice depends on the use case.</p>
 */
public class BreadthFirstSearch {

    /**
     * Searches for the shortest (fewest-edge) path from {@code originEmail} to
     * {@code destinationEmail} using breadth-first traversal.
     *
     * @param graph            the contact graph to search; must not be {@code null}.
     * @param originEmail      the starting vertex's email; must not be {@code null}.
     * @param destinationEmail the target vertex's email; must not be {@code null}.
     * @return a {@link PathResult} with the minimum-edge path if found, or an empty
     *         result if no path exists or either vertex is absent from the graph.
     * @throws IllegalArgumentException if any argument is {@code null} or blank.
     */
    public PathResult search(ContactGraph graph, String originEmail, String destinationEmail) {
        validateArgs(graph, originEmail, destinationEmail);

        Optional<Vertex> srcOpt = graph.findVertex(originEmail);
        Optional<Vertex> dstOpt = graph.findVertex(destinationEmail);
        if (srcOpt.isEmpty() || dstOpt.isEmpty()) return new PathResult(List.of());

        Vertex source      = srcOpt.get();
        Vertex destination = dstOpt.get();

        if (source.equals(destination)) return new PathResult(List.of(source));

        // Visited set: vertices are marked when enqueued, not when dequeued.
        // This is crucial for BFS correctness in cyclic graphs.
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

        return new PathResult(List.of()); // destination not reachable
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /**
     * Reconstructs the path from {@code source} to {@code destination} using the
     * predecessor map filled during the BFS traversal.
     *
     * @param predecessor map from each vertex to the vertex it was reached from.
     * @param source      the origin vertex.
     * @param destination the target vertex.
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
     * Validates that none of the arguments is {@code null} or blank.
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
}
