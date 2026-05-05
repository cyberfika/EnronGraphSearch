package br.edu.enron.view.component;

import br.edu.enron.design.DesignSystem;
import br.edu.enron.util.FontManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Métodos de fábrica Swing compartilhados e utilitários de estilo.
 * Centraliza a criação de rótulos, a estilização da barra de rolagem, a construção de caixas de combinação 
 * e outras construções repetitivas de UI para que as classes de painel permaneçam enxutas.
 */
public final class SwingHelper {

    // ── Fontes (resolvidas uma vez, reutilizadas em todos os lugares) ─────────
    public static final Font MONO_MD  = FontManager.getMonospacedFont(12);
    public static final Font MONO_SM  = FontManager.getMonospacedFont(11);
    public static final Font MONO_XS  = FontManager.getMonospacedFont(10);
    public static final Font SANS_BD  = FontManager.getSansSerifBoldFont(12);
    public static final Font SERIF_28 = FontManager.getSerifBoldFont(28);
    public static final Font SERIF_IT = FontManager.getSerifBoldItalicFont(28);
    public static final Font NUM_XL   = FontManager.getSerifBoldFont(44);
    public static final Font NUM_MD   = FontManager.getSerifBoldFont(16);

    private SwingHelper() { /* utilitário */ }

    // ── Fábrica de rótulos ───────────────────────────────────────────────────

    public static JLabel lbl(String text, Font font, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(font);
        l.setForeground(color);
        return l;
    }

    /** Crachá (badge) correspondente a .dataset-card .badge do design web. */
    public static JLabel badge(String text, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(MONO_XS);
        l.setForeground(DesignSystem.bg());
        l.setBackground(color);
        l.setOpaque(true);
        l.setBorder(new EmptyBorder(1, 6, 1, 6));
        return l;
    }

