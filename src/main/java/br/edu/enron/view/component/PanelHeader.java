package br.edu.enron.view.component;

import br.edu.enron.design.DesignSystem;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;

import static br.edu.enron.view.component.SwingHelper.*;

/**
 * Rich panel header matching the design_template panel-header.
 *
 * <p>Layout (top to bottom inside a bordered panel):</p>
 * <pre>
 *   — eyebrow text (monospace, small, muted)
 *   Title normal  <em>accent italic</em>  title end.     [pill] [pill]
 *   Subtitle / description text (sans-serif, muted)
 * </pre>
 */
public final class PanelHeader extends JPanel {

    public PanelHeader(String eyebrow,
                       String titleNormal, String titleAccent, String titleEnd,
                       String subtitle,
                       String... pills) {
        setLayout(new BorderLayout(0, 0));
        setBackground(DesignSystem.bg());
        setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0, 0, 1, 0, DesignSystem.rule()),
                new EmptyBorder(24, 28, 20, 28)
        ));

        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);

        // Eyebrow
        JLabel eyeLbl = lbl("— " + eyebrow, MONO_XS, DesignSystem.muted());
        eyeLbl.setAlignmentX(LEFT_ALIGNMENT);
        left.add(eyeLbl);
        left.add(Box.createVerticalStrut(8));

        // Title row
        JPanel titleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        titleRow.setOpaque(false);
        titleRow.setAlignmentX(LEFT_ALIGNMENT);
        titleRow.add(lbl(titleNormal, SERIF_28, DesignSystem.ink()));
        titleRow.add(lbl(titleAccent, SERIF_IT, DesignSystem.accent()));
        if (titleEnd != null && !titleEnd.isEmpty()) {
            titleRow.add(lbl(titleEnd, SERIF_28, DesignSystem.ink()));
        }
        left.add(titleRow);
        left.add(Box.createVerticalStrut(8));

        // Subtitle
        JLabel subLbl = new JLabel(
                "<html><body style='width:520px'>" + subtitle + "</body></html>");
        subLbl.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        subLbl.setForeground(DesignSystem.muted());
        subLbl.setAlignmentX(LEFT_ALIGNMENT);
        left.add(subLbl);

        add(left, BorderLayout.CENTER);

        // Pills (right-aligned)
        if (pills.length > 0) {
            JPanel pillBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
            pillBox.setOpaque(false);
            for (String p : pills) pillBox.add(propTag(p));
            add(pillBox, BorderLayout.EAST);
        }
    }
}
