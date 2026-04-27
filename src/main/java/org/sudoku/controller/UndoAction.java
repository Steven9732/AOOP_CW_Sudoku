package org.sudoku.controller;

import org.sudoku.model.SudokuModel;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class UndoAction implements ActionListener{
    private final SudokuModel model;

    public UndoAction(SudokuModel model) {
        this.model = model;
    }

    /**
     * Undo the latest move.
     * @param e the event to be processed
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        model.undo();
    }
}
