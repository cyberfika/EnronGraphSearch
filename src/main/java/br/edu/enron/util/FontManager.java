package br.edu.enron.util;

import java.awt.*;

/**
 * Cross-platform font manager.
 * Selects appropriate fonts for Windows, macOS, and Linux.
 */
public class FontManager {

    private static final String OS_NAME = System.getProperty("os.name").toLowerCase();
    private static final boolean IS_MAC = OS_NAME.contains("mac");
    private static final boolean IS_WINDOWS = OS_NAME.contains("win");

    public static Font getMonospacedFont(int size) {
        String family = IS_MAC ? "Monaco" : IS_WINDOWS ? "Consolas" : "Monospaced";
        return new Font(family, Font.PLAIN, size);
    }

    public static Font getMonospacedBoldFont(int size) {
        String family = IS_MAC ? "Monaco" : IS_WINDOWS ? "Consolas" : "Monospaced";
        return new Font(family, Font.BOLD, size);
    }

    public static Font getSansSerifFont(int size) {
        // Use system sans-serif that's consistent
        String family = IS_MAC ? "Helvetica Neue" : IS_WINDOWS ? "Segoe UI" : "SansSerif";
        return new Font(family, Font.PLAIN, size);
    }

    public static Font getSansSerifBoldFont(int size) {
        String family = IS_MAC ? "Helvetica Neue" : IS_WINDOWS ? "Segoe UI" : "SansSerif";
        return new Font(family, Font.BOLD, size);
    }

    public static Font getSerifFont(int size) {
        String family = IS_MAC ? "Georgia" : IS_WINDOWS ? "Georgia" : "Serif";
        return new Font(family, Font.PLAIN, size);
    }

    public static Font getSerifBoldFont(int size) {
        String family = IS_MAC ? "Georgia" : IS_WINDOWS ? "Georgia" : "Serif";
        return new Font(family, Font.BOLD, size);
    }

    public static Font getSerifBoldItalicFont(int size) {
        String family = IS_MAC ? "Georgia" : IS_WINDOWS ? "Georgia" : "Serif";
        return new Font(family, Font.BOLD | Font.ITALIC, size);
    }

    public static boolean isMac() {
        return IS_MAC;
    }

    public static boolean isWindows() {
        return IS_WINDOWS;
    }
}
