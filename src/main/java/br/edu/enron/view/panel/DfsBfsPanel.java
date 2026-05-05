package br.edu.enron.view.panel;

import br.edu.enron.design.DesignSystem;
import br.edu.enron.graph.ContactGraph;
import br.edu.enron.model.Edge;
import br.edu.enron.model.PathResult;
import br.edu.enron.model.Vertex;
import br.edu.enron.service.BreadthFirstSearch;
import br.edu.enron.service.DepthFirstSearch;
import br.edu.enron.view.GraphVisualizer;
import br.edu.enron.view.component.PanelHeader;
import br.edu.enron.view.component.QueryRow;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static br.edu.enron.view.component.SwingHelper.*;

/**
 * Aba 02 - Busca de caminho DFS/BFS entre dois vértices.
 */
public final class DfsBfsPanel extends JPanel {

    private final ContactGraph graph;
    private final DepthFirstSearch dfs = new DepthFirstSearch();
    private final BreadthFirstSearch bfs = new BreadthFirstSearch();
    private final GraphVisualizer visualizer;

    private final JComboBox<String> fromCombo;
    private final JComboBox<String> toCombo;
    private final JPanel resultHost;
    private String selectedAlgo = "BFS";

    public DfsBfsPanel(ContactGraph graph, GraphVisualizer visualizer) {
        this.graph = graph;
        this.visualizer = visualizer;
        setLayout(new BorderLayout());
        setBackground(DesignSystem.bg());

        add(new PanelHeader(
                "§02 · PERCURSO · BUSCA EM LARGURA · BUSCA EM PROFUNDIDADE",
                "X alcança Y? ", "Em qual rota.", "",
                "Verifique a conectividade entre dois indivíduos e visualize a rota percorrida. "
                        + "BFS retorna o caminho com menor número de arestas; DFS retorna a primeira rota "
                        + "encontrada explorando em profundidade. Ambos tratam ciclos por meio de conjunto de visitados.",
                "BFS", "CICLOS · TRATADOS"
        ), BorderLayout.NORTH);

        fromCombo = buildCombo(new ArrayList<>(graph.getOwnerEmails()));
        toCombo = buildCombo(sortedEmails());
        selectLastReal(toCombo);

        QueryRow query = new QueryRow();
        query.addComboCell("ORIGEM", "X", fromCombo, 1);
        query.addComboCell("DESTINO", "Y", toCombo, 1);
        query.addAlgoToggle("ALGORITMO", new String[]{"BFS", "DFS"}, 0, a -> selectedAlgo = a);
        query.addActionButton("EXECUTAR", e -> run());

        resultHost = new JPanel(new BorderLayout());
        resultHost.setBackground(DesignSystem.bg());
        resultHost.add(emptyResult("Sem consulta.", "Escolha origem, destino e execute a busca."), BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(0, 20));
        center.setBackground(DesignSystem.bg());
        center.setBorder(new EmptyBorder(24, 28, 20, 28));
        center.add(query, BorderLayout.NORTH);
        center.add(resultHost, BorderLayout.CENTER);

        add(center, BorderLayout.CENTER);
    }

    private void run() {
        String from = selectedEmail(fromCombo);
        String to = selectedEmail(toCombo);
        PathResult result = "DFS".equals(selectedAlgo)
                ? dfs.search(graph, from, to)
                : bfs.search(graph, from, to);

        resultHost.removeAll();
        resultHost.add(buildResult(result, from, to), BorderLayout.NORTH);
        resultHost.revalidate();
        resultHost.repaint();

        if (result.exists()) {
            visualizer.displayWithPath(graph, result, selectedAlgo + " Path", selectedAlgo);
        }
    }

    private JComponent buildResult(PathResult result, String from, String to) {
        if (!result.exists()) {
            return emptyResult("Sem caminho.", "Nao existe rota direcionada de " + from + " ate " + to + ".");
        }

        List<Vertex> vertices = result.getVertices();
        List<Edge> edges = pathEdges(result);
        int totalWeight = edges.stream().mapToInt(Edge::getWeight).sum();
        int maxWeight = edges.stream().mapToInt(Edge::getWeight).max().orElse(1);

        JPanel shell = new JPanel(new BorderLayout());
        shell.setBackground(DesignSystem.surface());
        shell.setBorder(new MatteBorder(1, 1, 1, 1, DesignSystem.rule()));

        JPanel main = new JPanel(new BorderLayout(0, 12));
        main.setBackground(DesignSystem.surface());
        main.setBorder(new EmptyBorder(20, 24, 20, 24));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(lbl(selectedAlgo + " · CAMINHO ENCONTRADO", MONO_XS, DesignSystem.muted()), BorderLayout.WEST);
        JLabel verdict = lbl("alcançado.", SERIF_IT, DesignSystem.accent());
        verdict.setHorizontalAlignment(SwingConstants.RIGHT);
        header.add(verdict, BorderLayout.EAST);
        main.add(header, BorderLayout.NORTH);

        JPanel chain = new JPanel();
        chain.setLayout(new BoxLayout(chain, BoxLayout.Y_AXIS));
        chain.setBackground(DesignSystem.surface());
        for (int i = 0; i < vertices.size(); i++) {
            chain.add(chainRow(vertices.get(i), i, vertices.size(), i < edges.size() ? edges.get(i) : null, maxWeight));
        }
        main.add(chain, BorderLayout.CENTER);

        JPanel side = sideStats(new String[][]{
                {"ALGORITMO", selectedAlgo},
                {"COMPRIMENTO", String.valueOf(Math.max(vertices.size() - 1, 0))},
                {"CUSTO DAS ARESTAS", formatInt(totalWeight)},
                {"VERTICES NO CAMINHO", String.valueOf(vertices.size())}
        }, 320);

        shell.add(main, BorderLayout.CENTER);
        shell.add(side, BorderLayout.EAST);
        return shell;
    }

