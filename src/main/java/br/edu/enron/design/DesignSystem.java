package br.edu.enron.design;

import javax.swing.*;
import java.awt.*;

/**
 * Design tokens translated from design_template/styles.css.
 */
public class DesignSystem {

    public enum Theme { DARK, LIGHT }

    private static Theme currentTheme = Theme.DARK;

    public static final Color DARK_BG        = new Color(31, 35, 41);
    public static final Color DARK_BG_2      = new Color(37, 42, 50);
    public static final Color DARK_SURFACE   = new Color(42, 47, 55);
    public static final Color DARK_SURFACE_2 = new Color(42, 47, 55);
    public static final Color DARK_INK       = new Color(246, 247, 251);
    public static final Color DARK_INK_2     = new Color(209, 217, 230);
    public static final Color DARK_MUTED     = new Color(158, 169, 191);
    public static final Color DARK_FAINT     = new Color(107, 125, 156);
    public static final Color DARK_RULE      = new Color(66, 73, 85);
    public static final Color DARK_RULE_2    = new Color(55, 62, 73);
    public static final Color DARK_ACCENT    = new Color(212, 170, 37);

    public static final Color LIGHT_BG        = new Color(251, 251, 251);
    public static final Color LIGHT_BG_2      = new Color(246, 246, 246);
    public static final Color LIGHT_SURFACE   = new Color(251, 251, 251);
    public static final Color LIGHT_SURFACE_2 = new Color(246, 246, 246);
    public static final Color LIGHT_INK       = new Color(46, 51, 64);
    public static final Color LIGHT_INK_2     = new Color(82, 92, 113);
    public static final Color LIGHT_MUTED     = new Color(122, 138, 170);
    public static final Color LIGHT_FAINT     = new Color(166, 180, 206);
    public static final Color LIGHT_RULE      = new Color(209, 217, 230);
    public static final Color LIGHT_RULE_2    = new Color(225, 235, 245);
    public static final Color LIGHT_ACCENT    = new Color(158, 102, 38);

    public static void setTheme(Theme theme) {
        currentTheme = theme;
    }

    public static Theme getTheme() {
        return currentTheme;
    }

    public static void applyToUIManager() {
        UIManager.put("Panel.background", bg());
        UIManager.put("OptionPane.background", bg());
        UIManager.put("OptionPane.messageForeground", ink());
        UIManager.put("Viewport.background", bg());

        UIManager.put("ComboBox.background", surface());
        UIManager.put("ComboBox.foreground", ink());
        UIManager.put("ComboBox.buttonBackground", surface());
        UIManager.put("ComboBox.selectionBackground", accent());
        UIManager.put("ComboBox.selectionForeground", bg());
        UIManager.put("ComboBox.border", BorderFactory.createEmptyBorder());
        UIManager.put("ComboBoxEditor.background", surface());
        UIManager.put("ComboBoxEditor.foreground", ink());

        UIManager.put("List.background", surface());
        UIManager.put("List.foreground", ink());
        UIManager.put("List.selectionBackground", accent());
        UIManager.put("List.selectionForeground", bg());

        UIManager.put("TextField.background", surface());
        UIManager.put("TextField.foreground", ink());
        UIManager.put("TextField.caretForeground", accent());
        UIManager.put("FormattedTextField.background", surface());
        UIManager.put("FormattedTextField.foreground", accent());
        UIManager.put("FormattedTextField.caretForeground", accent());
        UIManager.put("FormattedTextField.selectionBackground", accent());
        UIManager.put("FormattedTextField.selectionForeground", bg());
        UIManager.put("TextArea.background", surface());
        UIManager.put("TextArea.foreground", ink());
        UIManager.put("TextArea.caretForeground", accent());

        UIManager.put("ScrollBar.background", bg());
        UIManager.put("ScrollBar.thumb", rule());
        UIManager.put("Button.background", surface());
        UIManager.put("Button.foreground", ink());
        UIManager.put("Button.border", BorderFactory.createEmptyBorder());
        UIManager.put("Button.focus", new Color(0, 0, 0, 0));

        UIManager.put("Spinner.background", surface());
        UIManager.put("Spinner.foreground", ink());
    }

    public static Color bg() {
        return currentTheme == Theme.DARK ? DARK_BG : LIGHT_BG;
    }

    public static Color bg2() {
        return currentTheme == Theme.DARK ? DARK_BG_2 : LIGHT_BG_2;
    }

    public static Color surface() {
        return currentTheme == Theme.DARK ? DARK_SURFACE : LIGHT_SURFACE;
    }

    public static Color surface2() {
        return currentTheme == Theme.DARK ? DARK_SURFACE_2 : LIGHT_SURFACE_2;
    }

    public static Color ink() {
        return currentTheme == Theme.DARK ? DARK_INK : LIGHT_INK;
    }

    public static Color ink2() {
        return currentTheme == Theme.DARK ? DARK_INK_2 : LIGHT_INK_2;
    }

    public static Color muted() {
        return currentTheme == Theme.DARK ? DARK_MUTED : LIGHT_MUTED;
    }

    public static Color faint() {
        return currentTheme == Theme.DARK ? DARK_FAINT : LIGHT_FAINT;
    }

    public static Color rule() {
        return currentTheme == Theme.DARK ? DARK_RULE : LIGHT_RULE;
    }

    public static Color rule2() {
        return currentTheme == Theme.DARK ? DARK_RULE_2 : LIGHT_RULE_2;
    }

    public static Color accent() {
        return currentTheme == Theme.DARK ? DARK_ACCENT : LIGHT_ACCENT;
    }
}
