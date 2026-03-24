package org.sudoku.model;

public interface SudokuModel {
    void newGame();

    int getCellValue(int row, int column); // Get value of a specific cell

    boolean isFixed(int row, int column); // Check whether a specific cell is fixed

    boolean isEmpty(int row, int column); // Check whether the cell is empty

    boolean canBeEdit(int row, int column); // Check whether grids can be edited

    boolean setValue(int row, int column, int value); // Modify modifiable value

    boolean clearValue(int row, int column); // Erase value in selected cell

    boolean undo();

    boolean reset();

    boolean isValidationFeedbackEnabled();

    void setValidationFeedbackEnabled(boolean validationFeedbackEnabled); // To determine whether the cell with wrong answer need to be highlighted

    boolean isBoardValid();

    boolean isCellInvalid(int row, int column);

    boolean isSolved();

    boolean consumeCompletionEvent();

    void setRandomPuzzleSelectionEnabled(boolean enabled);

    boolean isRandomPuzzleSelectionEnabled();

    boolean applyHint();

    boolean isHintEnabled();

    void setHintEnabled(boolean enabled);
}
