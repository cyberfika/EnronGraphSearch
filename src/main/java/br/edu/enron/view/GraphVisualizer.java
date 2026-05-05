package br.edu.enron.view;

import br.edu.enron.graph.ContactGraph;
import br.edu.enron.model.Edge;
import br.edu.enron.model.PathResult;
import br.edu.enron.model.Vertex;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.ui.view.Viewer;

import java.util.*;

/**
 * Renderiza subconjuntos do {@link ContactGraph} usando GraphStream.
 *
 * <h2>Modos de visualização</h2>
 * <ol>
 *   <li><b>Top-N</b> ({@link #displayTopN}) — mostra os N nós de maior grau
 *       e as arestas entre eles. Útil para uma visão geral.</li>
 *   <li><b>Resultado de caminho</b> ({@link #displayWithPath}) — mostra apenas os nós
 *       e arestas de um resultado BFS/DFS/Dijkstra, além dos vizinhos imediatos
 *       para contexto.</li>
 *   <li><b>Rede ego</b> ({@link #displayEgoNetwork}) — mostra um nó escolhido
 *       e tudo dentro de K saltos.</li>
 * </ol>
 *
 * <p>O tamanho do nó é proporcional ao seu grau. Os rótulos são mostrados apenas nos
 * nós mais importantes para manter a exibição legível.</p>
 */
public class GraphVisualizer {

    private static final int DEFAULT_TOP_N = 30;
    private static final int MAX_LABELLED  = 25;

    // ── Folha de estilo de tema escuro com nós dimensionados por grau ────────
    private static final String STYLESHEET =
            "graph { fill-color: #252A32; padding: 60px; }" +
            "node {" +
            "  fill-color: rgba(209,217,230,75);" +
            "  size: 7px;" +
            "  text-size: 0;" +                 // oculto por padrão
            "  stroke-mode: none;" +
            "  z-index: 1;" +
            "}" +
            "node.large {" +
            "  fill-color: rgba(209,217,230,115);" +
            "  text-size: 11;" +
            "  text-color: #D1D9E6;" +
            "  text-style: bold;" +
            "  text-background-mode: rounded-box;" +
            "  text-background-color: rgba(42,47,55,220);" +
            "  text-padding: 3px;" +
            "  text-offset: 0px, -8px;" +
            "  z-index: 2;" +
            "}" +
            "node.highlighted {" +
            "  fill-color: #D4AA25;" +
            "  size: 20px;" +
            "  text-size: 13;" +
            "  text-color: #FFFFFF;" +
            "  text-style: bold;" +
            "  text-background-mode: rounded-box;" +
            "  text-background-color: rgba(42,47,55,230);" +
            "  text-padding: 3px;" +
            "  text-offset: 0px, -8px;" +
            "  stroke-color: #D4AA25;" +
            "  stroke-mode: plain;" +
            "  stroke-width: 2px;" +
            "  z-index: 20;" +
            "}" +
            "node.endpoint {" +
            "  fill-color: #F6F7FB;" +
            "  size: 25px;" +
            "  text-size: 13;" +
            "  text-color: #FFFFFF;" +
            "  text-style: bold;" +
            "  text-background-mode: rounded-box;" +
            "  text-background-color: rgba(42,47,55,230);" +
            "  text-padding: 4px;" +
            "  text-offset: 0px, -10px;" +
            "  stroke-color: #D4AA25;" +
            "  stroke-mode: plain;" +
            "  stroke-width: 3px;" +
            "  z-index: 21;" +
            "}" +
            "node.ego {" +
            "  fill-color: #D4AA25;" +
            "  text-size: 14;" +
            "  text-color: #FFFFFF;" +
            "  text-style: bold;" +
            "  text-background-mode: rounded-box;" +
            "  text-background-color: rgba(42,47,55,230);" +
            "  text-padding: 4px;" +
            "  text-offset: 0px, -10px;" +
            "  stroke-color: #D4AA25;" +
            "  stroke-mode: plain;" +
            "  stroke-width: 2px;" +
            "  z-index: 20;" +
            "}" +
            "edge {" +
            "  fill-color: rgba(55,62,73,38);" +
            "  size: 0.45px;" +
            "  arrow-size: 3px, 2px;" +
            "  text-size: 0;" +
            "  z-index: 0;" +
            "}" +
            "edge.highlighted {" +
            "  fill-color: #D4AA25;" +
            "  size: 5px;" +
            "  arrow-size: 16px, 8px;" +
            "  text-size: 13;" +
            "  text-color: #D4AA25;" +
            "  text-style: bold;" +
            "  text-background-mode: rounded-box;" +
            "  text-background-color: rgba(31,35,41,235);" +
            "  text-padding: 4px;" +
            "  text-offset: 0px, -10px;" +
            "  z-index: 15;" +
            "}" +
            "edge.strong {" +
            "  fill-color: rgba(66,73,85,55);" +
            "  size: 0.8px;" +
            "  arrow-size: 4px, 2px;" +
            "  text-size: 0;" +
            "  z-index: 0;" +
            "}";

