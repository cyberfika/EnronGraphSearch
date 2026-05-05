package br.edu.enron.view.panel;

import br.edu.enron.design.DesignSystem;
import br.edu.enron.graph.ContactGraph;
import br.edu.enron.model.DegreeResult;
import br.edu.enron.model.Edge;
import br.edu.enron.view.component.PanelHeader;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import static br.edu.enron.view.component.SwingHelper.*;

/**
 * Tab 01 — Visão Geral: graph statistics and leaderboards.
 */
public final class OverviewPanel extends JPanel {

    public OverviewPanel(ContactGraph graph, List<DegreeResult> top20Out, List<DegreeResult> top20In,
                         int totalMessages, double density) {
        setLayout(new BorderLayout());
        setBackground(DesignSystem.bg());

        add(new PanelHeader(
                "§01 · VISÃO GERAL · GRAFO DIRECIONADO · PONDERADO · ROTULADO",
                "A topologia ", "de uma corporação", ", em arestas.",
                "Cada vértice é um indivíduo identificado por seu e-mail. Cada aresta é um envio. "
                + "O peso é a frequência. Os algoritmos de percurso desta tela são adaptados de Dijkstra, "
                + "BFS e DFS — todos com tratamento explícito de ciclos.",
                "DIRECIONADO", "PONDERADO", "ROTULADO"
        ), BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout(0, 16));
        body.setBackground(DesignSystem.bg());
        body.setBorder(new EmptyBorder(20, 28, 20, 28));

        body.add(statCards(graph, totalMessages, density), BorderLayout.NORTH);

        JPanel lower = new JPanel(new BorderLayout(0, 14));
        lower.setBackground(DesignSystem.bg());
        lower.add(leaderboard(top20Out, top20In), BorderLayout.CENTER);
        body.add(lower, BorderLayout.CENTER);

        JScrollPane scroll = new JScrollPane(body);
        scroll.setBorder(null);
        scroll.setBackground(DesignSystem.bg());
        scroll.getViewport().setBackground(DesignSystem.bg());
        styleScrollBar(scroll.getVerticalScrollBar());
        add(scroll, BorderLayout.CENTER);
    }

    // ── Stat cards ──────────────────────────────────────────────────────────

    private JPanel statCards(ContactGraph graph, int totalMessages, double density) {
        JPanel p = new JPanel(new GridLayout(1, 4, 0, 0));
        p.setBackground(DesignSystem.bg());
        p.setBorder(new MatteBorder(1, 1, 0, 0, DesignSystem.rule()));
        p.setPreferredSize(new Dimension(0, 130));

        double avgDeg = graph.vertexCount() > 1 ? (double) graph.edgeCount() / graph.vertexCount() : 0;

        p.add(statCell("VÉRTICES", formatInt(graph.vertexCount()), "INDIVÍDUOS", "getNumeroVertices()", DesignSystem.ink()));
        p.add(statCell("ARESTAS", formatInt(graph.edgeCount()), "RELAÇÕES", "getNumeroArestas()", DesignSystem.ink()));
        p.add(statCell("MENSAGENS (Σ PESOS)", formatInt(totalMessages), "", "soma das frequências", DesignSystem.accent()));
        p.add(statCell("DENSIDADE", String.format("%.2f", density), "%",
                "grau médio · " + String.format("%.2f", avgDeg), DesignSystem.accent()));
        return p;
    }

