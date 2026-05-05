package br.edu.enron.app;

import br.edu.enron.design.DesignSystem;
import br.edu.enron.graph.ContactGraph;
import br.edu.enron.model.DegreeResult;
import br.edu.enron.parser.EnronDatasetReader;
import br.edu.enron.service.ContactAnalyzer;
import br.edu.enron.util.FontManager;
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
 * Ponto de entrada da aplicação para o Enron Graph Analyzer.
 *
 * <p>Sempre abre uma tela gráfica de boas-vindas — nenhum argumento de linha de comando é necessário.
 * Se {@code data/maildir} for encontrado ao lado do diretório de trabalho, ele será
 * pré-selecionado automaticamente.</p>
 *
 * <h2>Uso opcional via CLI</h2>
 * <pre>
 *   mvn exec:java -Dexec.args="data/maildir"            # pular tela de boas-vindas
 *   mvn exec:java -Dexec.args="data/maildir --rebuild"  # forçar reconstrução do cache
 * </pre>
 */
public class Main {

    public static void main(String[] args) throws Exception {
        // Usar Nimbus L&F para controle de cores consistente entre plataformas
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception e) {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
        DesignSystem.applyToUIManager();

        if (args.length > 0) {
            // Caminho CLI fornecido — pular tela de boas-vindas
            boolean rebuild = List.of(args).contains("--rebuild");
            loadAndRun(Paths.get(args[0]), rebuild);
        } else {
            // Sempre mostrar a tela visual de boas-vindas
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
    // Tela de boas-vindas
    // =========================================================================

    /**
     * Constrói e exibe a tela de boas-vindas em janela inteira.
     * Bloqueia até que o usuário faça uma escolha.
     *
     * @return a escolha de inicialização do usuário.
     */
    private static StartupChoice showWelcomeScreen() {
        AtomicReference<StartupChoice> result =
                new AtomicReference<>(new StartupChoice(true, false, null, false));

        JDialog dlg = new JDialog((Frame) null, "Enron Graph Analyzer", true);
        dlg.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dlg.setResizable(false);

        // ── painel raiz ──────────────────────────────────────────────────────
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(DesignSystem.DARK_BG);

        // ── banner de cabeçalho ──────────────────────────────────────────────
        JPanel banner = new JPanel(new BorderLayout());
        banner.setBackground(DesignSystem.DARK_SURFACE);
        banner.setBorder(new EmptyBorder(28, 32, 24, 32));

        JLabel title = new JLabel("Enron Graph Analyzer");
        title.setFont(FontManager.getSansSerifBoldFont(26));
        title.setForeground(DesignSystem.DARK_INK);

        JLabel subtitle = new JLabel(
                "Análise de grafo de contatos direcionado sobre o Enron Email Dataset");
        subtitle.setFont(FontManager.getSansSerifFont(14));
        subtitle.setForeground(DesignSystem.DARK_INK_2);

        banner.add(title,    BorderLayout.NORTH);
        banner.add(subtitle, BorderLayout.SOUTH);
        root.add(banner, BorderLayout.NORTH);

        // ── centro: seletor de caminho + cards de ação ───────────────────────
        JPanel centre = new JPanel();
        centre.setLayout(new BoxLayout(centre, BoxLayout.Y_AXIS));
        centre.setBackground(DesignSystem.DARK_BG);
        centre.setBorder(new EmptyBorder(24, 32, 16, 32));

        // Linha do caminho
        JLabel pathLbl = new JLabel("Pasta do dataset (maildir):");
        pathLbl.setFont(FontManager.getSansSerifBoldFont(13));
        pathLbl.setForeground(DesignSystem.DARK_INK);
        pathLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextField pathField = new JTextField();
        pathField.setFont(FontManager.getMonospacedFont(12));
        pathField.setBackground(DesignSystem.DARK_SURFACE);
        pathField.setForeground(DesignSystem.DARK_INK);
        pathField.setCaretColor(DesignSystem.DARK_INK);
        pathField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(DesignSystem.DARK_RULE, 1),
                BorderFactory.createEmptyBorder(4, 6, 4, 6)));
        pathField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        pathField.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Detecção automática do maildir
        File autoMaildir = new File(System.getProperty("user.dir"), "data/maildir");
        if (autoMaildir.isDirectory()) {
            pathField.setText(autoMaildir.getAbsolutePath());
            pathField.setForeground(DesignSystem.DARK_ACCENT);
        }

        JButton browseBtn = new JButton("  Procurar…  ");
        browseBtn.setFont(FontManager.getSansSerifFont(12));
        browseBtn.setBackground(DesignSystem.DARK_SURFACE_2);
        browseBtn.setForeground(DesignSystem.DARK_INK);
        browseBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        browseBtn.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            fc.setDialogTitle("Selecione a pasta maildir");
            fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            fc.setAcceptAllFileFilterUsed(false);
            String current = pathField.getText().trim();
            fc.setCurrentDirectory(current.isEmpty()
                    ? new File(System.getProperty("user.dir"))
                    : new File(current));
            if (fc.showOpenDialog(dlg) == JFileChooser.APPROVE_OPTION) {
                pathField.setText(fc.getSelectedFile().getAbsolutePath());
                pathField.setForeground(DesignSystem.DARK_ACCENT);
            }
        });

        JPanel pathRow = new JPanel(new BorderLayout(8, 0));
        pathRow.setBackground(DesignSystem.DARK_BG);
        pathRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        pathRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        pathRow.add(pathField, BorderLayout.CENTER);
        pathRow.add(browseBtn, BorderLayout.EAST);

        JCheckBox rebuildCheck = new JCheckBox("Forçar reconstrução do cache (reanalisar arquivos)");
        rebuildCheck.setFont(FontManager.getSansSerifFont(12));
        rebuildCheck.setBackground(DesignSystem.DARK_BG);
        rebuildCheck.setForeground(DesignSystem.DARK_INK_2);
        rebuildCheck.setAlignmentX(Component.LEFT_ALIGNMENT);

        centre.add(pathLbl);
        centre.add(Box.createVerticalStrut(6));
        centre.add(pathRow);
        centre.add(Box.createVerticalStrut(8));
        centre.add(rebuildCheck);
        centre.add(Box.createVerticalStrut(20));

        // ── cards de ação ────────────────────────────────────────────────────
        JPanel cards = new JPanel(new GridLayout(1, 2, 16, 0));
        cards.setBackground(DesignSystem.DARK_BG);
        cards.setAlignmentX(Component.LEFT_ALIGNMENT);
        cards.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));

        // Card 1 — Carregar dataset
        JPanel loadCard = actionCard(
                "Carregar Dataset Enron",
                "Lê 150 caixas de correio,\nconstrói o grafo de contatos.",
                DesignSystem.DARK_ACCENT, () -> {
                    String path = pathField.getText().trim();
                    if (path.isEmpty()) {
                        JOptionPane.showMessageDialog(dlg,
                                "Por favor, selecione ou procure a pasta maildir.",
                                "Pasta não selecionada", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    result.set(new StartupChoice(false, false, path, rebuildCheck.isSelected()));
                    dlg.dispose();
                });

        // Card 2 — Modo demonstração
        JPanel demoCard = actionCard(
                "Modo Demonstração",
                "Usa um grafo pequeno embutido\n(não requer arquivos do dataset).",
                DesignSystem.DARK_SURFACE_2, () -> {
                    result.set(new StartupChoice(false, true, null, false));
                    dlg.dispose();
                });

        cards.add(loadCard);
        cards.add(demoCard);
        centre.add(cards);

        root.add(centre, BorderLayout.CENTER);

        // ── rodapé ───────────────────────────────────────────────────────────
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.LEFT));
        footer.setBackground(DesignSystem.DARK_SURFACE);
        footer.setBorder(new EmptyBorder(4, 28, 4, 28));
        JLabel footerLbl = new JLabel(
                "Java 17  ·  GraphStream 2.0  ·  Enron Email Dataset  ·  Projeto de Teoria dos Grafos");
        footerLbl.setFont(FontManager.getSansSerifFont(11));
        footerLbl.setForeground(DesignSystem.DARK_MUTED);
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
     * Constrói um card de ação clicável com título, descrição e cor.
     *
     * @param title   título do card.
     * @param desc    descrição em duas linhas (use {@code \n} como separador).
     * @param color   cor de fundo do card.
     * @param action  ação a ser executada quando o card for clicado.
     * @return o painel configurado.
     */
    private static JPanel actionCard(String title, String desc, Color color, Runnable action) {
        JPanel card = new JPanel(new BorderLayout(0, 6));
        card.setBackground(color);
        card.setBorder(new EmptyBorder(14, 18, 14, 18));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(FontManager.getSansSerifBoldFont(15));
        titleLbl.setForeground(DesignSystem.DARK_INK);

        String[] lines = desc.split("\n");
        JPanel descPanel = new JPanel(new GridLayout(lines.length, 1, 0, 2));
        descPanel.setOpaque(false);
        for (String line : lines) {
            JLabel l = new JLabel(line);
            l.setFont(FontManager.getSansSerifFont(12));
            l.setForeground(DesignSystem.DARK_INK_2);
            descPanel.add(l);
        }

        card.add(titleLbl,  BorderLayout.NORTH);
        card.add(descPanel, BorderLayout.CENTER);

        // Efeitos de clique e hover
        card.addMouseListener(new MouseAdapter() {
            private final Color normal  = color;
            private final Color hover   = color.brighter();
            @Override public void mouseClicked(MouseEvent e) { action.run(); }
            @Override public void mouseEntered(MouseEvent e) { card.setBackground(hover); card.repaint(); }
            @Override public void mouseExited(MouseEvent e)  { card.setBackground(normal); card.repaint(); }
        });

        return card;
    }

    /** Carrega a escolha do usuário feita na tela de boas-vindas. */
    private record StartupChoice(boolean cancelled, boolean useDemo, String path, boolean rebuild) {}

    // =========================================================================
    // Modo Dataset
    // =========================================================================

    /**
     * Carrega ou constrói o grafo de contatos a partir do diretório do dataset e, em seguida, executa todas as consultas.
     *
     * @param datasetRoot caminho para a pasta {@code maildir}.
     * @param rebuild     se {@code true}, ignora o cache existente e analisa tudo novamente.
     */
    private static void loadAndRun(Path datasetRoot, boolean rebuild) {
        EnronDatasetReader reader    = new EnronDatasetReader();
        Path               cacheFile = EnronDatasetReader.cachePath(datasetRoot);

        ContactGraph graph = null;
        if (!rebuild) graph = reader.loadFromCache(cacheFile);

        if (graph == null) {
            System.out.println("Construindo grafo a partir do dataset: " + datasetRoot);
            graph = reader.buildGraph(datasetRoot);
            reader.saveToCache(graph, cacheFile);
        } else {
            System.out.println("Grafo carregado do cache: " + cacheFile);
        }

        runAllQueries(graph);
    }

    // =========================================================================
    // Modo Demonstração
    // =========================================================================

    /**
     * Constrói um pequeno grafo manual para demonstração.
     *
     * <pre>
     *   alice  →  bob    peso 2
     *   alice  →  carol  peso 1
     *   bob    →  dave   peso 1
     *   carol  →  dave   peso 1
     *   dave   →  eve    peso 3
     *   bob    →  eve    peso 1
     * </pre>
     */
    private static void runDemoMode() {
        System.out.println("=== MODO DEMONSTRAÇÃO ===\n");

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

        // Registrar os remetentes da demo como proprietários para que apareçam no combo De
        graph.addOwner("alice@company.com");
        graph.addOwner("bob@company.com");
        graph.addOwner("carol@company.com");
        graph.addOwner("dave@company.com");

        runAllQueries(graph);
    }

    // =========================================================================
    // Compartilhado
    // =========================================================================

    /**
     * Imprime estatísticas no console e abre o painel de busca interativo.
     *
     * @param graph o grafo de contatos totalmente construído.
     */
    private static void runAllQueries(ContactGraph graph) {
        ContactAnalyzer analyzer = new ContactAnalyzer(graph);

        separator("1. ESTATÍSTICAS DO GRAFO");
        System.out.println("  Vértices : " + analyzer.getVertexCount());
        System.out.println("  Arestas  : " + analyzer.getEdgeCount());

        separator("2. TOP 20 GRAU DE SAÍDA (remetentes mais ativos)");
        printDegreeList(analyzer.getTop20OutDegree());

        separator("3. TOP 20 GRAU DE ENTRADA (destinatários mais procurados)");
        printDegreeList(analyzer.getTop20InDegree());

        separator("4-7. PAINEL DE BUSCA INTERATIVO");
        System.out.println("  Use as janelas da interface gráfica para executar DFS, BFS, Distância D e Caminho Crítico.");

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

    /**
     * Força as cores do tema escuro globalmente no UIManager (corrige fundos brancos no Windows).
     * Isso é chamado APÓS definir o Look & Feel para substituir seus padrões.
     */
    private static void forceDarkThemeOnUIManager() {
        Color darkBg     = DesignSystem.DARK_SURFACE;
        Color darkText   = DesignSystem.DARK_INK_2;
        Color darkBorder = DesignSystem.DARK_RULE;

        // ComboBox
        UIManager.put("ComboBox.background", darkBg);
        UIManager.put("ComboBox.foreground", darkText);
        UIManager.put("ComboBox.buttonBackground", darkBg);

        // Editor do ComboBox
        UIManager.put("ComboBoxEditor.background", darkBg);
        UIManager.put("ComboBoxEditor.foreground", darkText);
        UIManager.put("ComboBoxEditor.border", darkBorder);

        // Lista popup do ComboBox
        UIManager.put("List.background", darkBg);
        UIManager.put("List.foreground", darkText);
        UIManager.put("List.selectionBackground", DesignSystem.DARK_ACCENT);
        UIManager.put("List.selectionForeground", DesignSystem.DARK_BG);

        // TextField
        UIManager.put("TextField.background", darkBg);
        UIManager.put("TextField.foreground", darkText);

        // TextArea
        UIManager.put("TextArea.background", darkBg);
        UIManager.put("TextArea.foreground", darkText);

        // Spinner
        UIManager.put("Spinner.background", darkBg);
        UIManager.put("Spinner.foreground", darkText);
    }
}
