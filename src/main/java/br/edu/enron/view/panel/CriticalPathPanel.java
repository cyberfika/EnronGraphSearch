package br.edu.enron.view.panel;

import br.edu.enron.design.DesignSystem;
import br.edu.enron.graph.ContactGraph;
import br.edu.enron.model.Edge;
import br.edu.enron.model.PathResult;
import br.edu.enron.model.Vertex;
import br.edu.enron.service.CriticalPathService;
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
 * Aba 04 - Caminho crítico usando Dijkstra com peso inverso.
 */
public final class CriticalPathPanel extends JPanel {

    private final ContactGraph graph;
    private final CriticalPathService critPath = new CriticalPathService();
    private final GraphVisualizer visualizer;

    private final JComboBox<String> fromCombo;
    private final JComboBox<String> toCombo;
    private final JPanel resultHost;

    public CriticalPathPanel(ContactGraph graph, GraphVisualizer visualizer) {
        this.graph = graph;
        this.visualizer = visualizer;
        setLayout(new BorderLayout());
        setBackground(DesignSystem.bg());

        add(new PanelHeader(
                "§04 · CAMINHO CRÍTICO · DIJKSTRA ADAPTADO · CUSTO = 1 / PESO",
                "O canal ", "de maior dependência", " entre A e C.",
                "Adaptação do Dijkstra usando o inverso do peso da aresta. Como peso alto representa "
                        + "relação forte, custo baixo (1/peso) faz com que o algoritmo prefira atravessar laços "
                        + "densos. O caminho retornado é o de maior dependência acumulada.",
                "CUSTO = 1 / PESO"
        ), BorderLayout.NORTH);

        fromCombo = buildCombo(new ArrayList<>(graph.getOwnerEmails()));
        toCombo = buildCombo(sortedEmails());
        selectLastReal(toCombo);

        QueryRow query = new QueryRow();
        query.addComboCell("INDIVIDUO", "A", fromCombo, 1);
        query.addComboCell("INDIVIDUO", "C", toCombo, 1);
        query.addInfoCell("ADAPTACAO", "min Σ 1/peso");
        query.addActionButton("CALCULAR", e -> run());

        resultHost = new JPanel(new BorderLayout());
        resultHost.setBackground(DesignSystem.bg());
        resultHost.add(emptyResult("Sem consulta.", "Escolha A, C e calcule o caminho critico."), BorderLayout.NORTH);

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
        PathResult result = critPath.computeCriticalPath(graph, from, to);

        resultHost.removeAll();
        resultHost.add(buildResult(result), BorderLayout.NORTH);
        resultHost.revalidate();
        resultHost.repaint();

        if (result.exists()) {
            visualizer.displayWithPath(graph, result, "Caminho Critico", "Dijkstra");
        }
    }

