package br.edu.enron.view.component;

import br.edu.enron.design.DesignSystem;
import br.edu.enron.graph.ContactGraph;
import br.edu.enron.model.DegreeResult;
import br.edu.enron.model.Vertex;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static br.edu.enron.view.component.SwingHelper.*;

/**
 * Barra lateral esquerda correspondente à barra lateral do design_template.
 *
 * <p>Seções: FONTE / CORPUS, card do dataset, RESUMO / LIVE, lista DIRETÓRIO.</p>
 */
public final class Sidebar extends JPanel {
    private static final int WIDTH = 264;

    public Sidebar(ContactGraph graph, int totalMessages, double density) {
        setLayout(new BorderLayout());
        setBackground(DesignSystem.bg());
        setPreferredSize(new Dimension(WIDTH, 0));
        setMinimumSize(new Dimension(WIDTH, 0));
        setMaximumSize(new Dimension(WIDTH, Integer.MAX_VALUE));
        setBorder(new MatteBorder(0, 0, 0, 1, DesignSystem.rule()));

        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.setBackground(DesignSystem.bg());
        top.setMaximumSize(new Dimension(WIDTH, Integer.MAX_VALUE));
        top.setAlignmentX(Component.LEFT_ALIGNMENT);

        top.add(sideSection("FONTE", "CORPUS"));
        top.add(fonteInfo(graph));
        top.add(datasetSection(graph));
        top.add(sideSection("RESUMO", "LIVE"));
        top.add(resumoPanel(graph, totalMessages, density));
        top.add(sideSection("DIRETÓRIO",
                String.valueOf(graph.getOwnerEmails().size())));

        add(top, BorderLayout.NORTH);
        add(buildDirectory(graph), BorderLayout.CENTER);
    }

    // ── Construtores internos ───────────────────────────────────────────────

