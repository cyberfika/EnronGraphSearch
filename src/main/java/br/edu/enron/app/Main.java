package br.edu.enron.app;

import br.edu.enron.graph.ContactGraph;
import br.edu.enron.model.DegreeResult;
import br.edu.enron.parser.EnronDatasetReader;
import br.edu.enron.service.ContactAnalyzer;
import br.edu.enron.view.SearchPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Application entry point for the Enron Graph Analyzer.
 *
 * <p>Always opens a graphical welcome screen — no command-line arguments required.
 * If {@code data/maildir} is found next to the working directory it is
 * pre-selected automatically.</p>
 *
 * <h2>Optional CLI usage</h2>
 * <pre>
 *   mvn exec:java -Dexec.args="data/maildir"            # skip welcome screen
 *   mvn exec:java -Dexec.args="data/maildir --rebuild"  # force cache rebuild
 * </pre>
 */
public class Main {

    // ── colour palette shared by the welcome screen ──────────────────────────
    private static final Color NAVY   = new Color(18,  50, 100);
    private static final Color BLUE   = new Color(30,  90, 170);
    private static final Color GREEN  = new Color(34, 153,  84);
    private static final Color GREY   = new Color(90,  90,  90);
    private static final Color LIGHT  = new Color(245, 247, 252);

    public static void main(String[] args) throws Exception {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

        if (args.length > 0) {
            // CLI path provided — skip welcome screen
            boolean rebuild = List.of(args).contains("--rebuild");
            loadAndRun(Paths.get(args[0]), rebuild);
        } else {
            // Always show the visual welcome screen
            AtomicReference<StartupChoice> choice = new AtomicReference<>();
            SwingUtilities.invokeAndWait(() -> choice.set(showWelcomeScreen()));

            StartupChoice sc = choice.get();
            if (sc == null || sc.cancelled()) return;

            if (sc.useDemo()) {
                runDemoMode();
            } else {
                loadAndRun(Paths.get(sc.path()), sc.rebuild());
            }
        }
    }

    // =========================================================================
    // Welcome screen
    // =========================================================================

