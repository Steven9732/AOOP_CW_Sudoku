package org.sudoku.controller;

import org.sudoku.model.SudokuModel;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.function.IntSupplier;

public class HintAction implements ActionListener {
    private final SudokuModel model;
    private final IntSupplier selectedRow;
    private final IntSupplier selectedColumn;

    public HintAction(SudokuModel model, IntSupplier selectedRow, IntSupplier selectedColumn) {
        this.model = model;
        this.selectedRow = selectedRow;
        this.selectedColumn = selectedColumn;
    }

    /**
     * Applies a hint when a valid selection exists.
     * @param e the hint action event
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        int row = selectedRow.getAsInt();
        int column = selectedColumn.getAsInt();
        assert row >= 0 && row < SudokuModel.SIZE : "Selected row is out of bounds";
        assert column >= 0 && column < SudokuModel.SIZE : "Selected column is out of bounds";
        model.applyHint(row, column);
    }
}