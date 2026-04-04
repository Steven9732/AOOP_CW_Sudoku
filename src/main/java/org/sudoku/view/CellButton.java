package org.sudoku.view;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import java.awt.*;

final class CellButton extends JButton {
    final int row;
    final int column;

    public CellButton(int row, int column) {
        super("");
        this.row = row;
        this.column = column;
        setFont(getFont().deriveFont(Font.PLAIN, 12f));
        setHorizontalAlignment(SwingConstants.CENTER);
        setMargin(new Insets(0,0,0,0));
        setFocusPainted(false);
    }

    void applyStyle(boolean fixed, boolean invalid, boolean selected) {
        if (fixed) {
            setBackground(new Color(230, 230, 230));
            setFont(getFont().deriveFont(Font.BOLD, 18f));
        } else {
            setBackground(Color.WHITE);
            setFont(getFont().deriveFont(Font.PLAIN, 18f));
        }

        if (invalid) {
            setBackground(new Color(255, 215, 215));
        }

        if (selected) {
            setBorder(new MatteBorder(3, 3, 3, 3, new Color(60, 120, 255)));
        }
    }
}
