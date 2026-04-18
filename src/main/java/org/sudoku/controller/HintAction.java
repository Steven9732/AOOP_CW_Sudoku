package org.sudoku.controller;

import org.sudoku.model.Model;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.function.IntSupplier;

public class HintAction implements ActionListener {
    private final Model model;
    private final IntSupplier selectedRow;
    private final IntSupplier selectedColumn;

    public HintAction(Model model, IntSupplier selectedRow, IntSupplier selectedColumn) {
        this.model = model;
        this.selectedRow = selectedRow;
        this.selectedColumn = selectedColumn;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        int row = selectedRow.getAsInt();
        int column = selectedColumn.getAsInt();
        if (row < 0 || column < 0) return;
        model.applyHint(row, column);
    }
}