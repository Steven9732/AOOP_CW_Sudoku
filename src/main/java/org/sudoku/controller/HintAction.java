package org.sudoku.controller;

import org.sudoku.model.Model;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class HintAction implements ActionListener{
    private final Model model;

    public HintAction(Model model) {
        this.model = model;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        model.applyHint();
    }
}
