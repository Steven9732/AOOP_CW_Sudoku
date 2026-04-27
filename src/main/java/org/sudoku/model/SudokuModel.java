package org.sudoku.model;

import java.util.Observer;

//@SuppressWarnings("deprecation")
public interface SudokuModel {
    int SIZE = 9;

    /**
     * Starts a new game.
     */
    void newGame();

    /**
     * Returns the current value stored in one cell.
     */
    int getCellValue(int row, int column);

    /**
     * Returns whether one cell is fixed by the original puzzle.
     */
    boolean isFixed(int row, int column);

    /**
     * Returns whether one cell is currently empty.
     */
    boolean isEmpty(int row, int column);

    /**
     * Returns whether one cell can be edited by the player.
     */
    boolean canBeEdit(int row, int column);

    /**
     * Set value to specific cell.
     */
    boolean setValue(int row, int column, int value);

    /**
     * Clear value in a specific cell.
     */
    boolean clearValue(int row, int column);

    /**
     * Reverts the most recent accepted board-changing action.
     */
    boolean undo();

    /**
     * Returns whether an undo action is currently available.
     */
    boolean canUndo();

    /**
     * Restores the current puzzle to its initial state.
     */
    boolean reset();

    /**
     * Returns whether immediate validation feedback is enabled.
     */
    boolean isValidationFeedbackEnabled();

    /**
     * Enables or disables immediate validation feedback.
     */
    void setValidationFeedbackEnabled(boolean validationFeedbackEnabled);

    /**
     * Returns whether the board is valid.
     */
    boolean isBoardValid();

    /**
     * Returns whether one cell is currently invalid because of duplication.
     */
    boolean isCellInvalid(int row, int column);

    /**
     * Returns whether the current puzzle is solved.
     */
    boolean isSolved();

    /**
     * Consumes the one-shot completion event.
     */
    boolean consumeCompletionEvent();

    /**
     * Enables or disables random puzzle selection.
     */
    void setRandomPuzzleSelectionEnabled(boolean enabled);

    /**
     * Applies one hint to one eligible cell.
     */
    boolean applyHint(int row, int column);

    /**
     * Returns whether a hint can currently be applied to one cell.
     */
    boolean canApplyHint(int row, int column);

    /**
     * Enables or disables the hint feature.
     */
    void setHintEnabled(boolean enabled);

    /**
     * Registers a view observer for model updates.
     */
    void addObserver(Observer observer);
}