    private JPanel sideSection(String left, String right) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(DesignSystem.bg());
        p.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0, 0, 1, 0, DesignSystem.rule()),
                new EmptyBorder(8, 14, 8, 14)
        ));
        p.setPreferredSize(new Dimension(WIDTH, 32));
        p.setMaximumSize(new Dimension(WIDTH, 32));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(lbl(left, MONO_XS, DesignSystem.faint()), BorderLayout.WEST);
        JLabel r = lbl(right, MONO_XS, DesignSystem.muted());
        r.setHorizontalAlignment(SwingConstants.RIGHT);
        p.add(r, BorderLayout.EAST);
        return p;
    }

    private JPanel fonteInfo(ContactGraph graph) {
        int totalWeight = 0;
        for (var e : graph.getEdges()) totalWeight += e.getWeight();

        JPanel p = new JPanel(new GridLayout(3, 2, 0, 2));
        p.setBackground(DesignSystem.bg());
        p.setBorder(new EmptyBorder(6, 14, 6, 14));
        p.setPreferredSize(new Dimension(WIDTH, 64));
        p.setMaximumSize(new Dimension(WIDTH, 64));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);

        addKV(p, "Dataset", "enron v1.0");
        addKV(p, "Mensagens", formatInt(totalWeight));
        addKV(p, "Janela", "1999–2002");
        return p;
    }

    private JPanel datasetSection(ContactGraph graph) {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(DesignSystem.bg());
        outer.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0, 0, 1, 0, DesignSystem.rule()),
                new EmptyBorder(12, 14, 12, 18)
        ));
        outer.setPreferredSize(new Dimension(WIDTH, 90));
        outer.setMaximumSize(new Dimension(WIDTH, 90));
        outer.setAlignmentX(Component.LEFT_ALIGNMENT);
        outer.add(datasetCard(graph), BorderLayout.CENTER);
        return outer;
    }

    private JPanel datasetCard(ContactGraph graph) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(DesignSystem.bg2());
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(DesignSystem.rule(), 1),
                new EmptyBorder(10, 14, 10, 14)
        ));
        p.setPreferredSize(new Dimension(WIDTH - 32, 64));
        p.setMaximumSize(new Dimension(WIDTH - 32, 64));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel nameRow = new JPanel(new BorderLayout());
        nameRow.setOpaque(false);
        nameRow.add(lbl("enron-public.maildir", MONO_SM, DesignSystem.ink()), BorderLayout.WEST);
        nameRow.add(badge("ATIVO", DesignSystem.accent()), BorderLayout.EAST);

        JLabel sub = lbl(
                "grafo construído · " + graph.vertexCount()
                        + " vértices · " + graph.edgeCount() + " arestas",
                MONO_XS, DesignSystem.muted());

        p.add(nameRow);
        p.add(Box.createVerticalStrut(4));
        p.add(sub);
        return p;
    }

    private JPanel resumoPanel(ContactGraph graph, int totalMessages, double density) {
        JPanel p = new JPanel(new GridLayout(4, 2, 0, 2));
        p.setBackground(DesignSystem.bg());
        p.setBorder(new EmptyBorder(6, 14, 6, 14));
        p.setPreferredSize(new Dimension(WIDTH, 88));
        p.setMaximumSize(new Dimension(WIDTH, 88));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);

        addKV(p, "|V|", String.valueOf(graph.vertexCount()));
        addKV(p, "|E|", String.valueOf(graph.edgeCount()));
        addKV(p, "Σ pesos", formatInt(totalMessages));
        addKV(p, "Densidade", String.format("%.2f %%", density));
        return p;
    }

    private void addKV(JPanel p, String key, String val) {
        p.add(lbl(key, MONO_SM, DesignSystem.muted()));
        JLabel v = lbl(val, MONO_SM, DesignSystem.ink());
        v.setHorizontalAlignment(SwingConstants.RIGHT);
        p.add(v);
    }

    private JScrollPane buildDirectory(ContactGraph graph) {
        JPanel list = new JPanel();
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.setBackground(DesignSystem.bg());
        list.setAlignmentX(Component.LEFT_ALIGNMENT);

        List<DegreeResult> sorted = new ArrayList<>();
        for (String email : graph.getOwnerEmails()) {
            graph.findVertex(email).ifPresent(v ->
                    sorted.add(new DegreeResult(email, graph.outDegree(v))));
        }
        sorted.sort(Comparator.comparingInt(DegreeResult::getDegree).reversed());

        for (DegreeResult dr : sorted) {
            list.add(directoryRow(dr.getEmail(), dr.getDegree()));
        }
        list.add(Box.createVerticalGlue());

        JScrollPane scroll = new JScrollPane(list);
        scroll.setBorder(null);
        scroll.setBackground(DesignSystem.bg());
        scroll.getViewport().setBackground(DesignSystem.bg());
        styleScrollBar(scroll.getVerticalScrollBar());
        return scroll;
    }

    private JPanel directoryRow(String email, int degree) {
        String user = email.contains("@") ? email.split("@")[0] : email;

        JPanel row = new JPanel(new BorderLayout(4, 0));
        row.setBackground(DesignSystem.bg());
        row.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0, 0, 1, 0, DesignSystem.rule2()),
                new EmptyBorder(6, 14, 6, 14)
        ));
        row.setMaximumSize(new Dimension(WIDTH, 34));
        row.setPreferredSize(new Dimension(WIDTH, 34));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel nameLbl = lbl(user, MONO_SM, DesignSystem.ink2());
        JLabel degLbl = lbl(String.valueOf(degree), MONO_XS, DesignSystem.muted());
        degLbl.setHorizontalAlignment(SwingConstants.RIGHT);

        row.add(nameLbl, BorderLayout.CENTER);
        row.add(degLbl, BorderLayout.EAST);

        row.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                row.setBackground(DesignSystem.bg2());
                nameLbl.setForeground(DesignSystem.ink());
            }
            @Override public void mouseExited(MouseEvent e) {
                row.setBackground(DesignSystem.bg());
                nameLbl.setForeground(DesignSystem.ink2());
            }
        });
        return row;
    }
}
