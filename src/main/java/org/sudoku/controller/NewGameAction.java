package org.sudoku.controller;

import org.sudoku.model.Model;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class NewGameAction implements ActionListener{
    private final Model model;

    public NewGameAction(Model model) {
        this.model = model;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        model.newGame();
    }
}