    /**
     * Builds and shows the full-window welcome screen.
     * Blocks until the user makes a choice.
     *
     * @return the user's startup choice.
     */
    private static StartupChoice showWelcomeScreen() {
        AtomicReference<StartupChoice> result =
                new AtomicReference<>(new StartupChoice(true, false, null, false));

        JDialog dlg = new JDialog((Frame) null, "Enron Graph Analyzer", true);
        dlg.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dlg.setResizable(false);

        // ── root panel ───────────────────────────────────────────────────────
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(LIGHT);

        // ── header banner ────────────────────────────────────────────────────
        JPanel banner = new JPanel(new BorderLayout());
        banner.setBackground(NAVY);
        banner.setBorder(new EmptyBorder(28, 32, 24, 32));

        JLabel title = new JLabel("Enron Graph Analyzer");
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 26));
        title.setForeground(Color.WHITE);

        JLabel subtitle = new JLabel(
                "Directed contact-graph analysis over the Enron Email Dataset");
        subtitle.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        subtitle.setForeground(new Color(180, 205, 240));

        banner.add(title,    BorderLayout.NORTH);
        banner.add(subtitle, BorderLayout.SOUTH);
        root.add(banner, BorderLayout.NORTH);

        // ── centre: path selector + action cards ─────────────────────────────
        JPanel centre = new JPanel();
        centre.setLayout(new BoxLayout(centre, BoxLayout.Y_AXIS));
        centre.setBackground(LIGHT);
        centre.setBorder(new EmptyBorder(24, 32, 16, 32));

        // Path row
        JLabel pathLbl = new JLabel("Dataset folder (maildir):");
        pathLbl.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        pathLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextField pathField = new JTextField();
        pathField.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        pathField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        pathField.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Auto-detect maildir
        File autoMaildir = new File(System.getProperty("user.dir"), "data/maildir");
        if (autoMaildir.isDirectory()) {
            pathField.setText(autoMaildir.getAbsolutePath());
            pathField.setForeground(new Color(30, 100, 30));
        }

        JButton browseBtn = new JButton("  Browse…  ");
        browseBtn.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        browseBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        browseBtn.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            fc.setDialogTitle("Select the maildir folder");
            fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            fc.setAcceptAllFileFilterUsed(false);
            String current = pathField.getText().trim();
            fc.setCurrentDirectory(current.isEmpty()
                    ? new File(System.getProperty("user.dir"))
                    : new File(current));
            if (fc.showOpenDialog(dlg) == JFileChooser.APPROVE_OPTION) {
                pathField.setText(fc.getSelectedFile().getAbsolutePath());
                pathField.setForeground(new Color(30, 100, 30));
            }
        });

        JPanel pathRow = new JPanel(new BorderLayout(8, 0));
        pathRow.setBackground(LIGHT);
        pathRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        pathRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        pathRow.add(pathField, BorderLayout.CENTER);
        pathRow.add(browseBtn, BorderLayout.EAST);

        JCheckBox rebuildCheck = new JCheckBox("Force cache rebuild (re-parse all files)");
        rebuildCheck.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        rebuildCheck.setBackground(LIGHT);
        rebuildCheck.setAlignmentX(Component.LEFT_ALIGNMENT);

        centre.add(pathLbl);
        centre.add(Box.createVerticalStrut(6));
        centre.add(pathRow);
        centre.add(Box.createVerticalStrut(8));
        centre.add(rebuildCheck);
        centre.add(Box.createVerticalStrut(20));

        // ── action cards ─────────────────────────────────────────────────────
        JPanel cards = new JPanel(new GridLayout(1, 2, 16, 0));
        cards.setBackground(LIGHT);
        cards.setAlignmentX(Component.LEFT_ALIGNMENT);
        cards.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));

        // Card 1 — Load dataset
        JPanel loadCard = actionCard(
                "Load Enron Dataset",
                "Reads 150 user mailboxes,\nbuilds the full contact graph.",
                BLUE, () -> {
                    String path = pathField.getText().trim();
                    if (path.isEmpty()) {
                        JOptionPane.showMessageDialog(dlg,
                                "Please select or browse to the maildir folder.",
                                "No folder selected", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    result.set(new StartupChoice(false, false, path, rebuildCheck.isSelected()));
                    dlg.dispose();
                });

        // Card 2 — Demo mode
        JPanel demoCard = actionCard(
                "Demo Mode",
                "Uses a small built-in graph\n(no dataset files required).",
                GREEN, () -> {
                    result.set(new StartupChoice(false, true, null, false));
                    dlg.dispose();
                });

        cards.add(loadCard);
        cards.add(demoCard);
        centre.add(cards);

        root.add(centre, BorderLayout.CENTER);

        // ── footer ───────────────────────────────────────────────────────────
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.LEFT));
        footer.setBackground(new Color(220, 225, 235));
        footer.setBorder(new EmptyBorder(4, 28, 4, 28));
        JLabel footerLbl = new JLabel(
                "Java 17  ·  GraphStream 2.0  ·  Enron Email Dataset  ·  Graph Theory Project");
        footerLbl.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        footerLbl.setForeground(GREY);
        footer.add(footerLbl);
        root.add(footer, BorderLayout.SOUTH);

        dlg.setContentPane(root);
        dlg.setSize(620, 400);
        dlg.setMinimumSize(new Dimension(580, 370));
        dlg.setLocationRelativeTo(null);
        dlg.setVisible(true);

        return result.get();
    }

    /**
     * Builds a clickable action card with a title, description and colour.
     *
     * @param title   card heading.
     * @param desc    two-line description (use {@code \n} as separator).
     * @param color   card background colour.
     * @param action  action to run when the card is clicked.
     * @return the configured panel.
     */
    private static JPanel actionCard(String title, String desc, Color color, Runnable action) {
        JPanel card = new JPanel(new BorderLayout(0, 6));
        card.setBackground(color);
        card.setBorder(new EmptyBorder(14, 18, 14, 18));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
        titleLbl.setForeground(Color.WHITE);

        String[] lines = desc.split("\n");
        JPanel descPanel = new JPanel(new GridLayout(lines.length, 1, 0, 2));
        descPanel.setOpaque(false);
        for (String line : lines) {
            JLabel l = new JLabel(line);
            l.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
            l.setForeground(new Color(220, 235, 255));
            descPanel.add(l);
        }

        card.add(titleLbl,  BorderLayout.NORTH);
        card.add(descPanel, BorderLayout.CENTER);

        // Click and hover effects
        card.addMouseListener(new MouseAdapter() {
            private final Color normal  = color;
            private final Color hover   = color.brighter();
            @Override public void mouseClicked(MouseEvent e) { action.run(); }
            @Override public void mouseEntered(MouseEvent e) { card.setBackground(hover); card.repaint(); }
            @Override public void mouseExited(MouseEvent e)  { card.setBackground(normal); card.repaint(); }
        });

        return card;
    }

    /** Carries the user's choice out of the welcome screen. */
    private record StartupChoice(boolean cancelled, boolean useDemo, String path, boolean rebuild) {}

    // =========================================================================
    // Dataset mode
    // =========================================================================

    /**
     * Loads or builds the contact graph from the dataset directory, then runs all queries.
     *
     * @param datasetRoot path to the {@code maildir} folder.
     * @param rebuild     if {@code true}, ignores existing cache and re-parses everything.
     */
    private static void loadAndRun(Path datasetRoot, boolean rebuild) {
        EnronDatasetReader reader    = new EnronDatasetReader();
        Path               cacheFile = EnronDatasetReader.cachePath(datasetRoot);

        ContactGraph graph = null;
        if (!rebuild) graph = reader.loadFromCache(cacheFile);

        if (graph == null) {
            System.out.println("Building graph from dataset: " + datasetRoot);
            graph = reader.buildGraph(datasetRoot);
            reader.saveToCache(graph, cacheFile);
        } else {
            System.out.println("Graph loaded from cache: " + cacheFile);
        }

        runAllQueries(graph);
    }

    // =========================================================================
    // Demo mode
    // =========================================================================

    /**
     * Builds a small hand-crafted graph for demonstration.
     *
     * <pre>
     *   alice  →  bob    weight 2
     *   alice  →  carol  weight 1
     *   bob    →  dave   weight 1
     *   carol  →  dave   weight 1
     *   dave   →  eve    weight 3
     *   bob    →  eve    weight 1
     * </pre>
     */
    private static void runDemoMode() {
        System.out.println("=== DEMO MODE ===\n");

        ContactGraph graph = new ContactGraph();
        graph.addEdge("alice@company.com", "bob@company.com");
        graph.addEdge("alice@company.com", "bob@company.com");
        graph.addEdge("alice@company.com", "carol@company.com");
        graph.addEdge("bob@company.com",   "dave@company.com");
        graph.addEdge("carol@company.com", "dave@company.com");
        graph.addEdge("dave@company.com",  "eve@company.com");
        graph.addEdge("dave@company.com",  "eve@company.com");
        graph.addEdge("dave@company.com",  "eve@company.com");
        graph.addEdge("bob@company.com",   "eve@company.com");

        // Register the demo senders as owners so they appear in the From combo
        graph.addOwner("alice@company.com");
        graph.addOwner("bob@company.com");
        graph.addOwner("carol@company.com");
        graph.addOwner("dave@company.com");

        runAllQueries(graph);
    }

    // =========================================================================
    // Shared
    // =========================================================================

    /**
     * Prints statistics to the console and opens the interactive search panel.
     *
     * @param graph the fully built contact graph.
     */
    private static void runAllQueries(ContactGraph graph) {
        ContactAnalyzer analyzer = new ContactAnalyzer(graph);

        separator("1. GRAPH STATISTICS");
        System.out.println("  Vertices : " + analyzer.getVertexCount());
        System.out.println("  Edges    : " + analyzer.getEdgeCount());

        separator("2. TOP 20 OUT-DEGREE (most active senders)");
        printDegreeList(analyzer.getTop20OutDegree());

        separator("3. TOP 20 IN-DEGREE (most contacted recipients)");
        printDegreeList(analyzer.getTop20InDegree());

        separator("4-7. INTERACTIVE SEARCH PANEL");
        System.out.println("  Use the GUI windows to run DFS, BFS, Distance D and Critical Path.");

        SwingUtilities.invokeLater(() -> new SearchPanel(graph));
    }

    private static void separator(String title) {
        System.out.println("\n--- " + title + " ---");
    }

    private static void printDegreeList(List<DegreeResult> results) {
        for (int i = 0; i < results.size(); i++) {
            System.out.printf("  %2d. %s%n", i + 1, results.get(i));
        }
    }
}