    private JPanel chainRow(Vertex vertex, int index, int total, Edge edge, int maxWeight) {
        boolean start = index == 0;
        boolean end = index == total - 1;
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setBackground(DesignSystem.surface());

        JPanel rail = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                if (!end) {
                    g2.setColor(DesignSystem.rule());
                    g2.fillRect(getWidth() / 2, 24, 1, getHeight() - 24);
                }
                g2.setColor(start ? DesignSystem.ink() : DesignSystem.accent());
                g2.fillOval((getWidth() - 8) / 2, 14, 8, 8);
                if (end) {
                    g2.setColor(new Color(DesignSystem.accent().getRed(), DesignSystem.accent().getGreen(), DesignSystem.accent().getBlue(), 70));
                    g2.drawOval((getWidth() - 18) / 2, 9, 18, 18);
                }
                g2.dispose();
            }
        };
        rail.setOpaque(false);
        rail.setPreferredSize(new Dimension(32, 58));
        row.add(rail, BorderLayout.WEST);

        JPanel body = new JPanel(new BorderLayout(0, 6));
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(8, 0, 12, 0));
        body.add(lbl(vertex.getEmail(), MONO_MD, DesignSystem.ink()), BorderLayout.NORTH);
        if (edge != null) body.add(edgeRow(edge, maxWeight), BorderLayout.CENTER);
        row.add(body, BorderLayout.CENTER);
        return row;
    }

    private JPanel edgeRow(Edge edge, int maxWeight) {
        JPanel p = new JPanel(new BorderLayout(8, 0));
        p.setOpaque(false);
        p.add(lbl("envia", MONO_XS, DesignSystem.muted()), BorderLayout.WEST);
        p.add(microBar(edge.getWeight(), maxWeight), BorderLayout.CENTER);
        p.add(lbl(edge.getWeight() + " msgs", MONO_XS, DesignSystem.accent()), BorderLayout.EAST);
        return p;
    }

    private JPanel microBar(int value, int max) {
        JPanel p = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                int y = (getHeight() - 4) / 2;
                int fill = max > 0 ? (int) (getWidth() * (value / (double) max)) : 0;
                g.setColor(DesignSystem.bg2());
                g.fillRect(0, y, getWidth(), 4);
                g.setColor(DesignSystem.accent());
                g.fillRect(0, y, fill, 4);
            }
        };
        p.setOpaque(false);
        p.setPreferredSize(new Dimension(160, 14));
        return p;
    }

    private JPanel sideStats(String[][] rows, int width) {
        JPanel side = new JPanel();
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
        side.setBackground(DesignSystem.surface());
        side.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0, 1, 0, 0, DesignSystem.rule()),
                new EmptyBorder(20, 24, 20, 24)
        ));
        side.setPreferredSize(new Dimension(width, 0));
        for (int i = 0; i < rows.length; i++) {
            boolean accent = i == 0;
            JPanel row = new JPanel(new BorderLayout(8, 4));
            row.setBackground(DesignSystem.surface());
            row.setBorder(BorderFactory.createCompoundBorder(
                    new MatteBorder(0, 0, 1, 0, DesignSystem.rule2()),
                    new EmptyBorder(0, 0, 12, 0)
            ));
            row.add(lbl(rows[i][0], MONO_XS, DesignSystem.muted()), BorderLayout.WEST);
            JLabel value = lbl(rows[i][1], accent ? MONO_MD : SERIF_28, accent ? DesignSystem.accent() : DesignSystem.ink());
            value.setHorizontalAlignment(SwingConstants.RIGHT);
            row.add(value, BorderLayout.EAST);
            side.add(row);
            side.add(Box.createVerticalStrut(12));
        }
        return side;
    }

    private List<Edge> pathEdges(PathResult result) {
        List<Edge> edges = new ArrayList<>();
        List<Vertex> vertices = result.getVertices();
        for (int i = 0; i < vertices.size() - 1; i++) {
            Vertex from = vertices.get(i);
            Vertex to = vertices.get(i + 1);
            for (Edge edge : graph.getOutEdges(from)) {
                if (edge.getDestination().equals(to)) {
                    edges.add(edge);
                    break;
                }
            }
        }
        return edges;
    }

    private List<String> sortedEmails() {
        List<String> emails = new ArrayList<>();
        for (var v : graph.getVertices()) emails.add(v.getEmail());
        emails.sort(String::compareToIgnoreCase);
        return emails;
    }
}