    // =====================================================================
    // Modo 1 — Visão geral Top-N (padrão para o botão "show graph")
    // =====================================================================

    /**
     * Exibe os principais N nós por grau com as arestas entre eles.
     * O tamanho do nó escala com o grau. Apenas os nós do topo recebem rótulos.
     */
    public void display(ContactGraph graph, String title) {
        displayTopN(graph, title, DEFAULT_TOP_N);
    }

    public void displayTopN(ContactGraph graph, String title, int n) {
        System.setProperty("org.graphstream.ui", "swing");

        Set<Vertex> seeds = topNByTotalDegree(graph, n);
        ContactGraph sub = buildSubgraph(graph, seeds);
        Graph gs = toGsGraph(sub, title, graph);

        applyDegreeScaling(gs, graph, seeds, MAX_LABELLED);

        gs.setAttribute("ui.stylesheet", STYLESHEET);
        gs.setAttribute("ui.quality");
        gs.setAttribute("ui.antialias");

        Viewer viewer = gs.display();
        viewer.setCloseFramePolicy(Viewer.CloseFramePolicy.HIDE_ONLY);
    }

    // =====================================================================
    // Modo 2 — Resultado de caminho (BFS / DFS / Dijkstra)
    // =====================================================================

    /**
     * Exibe apenas o caminho e sua vizinhança imediata.
     * Os nós do caminho são destacados; as extremidades ficam brancas com borda amarela.
     */
    public void displayWithPath(ContactGraph graph, PathResult path,
                                String title, String label) {
        System.setProperty("org.graphstream.ui", "swing");

        Set<Vertex> seeds = new LinkedHashSet<>(path.getVertices());
        for (Vertex v : path.getVertices()) {
            addWeightedContext(seeds, graph.getOutEdges(v), 3);
            addWeightedContext(seeds, incomingEdges(graph, v), 3);
            if (seeds.size() > 70) break;
        }

        ContactGraph sub = buildSubgraph(graph, seeds);
        Graph gs = toGsGraph(sub, title, graph);

        // Dimensionar nós que não pertencem ao caminho
        Set<String> pathEmails = new HashSet<>();
        for (Vertex v : path.getVertices()) pathEmails.add(v.getEmail());
        applyDegreeScaling(gs, graph, seeds, 0); // sem rótulos nos nós de contexto

        // Destacar caminho
        highlightPath(gs, path);

        gs.setAttribute("ui.stylesheet", STYLESHEET);
        gs.setAttribute("ui.quality");
        gs.setAttribute("ui.antialias");

        System.out.println("[" + label + "] " + path);

        Viewer viewer = gs.display();
        viewer.setCloseFramePolicy(Viewer.CloseFramePolicy.HIDE_ONLY);
    }

    // =====================================================================
    // Modo 3 — Rede ego (K saltos a partir de um nó)
    // =====================================================================