    private JPanel statCell(String label, String value, String unit, String note, Color valueColor) {
        JPanel cell = new JPanel(new BorderLayout(0, 6));
        cell.setBackground(DesignSystem.surface());
        cell.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0, 0, 0, 1, DesignSystem.rule()),
                new EmptyBorder(16, 18, 14, 18)
        ));

        cell.add(lbl(label.toUpperCase(), MONO_XS, DesignSystem.muted()), BorderLayout.NORTH);

        JPanel valRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
        valRow.setOpaque(false);
        valRow.add(lbl(value, NUM_XL, valueColor));
        if (!unit.isEmpty()) {
            JLabel u = lbl(unit, NUM_MD, DesignSystem.muted());
            u.setVerticalAlignment(SwingConstants.BOTTOM);
            valRow.add(u);
        }
        cell.add(valRow, BorderLayout.CENTER);
        cell.add(lbl(note, MONO_XS, DesignSystem.faint()), BorderLayout.SOUTH);
        return cell;
    }

    // ── Leaderboard ─────────────────────────────────────────────────────────

    private JPanel leaderboard(List<DegreeResult> top20Out, List<DegreeResult> top20In) {
        JPanel p = new JPanel(new GridLayout(1, 2, 0, 0));
        p.setBackground(DesignSystem.bg());
        p.setBorder(BorderFactory.createLineBorder(DesignSystem.rule(), 1));

        int maxOut = top20Out.isEmpty() ? 1 : top20Out.get(0).getDegree();
        int maxIn  = top20In.isEmpty()  ? 1 : top20In.get(0).getDegree();

        p.add(leaderColumn("↗  TOP 20 · GRAU DE SAÍDA", "remetentes mais ativos", top20Out, maxOut));
        p.add(leaderColumn("↙  TOP 20 · GRAU DE ENTRADA", "destinatários mais procurados", top20In, maxIn));
        return p;
    }

    private JPanel leaderColumn(String title, String subtitle,
                                List<DegreeResult> entries, int maxDeg) {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(DesignSystem.surface());
        outer.setBorder(new MatteBorder(0, 0, 0, 1, DesignSystem.rule()));

        JPanel hdr = new JPanel(new BorderLayout());
        hdr.setBackground(DesignSystem.surface());
        hdr.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0, 0, 1, 0, DesignSystem.rule()),
                new EmptyBorder(10, 16, 10, 16)
        ));
        hdr.add(lbl(title, MONO_SM, DesignSystem.ink()), BorderLayout.WEST);
        JLabel sub = lbl(subtitle, MONO_XS, DesignSystem.muted());
        sub.setHorizontalAlignment(SwingConstants.RIGHT);
        hdr.add(sub, BorderLayout.EAST);

        JPanel rows = new JPanel();
        rows.setLayout(new BoxLayout(rows, BoxLayout.Y_AXIS));
        rows.setBackground(DesignSystem.surface());
        for (int i = 0; i < entries.size(); i++) {
            rows.add(lbRow(i + 1, entries.get(i), maxDeg));
        }
        rows.add(Box.createVerticalGlue());

        JScrollPane scroll = new JScrollPane(rows);
        scroll.setBorder(null);
        scroll.setBackground(DesignSystem.surface());
        scroll.getViewport().setBackground(DesignSystem.surface());
        styleScrollBar(scroll.getVerticalScrollBar());

        outer.add(hdr, BorderLayout.NORTH);
        outer.add(scroll, BorderLayout.CENTER);
        return outer;
    }

    private JPanel lbRow(int rank, DegreeResult dr, int maxDeg) {
        final int BAR_W = 100;

        JPanel row = new JPanel(new GridBagLayout());
        row.setBackground(DesignSystem.surface());
        row.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0, 0, 1, 0, DesignSystem.rule2()),
                new EmptyBorder(7, 16, 7, 16)
        ));

        GridBagConstraints gc = new GridBagConstraints();
        gc.anchor = GridBagConstraints.WEST;
        gc.insets = new Insets(0, 0, 0, 10);

        gc.gridx = 0; gc.gridy = 0; gc.weightx = 0;
        row.add(lbl(String.format("%02d", rank), MONO_XS, DesignSystem.faint()), gc);

        gc.gridx = 1; gc.weightx = 1; gc.fill = GridBagConstraints.HORIZONTAL;
        JPanel emailCol = new JPanel();
        emailCol.setLayout(new BoxLayout(emailCol, BoxLayout.Y_AXIS));
        emailCol.setOpaque(false);
        emailCol.add(lbl(dr.getEmail(), MONO_SM, DesignSystem.ink()));
        row.add(emailCol, gc);

        gc.gridx = 2; gc.weightx = 0; gc.fill = GridBagConstraints.NONE;
        gc.anchor = GridBagConstraints.EAST;
        row.add(degreeBar(dr.getDegree(), maxDeg, BAR_W), gc);

        row.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { row.setBackground(DesignSystem.bg2()); }
            @Override public void mouseExited(MouseEvent e)  { row.setBackground(DesignSystem.surface()); }
        });
        return row;
    }

    private JPanel degreeBar(int degree, int maxDeg, int barWidth) {
        int fill = maxDeg > 0 ? (int) ((double) degree / maxDeg * barWidth) : 0;

        JPanel p = new JPanel(new BorderLayout(6, 0));
        p.setOpaque(false);

        JLabel num = lbl(String.valueOf(degree), MONO_SM, DesignSystem.accent());
        num.setPreferredSize(new Dimension(36, 14));
        num.setHorizontalAlignment(SwingConstants.RIGHT);

        JPanel track = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                int y = (getHeight() - 6) / 2;
                g.setColor(DesignSystem.bg2());
                g.fillRect(0, y, barWidth, 6);
                g.setColor(DesignSystem.accent());
                g.fillRect(0, y, fill, 6);
            }
        };
        track.setOpaque(false);
        track.setPreferredSize(new Dimension(barWidth, 14));

        p.add(num, BorderLayout.WEST);
        p.add(track, BorderLayout.EAST);
        return p;
    }
}
