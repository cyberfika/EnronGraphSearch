package br.edu.enron.view;

import br.edu.enron.design.DesignSystem;
import br.edu.enron.graph.ContactGraph;
import br.edu.enron.model.Edge;
import br.edu.enron.service.ContactAnalyzer;
import br.edu.enron.util.FontManager;
import br.edu.enron.view.component.Sidebar;
import br.edu.enron.view.component.StatusBar;
import br.edu.enron.view.panel.CriticalPathPanel;
import br.edu.enron.view.panel.DfsBfsPanel;
import br.edu.enron.view.panel.DistancePanel;
import br.edu.enron.view.panel.OverviewPanel;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import static br.edu.enron.view.component.SwingHelper.*;

/**
 * Janela principal da aplicação — uma estrutura leve que compõe as classes de painel e componentes.
 *
 * <p>Layout: estrutura em tela cheia com uma barra superior fixa (44 px), uma barra lateral de 264 px,
 * uma área de cards principal (quatro abas) e uma barra de status de 24 px.</p>
 */
public class SearchPanel extends JFrame {

    private final ContactGraph    graph;
    private final GraphVisualizer visualizer = new GraphVisualizer();
    private final int             totalMessages;
    private final double          density;

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel     mainCards;
    private JButton          activeTabBtn;
    private TweaksPanel      tweaksPanel;
    private StatusBar        statusBar;

    public SearchPanel(ContactGraph graph) {
        super("Enron Graph Analyzer");
        if (graph == null) throw new IllegalArgumentException("Graph must not be null.");
        this.graph         = graph;
        this.totalMessages = computeTotalMessages();
        this.density       = computeDensity();
        this.mainCards     = new JPanel(cardLayout);
        mainCards.setBackground(DesignSystem.bg());

        buildFrame();
        initTweaksPanel();

        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(1100, 650));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    // ── Esqueleto da janela (Frame skeleton) ───────────────────────────────

    private void buildFrame() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(DesignSystem.bg());
        root.add(buildTopbar(),                                      BorderLayout.NORTH);
        root.add(new Sidebar(graph, totalMessages, density),         BorderLayout.WEST);
        root.add(buildMainArea(),                                    BorderLayout.CENTER);

        statusBar = new StatusBar(graph);
        root.add(statusBar, BorderLayout.SOUTH);