    /**
     * Exibe a vizinhança de um determinado nó até K saltos.
     * O nó central é destacado em dourado.
     */
    public void displayEgoNetwork(ContactGraph graph, String centerEmail,
                                  int hops, String title) {
        System.setProperty("org.graphstream.ui", "swing");

        Optional<Vertex> opt = graph.findVertex(centerEmail);
        if (opt.isEmpty()) {
            System.out.println("[Visualizer] Nó não encontrado: " + centerEmail);
            return;
        }

        Set<Vertex> seeds = bfsHops(graph, opt.get(), hops);
        ContactGraph sub = buildSubgraph(graph, seeds);
        Graph gs = toGsGraph(sub, title, graph);

        applyDegreeScaling(gs, graph, seeds, MAX_LABELLED);

        // Marcar centro
        org.graphstream.graph.Node center = gs.getNode(centerEmail);
        if (center != null) {
            center.setAttribute("ui.class", "ego");
            center.setAttribute("ui.label", shortLabel(centerEmail));
            center.setAttribute("ui.size", 30);
        }

        gs.setAttribute("ui.stylesheet", STYLESHEET);
        gs.setAttribute("ui.quality");
        gs.setAttribute("ui.antialias");

        Viewer viewer = gs.display();
        viewer.setCloseFramePolicy(Viewer.CloseFramePolicy.HIDE_ONLY);
    }

    // =====================================================================
    // Auxiliares de construção do grafo
    // =====================================================================

    private Graph toGsGraph(ContactGraph sub, String id, ContactGraph fullGraph) {
        Graph gs = new MultiGraph(id);

        for (Vertex v : sub.getVertices()) {
            gs.addNode(v.getEmail());
        }

        int eid = 0;
        for (Edge e : sub.getEdges()) {
            try {
                org.graphstream.graph.Edge ge = gs.addEdge(
                        "e" + eid++,
                        e.getOrigin().getEmail(),
                        e.getDestination().getEmail(),
                        true);
                ge.setAttribute("ui.label", String.valueOf(e.getWeight()));
                // Marcar arestas fortes
                if (e.getWeight() >= 5) {
                    ge.setAttribute("ui.class", "strong");
                }
            } catch (Exception ignored) { }
        }
        return gs;
    }

    private ContactGraph buildSubgraph(ContactGraph source, Set<Vertex> seeds) {
        ContactGraph sub = new ContactGraph();
        for (Vertex v : seeds) sub.addVertex(v.getEmail());
        for (Vertex v : seeds) {
            for (Edge e : source.getOutEdges(v)) {
                if (seeds.contains(e.getDestination())) {
                    for (int w = 0; w < e.getWeight(); w++) {
                        sub.addEdge(e.getOrigin().getEmail(), e.getDestination().getEmail());
                    }
                }
            }
        }
        return sub;
    }

    // =====================================================================
    // Dimensionamento por grau e rotulagem
    // =====================================================================

