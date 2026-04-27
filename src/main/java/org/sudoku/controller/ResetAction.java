package org.sudoku.controller;

import org.sudoku.model.SudokuModel;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class ResetAction implements ActionListener{
    private final SudokuModel model;

    public ResetAction(SudokuModel model){
        this.model = model;
    }

    /**
     * Resets the current game.
     * @param e the event to be processed
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        model.reset();
    }
}
