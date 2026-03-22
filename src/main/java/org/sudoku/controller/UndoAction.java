package org.sudoku.controller;

import org.sudoku.model.Model;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class UndoAction implements ActionListener{
    private final Model model;

    public UndoAction(Model model) {
        this.model = model;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        model.undo();
    }
}
