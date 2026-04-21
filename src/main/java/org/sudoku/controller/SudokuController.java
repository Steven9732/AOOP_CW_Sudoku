package org.sudoku.controller;

import org.sudoku.model.Model;
import org.sudoku.view.SudokuFrame;

import javax.swing.JButton;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Objects;

public final class SudokuController {
    private final Model model;
    private final SudokuFrame frame;

    private int selectedRow = -1;
    private int selectedColumn = -1;

    /**
     * Creates a controller for the given model and frame.
     * @param model the Sudoku model
     * @param frame the Sudoku GUI frame
     */
    public SudokuController(Model model, SudokuFrame frame) {
        this.model = Objects.requireNonNull(model, "model must not be null");
        this.frame = Objects.requireNonNull(frame, "frame must not be null");

        frame.getBoardPanel().setCellClickHandler((row, column) -> {
            selectedRow = row;
            selectedColumn = column;
            refreshView();
            frame.requestFocusInWindow();
        });

        frame.getEraseButton().addActionListener(event -> {
            new EraseAction(model, () -> selectedRow, () -> selectedColumn).actionPerformed(event);
            frame.requestFocusInWindow();
        });

        frame.getUndoButton().addActionListener(event -> {
            new UndoAction(model).actionPerformed(event);
            frame.requestFocusInWindow();
        });

        frame.getHintButton().addActionListener(event -> {
            new HintAction(model, () -> selectedRow, () -> selectedColumn).actionPerformed(event);
            frame.requestFocusInWindow();
        });

        frame.getResetButton().addActionListener(event -> {
            new ResetAction(model).actionPerformed(event);
            frame.requestFocusInWindow();
        });

        frame.getNewGameButton().addActionListener(event -> {
            new NewGameAction(model).actionPerformed(event);
            frame.requestFocusInWindow();
        });

        for (int i = 1; i <= 9; i++) {
            final int digit = i;
            frame.getDigitButton(i).addActionListener(event -> {

                // A digit is applied only when there is a valid selected cell.
                assert selectedRow >= 0 && selectedRow < Model.SIZE : "Selected row is out of bounds";
                assert selectedColumn >= 0 && selectedColumn < Model.SIZE : "Selected column is out of bounds";

                model.setValue(selectedRow, selectedColumn, digit);
            });
        }

        // Bind GUI option toggles to the model flags.
        frame.getValidationCheckBox().addActionListener(event ->
                model.setValidationFeedbackEnabled(frame.getValidationCheckBox().isSelected())
        );

        frame.getHintCheckBox().addActionListener(event ->
                model.setHintEnabled(frame.getHintCheckBox().isSelected())
        );

        frame.getRandomCheckBox().addActionListener(event ->
                model.setRandomPuzzleSelectionEnabled(frame.getRandomCheckBox().isSelected())
        );

        refreshView();

        frame.setFocusable(true);
        frame.requestFocusInWindow();
        frame.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                // Only editable selected cells can receive typed digits.
                if (selectedRow < 0 || selectedRow >= Model.SIZE
                        || selectedColumn < 0 || selectedColumn >= Model.SIZE) {
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

                // Arrow keys move the selection.
                switch (code) {
                    case KeyEvent.VK_UP -> moveSelection(-1, 0);
                    case KeyEvent.VK_DOWN -> moveSelection(1, 0);
                    case KeyEvent.VK_LEFT -> moveSelection(0, -1);
                    case KeyEvent.VK_RIGHT -> moveSelection(0, 1);
                    case KeyEvent.VK_BACK_SPACE, KeyEvent.VK_DELETE -> {
                        // Keep selected cell in boundaries
                        if (selectedRow >= 0 && selectedRow < Model.SIZE
                                && selectedColumn >= 0 && selectedColumn < Model.SIZE
                                && model.canBeEdit(selectedRow, selectedColumn)) {
                            model.clearValue(selectedRow, selectedColumn);
                        }
                    }
                    default -> {
                    }
                }
            }
        });
    }

    /**
     * Refreshes the view after the model changes.
     * @return true if a one-shot completion event is pending
     */
    public boolean handleModelChanged() {
        refreshView();
        return model.consumeCompletionEvent();
    }

    /**
     * Moves the current selection within board boundaries.
     * @param deltaRow row offset
     * @param deltaColumn column offset
     */
    private void moveSelection(int deltaRow, int deltaColumn) {
        // If nothing is selected yet, start from the top-left cell.
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

    /**
     * Refreshes board display, selection display, control states, and status text.
     */
    private void refreshView() {
        frame.getBoardPanel().setSelectedCell(selectedRow, selectedColumn);
        frame.getBoardPanel().refreshFromModel(model);
        updateControlStates();

        String selected = (selectedRow < 0 || selectedColumn < 0)
                ? "no selection"
                : ("selected = (" + (selectedRow + 1) + ", " + (selectedColumn + 1) + ")");
        String valid = model.isBoardValid() ? "valid" : "invalid";
        String solved = model.isSolved() ? "solved" : "";

        frame.setStatusText(selected + " | " + valid + (solved.isEmpty() ? "" : " | " + solved));
    }

    /**
     * Updates button enabled states according to the current selection and model state.
     */
    private void updateControlStates() {
        // Enable or disable controls according to the current selection and model state.
        boolean hasSelection = selectedRow >= 0 && selectedRow < Model.SIZE
                && selectedColumn >= 0 && selectedColumn < Model.SIZE;
        boolean selectedEditable = hasSelection && model.canBeEdit(selectedRow, selectedColumn);
        boolean selectedNonEmptyEditable = selectedEditable && !model.isEmpty(selectedRow, selectedColumn);
        boolean selectedHintApplicable = hasSelection && model.canApplyHint(selectedRow, selectedColumn);

        frame.getEraseButton().setEnabled(selectedNonEmptyEditable);
        frame.getUndoButton().setEnabled(model.canUndo());
        frame.getHintButton().setEnabled(selectedHintApplicable);

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