    /** Pílula de etiqueta de propriedade (rótulo com borda). */
    public static JLabel propTag(String text) {
        JLabel l = new JLabel(text);
        l.setFont(MONO_XS);
        l.setForeground(DesignSystem.ink2());
        l.setOpaque(true);
        l.setBackground(DesignSystem.surface2());
        l.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(DesignSystem.rule(), 1),
                new EmptyBorder(4, 10, 4, 10)
        ));
        return l;
    }

    // ── Estilização da barra de rolagem ──────────────────────────────────────

    public static void styleScrollBar(JScrollBar sb) {
        sb.setBackground(DesignSystem.bg());
        sb.setUI(new BasicScrollBarUI() {
            @Override protected void configureScrollBarColors() {
                thumbColor = DesignSystem.rule();
                trackColor = DesignSystem.bg();
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

    // ── Construtor de caixa de combinação (grupos alfabéticos + tema escuro) ──

    public static JComboBox<String> buildCombo(List<String> sortedEmails) {
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
        char currentLetter = 0;
        List<String> sorted = new ArrayList<>(sortedEmails);
        sorted.sort(String::compareToIgnoreCase);
        for (String email : sorted) {
            char first = Character.toUpperCase(email.charAt(0));
            if (first != currentLetter) {
                currentLetter = first;
                model.addElement("── " + currentLetter + " ──");
            }
            model.addElement(email);
        }

        JComboBox<String> combo = new JComboBox<>(model);
        combo.setFont(MONO_SM);
        combo.setPreferredSize(new Dimension(460, 28));
        combo.setMaximumRowCount(18);
        combo.setBackground(DesignSystem.surface());
        combo.setForeground(DesignSystem.ink());
        combo.setOpaque(true);
        combo.setBorder(BorderFactory.createEmptyBorder());
        combo.setFocusable(false);
        combo.setUI(new BasicComboBoxUI() {
            @Override protected JButton createArrowButton() {
                JButton button = new JButton("▾");
                button.setFont(MONO_XS);
                button.setForeground(DesignSystem.accent());
                button.setBackground(DesignSystem.surface());
                button.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 8));
                button.setFocusPainted(false);
                button.setContentAreaFilled(false);
                return button;
            }
        });

        combo.setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(
                    JList<?> list, Object value, int index, boolean sel, boolean focus) {
                super.getListCellRendererComponent(list, value, index, sel, focus);
                setOpaque(true);
                list.setBackground(DesignSystem.surface());
                list.setForeground(DesignSystem.ink());

                String item = (String) value;
                if (item != null && item.startsWith("──")) {
                    setFont(MONO_XS);
                    setForeground(DesignSystem.accent());
                    setBackground(DesignSystem.surface());
                    setEnabled(false);
                } else {
                    setFont(index == -1 ? FontManager.getMonospacedBoldFont(12) : MONO_SM);
                    setForeground(sel ? DesignSystem.bg() : DesignSystem.ink());
                    setBackground(sel ? DesignSystem.accent() : DesignSystem.surface());
                    setBorder(new EmptyBorder(4, index == -1 ? 0 : 8, 4, 8));
                }
                return this;
            }
        });

        combo.addActionListener(e -> {
            String sel = (String) combo.getSelectedItem();
            if (sel != null && sel.startsWith("──")) {
                int idx = combo.getSelectedIndex();
                if (idx + 1 < combo.getItemCount()) combo.setSelectedIndex(idx + 1);
            }
        });

        for (int i = 0; i < model.getSize(); i++) {
            if (!model.getElementAt(i).startsWith("──")) { combo.setSelectedIndex(i); break; }
        }
        return combo;
    }

    public static void selectLastReal(JComboBox<String> combo) {
        DefaultComboBoxModel<String> m = (DefaultComboBoxModel<String>) combo.getModel();
        for (int i = m.getSize() - 1; i >= 0; i--) {
            if (!m.getElementAt(i).startsWith("──")) { combo.setSelectedIndex(i); break; }
        }
    }

    public static String selectedEmail(JComboBox<String> combo) {
        int idx = combo.getSelectedIndex();
        if (idx < 0) return "";
        DefaultComboBoxModel<String> m = (DefaultComboBoxModel<String>) combo.getModel();
        for (int i = idx; i < m.getSize(); i++) {
            String item = m.getElementAt(i);
            if (item != null && !item.startsWith("──")) return item.trim();
        }
        return "";
    }

    // ── Estilização do seletor numérico (Spinner) ────────────────────────────

    public static void styleSpinner(JSpinner spinner) {
        spinner.setFont(MONO_MD);
        spinner.setBackground(DesignSystem.surface());
        spinner.setForeground(DesignSystem.ink());
        spinner.setOpaque(true);
        spinner.setBorder(BorderFactory.createLineBorder(DesignSystem.rule(), 1));
        if (spinner.getEditor() instanceof JSpinner.DefaultEditor de) {
            JTextField tf = de.getTextField();
            tf.setBackground(DesignSystem.surface());
            tf.setForeground(DesignSystem.accent());
            tf.setDisabledTextColor(DesignSystem.accent());
            tf.setSelectedTextColor(DesignSystem.bg());
            tf.setSelectionColor(DesignSystem.accent());
            tf.setFont(FontManager.getSerifBoldFont(24));
            tf.setCaretColor(DesignSystem.accent());
            tf.setHorizontalAlignment(SwingConstants.CENTER);
            tf.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
            tf.setOpaque(true);
        }
        for (Component child : spinner.getComponents()) {
            child.setBackground(DesignSystem.surface());
            child.setForeground(DesignSystem.ink());
        }
    }

    // ── Área de texto de resultado ───────────────────────────────────────────

    public static JTextArea resultArea() {
        JTextArea area = new JTextArea(10, 60);
        area.setEditable(false);
        area.setFont(MONO_SM);
        area.setBackground(DesignSystem.surface());
        area.setForeground(DesignSystem.ink());
        area.setCaretColor(DesignSystem.accent());
        area.setBorder(new EmptyBorder(14, 18, 14, 18));
        area.setOpaque(true);
        return area;
    }

    public static JScrollPane resultScroll(JTextArea area) {
        JScrollPane scroll = new JScrollPane(area);
        scroll.setBorder(new MatteBorder(0, 0, 0, 0, DesignSystem.rule()));
        scroll.setBackground(DesignSystem.bg());
        scroll.getViewport().setBackground(DesignSystem.bg());
        styleScrollBar(scroll.getVerticalScrollBar());
        return scroll;
    }

    /** Adiciona texto ao início de um JTextArea (o mais recente no topo). */
    public static void prepend(JTextArea area, String text) {
        area.setText(text + "\n" + area.getText());
        area.setCaretPosition(0);
    }

    public static JPanel emptyResult(String title, String message) {
        JPanel p = new JPanel(new BorderLayout(0, 6));
        p.setBackground(DesignSystem.bg());
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createDashedBorder(DesignSystem.rule(), 1, 4),
                new EmptyBorder(48, 32, 48, 32)
        ));

        JLabel titleLbl = lbl(title, SERIF_IT, DesignSystem.ink());
        titleLbl.setHorizontalAlignment(SwingConstants.CENTER);
        JLabel msgLbl = lbl(message, MONO_SM, DesignSystem.muted());
        msgLbl.setHorizontalAlignment(SwingConstants.CENTER);

        p.add(titleLbl, BorderLayout.CENTER);
        p.add(msgLbl, BorderLayout.SOUTH);
        return p;
    }

    // ── Formatação de números ────────────────────────────────────────────────

    public static String formatInt(int n) {
        return String.format("%,d", n).replace(',', '.');
    }
}