        setContentPane(root);
    }

    // ── Barra Superior (Topbar) ───────────────────────────────────────────

    private JPanel buildTopbar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(DesignSystem.bg());
        bar.setPreferredSize(new Dimension(0, 44));
        bar.setBorder(new MatteBorder(0, 0, 1, 0, DesignSystem.rule()));

        // Marca (Brand)
        JPanel brand = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        brand.setOpaque(false);
        brand.add(lbl("\u2B21", new Font(Font.SANS_SERIF, Font.BOLD, 16), DesignSystem.accent()));
        brand.add(lbl("GRAPH\u00B7SEARCH", MONO_MD, DesignSystem.ink()));

        // Abas (Tabs)
        JPanel tabs = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        tabs.setOpaque(false);

        String[][] defs = {
            {"overview", "01  VIS\u00C3O GERAL"},
            {"dfsbfs",   "02  DFS \u00B7 BFS"},
            {"distance", "03  DIST\u00C2NCIA D"},
            {"critpath", "04  CAMINHO CR\u00CDTICO"}
        };
        for (String[] def : defs) {
            JButton btn = tabButton(def[0], def[1]);
            tabs.add(btn);
            if ("overview".equals(def[0])) { activeTabBtn = btn; activateTab(btn); }
        }

        // Lado direito — botão de ajustes + rótulo
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        right.add(buildTweaksButton());
        right.add(lbl("ANALISADOR DE CONTATOS \u00B7 ENRON", MONO_XS, DesignSystem.muted()));

        bar.add(brand, BorderLayout.WEST);
        bar.add(tabs,  BorderLayout.CENTER);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    private JButton buildTweaksButton() {
        JButton btn = new JButton("\u2699") {
            @Override protected void paintComponent(Graphics g) {
                ((Graphics2D) g).setColor(getBackground());
                ((Graphics2D) g).fillRect(0, 0, getWidth(), getHeight());
                super.paintComponent(g);
            }
        };
        btn.setFont(FontManager.getSansSerifFont(14));
        btn.setForeground(DesignSystem.ink2());
        btn.setBackground(DesignSystem.bg());
        btn.setOpaque(true);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(44, 44));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> {
            if (tweaksPanel != null) tweaksPanel.setVisible(!tweaksPanel.isVisible());
        });
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setForeground(DesignSystem.ink()); }
            @Override public void mouseExited(MouseEvent e)  { btn.setForeground(DesignSystem.ink2()); }
        });
        return btn;
    }

    private JButton tabButton(String card, String label) {
        JButton btn = new JButton(label) {
            @Override protected void paintComponent(Graphics g) {
                ((Graphics2D) g).setColor(getBackground());
                ((Graphics2D) g).fillRect(0, 0, getWidth(), getHeight());
                super.paintComponent(g);
            }
        };
        btn.setFont(MONO_SM);
        btn.setForeground(DesignSystem.ink2());
        btn.setBackground(DesignSystem.bg());
        btn.setOpaque(true);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setMargin(new Insets(0, 20, 0, 20));
        btn.setPreferredSize(new Dimension(btn.getPreferredSize().width + 8, 44));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btn.addActionListener(e -> {
            cardLayout.show(mainCards, card);
            if (activeTabBtn != null) deactivateTab(activeTabBtn);
            activeTabBtn = btn;
            activateTab(btn);
            String tabName = label.replaceAll("^\\d+\\s+", "").trim();
            if (statusBar != null) statusBar.setActivePanel(tabName);
        });
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { if (btn != activeTabBtn) btn.setForeground(DesignSystem.ink()); }
            @Override public void mouseExited(MouseEvent e)  { if (btn != activeTabBtn) btn.setForeground(DesignSystem.ink2()); }
        });
        return btn;
    }

    private void activateTab(JButton btn) {
        btn.setForeground(DesignSystem.ink());
        btn.setBorder(new MatteBorder(0, 0, 3, 0, DesignSystem.accent()) {
            @Override public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(DesignSystem.accent());
                g2.fillRect(x, y + h - 3, w, 3);
            }
        });
        btn.setBorderPainted(true);
    }

    private void deactivateTab(JButton btn) {
        btn.setForeground(DesignSystem.ink2());
        btn.setBorderPainted(false);
    }

    // ── Área principal de cards (Main card area) ───────────────────────────

    private JPanel buildMainArea() {
        ContactAnalyzer analyzer = new ContactAnalyzer(graph);

        mainCards.add(new OverviewPanel(graph, analyzer.getTop20OutDegree(),
                analyzer.getTop20InDegree(), totalMessages, density), "overview");
        mainCards.add(new DfsBfsPanel(graph, visualizer),   "dfsbfs");
        mainCards.add(new DistancePanel(graph, visualizer),   "distance");
        mainCards.add(new CriticalPathPanel(graph, visualizer), "critpath");

        cardLayout.show(mainCards, "overview");
        return mainCards;
    }

    // ── Ajustes / tema (Tweaks / theme) ────────────────────────────────────

    private void initTweaksPanel() {
        tweaksPanel = new TweaksPanel(this);
        tweaksPanel.setThemeChangeListener(this::refreshTheme);
    }

    private void refreshTheme() {
        SwingUtilities.invokeLater(() -> {
            getContentPane().removeAll();
            buildFrame();
            revalidate();
            repaint();
        });
    }

    // ── Utilitários (Utility) ───────────────────────────────────────────────

    private int computeTotalMessages() {
        int total = 0;
        for (Edge e : graph.getEdges()) total += e.getWeight();
        return total;
    }

    private double computeDensity() {
        long v = graph.vertexCount();
        return v <= 1 ? 0.0 : 100.0 * graph.edgeCount() / (v * (v - 1));
    }
}
