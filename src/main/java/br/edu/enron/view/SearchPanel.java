package br.edu.enron.view;

import br.edu.enron.graph.ContactGraph;
import br.edu.enron.model.DegreeResult;
import br.edu.enron.model.Edge;
import br.edu.enron.model.PathResult;
import br.edu.enron.model.Vertex;
import br.edu.enron.service.BreadthFirstSearch;
import br.edu.enron.service.ContactAnalyzer;
import br.edu.enron.service.CriticalPathService;
import br.edu.enron.service.DepthFirstSearch;
import br.edu.enron.service.DistanceCalculator;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Main application window — forensic dark design system.
 *
 * <p>Layout: full-screen shell with a fixed topbar (44 px), a 264 px sidebar,
 * a card-switched main area (four tabs), and a 24 px status bar. Color palette
 * and typography mirror the {@code design_template/} reference.</p>
 */
public class SearchPanel extends JFrame {

    // ── Color palette ─────────────────────────────────────────────────────────
    private static final Color BG         = new Color(13,  21,  32);
    private static final Color SIDEBAR_BG = new Color(10,  17,  27);
    private static final Color CARD_BG    = new Color(18,  30,  46);
    private static final Color TOPBAR_BG  = new Color( 8,  15,  25);
    private static final Color STATUS_BG  = new Color( 5,  10,  17);
    private static final Color ACCENT     = new Color(212, 170,  37);
    private static final Color BORDER_CLR = new Color(24,  38,  56);
    private static final Color TEXT_PRI   = new Color(220, 228, 240);
    private static final Color TEXT_SEC   = new Color( 90, 112, 138);
    private static final Color TEXT_MUT   = new Color( 45,  60,  78);
    private static final Color GREEN_OK   = new Color( 38, 185,  88);
    private static final Color TAG_BG     = new Color( 20,  35,  55);
    private static final Color ROW_HOVER  = new Color( 22,  36,  54);

    // ── Fonts ─────────────────────────────────────────────────────────────────
    private static final Font MONO_MD  = new Font(Font.MONOSPACED, Font.PLAIN, 12);
    private static final Font MONO_SM  = new Font(Font.MONOSPACED, Font.PLAIN, 11);
    private static final Font MONO_XS  = new Font(Font.MONOSPACED, Font.PLAIN, 10);
    private static final Font SANS_BD  = new Font(Font.SANS_SERIF, Font.BOLD,  12);
    private static final Font SERIF_28 = new Font(Font.SERIF,      Font.BOLD,  28);
    private static final Font SERIF_IT = new Font(Font.SERIF,      Font.BOLD | Font.ITALIC, 28);
    private static final Font NUM_XL   = new Font(Font.SANS_SERIF, Font.BOLD,  34);
    private static final Font NUM_MD   = new Font(Font.SANS_SERIF, Font.BOLD,  16);

    // ── Services ──────────────────────────────────────────────────────────────
    private final ContactGraph        graph;
    private final ContactAnalyzer     analyzer;
    private final DepthFirstSearch    dfs        = new DepthFirstSearch();
    private final BreadthFirstSearch  bfs        = new BreadthFirstSearch();
    private final DistanceCalculator  distCalc   = new DistanceCalculator();
    private final CriticalPathService critPath   = new CriticalPathService();
    private final GraphVisualizer     visualizer = new GraphVisualizer();

    // ── Layout state ──────────────────────────────────────────────────────────
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel     mainCards;
    private JButton          activeTabBtn;

    // ── Status bar labels ─────────────────────────────────────────────────────
    private final JLabel statusLeft;
    private final JLabel statusRight;

    // ── Query controls (per-tab, built once) ──────────────────────────────────
    private JComboBox<String> fromComboDfs;
    private JComboBox<String> toComboDfs;
    private JComboBox<String> fromComboDist;
    private JSpinner          distanceSpinner;
    private JComboBox<String> fromComboCrit;
    private JComboBox<String> toComboCrit;

    // ── Result areas ──────────────────────────────────────────────────────────
    private JTextArea resultDfs;
    private JTextArea resultDist;
    private JTextArea resultCrit;

    // ── Pre-computed stats ────────────────────────────────────────────────────
    private final int    totalMessages;
    private final double density;
    private final List<DegreeResult> top20Out;
    private final List<DegreeResult> top20In;

    // =========================================================================
    // Constructor
    // =========================================================================

