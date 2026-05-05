package br.edu.enron.view.component;

import br.edu.enron.design.DesignSystem;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import static br.edu.enron.view.component.SwingHelper.*;

/**
 * Faixa de consulta horizontal correspondente à grade .query do design_template.
 *
 * <p>Cada célula tem um rótulo pequeno em maiúsculas no topo e um controle abaixo.
 * As células são separadas por bordas finas. A última célula pode ser um
 * botão de ação primário (com cor de destaque).</p>
 */
public final class QueryRow extends JPanel {

    public QueryRow() {
        setLayout(new GridBagLayout());
        setBackground(DesignSystem.surface());
        setBorder(BorderFactory.createLineBorder(DesignSystem.rule(), 1));
    }

    // ── Construtores de células ─────────────────────────────────────────────

    /** Adiciona uma célula de caixa de combinação com rótulo. */
    public void addComboCell(String label, String accent, JComboBox<String> combo, double weight) {
        JPanel cell = makeCell(label, accent);
        combo.setPreferredSize(new Dimension(10, 28));
        cell.add(combo, BorderLayout.CENTER);
        addCell(cell, weight);
    }

    /** Adiciona uma célula de seletor numérico (spinner) com rótulo. */
    public void addSpinnerCell(String label, String accent, JSpinner spinner, double weight) {
        JPanel cell = makeCell(label, accent);
        cell.add(spinner, BorderLayout.CENTER);
        addCell(cell, weight);
    }

    /** Adiciona uma célula de alternância de algoritmo (Ex: BFS/DFS). */
    public void addAlgoToggle(String label, String[] options, int defaultIdx,
                              java.util.function.Consumer<String> onSelect) {
        JPanel cell = makeCell(label, null);

        JPanel toggle = new JPanel(new GridLayout(1, options.length, 0, 0));
        toggle.setBorder(BorderFactory.createLineBorder(DesignSystem.rule(), 1));
        toggle.setOpaque(false);

        JButton[] btns = new JButton[options.length];
        for (int i = 0; i < options.length; i++) {
            final int idx = i;
            JButton b = new JButton(options[i]);
            b.setFont(MONO_SM);
            b.setFocusPainted(false);
            b.setBorderPainted(false);
            b.setContentAreaFilled(false);
            b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            b.setPreferredSize(new Dimension(56, 28));
            btns[i] = b;

            b.addActionListener(e -> {
                for (int j = 0; j < btns.length; j++) {
                    btns[j].setBackground(j == idx ? DesignSystem.ink() : new Color(0, 0, 0, 0));
                    btns[j].setForeground(j == idx ? DesignSystem.bg() : DesignSystem.muted());
                    btns[j].setContentAreaFilled(j == idx);
                    btns[j].setOpaque(j == idx);
                }
                onSelect.accept(options[idx]);
            });
            toggle.add(b);
        }
        // Definir estado inicial
        for (int j = 0; j < btns.length; j++) {
            btns[j].setBackground(j == defaultIdx ? DesignSystem.ink() : new Color(0, 0, 0, 0));
            btns[j].setForeground(j == defaultIdx ? DesignSystem.bg() : DesignSystem.muted());
            btns[j].setContentAreaFilled(j == defaultIdx);
            btns[j].setOpaque(j == defaultIdx);
        }

        cell.add(toggle, BorderLayout.CENTER);
        addCell(cell, 0);
    }

    /** Adiciona uma célula de informação estática (ex: "min Σ 1/peso"). */
    public void addInfoCell(String label, String info) {
        JPanel cell = makeCell(label, null);
        JLabel infoLbl = lbl(info, MONO_MD, DesignSystem.ink());
        cell.add(infoLbl, BorderLayout.CENTER);
        addCell(cell, 0);
    }

    /** Adiciona o botão de ação principal (fundo de destaque, ocupa toda a altura). */
    public void addActionButton(String label, ActionListener handler) {
        JButton btn = new JButton(label + "  →") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(getBackground());
                g2.fillRect(0, 0, getWidth(), getHeight());
                super.paintComponent(g);
            }
        };
        btn.setFont(MONO_SM);
        btn.setForeground(new Color(40, 30, 10));
        btn.setBackground(DesignSystem.accent());
        btn.setOpaque(true);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(140, 0));
        btn.addActionListener(handler);

        Color normal = DesignSystem.accent();
        Color hover = normal.brighter();
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setBackground(hover); }
            @Override public void mouseExited(MouseEvent e)  { btn.setBackground(normal); }
        });

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = getComponentCount();
        gc.gridy = 0;
        gc.fill = GridBagConstraints.BOTH;
        gc.weighty = 1;
        add(btn, gc);
    }

    // ── Auxiliares internos ──────────────────────────────────────────────────

    private JPanel makeCell(String label, String accent) {
        JPanel cell = new JPanel(new BorderLayout(0, 4));
        cell.setOpaque(false);
        cell.setBorder(new EmptyBorder(10, 14, 10, 14));

        JPanel lblRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        lblRow.setOpaque(false);
        lblRow.add(lbl(label, MONO_XS, DesignSystem.faint()));
        if (accent != null) {
            lblRow.add(lbl(accent, MONO_XS, DesignSystem.accent()));
        }
        cell.add(lblRow, BorderLayout.NORTH);
        return cell;
    }

    private void addCell(JPanel cell, double weightx) {
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = getComponentCount();
        gc.gridy = 0;
        gc.fill = GridBagConstraints.BOTH;
        gc.weightx = weightx;
        gc.weighty = 1;

        // Adicionar borda esquerda exceto para a primeira célula
        if (gc.gridx > 0) {
            cell.setBorder(BorderFactory.createCompoundBorder(
                    new MatteBorder(0, 1, 0, 0, DesignSystem.rule()),
                    cell.getBorder()
            ));
        }
        add(cell, gc);
    }
}