    private JComponent buildResult(PathResult result) {
        if (!result.exists()) {
            return emptyResult("Sem canal entre A e C.", "Nao existe caminho direcionado conectando os dois individuos.");
        }

        List<Vertex> vertices = result.getVertices();
        List<Edge> edges = pathEdges(result);
        int maxWeight = edges.stream().mapToInt(Edge::getWeight).max().orElse(1);
        int dependency = edges.stream().mapToInt(Edge::getWeight).sum();

        JPanel shell = new JPanel(new BorderLayout());
        shell.setBackground(DesignSystem.surface());
        shell.setBorder(new MatteBorder(1, 1, 1, 1, DesignSystem.rule()));

        JPanel main = new JPanel(new BorderLayout(0, 14));
        main.setBackground(DesignSystem.surface());
        main.setBorder(new EmptyBorder(20, 24, 20, 24));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(lbl("CAMINHO CRITICO APROXIMADO", MONO_XS, DesignSystem.muted()), BorderLayout.WEST);
        JLabel verdict = lbl("canal identificado.", SERIF_IT, DesignSystem.accent());
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
        main.add(new CritFlow(edges, dependency), BorderLayout.SOUTH);

        JPanel side = sideStats(result, edges, dependency);
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
        p.add(lbl("peso original", MONO_XS, DesignSystem.muted()), BorderLayout.WEST);
        p.add(microBar(edge.getWeight(), maxWeight), BorderLayout.CENTER);
        p.add(lbl(edge.getWeight() + " · custo " + String.format("%.4f", edge.getInverseCost()), MONO_XS, DesignSystem.accent()), BorderLayout.EAST);
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

    private JPanel sideStats(PathResult result, List<Edge> edges, int dependency) {
        JPanel side = new JPanel();
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
        side.setBackground(DesignSystem.surface());
        side.setPreferredSize(new Dimension(340, 0));
        side.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0, 1, 0, 0, DesignSystem.rule()),
                new EmptyBorder(20, 24, 20, 24)
        ));

        int max = edges.stream().mapToInt(Edge::getWeight).max().orElse(0);
        int min = edges.stream().mapToInt(Edge::getWeight).min().orElse(0);
        addSideStat(side, "DEPENDENCIA ACUMULADA", String.valueOf(dependency), true);
        addSideStat(side, "CUSTO INVERSO", String.format("%.4f", result.getTotalCost()), false);
        addSideStat(side, "ARESTA MAIS FORTE", String.valueOf(max), true);
        addSideStat(side, "ARESTA MAIS FRACA", String.valueOf(min), false);
        return side;
    }

    private void addSideStat(JPanel side, String label, String value, boolean accent) {
        JPanel row = new JPanel(new BorderLayout(8, 4));
        row.setBackground(DesignSystem.surface());
        row.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0, 0, 1, 0, DesignSystem.rule2()),
                new EmptyBorder(0, 0, 12, 0)
        ));
        row.add(lbl(label, MONO_XS, DesignSystem.muted()), BorderLayout.WEST);
        JLabel v = lbl(value, accent ? SERIF_28 : MONO_MD, accent ? DesignSystem.accent() : DesignSystem.ink());
        v.setHorizontalAlignment(SwingConstants.RIGHT);
        row.add(v, BorderLayout.EAST);
        side.add(row);
        side.add(Box.createVerticalStrut(12));
    }

    private final class CritFlow extends JPanel {
        private final List<Edge> edges;
        private final int total;

        private CritFlow(List<Edge> edges, int total) {
            this.edges = List.copyOf(edges);
            this.total = Math.max(total, 1);
            setBackground(DesignSystem.bg2());
            setBorder(BorderFactory.createCompoundBorder(
                    new MatteBorder(1, 1, 1, 1, DesignSystem.rule()),
                    new EmptyBorder(16, 20, 24, 20)
            ));
            setPreferredSize(new Dimension(0, 96));
        }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setFont(MONO_XS);
            g2.setColor(DesignSystem.muted());
            g2.drawString("DISTRIBUICAO DA DEPENDENCIA ACUMULADA", 20, 18);
            g2.drawString("Σ = " + total, getWidth() - 70, 18);

            int x = 20;
            int y = 36;
            int w = getWidth() - 40;
            int h = 18;
            g2.setColor(DesignSystem.rule());
            g2.drawRect(x, y, w, h);

            int current = x + 1;
            for (int i = 0; i < edges.size(); i++) {
                Edge edge = edges.get(i);
                int segW = i == edges.size() - 1
                        ? x + w - current
                        : Math.max(2, (int) ((w - 1) * (edge.getWeight() / (double) total)));
                int shade = Math.max(0, 24 - i * 6);
                g2.setColor(new Color(
                        Math.max(0, DesignSystem.accent().getRed() - shade),
                        Math.max(0, DesignSystem.accent().getGreen() - shade),
                        DesignSystem.accent().getBlue()
                ));
                g2.fillRect(current, y + 1, segW, h - 1);
                g2.setColor(DesignSystem.bg());
                g2.drawString(String.valueOf(edge.getWeight()), current + Math.max(4, segW / 2 - 5), y + 13);
                current += segW;
            }
            g2.dispose();
        }
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