    /**
     * Dimensiona o tamanho do nó pelo grau (no grafo completo) e mostra rótulos
     * apenas nos nós mais importantes (top-labelled).
     */
    private void applyDegreeScaling(Graph gs, ContactGraph fullGraph,
                                    Set<Vertex> seeds, int maxLabels) {
        // Calcular grau para ordenação
        List<Map.Entry<String, Integer>> ranked = new ArrayList<>();
        for (Vertex v : seeds) {
            int deg = fullGraph.outDegree(v) + fullGraph.inDegree(v);
            ranked.add(Map.entry(v.getEmail(), deg));
        }
        ranked.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));

        int maxDeg = ranked.isEmpty() ? 1 : Math.max(ranked.get(0).getValue(), 1);

        // Top N recebem rótulos
        Set<String> labelled = new HashSet<>();
        for (int i = 0; i < Math.min(maxLabels, ranked.size()); i++) {
            labelled.add(ranked.get(i).getKey());
        }

        for (Map.Entry<String, Integer> entry : ranked) {
            org.graphstream.graph.Node node = gs.getNode(entry.getKey());
            if (node == null) continue;

            int deg = entry.getValue();
            int size = maxLabels == 0
                    ? 5 + (int) (5.0 * deg / maxDeg)
                    : 8 + (int) (27.0 * deg / maxDeg);
            node.setAttribute("ui.size", size);

            if (labelled.contains(entry.getKey())) {
                node.setAttribute("ui.class", "large");
                node.setAttribute("ui.label", shortLabel(entry.getKey()));
            }
        }
    }

    // =====================================================================
    // Destaque de caminho
    // =====================================================================

    private void highlightPath(Graph gs, PathResult path) {
        List<Vertex> verts = path.getVertices();
        if (verts.isEmpty()) return;

        for (int i = 0; i < verts.size(); i++) {
            String email = verts.get(i).getEmail();
            org.graphstream.graph.Node node = gs.getNode(email);
            if (node == null) continue;

            if (i == 0 || i == verts.size() - 1) {
                node.setAttribute("ui.class", "endpoint");
            } else {
                node.setAttribute("ui.class", "highlighted");
            }
            node.setAttribute("ui.label", shortLabel(email));
            node.setAttribute("ui.size",
                    (i == 0 || i == verts.size() - 1) ? 28 : 22);
        }

        // Destacar arestas ao longo do caminho
        for (int i = 0; i < verts.size() - 1; i++) {
            String from = verts.get(i).getEmail();
            String to = verts.get(i + 1).getEmail();
            gs.edges()
              .filter(e -> e.getSourceNode().getId().equals(from)
                        && e.getTargetNode().getId().equals(to))
              .findFirst()
              .ifPresent(e -> {
                  e.setAttribute("ui.class", "highlighted");
                  e.setAttribute("layout.weight", 0.01);
              });
        }
    }

    private void addWeightedContext(Set<Vertex> seeds, List<Edge> edges, int limit) {
        List<Edge> ranked = new ArrayList<>(edges);
        ranked.sort(Comparator.comparingInt(Edge::getWeight).reversed());
        for (int i = 0; i < Math.min(limit, ranked.size()); i++) {
            Edge edge = ranked.get(i);
            seeds.add(edge.getOrigin());
            seeds.add(edge.getDestination());
        }
    }

    private List<Edge> incomingEdges(ContactGraph graph, Vertex vertex) {
        List<Edge> incoming = new ArrayList<>();
        for (Vertex other : graph.getVertices()) {
            for (Edge edge : graph.getOutEdges(other)) {
                if (edge.getDestination().equals(vertex)) {
                    incoming.add(edge);
                }
            }
        }
        return incoming;
    }

    // =====================================================================
    // Auxiliares de seleção
    // =====================================================================

    private Set<Vertex> topNByTotalDegree(ContactGraph graph, int n) {
        List<Vertex> sorted = new ArrayList<>(graph.getVertices());
        sorted.sort((a, b) -> Integer.compare(
                graph.outDegree(b) + graph.inDegree(b),
                graph.outDegree(a) + graph.inDegree(a)));

        Set<Vertex> result = new LinkedHashSet<>();
        for (int i = 0; i < Math.min(n, sorted.size()); i++) {
            result.add(sorted.get(i));
        }
        return result;
    }

    private Set<Vertex> bfsHops(ContactGraph graph, Vertex start, int maxHops) {
        Set<Vertex> visited = new LinkedHashSet<>();
        Queue<Vertex> queue = new LinkedList<>();
        Map<Vertex, Integer> dist = new HashMap<>();

        visited.add(start);
        queue.add(start);
        dist.put(start, 0);

        while (!queue.isEmpty()) {
            Vertex curr = queue.poll();
            int d = dist.get(curr);
            if (d >= maxHops) continue;

            for (Edge e : graph.getOutEdges(curr)) {
                Vertex nb = e.getDestination();
                if (!visited.contains(nb)) {
                    visited.add(nb);
                    dist.put(nb, d + 1);
                    queue.add(nb);
                }
            }
        }
        return visited;
    }

    /** Extrai o nome de usuário do e-mail para rótulos compactos. */
    private static String shortLabel(String email) {
        int at = email.indexOf('@');
        return at > 0 ? email.substring(0, at) : email;
    }
}
