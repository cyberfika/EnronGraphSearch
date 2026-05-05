package br.edu.enron.service;

import br.edu.enron.graph.ContactGraph;
import br.edu.enron.model.Edge;
import br.edu.enron.model.Vertex;

import java.util.*;

/**
 * Returns all vertices that are located at <em>exactly</em> {@code D} directed
 * edges away from a given source vertex.
 *
 * <h2>Algorithm — BFS by levels</h2>
 * <p>A standard BFS is run from the source, but instead of stopping at the
 * destination it expands all vertices level by level. Each level corresponds to
 * one additional edge of distance. The algorithm stops as soon as it has fully
 * expanded level {@code D}; vertices at levels {@code 0 … D-1} are not included
 * in the result.</p>
 *
 * <ul>
 *   <li>Distance {@code 0} → only the source vertex itself.</li>
 *   <li>Distance {@code 1} → all direct out-neighbours of the source.</li>
 *   <li>Distance {@code D} → vertices reachable in exactly {@code D} steps.</li>
 * </ul>
 *
 * <h2>Cycle handling</h2>
 * <p>A visited set prevents any vertex from being enqueued more than once, so
 * cycles do not cause infinite loops and a vertex's distance is always the
 * shortest path distance from the source (BFS guarantee).</p>
 */
public class DistanceCalculator {

    /**
     * Returns all vertices reachable from {@code originEmail} in exactly
     * {@code distance} directed edges.
     *
     * <p>The result is sorted alphabetically by email address for deterministic
     * output and easy validation.</p>
     *
     * @param graph       the contact graph; must not be {@code null}.
     * @param originEmail the source vertex's email; must not be {@code null} or blank.
     * @param distance    the exact edge distance to query; must be &ge; 0.
     * @return sorted, unmodifiable list of vertices at exactly {@code distance} hops;
     *         empty if none exist or the source is absent.
     * @throws IllegalArgumentException if any argument is invalid.
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

        // BFS with level tracking
        // currentLevel holds all vertices at the current BFS depth
        Set<Vertex>   visited      = new HashSet<>();
        List<Vertex>  currentLevel = new ArrayList<>();

        visited.add(source);
        currentLevel.add(source);

        for (int level = 1; level <= distance; level++) {
            List<Vertex> nextLevel = new ArrayList<>();
            for (Vertex v : currentLevel) {
                for (Edge edge : graph.getOutEdges(v)) {
                    Vertex neighbour = edge.getDestination();
                    if (visited.add(neighbour)) { // add returns true if the element was new
                        nextLevel.add(neighbour);
                    }
                }
            }
            if (nextLevel.isEmpty()) return List.of(); // graph exhausted before reaching D
            currentLevel = nextLevel;
        }

        // currentLevel now contains exactly the vertices at distance D
        List<Vertex> result = new ArrayList<>(currentLevel);
        result.sort(Comparator.comparing(Vertex::getEmail));
        return Collections.unmodifiableList(result);
    }
}
