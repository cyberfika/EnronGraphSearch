package br.edu.enron.view;

import br.edu.enron.design.DesignSystem;
import br.edu.enron.util.FontManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Floating tweaks panel (Dark/Light mode toggle).
 * Mirrors the tweaks-panel.jsx from the design_template.
 */
public class TweaksPanel extends JDialog {

    private Runnable themeChangeListener;

    public TweaksPanel(Frame owner) {
        super(owner, "Tweaks", false);
        setUndecorated(false);
        setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
        setResizable(false);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(DesignSystem.LIGHT_BG);
        content.setBorder(new EmptyBorder(14, 14, 14, 14));

        // Title
        JLabel titleLbl = new JLabel("Tweaks");
        titleLbl.setFont(FontManager.getSansSerifBoldFont(12));
        titleLbl.setForeground(DesignSystem.LIGHT_INK);
        titleLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(titleLbl);
        content.add(Box.createVerticalStrut(10));

        // Theme section
        JLabel themeSection = new JLabel("TEMA");
        themeSection.setFont(FontManager.getMonospacedFont(10));
        themeSection.setForeground(DesignSystem.LIGHT_MUTED);
        themeSection.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(themeSection);
        content.add(Box.createVerticalStrut(8));

        // Dark/Light radio buttons
        JPanel themeButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        themeButtons.setBackground(DesignSystem.LIGHT_BG);
        themeButtons.setAlignmentX(Component.LEFT_ALIGNMENT);

        JToggleButton darkBtn = new JToggleButton("dark");
        JToggleButton lightBtn = new JToggleButton("light");

        darkBtn.setFont(FontManager.getMonospacedFont(11));
        lightBtn.setFont(FontManager.getMonospacedFont(11));

        darkBtn.setSelected(DesignSystem.getTheme() == DesignSystem.Theme.DARK);
        lightBtn.setSelected(DesignSystem.getTheme() == DesignSystem.Theme.LIGHT);

        darkBtn.addActionListener((e) -> {
            DesignSystem.setTheme(DesignSystem.Theme.DARK);
            darkBtn.setSelected(true);
            lightBtn.setSelected(false);
            updateTheme();
        });

        lightBtn.addActionListener((e) -> {
            DesignSystem.setTheme(DesignSystem.Theme.LIGHT);
            lightBtn.setSelected(true);
            darkBtn.setSelected(false);
            updateTheme();
        });

        themeButtons.add(darkBtn);
        themeButtons.add(lightBtn);
        content.add(themeButtons);

        setContentPane(content);
        setSize(280, 180);

        // Position at bottom-right of owner frame (matching web design)
        if (owner != null) {
            int x = owner.getX() + owner.getWidth() - 280 - 16;
            int y = owner.getY() + owner.getHeight() - 180 - 16;
            setLocation(x, y);
        } else {
            setLocationRelativeTo(owner);
        }
    }

    /**
     * Sets a listener to be notified when the theme changes.
     * @param listener callback to invoke when theme is updated
     */
    public void setThemeChangeListener(Runnable listener) {
        this.themeChangeListener = listener;
    }

    private void updateTheme() {
        System.out.println("[Tweaks] Theme changed to: " + DesignSystem.getTheme());
        DesignSystem.applyToUIManager();
        Window owner = getOwner();
        if (owner != null) {
            SwingUtilities.updateComponentTreeUI(owner);
            owner.invalidate();
            owner.validate();
            owner.repaint();
        }
        SwingUtilities.updateComponentTreeUI(this);
        // Notify listener if set
        if (themeChangeListener != null) {
            themeChangeListener.run();
        }
    }
}
