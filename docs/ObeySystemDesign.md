# MASTER PLAN: EXHAUSTIVE DESIGN SYSTEM CORRECTION (ENRON ANALYZER)

This guide is a **line-by-line, pixel-by-pixel** manual to align the Java Swing interface with the original design (web/React). The developer/GPT must follow these rules like a robot. Do not invent. Do not assume. Simply copy the aesthetics from the `design_template` folder.

**THE GOLDEN RULE:** If there is any doubt about how a component should be drawn (margin, padding, color, border), **OPEN THE FILES IN THE `design_template` FOLDER** (`styles.css`, `panels.jsx`, `pathviz.jsx`, `app.jsx`, `index.html`) and translate the CSS/React logic to Java Swing.

---

## 1. EXACT HEX COLORS (DO NOT USE APPROXIMATIONS)

Open the file `src/main/java/br/edu/enron/design/DesignSystem.java` and ensure the converted values are EXACTLY these:

| CSS Token | Dark Mode (RGB) | Light Mode (RGB) | Dark Hex | Light Hex |
| :--- | :--- | :--- | :--- | :--- |
| `--bg` | `31, 35, 41` | `251, 251, 251` | `#1F2329` | `#FBFBFB` |
| `--bg-2` | `37, 42, 50` | `246, 246, 246` | `#252A32` | `#F6F6F6` |
| `--surface` | `42, 47, 55` | `251, 251, 251` | `#2A2F37` | `#FBFBFB` |
| `--ink` | `246, 247, 251` | `46, 51, 64` | `#F6F7FB` | `#2E3340` |
| `--ink-2` | `209, 217, 230` | `82, 92, 113` | `#D1D9E6` | `#525C71` |
| `--muted` | `158, 169, 191` | `122, 138, 170` | `#9EA9BF` | `#7A8AAA` |
| `--faint` | `107, 125, 156` | `166, 180, 206` | `#6B7D9C` | `#A6B4CE` |
| `--accent` | `212, 170, 37` | `158, 102, 38` | `#D4AA25` | `#9E6626` |
| `--rule` | `66, 73, 85` | `209, 217, 230` | `#424955` | `#D1D9E6` |
| `--rule-2` | `55, 62, 73` | `225, 235, 245` | `#373E49` | `#E1EBF5` |

---

## 2. LAYOUT RULES AND COMPONENTS (SCREEN BY SCREEN)

### 2.1 The Sidebar - Reference: `app.jsx` -> `.sidebar`
**What is wrong:** It looks like old Java, with heavy borders and no visual interaction.
**Step-by-Step Action:**
1.  **Absolute Width:** Exactly 264px. No resizing.
2.  **Border:** Only 1px on the right (`MatteBorder` with `--rule` color). Everything else is clean.
3.  **Dataset Card (`.dataset-card`):** `--bg-2` background. The "ACTIVE" badge must be a rectangle with `--accent` background and text in the screen's background color.
4.  **Directory List (`.side-list-row`):** 
    - Fixed height of 34px per row.
    - On Hover, the ENTIRE row must change the background to `--bg-2` and the text to `--ink`.
    - Refer to `styles.css` classes `.side-list-row` and `.side-list-row:hover`.

### 2.2 Overview Panel (OverviewPanel) - Reference: `panels.jsx` -> `OverviewPanel`
**What is wrong:** The numbers are small. They look like standard text boxes.
**Step-by-Step Action:**
1.  **Grid (`.stat-grid`):** 4 horizontal columns. 1px borders only on the right and bottom of each cell (`MatteBorder`).
2.  **Number Typography (`.stat-cell .value`):** **Serif** font (Instrument Serif or Georgia), Giant size (e.g., 44pt), `--ink` color.
3.  **Labels (`.stat-cell .label`):** **Monospace** font, 10pt size, Uppercase, `--muted` color.
4.  **Highlights:** If the cell has the `.accent` class in CSS (e.g., Distance or Messages values), the number must use the `--accent` color (Amber).

