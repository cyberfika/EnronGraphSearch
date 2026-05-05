package br.edu.enron.service;

import br.edu.enron.graph.ContactGraph;
import br.edu.enron.model.Edge;
import br.edu.enron.model.PathResult;
import br.edu.enron.model.Vertex;

import java.util.*;

/**
 * Iterative depth-first search (DFS) over a {@link ContactGraph}.
 *
 * <h2>Algorithm</h2>
 * <p>The iterative approach uses an explicit {@link ArrayDeque} as a stack instead
 * of Java's call stack, which avoids {@code StackOverflowError} on large graphs
 * (the Enron dataset can have tens of thousands of vertices and hundreds of
 * thousands of edges).</p>
 *
 * <p>Steps:</p>
 * <ol>
 *   <li>Push the origin vertex onto the stack.</li>
 *   <li>Pop the top vertex. If already visited, skip it.</li>
 *   <li>Mark it as visited.</li>
 *   <li>If it equals the destination, reconstruct and return the path.</li>
 *   <li>Push all unvisited neighbours onto the stack, recording their predecessor.</li>
 *   <li>Repeat until the stack is empty or the destination is found.</li>
 * </ol>
 *
 * <h2>Cycle handling</h2>
 * <p>A {@code HashSet<Vertex>} of visited vertices ensures that no vertex is
 * processed more than once. Because a vertex is marked visited before its
 * neighbours are pushed, cycles cannot cause infinite loops.</p>
 */
public class DepthFirstSearch {

    /**
     * Searches for a path from {@code originEmail} to {@code destinationEmail}
     * using depth-first traversal.
     *
     * <p>The returned path is a sequence of vertices from origin to destination.
     * It is not necessarily the shortest path — DFS explores as deep as possible
     * before backtracking, so the route depends on adjacency order.</p>
     *
     * @param graph            the contact graph to search; must not be {@code null}.
     * @param originEmail      the starting vertex's email; must not be {@code null}.
     * @param destinationEmail the target vertex's email; must not be {@code null}.
     * @return a {@link PathResult} containing the path if found, or an empty result
     *         if no path exists or either vertex is absent from the graph.
     * @throws IllegalArgumentException if any argument is {@code null} or blank.
     */
    public PathResult search(ContactGraph graph, String originEmail, String destinationEmail) {
        validateArgs(graph, originEmail, destinationEmail);

        Optional<Vertex> srcOpt  = graph.findVertex(originEmail);
        Optional<Vertex> dstOpt  = graph.findVertex(destinationEmail);
        if (srcOpt.isEmpty() || dstOpt.isEmpty()) return new PathResult(List.of());

        Vertex source      = srcOpt.get();
        Vertex destination = dstOpt.get();

        if (source.equals(destination)) return new PathResult(List.of(source));

        // visited set prevents revisiting vertices in cyclic graphs
        Set<Vertex>        visited     = new HashSet<>();
        // predecessor map allows path reconstruction without storing the full path per stack entry
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

        return new PathResult(List.of()); // destination not reachable
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /**
     * Reconstructs the path from {@code source} to {@code destination} by
     * following the predecessor map backwards and reversing the result.
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
        // Safety check: if reconstruction does not reach the source something went wrong
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
