package br.edu.enron.design;

import java.awt.*;

/**
 * Design System — exact colors and typography from design_template/styles.css
 * Supports Dark and Light themes.
 *
 * All colors converted from oklch() CSS values to RGB for Java Swing.
 */
public class DesignSystem {

    public enum Theme { DARK, LIGHT }

    private static Theme currentTheme = Theme.DARK;

    // ─── DARK THEME (oklch values) ───────────────────────────────────────────
    // --bg:        oklch(0.175 0.012 250);
    // --surface:   oklch(0.225 0.013 250);
    // --ink:       oklch(0.965 0.005 90);

    public static final Color DARK_BG         = new Color(29,   45,  68);    // oklch(0.175 0.012 250)
    public static final Color DARK_BG_2       = new Color(35,   53,  78);    // oklch(0.205 0.014 250)
    public static final Color DARK_SURFACE    = new Color(38,   60,  88);    // oklch(0.225 0.013 250)
    public static final Color DARK_SURFACE_2  = new Color(43,   66,  97);    // oklch(0.255 0.013 250)
    public static final Color DARK_INK        = new Color(246, 247, 251);    // oklch(0.965 0.005 90)
    public static final Color DARK_INK_2      = new Color(209, 217, 230);    // oklch(0.82  0.01  80)
    public static final Color DARK_MUTED      = new Color(158, 169, 191);    // oklch(0.62  0.01  250)
    public static final Color DARK_FAINT      = new Color(107, 125, 156);    // oklch(0.42  0.012 250)
    public static final Color DARK_RULE       = new Color(82,  100, 130);    // oklch(0.32  0.013 250)
    public static final Color DARK_RULE_2     = new Color(69,   87, 115);    // oklch(0.27  0.013 250)
    public static final Color DARK_ACCENT     = new Color(212, 170,  37);    // oklch(0.82  0.15  75)

    // ─── LIGHT THEME (oklch values) ──────────────────────────────────────────
    // --bg:        oklch(0.985 0.004 90);
    // --surface:   oklch(0.985 0.004 90);
    // --ink:       oklch(0.18  0.012 250);

    public static final Color LIGHT_BG         = new Color(251, 251, 251);   // oklch(0.985 0.004 90)
    public static final Color LIGHT_BG_2       = new Color(246, 246, 246);   // oklch(0.965 0.005 85)
    public static final Color LIGHT_SURFACE    = new Color(251, 251, 251);   // oklch(0.985 0.004 90)
    public static final Color LIGHT_SURFACE_2  = new Color(241, 241, 241);   // oklch(0.945 0.006 85)
    public static final Color LIGHT_INK        = new Color(46,   51,  64);   // oklch(0.18  0.012 250)
    public static final Color LIGHT_INK_2      = new Color(82,   92, 113);   // oklch(0.32  0.01  250)
    public static final Color LIGHT_MUTED      = new Color(122, 138, 170);   // oklch(0.48  0.012 250)
    public static final Color LIGHT_FAINT      = new Color(166, 180, 206);   // oklch(0.65  0.012 250)
    public static final Color LIGHT_RULE       = new Color(209, 217, 230);   // oklch(0.82  0.008 90)
    public static final Color LIGHT_RULE_2     = new Color(225, 235, 245);   // oklch(0.88  0.007 90)
    public static final Color LIGHT_ACCENT     = new Color(158, 102,  38);   // oklch(0.62  0.16  50)

    // ─── TYPOGRAPHY ──────────────────────────────────────────────────────────
    // Fonts must use FontManager for cross-platform consistency

    // ─── PUBLIC API ───────────────────────────────────────────────────────────

    public static void setTheme(Theme theme) {
        currentTheme = theme;
    }

    public static Theme getTheme() {
        return currentTheme;
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
