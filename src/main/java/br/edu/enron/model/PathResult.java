package br.edu.enron.model;

import java.util.List;
import java.util.StringJoiner;

/**
 * Represents a path found by a graph traversal algorithm (DFS, BFS, or Dijkstra).
 *
 * <p>This class is shared across all three use cases:</p>
 * <ul>
 *   <li><strong>DFS / BFS</strong> — the {@code totalCost} field is {@code 0.0}
 *       because those algorithms only check reachability, not weighted cost.</li>
 *   <li><strong>Critical path (adapted Dijkstra)</strong> — {@code totalCost} holds
 *       the sum of inverse edge weights along the path; the caller also computes and
 *       stores the accumulated dependency (sum of original weights) separately via
 *       {@link #getAccumulatedDependency()}.</li>
 * </ul>
 *
 * <p>The vertex list is always returned as an unmodifiable copy to prevent external
 * code from mutating a result after it has been produced.</p>
 */
public final class PathResult {

    /** Ordered sequence of vertices from source to target. Empty when no path exists. */
    private final List<Vertex> vertices;

    /**
     * Sum of edge costs along the path. For DFS/BFS this is {@code 0.0}.
     * For the critical path it is the sum of {@code 1.0 / weight} values.
     */
    private final double totalCost;

    /**
     * Sum of original edge weights along the path.
     * Only meaningful for the critical-path result; {@code 0.0} for DFS/BFS.
     */
    private final double accumulatedDependency;

    /**
     * Constructs a {@code PathResult} for DFS or BFS (no cost semantics).
     *
     * @param vertices ordered list of vertices on the path; must not be {@code null}.
     */
    public PathResult(List<Vertex> vertices) {
        this(vertices, 0.0, 0.0);
    }

    /**
     * Constructs a {@code PathResult} for the critical-path algorithm.
     *
     * @param vertices              ordered vertex list; must not be {@code null}.
     * @param totalCost             sum of inverse-weight costs along the path.
     * @param accumulatedDependency sum of original weights along the path.
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
     * Returns the ordered list of vertices forming the path.
     *
     * @return unmodifiable vertex list; empty if no path was found.
     */
    public List<Vertex> getVertices() {
        return vertices;
    }

    /**
     * Returns the total inverse-weight cost of the path, meaningful only for
     * the adapted Dijkstra result.
     *
     * @return total cost; {@code 0.0} for DFS/BFS results.
     */
    public double getTotalCost() {
        return totalCost;
    }

    /**
     * Returns the accumulated dependency, defined as the sum of the original edge
     * weights along the critical path. Only meaningful for the Dijkstra result.
     *
     * @return sum of original weights; {@code 0.0} for DFS/BFS results.
     */
    public double getAccumulatedDependency() {
        return accumulatedDependency;
    }

    /**
     * Returns {@code true} if a path was actually found (vertex list is non-empty).
     *
     * @return {@code false} when no path exists between the queried vertices.
     */
    public boolean exists() {
        return !vertices.isEmpty();
    }

    /**
     * Returns a human-readable representation of the path as a chain of email
     * addresses separated by {@code ->}.
     *
     * @return formatted path string, or {@code "(no path)"} if empty.
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
