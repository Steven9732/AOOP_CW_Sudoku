package org.sudoku.view;

import org.sudoku.model.SudokuModel;

import javax.swing.*;
import java.awt.*;
import javax.swing.border.Border;
import javax.swing.border.MatteBorder;

public class BoardPanel extends JPanel {
    public interface CellClickHandler {
        void onCellClicked(int row, int column);
    }

    private final CellButton[][] cells = new CellButton[SudokuModel.SIZE][SudokuModel.SIZE];
    private int selectedRow = -1;
    private int selectedColumn = -1;

    private CellClickHandler cellClickHandler = (r, c) -> {};

    public int getSlectedRow() {
        return selectedRow;
    }

    public int getSlectedColumn() {
        return selectedColumn;
    }

    public void setSelectedCell(int row, int column) {
        this.selectedRow = row;
        this.selectedColumn = column;
    }

    public BoardPanel() {
        super(new GridLayout(SudokuModel.SIZE, SudokuModel.SIZE, 0, 0));
        setFocusable(false);

        for (int row = 0; row < SudokuModel.SIZE; row++) {
            for (int column = 0; column < SudokuModel.SIZE; column++) {
                CellButton b = new CellButton(row, column);
                b.setMargin(new Insets(0,0,0,0));
                b.setFocusPainted(false);
                b.setFocusable(false);
                int finalColumn = column;
                int finalRow = row;
                b.addActionListener(e -> cellClickHandler.onCellClicked(finalRow, finalColumn));

                // Draw lines for each 3x3 cells
                int top = (finalRow % 3 == 0) ? 3 : 1;
                int left = (finalColumn % 3 == 0) ? 3 : 1;
                int bottom = (finalRow == 8) ? 3 : 1;
                int right = (finalColumn == 8) ? 3 : 1;
                Border border = new MatteBorder(top, left, bottom, right, Color.DARK_GRAY);
                b.setNormalBorder(border);
                cells[row][column] = b;
                add(b);
            }
        }
    }

    public void setCellClickHandler(CellClickHandler handler) {
        this.cellClickHandler = (handler == null) ? (r, c) -> {} : handler;
    }

    public void refreshFromModel(SudokuModel model) {
        boolean showInvalid = model.isValidationFeedbackEnabled();

        for (int r = 0; r < SudokuModel.SIZE; r++) {
            for (int c = 0; c < SudokuModel.SIZE; c++) {
                CellButton b = cells[r][c];
                int v = model.getCellValue(r, c);

                b.setText(v == 0 ? "" : String.valueOf(v));

                boolean fixed = model.isFixed(r, c);
                boolean invalid = showInvalid && v != 0 && model.isCellInvalid(r, c);
                boolean selected = (r == selectedRow && c == selectedColumn);

                b.applyStyle(fixed, invalid, selected);
            }
        }
        repaint();
    }
}
