package org.sudoku.controller;

import org.sudoku.model.SudokuModel;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class NewGameAction implements ActionListener{
    private final SudokuModel model;

    public NewGameAction(SudokuModel model) {
        this.model = model;
    }

    /**
     * Starts a new game.
     * @param e the event to be processed
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        model.newGame();
    }
}
