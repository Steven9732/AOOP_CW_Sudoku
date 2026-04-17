package org.sudoku.controller;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import org.sudoku.model.Model;
import org.sudoku.view.SudokuFrame;

import javax.swing.JButton;

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
            public void keyTyped(KeyEvent e) {
                if (selectedRow < 0 || selectedColumn < 0) {
                    return;
                }
                if (!model.canBeEdit(selectedRow, selectedColumn)) {
                    return;
                }

                char ch = e.getKeyChar();
                if (ch >= '1' && ch <= '9') {
                    int digit = ch - '0';
                    model.setValue(selectedRow, selectedColumn, digit);
                }
            }

            @Override
            public void keyPressed(KeyEvent e) {
                int code = e.getKeyCode();

                switch (code) {
                    case KeyEvent.VK_UP -> moveSelection(-1, 0);
                    case KeyEvent.VK_DOWN -> moveSelection(1, 0);
                    case KeyEvent.VK_LEFT -> moveSelection(0, -1);
                    case KeyEvent.VK_RIGHT -> moveSelection(0, 1);
                    case KeyEvent.VK_BACK_SPACE, KeyEvent.VK_DELETE -> {
                        if (selectedRow >= 0 && selectedColumn >= 0 && model.canBeEdit(selectedRow, selectedColumn)) {
                            model.clearValue(selectedRow, selectedColumn);
                        }
                    }
                    default -> {
                    }
                }
            }
        });
    }

    public boolean handleModelChanged() {
        refreshView();
        return model.consumeCompletionEvent();
    }

    private void moveSelection(int deltaRow, int deltaColumn) {
        if (selectedRow < 0 || selectedColumn < 0) {
            selectedRow = 0;
            selectedColumn = 0;
        } else {
            selectedRow = Math.max(0, Math.min(Model.SIZE - 1, selectedRow + deltaRow));
            selectedColumn = Math.max(0, Math.min(Model.SIZE - 1, selectedColumn + deltaColumn));
        }

        refreshView();
        frame.requestFocusInWindow();
    }

    private void refreshView() {
        frame.getBoardPanel().setSelectedCell(selectedRow, selectedColumn);
        frame.getBoardPanel().refreshFromModel(model);
        updateControlStates();

        String selected = (selectedRow < 0)
                ? "no selection"
                : ("selected = (" + (selectedRow + 1) + ", " + (selectedColumn + 1) + ")");
        String valid = model.isBoardValid() ? "valid" : "invalid";
        String solved = model.isSolved() ? "solved" : "";
        frame.setStatusText(selected + " | " + valid + (solved.isEmpty() ? "" : " | " + solved));
    }

    private void updateControlStates() {
        boolean hasSelection = selectedRow >= 0 && selectedColumn >= 0;
        boolean selectedEditable = hasSelection && model.canBeEdit(selectedRow, selectedColumn);
        boolean selectedNonEmptyEditable = selectedEditable && !model.isEmpty(selectedRow, selectedColumn);

        frame.getEraseButton().setEnabled(selectedNonEmptyEditable);
        frame.getUndoButton().setEnabled(model.canUndo());
        frame.getHintButton().setEnabled(model.canApplyHint());

        for (int i = 1; i <= 9; i++) {
            JButton button = frame.getDigitButton(i);
            button.setEnabled(selectedEditable);
        }

        frame.getResetButton().setEnabled(true);
        frame.getNewGameButton().setEnabled(true);

        frame.getValidationCheckBox().setEnabled(true);
        frame.getHintCheckBox().setEnabled(true);
        frame.getRandomCheckBox().setEnabled(true);
    }
}