    public SearchPanel(ContactGraph graph) {
        super("Enron Graph Analyzer");
        if (graph == null) throw new IllegalArgumentException("Graph must not be null.");
        this.graph    = graph;
        this.analyzer = new ContactAnalyzer(graph);

        this.top20Out      = analyzer.getTop20OutDegree();
        this.top20In       = analyzer.getTop20InDegree();
        this.totalMessages = computeTotalMessages();
        this.density       = computeDensity();

        this.mainCards = new JPanel(cardLayout);
        mainCards.setBackground(BG);

        this.statusLeft = lbl(
            "ANALISADOR · ONLINE · JVM " + jvmShortVersion()
            + " · GRAFO " + graph.vertexCount() + "/" + graph.edgeCount()
            + " · PAINEL · VISÃO GERAL",
            MONO_XS, TEXT_SEC);
        this.statusRight = lbl(currentTime(), MONO_XS, TEXT_MUT);

        buildFrame();
        startClock();

        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(1100, 650));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    // =========================================================================
    // Frame skeleton
    // =========================================================================

    private void buildFrame() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG);
        root.add(buildTopbar(),    BorderLayout.NORTH);
        root.add(buildSidebar(),   BorderLayout.WEST);
        root.add(buildMainArea(),  BorderLayout.CENTER);
        root.add(buildStatusbar(), BorderLayout.SOUTH);
        setContentPane(root);
    }

    // =========================================================================
    // Topbar
    // =========================================================================

    private JPanel buildTopbar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(TOPBAR_BG);
        bar.setPreferredSize(new Dimension(0, 44));
        bar.setBorder(new MatteBorder(0, 0, 1, 0, BORDER_CLR));

        // Brand
        JPanel brand = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        brand.setOpaque(false);
        brand.add(lbl("⬡", new Font(Font.SANS_SERIF, Font.BOLD, 16), ACCENT));
        brand.add(lbl("GRAPH·SEARCH", MONO_MD, TEXT_PRI));

        // Tabs
        JPanel tabs = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        tabs.setOpaque(false);

        String[][] defs = {
            {"overview", "01  VISÃO GERAL"},
            {"dfsbfs",   "02  DFS · BFS"},
            {"distance", "03  DISTÂNCIA D"},
            {"critpath", "04  CAMINHO CRÍTICO"}
        };
        for (String[] def : defs) {
            JButton btn = tabButton(def[0], def[1]);
            tabs.add(btn);
            if ("overview".equals(def[0])) { activeTabBtn = btn; activateTab(btn); }
        }

        // Right
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 0));
        right.setOpaque(false);
        right.add(lbl("ANALISADOR DE CONTATOS · ENRON", MONO_XS, TEXT_MUT));

        bar.add(brand, BorderLayout.WEST);
        bar.add(tabs,  BorderLayout.CENTER);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    private JButton tabButton(String card, String label) {
        JButton btn = new JButton(label);
        btn.setFont(MONO_SM);
        btn.setForeground(TEXT_SEC);
        btn.setBackground(TOPBAR_BG);
        btn.setOpaque(true);
        btn.setContentAreaFilled(true);
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
            statusLeft.setText("ANALISADOR · ONLINE · JVM " + jvmShortVersion()
                + " · GRAFO " + graph.vertexCount() + "/" + graph.edgeCount()
                + " · PAINEL · " + tabName.toUpperCase());
        });
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { if (btn != activeTabBtn) btn.setForeground(TEXT_PRI); }
            @Override public void mouseExited(MouseEvent e)  { if (btn != activeTabBtn) btn.setForeground(TEXT_SEC); }
        });
        return btn;
    }

    private void activateTab(JButton btn) {
        btn.setForeground(TEXT_PRI);
        btn.setBorder(new MatteBorder(0, 0, 2, 0, ACCENT));
        btn.setBorderPainted(true);
    }

    private void deactivateTab(JButton btn) {
        btn.setForeground(TEXT_SEC);
        btn.setBorderPainted(false);
    }

    // =========================================================================
    // Sidebar
    // =========================================================================

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(SIDEBAR_BG);
        sidebar.setPreferredSize(new Dimension(264, 0));
        sidebar.setBorder(new MatteBorder(0, 0, 0, 1, BORDER_CLR));

        // Fixed upper sections
        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.setBackground(SIDEBAR_BG);
        top.add(sideSection("FONTE", "CORPUS"));
        top.add(datasetCard());
        top.add(sideSection("RESUMO", "LIVE"));
        top.add(resumoPanel());
        top.add(sideSection("DIRETÓRIO", String.valueOf(graph.getOwnerEmails().size())));

        sidebar.add(top,                  BorderLayout.NORTH);
        sidebar.add(buildDirectory(),     BorderLayout.CENTER);
        return sidebar;
    }

    private JPanel sideSection(String left, String right) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(new Color(7, 13, 21));
        p.setBorder(new EmptyBorder(6, 10, 6, 10));
        p.setMaximumSize(new Dimension(264, 26));
        p.add(lbl(left,  MONO_XS, TEXT_MUT), BorderLayout.WEST);
        p.add(lbl(right, MONO_XS, TEXT_MUT), BorderLayout.EAST);
        return p;
    }

    private JPanel datasetCard() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(new Color(14, 23, 36));
        p.setBorder(new EmptyBorder(10, 10, 10, 10));
        p.setMaximumSize(new Dimension(264, 72));

        JPanel nameRow = new JPanel(new BorderLayout());
        nameRow.setOpaque(false);
        nameRow.add(lbl("enron-public.maildir", MONO_SM, TEXT_PRI), BorderLayout.WEST);
        nameRow.add(badge("ATIVO", GREEN_OK),                        BorderLayout.EAST);

        JLabel sub = lbl(
            "grafo construído · " + graph.vertexCount() + " vértices · " + graph.edgeCount() + " arestas",
            MONO_XS, TEXT_MUT);

        p.add(nameRow);
        p.add(Box.createVerticalStrut(4));
        p.add(sub);
        return p;
    }

    private JPanel resumoPanel() {
        JPanel p = new JPanel(new GridLayout(4, 2, 0, 2));
        p.setBackground(new Color(11, 19, 31));
        p.setBorder(new EmptyBorder(6, 10, 6, 10));
        p.setMaximumSize(new Dimension(264, 88));

        String[][] rows = {
            {"|V|",       String.valueOf(graph.vertexCount())},
            {"|E|",       String.valueOf(graph.edgeCount())},
            {"Σ pesos",   formatInt(totalMessages)},
            {"Densidade", String.format("%.2f %%", density)}
        };
        for (String[] row : rows) {
            p.add(lbl(row[0], MONO_SM, TEXT_MUT));
            JLabel val = lbl(row[1], MONO_SM, TEXT_PRI);
            val.setHorizontalAlignment(SwingConstants.RIGHT);
            p.add(val);
        }
        return p;
    }

    private JScrollPane buildDirectory() {
        JPanel list = new JPanel();
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.setBackground(SIDEBAR_BG);

        // Sort owners by out-degree descending
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
        scroll.setBackground(SIDEBAR_BG);
        scroll.getViewport().setBackground(SIDEBAR_BG);
        styleScrollBar(scroll.getVerticalScrollBar());
        return scroll;
    }

    private JPanel directoryRow(String email, int degree) {
        String user = email.contains("@") ? email.split("@")[0] : email;

        JPanel row = new JPanel(new BorderLayout(4, 0));
        row.setBackground(SIDEBAR_BG);
        row.setBorder(BorderFactory.createCompoundBorder(
            new MatteBorder(0, 0, 1, 0, new Color(15, 24, 37)),
            new EmptyBorder(6, 10, 6, 10)
        ));
        row.setMaximumSize(new Dimension(264, 34));
        row.setPreferredSize(new Dimension(0, 34));

        JLabel nameLbl   = lbl(user, MONO_XS, TEXT_SEC);
        JLabel degreeLbl = lbl(String.valueOf(degree), MONO_XS, ACCENT);
        degreeLbl.setHorizontalAlignment(SwingConstants.RIGHT);

        row.add(nameLbl,   BorderLayout.CENTER);
        row.add(degreeLbl, BorderLayout.EAST);

        row.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { row.setBackground(ROW_HOVER); nameLbl.setForeground(TEXT_PRI); }
            @Override public void mouseExited(MouseEvent e)  { row.setBackground(SIDEBAR_BG); nameLbl.setForeground(TEXT_SEC); }
        });
        return row;
    }

    // =========================================================================
    // Main card area
    // =========================================================================

    private JPanel buildMainArea() {
        mainCards.add(overviewPanel(), "overview");
        mainCards.add(dfsBfsPanel(),   "dfsbfs");
        mainCards.add(distancePanel(), "distance");
        mainCards.add(critPathPanel(), "critpath");
        cardLayout.show(mainCards, "overview");
        return mainCards;
    }

    // ── Tab 01: Visão Geral ───────────────────────────────────────────────────

    private JPanel overviewPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG);
        panel.add(sectionHeader("§01", "VISÃO GERAL", "GRAFO DIRECIONADO · PONDERADO · ROTULADO"),
                  BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout(0, 16));
        body.setBackground(BG);
        body.setBorder(new EmptyBorder(20, 24, 20, 24));
        body.add(heroBlock(),     BorderLayout.NORTH);

        JPanel lower = new JPanel(new BorderLayout(0, 14));
        lower.setBackground(BG);
        lower.add(statCards(),       BorderLayout.NORTH);
        lower.add(leaderboard(),     BorderLayout.CENTER);

        body.add(lower, BorderLayout.CENTER);

        JScrollPane scroll = new JScrollPane(body);
        scroll.setBorder(null);
        scroll.setBackground(BG);
        scroll.getViewport().setBackground(BG);
        styleScrollBar(scroll.getVerticalScrollBar());
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private JPanel heroBlock() {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setBackground(BG);
        p.setBorder(new EmptyBorder(0, 0, 14, 0));

        // Composite title: normal + italic amber + normal
        JPanel titleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        titleRow.setOpaque(false);
        titleRow.add(lbl("A topologia ", SERIF_28, TEXT_PRI));
        titleRow.add(lbl("de uma corporação", SERIF_IT, ACCENT));
        titleRow.add(lbl(", em arestas.", SERIF_28, TEXT_PRI));

        JLabel body = new JLabel(
            "<html><body style='width:520px'>"
            + "Cada vértice é um indivíduo identificado por seu e-mail. Cada aresta é um envio. "
            + "O peso é a frequência. Os algoritmos de percurso desta tela são adaptados de Dijkstra, "
            + "BFS e DFS — todos com tratamento explícito de ciclos."
            + "</body></html>");
        body.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        body.setForeground(TEXT_SEC);

        // Property tags
        JPanel tags = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        tags.setOpaque(false);
        tags.add(propTag("DIRECIONADO"));
        tags.add(propTag("PONDERADO"));
        tags.add(propTag("ROTULADO"));

        JPanel bodyRow = new JPanel(new BorderLayout(0, 0));
        bodyRow.setOpaque(false);
        bodyRow.add(body, BorderLayout.CENTER);
        bodyRow.add(tags, BorderLayout.EAST);

        p.add(titleRow, BorderLayout.NORTH);
        p.add(bodyRow,  BorderLayout.CENTER);
        return p;
    }

    private JLabel propTag(String text) {
        JLabel l = new JLabel(text);
        l.setFont(MONO_XS);
        l.setForeground(TEXT_SEC);
        l.setOpaque(true);
        l.setBackground(TAG_BG);
        l.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_CLR, 1),
            new EmptyBorder(4, 10, 4, 10)
        ));
        return l;
    }

    private JPanel statCards() {
        JPanel p = new JPanel(new GridLayout(1, 4, 12, 0));
        p.setBackground(BG);
        p.setPreferredSize(new Dimension(0, 120));

        p.add(statCard("VÉRTICES",            formatInt(graph.vertexCount()),
                       "INDIVÍDUOS",          "getNumeroVertices()", TEXT_PRI));
        p.add(statCard("ARESTAS",             formatInt(graph.edgeCount()),
                       "RELAÇÕES",            "getNumeroArestas()", TEXT_PRI));
        p.add(statCard("MENSAGENS (Σ PESOS)", formatInt(totalMessages),
                       "",                    "soma das frequências", TEXT_PRI));
        p.add(statCard("DENSIDADE",           String.format("%.2f", density),
                       "%",                   "grau médio · " + String.format("%.2f",
                            graph.vertexCount() > 1 ? (double) graph.edgeCount() / graph.vertexCount() : 0),
                       ACCENT));
        return p;
    }

    private JPanel statCard(String label, String value, String unit, String sub, Color valueColor) {
        JPanel card = new JPanel(new BorderLayout(0, 6));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_CLR, 1),
            new EmptyBorder(14, 18, 14, 18)
        ));

        JLabel topLbl = lbl(label, MONO_XS, TEXT_MUT);

        JPanel valRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
        valRow.setOpaque(false);
        valRow.add(lbl(value, NUM_XL, valueColor));
        if (!unit.isEmpty()) {
            JLabel unitLbl = lbl(unit, NUM_MD, valueColor);
            unitLbl.setVerticalAlignment(SwingConstants.BOTTOM);
            valRow.add(unitLbl);
        }

        JLabel subLbl = lbl(sub, MONO_XS, TEXT_MUT);

        card.add(topLbl, BorderLayout.NORTH);
        card.add(valRow, BorderLayout.CENTER);
        card.add(subLbl, BorderLayout.SOUTH);
        return card;
    }

    private JPanel leaderboard() {
        JPanel p = new JPanel(new GridLayout(1, 2, 14, 0));
        p.setBackground(BG);

        int maxOut = top20Out.isEmpty() ? 1 : top20Out.get(0).getDegree();
        int maxIn  = top20In.isEmpty()  ? 1 : top20In.get(0).getDegree();

        p.add(leaderboardColumn("↗  TOP 20 · GRAU DE SAÍDA",  "remetentes mais ativos",      top20Out, maxOut));
        p.add(leaderboardColumn("↙  TOP 20 · GRAU DE ENTRADA","destinatários mais procurados", top20In,  maxIn));
        return p;
    }

    private JPanel leaderboardColumn(String title, String subtitle,
                                     List<DegreeResult> entries, int maxDeg) {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(CARD_BG);
        outer.setBorder(BorderFactory.createLineBorder(BORDER_CLR, 1));

        // Header
        JPanel hdr = new JPanel(new BorderLayout());
        hdr.setBackground(new Color(13, 22, 34));
        hdr.setBorder(new EmptyBorder(10, 14, 10, 14));
        hdr.add(lbl(title,    MONO_SM, TEXT_PRI), BorderLayout.WEST);
        JLabel sub = lbl(subtitle, MONO_XS, TEXT_MUT);
        sub.setHorizontalAlignment(SwingConstants.RIGHT);
        hdr.add(sub, BorderLayout.EAST);

        // Rows
        JPanel rows = new JPanel();
        rows.setLayout(new BoxLayout(rows, BoxLayout.Y_AXIS));
        rows.setBackground(CARD_BG);

        for (int i = 0; i < entries.size(); i++) {
            rows.add(lbRow(i + 1, entries.get(i), maxDeg));
        }
        rows.add(Box.createVerticalGlue());

        JScrollPane scroll = new JScrollPane(rows);
        scroll.setBorder(null);
        scroll.setBackground(CARD_BG);
        scroll.getViewport().setBackground(CARD_BG);
        styleScrollBar(scroll.getVerticalScrollBar());

        outer.add(hdr,    BorderLayout.NORTH);
        outer.add(scroll, BorderLayout.CENTER);
        return outer;
    }

    private JPanel lbRow(int rank, DegreeResult dr, int maxDeg) {
        JPanel row = new JPanel(new GridBagLayout());
        row.setBackground(CARD_BG);
        row.setBorder(BorderFactory.createCompoundBorder(
            new MatteBorder(0, 0, 1, 0, new Color(14, 24, 37)),
            new EmptyBorder(7, 14, 7, 14)
        ));

        GridBagConstraints gc = new GridBagConstraints();
        gc.anchor = GridBagConstraints.WEST;
        gc.insets = new Insets(0, 0, 0, 10);

        // Rank
        gc.gridx = 0; gc.gridy = 0; gc.weightx = 0;
        row.add(lbl(String.format("%02d", rank), MONO_XS, TEXT_MUT), gc);

        // Email column (two lines: email + sub)
        gc.gridx = 1; gc.weightx = 1; gc.fill = GridBagConstraints.HORIZONTAL;
        JPanel emailCol = new JPanel();
        emailCol.setLayout(new BoxLayout(emailCol, BoxLayout.Y_AXIS));
        emailCol.setOpaque(false);
        JLabel emailLbl = lbl(dr.getEmail(), MONO_SM, TEXT_PRI);
        emailLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        emailCol.add(emailLbl);
        row.add(emailCol, gc);

        // Degree + amber bar
        gc.gridx = 2; gc.weightx = 0; gc.fill = GridBagConstraints.NONE;
        gc.anchor = GridBagConstraints.EAST;
        row.add(degreeBar(dr.getDegree(), maxDeg), gc);

        // Hover
        Color normal = CARD_BG;
        Color hover  = ROW_HOVER;
        row.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { row.setBackground(hover); }
            @Override public void mouseExited(MouseEvent e)  { row.setBackground(normal); }
        });

        return row;
    }

    private JPanel degreeBar(int degree, int maxDeg) {
        final int BAR_W = 100;
        final int barFill = maxDeg > 0 ? (int) ((double) degree / maxDeg * BAR_W) : 0;

        JPanel p = new JPanel(new BorderLayout(6, 0));
        p.setOpaque(false);

        JLabel numLbl = lbl(String.valueOf(degree), MONO_SM, ACCENT);
        numLbl.setPreferredSize(new Dimension(28, 14));
        numLbl.setHorizontalAlignment(SwingConstants.RIGHT);

        JPanel track = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                int y = (getHeight() - 4) / 2;
                g.setColor(new Color(28, 44, 64));
                g.fillRect(0, y, BAR_W, 4);
                g.setColor(ACCENT);
                g.fillRect(0, y, barFill, 4);
            }
        };
        track.setOpaque(false);
        track.setPreferredSize(new Dimension(BAR_W, 14));

        p.add(numLbl, BorderLayout.WEST);
        p.add(track,  BorderLayout.EAST);
        return p;
    }

    // ── Tab 02: DFS · BFS ────────────────────────────────────────────────────

    private JPanel dfsBfsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG);
        panel.add(sectionHeader("§02", "DFS · BFS", "TRAVESSIA EM PROFUNDIDADE E LARGURA"),
                  BorderLayout.NORTH);

        fromComboDfs = buildCombo(new ArrayList<>(graph.getOwnerEmails()));
        toComboDfs   = buildCombo(sortedEmails());
        selectLastReal(toComboDfs);
        resultDfs = resultArea();

        JPanel controls = queryGrid(
            lbl("DE:",   MONO_SM, TEXT_SEC), fromComboDfs,
            lbl("PARA:", MONO_SM, TEXT_SEC), toComboDfs
        );
        JPanel btnRow = btnRow(
            actionBtn("DFS",          new Color(38,  88, 158), e -> onDfs()),
            actionBtn("BFS",          new Color(30, 120,  74), e -> onBfs()),
            actionBtn("Mostrar Grafo",new Color(35,  50,  70), e -> visualizer.display(graph, "Enron Contact Graph"))
        );

        panel.add(queryArea(controls, btnRow), BorderLayout.NORTH);
        panel.add(resultScroll(resultDfs),     BorderLayout.CENTER);
        return panel;
    }

    // ── Tab 03: Distância D ──────────────────────────────────────────────────

    private JPanel distancePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG);
        panel.add(sectionHeader("§03", "DISTÂNCIA D", "ALCANÇABILIDADE POR NÍVEL"),
                  BorderLayout.NORTH);

        fromComboDist   = buildCombo(new ArrayList<>(graph.getOwnerEmails()));
        distanceSpinner = new JSpinner(new SpinnerNumberModel(2, 0, 20, 1));
        styleSpinner(distanceSpinner);
        resultDist = resultArea();

        JPanel controls = queryGrid(
            lbl("DE:", MONO_SM, TEXT_SEC), fromComboDist,
            lbl("D:",  MONO_SM, TEXT_SEC), distanceSpinner
        );
        JPanel btnRow = btnRow(
            actionBtn("Calcular Distância D", new Color(92, 44, 148), e -> onDistance())
        );

        panel.add(queryArea(controls, btnRow), BorderLayout.NORTH);
        panel.add(resultScroll(resultDist),    BorderLayout.CENTER);
        return panel;
    }

    // ── Tab 04: Caminho Crítico ───────────────────────────────────────────────

    private JPanel critPathPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG);
        panel.add(sectionHeader("§04", "CAMINHO CRÍTICO", "DIJKSTRA ADAPTADO · CUSTO = 1 / PESO"),
                  BorderLayout.NORTH);

        fromComboCrit = buildCombo(new ArrayList<>(graph.getOwnerEmails()));
        toComboCrit   = buildCombo(sortedEmails());
        selectLastReal(toComboCrit);
        resultCrit = resultArea();

        JPanel controls = queryGrid(
            lbl("DE:",   MONO_SM, TEXT_SEC), fromComboCrit,
            lbl("PARA:", MONO_SM, TEXT_SEC), toComboCrit
        );
        JPanel btnRow = btnRow(
            actionBtn("Calcular Caminho Crítico", new Color(148, 40, 40), e -> onCriticalPath())
        );

        panel.add(queryArea(controls, btnRow), BorderLayout.NORTH);
        panel.add(resultScroll(resultCrit),    BorderLayout.CENTER);
        return panel;
    }

    // ── Statusbar ─────────────────────────────────────────────────────────────

    private JPanel buildStatusbar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(STATUS_BG);
        bar.setPreferredSize(new Dimension(0, 24));
        bar.setBorder(new MatteBorder(1, 0, 0, 0, BORDER_CLR));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 3));
        left.setOpaque(false);
        left.add(lbl("■", MONO_XS, GREEN_OK));
        left.add(statusLeft);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 3));
        right.setOpaque(false);
        right.add(lbl("BR.EDU.ENRON.APP.MAIN", MONO_XS, TEXT_MUT));
        right.add(statusRight);

        bar.add(left,  BorderLayout.WEST);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    // =========================================================================
    // Action handlers
    // =========================================================================

    private void onDfs() {
        String from = selectedEmail(fromComboDfs);
        String to   = selectedEmail(toComboDfs);
        PathResult r = dfs.search(graph, from, to);
        appendPath(resultDfs, "DFS", from, to, r);
        if (r.exists()) visualizer.displayWithPath(graph, r, "DFS Path", "DFS");
    }

    private void onBfs() {
        String from = selectedEmail(fromComboDfs);
        String to   = selectedEmail(toComboDfs);
        PathResult r = bfs.search(graph, from, to);
        appendPath(resultDfs, "BFS", from, to, r);
        if (r.exists()) visualizer.displayWithPath(graph, r, "BFS Path", "BFS");
    }

    private void onDistance() {
        String from = selectedEmail(fromComboDist);
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
        prepend(resultDist, sb.toString());
    }

    private void onCriticalPath() {
        String from = selectedEmail(fromComboCrit);
        String to   = selectedEmail(toComboCrit);
        PathResult r = critPath.computeCriticalPath(graph, from, to);

        StringBuilder sb = new StringBuilder();
        sb.append("=== CAMINHO CRÍTICO [").append(from).append("] → [").append(to).append("] ===\n");
        if (!r.exists()) {
            sb.append("  (nenhum caminho encontrado)\n");
        } else {
            sb.append("  Caminho          : ").append(r).append("\n");
            sb.append(String.format("  Custo inverso    : %.4f%n", r.getTotalCost()));
            sb.append(String.format("  Depend. acumulada: %.0f%n",  r.getAccumulatedDependency()));
        }
        prepend(resultCrit, sb.toString());
        if (r.exists()) visualizer.displayWithPath(graph, r, "Caminho Crítico", "Dijkstra");
    }

    // =========================================================================
    // Shared UI builders
    // =========================================================================

    private JPanel sectionHeader(String num, String title, String sub) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(new Color(10, 18, 28));
        p.setBorder(BorderFactory.createCompoundBorder(
            new MatteBorder(0, 0, 1, 0, BORDER_CLR),
            new EmptyBorder(9, 24, 9, 24)
        ));
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        row.setOpaque(false);
        row.add(lbl(num + " ·", MONO_SM, ACCENT));
        row.add(lbl(title,      MONO_SM, TEXT_PRI));
        row.add(lbl("· " + sub, MONO_SM, TEXT_MUT));
        p.add(row, BorderLayout.WEST);
        return p;
    }

    /** Builds a 2-column label/field grid for query controls. */
    private JPanel queryGrid(JComponent... pairs) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);

        GridBagConstraints lc = new GridBagConstraints();
        lc.anchor = GridBagConstraints.WEST;
        lc.insets = new Insets(5, 0, 5, 12);

        GridBagConstraints fc = new GridBagConstraints();
        fc.fill    = GridBagConstraints.HORIZONTAL;
        fc.weightx = 1.0;
        fc.insets  = new Insets(5, 0, 5, 0);

        for (int i = 0; i < pairs.length; i += 2) {
            lc.gridx = 0; lc.gridy = i / 2; p.add(pairs[i],     lc);
            fc.gridx = 1; fc.gridy = i / 2; p.add(pairs[i + 1], fc);
        }
        return p;
    }

    private JPanel queryArea(JPanel controls, JPanel buttons) {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setBackground(CARD_BG);
        p.setBorder(BorderFactory.createCompoundBorder(
            new MatteBorder(0, 0, 1, 0, BORDER_CLR),
            new EmptyBorder(20, 24, 16, 24)
        ));
        p.add(controls, BorderLayout.CENTER);
        p.add(buttons,  BorderLayout.SOUTH);
        return p;
    }

    private JPanel btnRow(JButton... buttons) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        p.setOpaque(false);
        for (JButton b : buttons) p.add(b);
        return p;
    }

    private JButton actionBtn(String label, Color bg, java.util.function.Consumer<ActionEvent> handler) {
        JButton btn = new JButton(label);
        btn.setFont(MONO_SM);
        btn.setForeground(TEXT_PRI);
        btn.setBackground(bg);
        btn.setOpaque(true);
        btn.setContentAreaFilled(true);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setMargin(new Insets(8, 18, 8, 18));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(handler::accept);
        Color hover = bg.brighter();
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setBackground(hover); }
            @Override public void mouseExited(MouseEvent e)  { btn.setBackground(bg); }
        });
        return btn;
    }

    private JTextArea resultArea() {
        JTextArea area = new JTextArea(10, 60);
        area.setEditable(false);
        area.setFont(MONO_SM);
        area.setBackground(new Color(7, 12, 20));
        area.setForeground(new Color(0, 220, 110));
        area.setCaretColor(ACCENT);
        area.setBorder(new EmptyBorder(14, 18, 14, 18));
        return area;
    }

    private JScrollPane resultScroll(JTextArea area) {
        JScrollPane scroll = new JScrollPane(area);
        scroll.setBorder(new MatteBorder(0, 0, 0, 0, BORDER_CLR));
        scroll.setBackground(new Color(7, 12, 20));
        scroll.getViewport().setBackground(new Color(7, 12, 20));
        styleScrollBar(scroll.getVerticalScrollBar());
        return scroll;
    }

    private void styleSpinner(JSpinner spinner) {
        spinner.setFont(MONO_MD);
        spinner.setBackground(CARD_BG);
        spinner.setForeground(TEXT_PRI);
        if (spinner.getEditor() instanceof JSpinner.DefaultEditor de) {
            de.getTextField().setBackground(CARD_BG);
            de.getTextField().setForeground(TEXT_PRI);
            de.getTextField().setFont(MONO_MD);
            de.getTextField().setCaretColor(ACCENT);
        }
    }

    /** Styles a scroll bar to be thin and dark (matches the dark theme). */
    private void styleScrollBar(JScrollBar sb) {
        sb.setBackground(SIDEBAR_BG);
        sb.setUI(new BasicScrollBarUI() {
            @Override protected void configureScrollBarColors() {
                thumbColor = new Color(38, 58, 82);
                trackColor = new Color(10, 17, 27);
            }
            @Override protected JButton createDecreaseButton(int o) { return zeroBtn(); }
            @Override protected JButton createIncreaseButton(int o) { return zeroBtn(); }
            private JButton zeroBtn() {
                JButton b = new JButton();
                b.setPreferredSize(new Dimension(0, 0));
                return b;
            }
        });
    }

    // =========================================================================
    // Combo box builder (alphabetical groups + dark theme)
    // =========================================================================

    private JComboBox<String> buildCombo(List<String> sortedEmails) {
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
        char currentLetter = 0;
        for (String email : sortedEmails) {
            char first = Character.toUpperCase(email.charAt(0));
            if (first != currentLetter) {
                currentLetter = first;
                model.addElement("── " + currentLetter + " ──");
            }
            model.addElement(email);
        }

        JComboBox<String> combo = new JComboBox<>(model);
        combo.setFont(MONO_SM);
        combo.setBackground(CARD_BG);
        combo.setForeground(TEXT_PRI);
        combo.setPreferredSize(new Dimension(460, 28));
        combo.setMaximumRowCount(18);

        combo.setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(
                    JList<?> list, Object value, int index, boolean sel, boolean focus) {
                super.getListCellRendererComponent(list, value, index, sel, focus);
                String item = (String) value;
                if (item != null && item.startsWith("──")) {
                    setFont(MONO_XS);
                    setForeground(ACCENT);
                    setBackground(new Color(10, 18, 30));
                    setEnabled(false);
                } else {
                    setFont(MONO_SM);
                    setForeground(sel ? new Color(10, 18, 30) : TEXT_PRI);
                    setBackground(sel ? ACCENT : CARD_BG);
                }
                return this;
            }
        });

        // Skip separators on selection
        combo.addActionListener(e -> {
            String sel = (String) combo.getSelectedItem();
            if (sel != null && sel.startsWith("──")) {
                int idx = combo.getSelectedIndex();
                if (idx + 1 < combo.getItemCount()) combo.setSelectedIndex(idx + 1);
            }
        });

        // Default: first real email
        for (int i = 0; i < model.getSize(); i++) {
            if (!model.getElementAt(i).startsWith("──")) { combo.setSelectedIndex(i); break; }
        }
        return combo;
    }

    private void selectLastReal(JComboBox<String> combo) {
        DefaultComboBoxModel<String> model = (DefaultComboBoxModel<String>) combo.getModel();
        for (int i = model.getSize() - 1; i >= 0; i--) {
            if (!model.getElementAt(i).startsWith("──")) { combo.setSelectedIndex(i); break; }
        }
    }

    private String selectedEmail(JComboBox<String> combo) {
        int idx = combo.getSelectedIndex();
        if (idx < 0) return "";
        DefaultComboBoxModel<String> model = (DefaultComboBoxModel<String>) combo.getModel();
        for (int i = idx; i < model.getSize(); i++) {
            String item = model.getElementAt(i);
            if (item != null && !item.startsWith("──")) return item.trim();
        }
        return "";
    }

    // =========================================================================
    // Result helpers
    // =========================================================================

    private void appendPath(JTextArea area, String algo, String from, String to, PathResult r) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== ").append(algo)
          .append(" [").append(from).append("] → [").append(to).append("] ===\n");
        if (!r.exists()) {
            sb.append("  (nenhum caminho encontrado)\n");
        } else {
            sb.append("  Caminho : ").append(r).append("\n");
            sb.append("  Saltos  : ").append(r.getVertices().size() - 1).append("\n");
        }
        prepend(area, sb.toString());
    }

    private void prepend(JTextArea area, String text) {
        area.setText(text + "\n" + area.getText());
        area.setCaretPosition(0);
    }

    // =========================================================================
    // Utility
    // =========================================================================

    private List<String> sortedEmails() {
        List<String> emails = new ArrayList<>();
        for (Vertex v : graph.getVertices()) emails.add(v.getEmail());
        Collections.sort(emails);
        return emails;
    }

    private int computeTotalMessages() {
        int total = 0;
        for (Edge e : graph.getEdges()) total += e.getWeight();
        return total;
    }

    private double computeDensity() {
        long v = graph.vertexCount();
        if (v <= 1) return 0.0;
        return 100.0 * graph.edgeCount() / (v * (v - 1));
    }

    private static JLabel lbl(String text, Font font, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(font);
        l.setForeground(color);
        return l;
    }

    private JLabel badge(String text, Color color) {
        JLabel l = new JLabel(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 28));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 5, 5);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        l.setFont(MONO_XS);
        l.setForeground(color);
        l.setOpaque(false);
        l.setBorder(new EmptyBorder(2, 6, 2, 6));
        return l;
    }

    private static String formatInt(int n) {
        // Use dot as thousands separator (Portuguese convention)
        return String.format("%,d", n).replace(',', '.');
    }

    private static String jvmShortVersion() {
        String v = System.getProperty("java.version");
        return v != null ? v : "17";
    }

    private static String currentTime() {
        return LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }

    private void startClock() {
        new javax.swing.Timer(1000, e -> statusRight.setText(currentTime())).start();
    }
}
