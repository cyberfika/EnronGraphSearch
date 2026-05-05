package br.edu.enron.view.panel;

import br.edu.enron.design.DesignSystem;
import br.edu.enron.graph.ContactGraph;
import br.edu.enron.model.Vertex;
import br.edu.enron.service.DistanceCalculator;
import br.edu.enron.view.GraphVisualizer;
import br.edu.enron.view.component.PanelHeader;
import br.edu.enron.view.component.QueryRow;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static br.edu.enron.view.component.SwingHelper.*;

/**
 * Aba 03 — Distância D: consulta por nível BFS.
 */
public final class DistancePanel extends JPanel {

    private final ContactGraph graph;
    private final DistanceCalculator distCalc = new DistanceCalculator();
    private final GraphVisualizer visualizer;

    private final JComboBox<String> fromCombo;
    private final JSpinner distanceSpinner;
    private final JTextArea resultArea;

    public DistancePanel(ContactGraph graph, GraphVisualizer visualizer) {
        this.graph = graph;
        this.visualizer = visualizer;
        setLayout(new BorderLayout());
        setBackground(DesignSystem.bg());

        add(new PanelHeader(
                "§03 · DISTÂNCIA · BFS POR NÍVEIS",
                "Quem está ", "exatamente a D arestas", " daqui.",
                "Busca em largura por níveis a partir de N. Retorna apenas vértices a distância exata D "
                + "— nem antes, nem depois — em ordem alfabética.",
                "D = 3"
        ), BorderLayout.NORTH);

        fromCombo = buildCombo(new ArrayList<>(graph.getOwnerEmails()));
        distanceSpinner = new JSpinner(new SpinnerNumberModel(2, 0, 20, 1));
        styleSpinner(distanceSpinner);

        QueryRow query = new QueryRow();
        query.addComboCell("NÓ N", "ORIGEM", fromCombo, 1);
        query.addSpinnerCell("DISTÂNCIA", "D", distanceSpinner, 0);
        query.addActionButton("COMPUTAR", e -> run());
        query.addActionButton("EGO REDE", e -> {
            String from = selectedEmail(fromCombo);
            int d = (int) distanceSpinner.getValue();
            visualizer.displayEgoNetwork(graph, from, d,
                    "Ego " + d + "-hop — " + from);
        });

        resultArea = resultArea();
        JPanel center = new JPanel(new BorderLayout());
        center.setBackground(DesignSystem.bg());
        center.add(query, BorderLayout.NORTH);
        center.add(resultScroll(resultArea), BorderLayout.CENTER);

        add(center, BorderLayout.CENTER);
    }

    private void run() {
        String from = selectedEmail(fromCombo);
        int d = (int) distanceSpinner.getValue();
        List<Vertex> verts = distCalc.getVerticesAtDistance(graph, from, d);

        StringBuilder sb = new StringBuilder();
        sb.append("=== NÓS A DISTÂNCIA ").append(d).append(" DE [").append(from).append("] ===\n");
        if (verts.isEmpty()) {
            sb.append("  (nenhum encontrado)\n");
        } else {
            for (Vertex v : verts) sb.append("  ").append(v.getEmail()).append("\n");
            sb.append("  Total: ").append(verts.size()).append(" nó(s)\n");
        }
        prepend(resultArea, sb.toString());
    }
}