### 2.3 DFS/BFS Panel (DfsBfsPanel) - Reference: `panels.jsx` -> `QueryBar` and `ResultBlock`
**What is wrong:** Combo boxes look like native Windows (white), misaligned, and the execute button lacks the correct layout. The result appears in a generic green terminal text box.
**Step-by-Step Action:**
1.  **The Search Bar (`.query`):**
    - Horizontal layout. Cells side-by-side, divided by a 1px vertical line (`--rule`).
    - Entire bar background: `--surface`.
2.  **Combo Boxes (Origin/Destination):**
    - Background MUST be equal to `--surface` and text `--ink`. No native OS rounded borders.
    - The label "ORIGEM X" or "DESTINO Y" must be **above** the combo box, inside the same cell, in 10pt Monospace font, `--faint` color. Letters 'X' and 'Y' in `--accent` color.
3.  **Algorithm Toggle:** "DFS" and "BFS" buttons stuck together. The active one has `--ink` background and `--bg` text. The inactive one has a transparent background.
4.  **Execute Button (`.btn.primary`):** 
    - Located in the last cell on the right.
    - Background **100% filled** with `--accent` color. Dark text. No native borders.
5.  **Result Display (`.result`):**
    - MUST appear **exactly below** the search bar.
    - Read the `pathviz.jsx` file (`PathChain` component). The result is not plain text in a terminal `JTextArea`!
    - Visually draw the chain: a dot, a vertical line, the email, edge weight. If a rich vertical graph is not possible, use stacked panels (`BoxLayout.Y_AXIS`) simulating the design's result lines, with Monospace font.

### 2.4 Critical Path (CritPathPanel)
Same search bar layout rules as DFS/BFS. The bottom graphic visualization (proportional bars) must be drawn with `paintComponent` simulating the divs of the `.crit-flow-bar` CSS class.

### 2.5 Graph Visualization (GraphVisualizer)
**What is wrong:** GraphStream rendering rainbow colors from a school project.
**Step-by-Step Action:**
GraphStream accepts CSS string injection. You MUST change the `STYLESHEET` string inside `GraphVisualizer.java`:
1.  The graph panel background must be `--bg-2`.
2.  **Default Node:** `fill-color: rgba(x,y,z);` (Use the RGB of `--ink-2` or `--rule`). No colored borders.
3.  **Default Edge:** `fill-color` equal to `--rule-2`.
4.  **Highlighted Node (Path/Highlighted):** `fill-color` and `stroke-color` equal to `--accent` (Amber). Size should be slightly larger.
5.  **Endpoints (Start/End):** `fill-color` equal to `--ink` or bright white, with an `--accent` halo. ZERO use of generic green and red.

---

## 3. THEME MECHANICS AND UIMANAGER
If scrollbar backgrounds or selects remain white, it is because the `UIManager` was not updated.

**Step-by-Step Action in `DesignSystem.java`:**
Create the `public static void applyToUIManager()` method and populate it aggressively. Example:
```java
UIManager.put("Panel.background", bg());
UIManager.put("OptionPane.background", bg());
UIManager.put("ComboBox.background", surface());
UIManager.put("ComboBox.foreground", ink());
UIManager.put("ComboBox.selectionBackground", accent());
UIManager.put("ComboBox.selectionForeground", bg());
UIManager.put("TextField.background", surface());
UIManager.put("TextField.foreground", ink());
UIManager.put("TextField.caretForeground", accent());
UIManager.put("ScrollBar.background", bg());
UIManager.put("ScrollBar.thumb", rule());
// Force buttons to not use the native OS border:
UIManager.put("Button.background", surface());
UIManager.put("Button.border", BorderFactory.createEmptyBorder());
```
Whenever the theme changes in `TweaksPanel`, invoke `DesignSystem.applyToUIManager()` and then `SwingUtilities.updateComponentTreeUI(mainFrame)`.

---

## FINAL INSTRUCTION FOR THE DEV/GPT
You are not allowed to invent layouts. If Java Swing makes it difficult to create something similar to the web, use `JPanel` overlays, `MatteBorder`s, and override `paintComponent`. Read `styles.css` to understand margins (`--s-2`, `--s-3`), paddings, and visual hierarchy. The goal is to look indistinguishable from the `example.png` image.