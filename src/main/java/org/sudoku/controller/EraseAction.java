package org.sudoku.controller;

import org.sudoku.model.Model;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.function.IntSupplier;

public class EraseAction implements ActionListener{
    private final Model model;
    private final IntSupplier selectedRow;
    private final IntSupplier selectedColumn;

    public EraseAction(Model model, IntSupplier selectedRow, IntSupplier selectedColumn) {
        this.model = model;
        this.selectedRow = selectedRow;
        this.selectedColumn = selectedColumn;
    }

    /**
     * Clears the selected cell when a valid selection exists.
     * @param e the erase action to be processed
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        int row = selectedRow.getAsInt();
        int column = selectedColumn.getAsInt();
        assert row >= 0 && row < Model.SIZE : "Selected row is out of bounds";
        assert column >= 0 && column < Model.SIZE : "Selected column is out of bounds";
        model.clearValue(row, column);
    }
}
