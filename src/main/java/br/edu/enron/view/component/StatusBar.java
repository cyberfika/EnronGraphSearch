package br.edu.enron.view.component;

import br.edu.enron.design.DesignSystem;
import br.edu.enron.graph.ContactGraph;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import static br.edu.enron.view.component.SwingHelper.*;

/**
 * Bottom status bar matching the design_template statusbar.
 *
 * <p>Shows: live indicator, JVM version, graph stats, active panel name,
 * main class path, and a ticking clock.</p>
 */
public final class StatusBar extends JPanel {

    private final JLabel panelLabel;
    private final JLabel clockLabel;

    public StatusBar(ContactGraph graph) {
        setLayout(new BorderLayout());
        setBackground(DesignSystem.bg());
        setPreferredSize(new Dimension(0, 24));
        setBorder(new MatteBorder(1, 0, 0, 0, DesignSystem.rule()));

        String jvm = System.getProperty("java.version", "17");

        panelLabel = lbl("VISÃO GERAL", MONO_XS, DesignSystem.ink2());
        clockLabel = lbl(currentTime(), MONO_XS, DesignSystem.muted());

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 3));
        left.setOpaque(false);
        left.add(lbl("■", MONO_XS, DesignSystem.accent()));
        left.add(lbl("ANALISADOR · ", MONO_XS, DesignSystem.muted()));
        left.add(lbl("ONLINE", MONO_XS, DesignSystem.ink()));
        left.add(lbl("· JVM " + jvm
                + " · GRAFO " + graph.vertexCount() + "/" + graph.edgeCount()
                + " · PAINEL ·", MONO_XS, DesignSystem.muted()));
        left.add(panelLabel);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 3));
        right.setOpaque(false);
        right.add(lbl("BR.EDU.ENRON.APP.MAIN", MONO_XS, DesignSystem.muted()));
        right.add(clockLabel);

        add(left, BorderLayout.WEST);
        add(right, BorderLayout.EAST);

        // Tick every second
        new Timer(1000, e -> clockLabel.setText(currentTime())).start();
    }

    /** Update the active-panel portion of the status bar. */
    public void setActivePanel(String name) {
        panelLabel.setText(name.toUpperCase());
    }

    private static String currentTime() {
        return LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }
}
