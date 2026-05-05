package br.edu.enron.view.component;

import br.edu.enron.design.DesignSystem;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;

import static br.edu.enron.view.component.SwingHelper.*;

/**
 * Cabeçalho de painel rico correspondente ao panel-header do design_template.
 *
 * <p>Layout (de cima para baixo dentro de um painel com borda):</p>
 * <pre>
 *   — texto de sobrancelha (monoespaçado, pequeno, suave)
 *   Título normal  <em>ênfase itálico</em>  fim do título.     [pílula] [pílula]
 *   Subtítulo / texto de descrição (sem serifa, suave)
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

        // Sobrancelha (Eyebrow)
        JLabel eyeLbl = lbl("— " + eyebrow, MONO_XS, DesignSystem.muted());
        eyeLbl.setAlignmentX(LEFT_ALIGNMENT);
        left.add(eyeLbl);
        left.add(Box.createVerticalStrut(8));

        // Linha do título
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

        // Subtítulo
        JLabel subLbl = new JLabel(
                "<html><body style='width:520px'>" + subtitle + "</body></html>");
        subLbl.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        subLbl.setForeground(DesignSystem.muted());
        subLbl.setAlignmentX(LEFT_ALIGNMENT);
        left.add(subLbl);

        add(left, BorderLayout.CENTER);

        // Pílulas (alinhadas à direita)
        if (pills.length > 0) {
            JPanel pillBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
            pillBox.setOpaque(false);
            for (String p : pills) pillBox.add(propTag(p));
            add(pillBox, BorderLayout.EAST);
        }
    }
}
