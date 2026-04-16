package org.sudoku.controller;

import java.awt.event.KeyAdapter;

import org.sudoku.model.Model;
import org.sudoku.view.SudokuFrame;

public final class SudokuController {
    private final Model model;
    private final SudokuFrame frame;

    private int selectedRow = -1;
    private int selectedColumn = -1;

    public SudokuController(Model model, SudokuFrame frame) {
        this.model = model;
        this.frame = frame;

        frame.getBoardPanel().setCellClickHandler((row, column) -> {
            selectedRow = row;
            selectedColumn = column;
            refreshView();
            frame.requestFocusInWindow();
        });

        // 5 buttons
        frame.getEraseButton().addActionListener(new EraseAction(model, () -> selectedRow, () -> selectedColumn));
        frame.getUndoButton().addActionListener(new UndoAction(model));
        frame.getHintButton().addActionListener(new HintAction(model));
        frame.getResetButton().addActionListener(new ResetAction(model));
        frame.getNewGameButton().addActionListener(new NewGameAction(model));

        // Virtual keypad
        for (int i = 1; i <= 9; i++) {
            final int digit = i;
            frame.getDigitButton(i).addActionListener(event -> {
                if (selectedRow < 0 || selectedColumn < 0) {
                    return;
                }
                model.setValue(selectedRow, selectedColumn, digit);
            });
        }

        // Flags
        frame.getValidationCheckBox().addActionListener(event -> {
            model.setValidationFeedbackEnabled(frame.getValidationCheckBox().isSelected());
        });

        frame.getHintCheckBox().addActionListener(event -> {
            model.setHintEnabled(frame.getHintCheckBox().isSelected());
        });

        frame.getRandomCheckBox().addActionListener(event -> {
            model.setRandomPuzzleSelectionEnabled(frame.getRandomCheckBox().isSelected());
        });

        refreshView();

        frame.setFocusable(true);
        frame.requestFocusInWindow();
        frame.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(java.awt.event.KeyEvent e) {
                if (selectedRow < 0 || selectedColumn < 0) return;

                char ch = e.getKeyChar();
                if (ch >= '1' && ch <= '9') {
                    int digit = ch - '0';
                    model.setValue(selectedRow, selectedColumn, digit);
                }
            }

            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                if (selectedRow < 0 || selectedColumn < 0) return;

                int code = e.getKeyCode();
                if (code == java.awt.event.KeyEvent.VK_BACK_SPACE ||
                        code == java.awt.event.KeyEvent.VK_DELETE) {
                    model.clearValue(selectedRow, selectedColumn);
                }
            }
        });
    }

    public boolean handleModelChanged() {
        refreshView();
        return model.consumeCompletionEvent();
    }

    private void refreshView() {
        frame.getBoardPanel().setSelectedCell(selectedRow, selectedColumn);
        frame.getBoardPanel().refreshFromModel(model);

        String selected = (selectedRow < 0)
                ? "no selection"
                : ("selected = (" + (selectedRow + 1) + ", " + (selectedColumn + 1) + ")");
        String valid = model.isBoardValid() ? "valid" : "invalid";
        String solved = model.isSolved() ? "solved" : " ";
        frame.setStatusText(selected + " | " + valid + (solved.isEmpty() ? "" : " | " + solved));
    }
}