package br.edu.enron.view;

import br.edu.enron.graph.ContactGraph;
import br.edu.enron.model.Edge;
import br.edu.enron.model.PathResult;
import br.edu.enron.model.Vertex;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.ui.view.Viewer;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Renders the {@link ContactGraph} and highlights traversal results using
 * the GraphStream library.
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *   <li>Convert the internal {@link ContactGraph} into a GraphStream
 *       {@link org.graphstream.graph.Graph} for rendering.</li>
 *   <li>Apply CSS-based styles: default node/edge appearance, highlighted path
 *       nodes and edges (for DFS, BFS, critical-path results).</li>
 *   <li>Display edge weights as labels.</li>
 * </ul>
 *
 * <p>This class is intentionally decoupled from all business logic.
 * It receives a fully-built {@link ContactGraph} and optional {@link PathResult}
 * objects from the services layer, and only concerns itself with rendering.</p>
 *
 * <h2>Performance note</h2>
 * <p>The full Enron graph has tens of thousands of vertices and edges; rendering
 * it all at once would be unusable. When the graph exceeds
 * {@link #MAX_NODES_FOR_FULL_RENDER} nodes, only the subgraph formed by the
 * top-100 out-degree vertices and their direct connections is displayed, with a
 * warning printed to the console.</p>
 */
public class GraphVisualizer {

    /**
     * Maximum number of nodes rendered without subgraph sampling.
     * Graphs larger than this threshold are trimmed to keep the UI responsive.
     */
    private static final int MAX_NODES_FOR_FULL_RENDER = 500;

    /** GraphStream stylesheet applied to all rendered graphs. */
    private static final String STYLESHEET =
            "node {" +
            "  fill-color: #4A90D9;" +
            "  size: 12px;" +
            "  text-size: 10;" +
            "  text-color: #222222;" +
            "  text-style: bold;" +
            "  text-background-mode: rounded-box;" +
            "  text-background-color: rgba(255,255,255,180);" +
            "  text-padding: 2px;" +
            "  stroke-mode: plain;" +
            "  stroke-color: #1A5276;" +
            "  stroke-width: 1px;" +
            "}" +
            "node.highlighted {" +
            "  fill-color: #E74C3C;" +
            "  size: 18px;" +
            "  stroke-color: #922B21;" +
            "  stroke-width: 2px;" +
            "}" +
            "node.endpoint {" +
            "  fill-color: #27AE60;" +
            "  size: 22px;" +
            "  stroke-color: #1E8449;" +
            "  stroke-width: 2px;" +
            "}" +
            "edge {" +
            "  fill-color: #AAB7B8;" +
            "  arrow-size: 8px, 4px;" +
            "  text-size: 9;" +
            "  text-color: #555555;" +
            "}" +
            "edge.highlighted {" +
            "  fill-color: #E74C3C;" +
            "  size: 3px;" +
            "  arrow-size: 10px, 5px;" +
            "}";

    /**
     * Renders the given {@link ContactGraph} in a GraphStream window.
     *
     * <p>If the graph has more than {@link #MAX_NODES_FOR_FULL_RENDER} nodes,
     * only a representative subgraph (top-100 out-degree vertices and their
     * direct neighbours) is displayed.</p>
     *
     * @param contactGraph the graph to visualize; must not be {@code null}.
     * @param title        window title string; must not be {@code null}.
     */
    public void display(ContactGraph contactGraph, String title) {
        if (contactGraph == null) throw new IllegalArgumentException("ContactGraph must not be null.");

        System.setProperty("org.graphstream.ui", "swing");
        Graph gsGraph = buildGsGraph(contactGraph, title);
        gsGraph.setAttribute("ui.stylesheet", STYLESHEET);
        gsGraph.setAttribute("ui.quality");
        gsGraph.setAttribute("ui.antialias");

        Viewer viewer = gsGraph.display();
        viewer.setCloseFramePolicy(Viewer.CloseFramePolicy.HIDE_ONLY);
    }

    /**
     * Renders the graph and highlights a traversal result path.
     *
     * <p>Source and destination nodes are coloured green (CSS class
     * {@code endpoint}). Intermediate nodes on the path are coloured red
     * ({@code highlighted}). Edges along the path are also highlighted.</p>
     *
     * @param contactGraph the full contact graph; must not be {@code null}.
     * @param pathResult   the path to highlight; must not be {@code null}.
     * @param title        window title string.
     * @param label        descriptive label shown in the console alongside the path.
     */
    public void displayWithPath(ContactGraph contactGraph,
                                 PathResult pathResult,
                                 String title,
                                 String label) {
        if (contactGraph == null) throw new IllegalArgumentException("ContactGraph must not be null.");
        if (pathResult == null)   throw new IllegalArgumentException("PathResult must not be null.");

        System.setProperty("org.graphstream.ui", "swing");

        // Build subgraph: include path vertices + neighbours to provide context
        Set<Vertex> subgraphSeeds = new HashSet<>(pathResult.getVertices());
        for (Vertex v : pathResult.getVertices()) {
            for (Edge e : contactGraph.getOutEdges(v)) subgraphSeeds.add(e.getDestination());
        }

        ContactGraph subgraph = buildSubgraph(contactGraph, subgraphSeeds);
        Graph gsGraph = buildGsGraph(subgraph, title);
        gsGraph.setAttribute("ui.stylesheet", STYLESHEET);
        gsGraph.setAttribute("ui.quality");
        gsGraph.setAttribute("ui.antialias");

        highlightPath(gsGraph, pathResult);

        System.out.println("[" + label + "] " + pathResult);

        Viewer viewer = gsGraph.display();
        viewer.setCloseFramePolicy(Viewer.CloseFramePolicy.HIDE_ONLY);
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /**
     * Converts a {@link ContactGraph} into a GraphStream {@link Graph}.
     * If the vertex count exceeds {@link #MAX_NODES_FOR_FULL_RENDER}, a sampled
     * subgraph of the highest-degree nodes is used instead.
     *
     * @param contactGraph the source graph.
     * @param id           the GraphStream graph identifier / window title.
     * @return populated GraphStream graph.
     */
    private Graph buildGsGraph(ContactGraph contactGraph, String id) {
        ContactGraph effective = contactGraph;

        if (contactGraph.vertexCount() > MAX_NODES_FOR_FULL_RENDER) {
            System.out.println("[Visualizer] Graph has " + contactGraph.vertexCount()
                    + " nodes — rendering top-" + MAX_NODES_FOR_FULL_RENDER + " subgraph.");
            Set<Vertex> top = topNByOutDegree(contactGraph, MAX_NODES_FOR_FULL_RENDER);
            effective = buildSubgraph(contactGraph, top);
        }

        Graph gsGraph = new MultiGraph(id);

        for (Vertex v : effective.getVertices()) {
            org.graphstream.graph.Node node = gsGraph.addNode(v.getEmail());
            node.setAttribute("ui.label", v.getEmail());
        }

        int edgeId = 0;
        for (Edge e : effective.getEdges()) {
            String eid = "e" + edgeId++;
            try {
                org.graphstream.graph.Edge gsEdge = gsGraph.addEdge(
                        eid,
                        e.getOrigin().getEmail(),
                        e.getDestination().getEmail(),
                        true   // directed
                );
                gsEdge.setAttribute("ui.label", String.valueOf(e.getWeight()));
            } catch (Exception ignored) {
                // MultiDiGraph allows parallel edges; ignore any rare ID collision
            }
        }

        return gsGraph;
    }

    /**
     * Applies highlight CSS classes to nodes and edges along a {@link PathResult}.
     *
     * @param gsGraph    the GraphStream graph where styles will be applied.
     * @param pathResult the path to highlight.
     */
    private void highlightPath(Graph gsGraph, PathResult pathResult) {
        List<Vertex> vertices = pathResult.getVertices();
        if (vertices.isEmpty()) return;

        for (int i = 0; i < vertices.size(); i++) {
            String email = vertices.get(i).getEmail();
            org.graphstream.graph.Node node = gsGraph.getNode(email);
            if (node == null) continue;

            if (i == 0 || i == vertices.size() - 1) {
                node.setAttribute("ui.class", "endpoint");
            } else {
                node.setAttribute("ui.class", "highlighted");
            }
        }

        // Highlight edges along the path
        for (int i = 0; i < vertices.size() - 1; i++) {
            String from = vertices.get(i).getEmail();
            String to   = vertices.get(i + 1).getEmail();
            gsGraph.edges()
                   .filter(e -> e.getSourceNode().getId().equals(from)
                             && e.getTargetNode().getId().equals(to))
                   .findFirst()
                   .ifPresent(e -> e.setAttribute("ui.class", "highlighted"));
        }
    }

    /**
     * Builds a {@link ContactGraph} containing only the specified seed vertices
     * and all edges between them.
     *
     * @param source  the full graph.
     * @param seeds   the vertex subset to include.
     * @return subgraph restricted to {@code seeds}.
     */
    private ContactGraph buildSubgraph(ContactGraph source, Set<Vertex> seeds) {
        ContactGraph sub = new ContactGraph();
        for (Vertex v : seeds) {
            sub.addVertex(v.getEmail());
            for (Edge e : source.getOutEdges(v)) {
                if (seeds.contains(e.getDestination())) {
                    // Replay edge additions to keep weight accurate
                    for (int w = 0; w < e.getWeight(); w++) {
                        sub.addEdge(e.getOrigin().getEmail(), e.getDestination().getEmail());
                    }
                }
            }
        }
        return sub;
    }

    /**
     * Returns the top-N vertices by out-degree from the given graph.
     *
     * @param graph the source graph.
     * @param n     maximum number of vertices to return.
     * @return set of selected vertices.
     */
    private Set<Vertex> topNByOutDegree(ContactGraph graph, int n) {
        List<Vertex> sorted = new java.util.ArrayList<>(graph.getVertices());
        sorted.sort((a, b) -> Integer.compare(graph.outDegree(b), graph.outDegree(a)));
        Set<Vertex> result = new HashSet<>();
        for (int i = 0; i < Math.min(n, sorted.size()); i++) {
            result.add(sorted.get(i));
        }
        return result;
    }
}
