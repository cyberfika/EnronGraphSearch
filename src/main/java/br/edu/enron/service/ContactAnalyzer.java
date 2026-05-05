package br.edu.enron.service;

import br.edu.enron.graph.ContactGraph;
import br.edu.enron.model.DegreeResult;
import br.edu.enron.model.Vertex;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Provides high-level analytical queries over a {@link ContactGraph}.
 *
 * <p>This service is the direct mapping to the graded evaluation criteria:</p>
 * <ul>
 *   <li>Vertex and edge counts (0.25 pt each).</li>
 *   <li>Top-20 out-degree and top-20 in-degree lists (0.25 pt each).</li>
 * </ul>
 *
 * <p>All ranking is performed with the natural ordering defined in
 * {@link DegreeResult}: descending degree, then ascending email for tie-breaking.</p>
 */
public class ContactAnalyzer {

    /** The graph on which all queries operate. */
    private final ContactGraph graph;

    /**
     * Constructs a {@code ContactAnalyzer} bound to the given graph.
     *
     * @param graph the contact graph; must not be {@code null}.
     * @throws IllegalArgumentException if {@code graph} is {@code null}.
     */
    public ContactAnalyzer(ContactGraph graph) {
        if (graph == null) throw new IllegalArgumentException("Graph must not be null.");
        this.graph = graph;
    }

    /**
     * Returns the total number of vertices (unique email addresses) in the graph.
     *
     * @return vertex count.
     */
    public int getVertexCount() {
        return graph.vertexCount();
    }

    /**
     * Returns the total number of directed edges (unique sender → recipient pairs)
     * in the graph.
     *
     * @return edge count.
     */
    public int getEdgeCount() {
        return graph.edgeCount();
    }

    /**
     * Returns the top 20 vertices ranked by out-degree (number of distinct people
     * they have sent at least one message to).
     *
     * <p>Ordering: descending degree; ties broken alphabetically by email.</p>
     *
     * @return unmodifiable list of at most 20 {@link DegreeResult} entries.
     */
    public List<DegreeResult> getTop20OutDegree() {
        return topN(20, true);
    }

    /**
     * Returns the top 20 vertices ranked by in-degree (number of distinct people
     * who have sent at least one message to them).
     *
     * <p>Ordering: descending degree; ties broken alphabetically by email.</p>
     *
     * @return unmodifiable list of at most 20 {@link DegreeResult} entries.
     */
    public List<DegreeResult> getTop20InDegree() {
        return topN(20, false);
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /**
     * Computes the top-N degree list for either direction.
     *
     * @param n      maximum number of entries to return.
     * @param outDir {@code true} for out-degree, {@code false} for in-degree.
     * @return sorted, trimmed list.
     */
    private List<DegreeResult> topN(int n, boolean outDir) {
        List<DegreeResult> results = new ArrayList<>();
        for (Vertex v : graph.getVertices()) {
            int degree = outDir ? graph.outDegree(v) : graph.inDegree(v);
            results.add(new DegreeResult(v.getEmail(), degree));
        }
        Collections.sort(results);
        return Collections.unmodifiableList(results.subList(0, Math.min(n, results.size())));
    }
}
