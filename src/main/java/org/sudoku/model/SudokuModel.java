package org.sudoku.model;

public interface SudokuModel {
    void newGame();

    int getCellValue(int row, int column);

    boolean isFixed(int row, int column);

    boolean isEmpty(int row, int column);

    boolean canBeEdit(int row, int column);

    boolean setValue(int row, int column, int value);

    boolean clearValue(int row, int column);

    boolean undo();

    boolean canUndo();

    boolean reset();

    boolean isValidationFeedbackEnabled();

    void setValidationFeedbackEnabled(boolean validationFeedbackEnabled);

    boolean isBoardValid();

    boolean isCellInvalid(int row, int column);

    boolean isSolved();

    boolean consumeCompletionEvent();

    void setRandomPuzzleSelectionEnabled(boolean enabled);

    boolean isRandomPuzzleSelectionEnabled();

    boolean applyHint();

    boolean canApplyHint();

    boolean isHintEnabled();

    void setHintEnabled(boolean enabled);
}