package org.sudoku.controller;

import org.sudoku.model.Model;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class ResetAction implements ActionListener{
    private final Model model;

    public ResetAction(Model model){
        this.model = model;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        model.reset();
    }
}
