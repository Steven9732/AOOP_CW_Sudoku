package org.sudoku.model;

import java.util.ArrayList;
import java.util.Arrays;

import static org.sudoku.model.Puzzle.is9x9;

final class Board {
    static final int SIZE = 9;

    private int[][] board;
    private boolean fixed[][];

    /**
     * Creates a board from the given initial puzzle.
     *
     * @param initial the initial 9x9 grid; non-zero cells are treated as fixed cells that users cannot modify them
     */
    public Board(int[][] initial) {
        assert is9x9(initial) : "Initial puzzle must be a 9x9 grid with values from 0 to 9.";
        this.board = deepCopy(initial);
        this.fixed = fixedFromInitial(initial);
    }

    /**
     * Creates a deep copy of a 2D int array.
     *
     * @param src the source array
     * @return a new 2D array with copied row contents
     */
    protected static int[][] deepCopy(int[][] src) {
        int[][] out = new int[src.length][];
        for (int i = 0; i < src.length; i++) {
            out[i] = Arrays.copyOf(src[i], src[i].length);
        }
        return out;
    }

    /**
     * Builds the fixed-cell map from the initial grid.
     * A cell is fixed if its initial value is non-zero.
     */
    private static boolean[][] fixedFromInitial(int[][] initial) {
        boolean[][] fixedBoard = new boolean[SIZE][SIZE];
        for (int row = 0; row < SIZE; row++) {
            for (int column = 0; column < SIZE; column++) {
                fixedBoard[row][column] = initial[row][column] != 0;
            }
        }
        return fixedBoard;
    }

    /**
     * Return whether the value is in legal range.
     * This board accepts 0 for empty cells and 1 to 9 for fixed values.
     */
    private static boolean isValidDigit(int value) {
        return value >= 0 && value <= 9;
    }

    /**
     * Return whether the selected row or column are within the board
     */
    private static boolean inRange(int row, int column) {
        return row >= 0 && row < SIZE && column >= 0 && column < SIZE;
    }

    /**
     * Returns whether the specified cell is fixed.
     *
     * @param row    row index from 0
     * @param column column index from 0
     * @return the cell value, where 0 means empty cell
     */
    protected int getCellValue(int row, int column) {
        assert inRange(row, column) : "Row or column of the game board is out of bounds";
        return board[row][column];
    }

    /**
     * Returns whether the selected cell is fixed
     *
     * @param row    row index from 0
     * @param column column index from 0
     * @return true if the original value of a cell is from 1 to 9
     */
    protected boolean isFixed(int row, int column) {
        assert inRange(row, column) : "Row or column of the game board is out of bounds";
        return fixed[row][column];
    }

    /**
     * Returns whether a specific cell can be edited by users
     *
     * @param row    row index from 0
     * @param column column index from 0
     * @return true if the cell is editable
     */
    protected boolean canBeEdit(int row, int column) {
        assert inRange(row, column) : "Row or column of the game board is out of bounds";
        return !fixed[row][column];
    }

    /**
     * Set a value to a selected cell
     *
     * @param row    row index from 0
     * @param column column index from 0
     * @param value  the new value to be assigned to a cell
     * @return true if the request is accepted, false otherwise
     */
    protected boolean setValue(int row, int column, int value) {
        assert inRange(row, column) : "Row or column of the game board is out of bounds";

        int oldCellValue = getCellValue(row, column);

        if (!canBeEdit(row, column)) {
            assert board[row][column] == oldCellValue : "This cell is fixed, can't be modified";
            return false;
        }
        if (!isValidDigit(value)) {
            assert board[row][column] == oldCellValue : "Only number between 0 and 9 is accepted.";
            return false;
        }
        // The board stays unchanged.
        if (board[row][column] == value) {
            assert oldCellValue == value;
            return true; // Don't need to notify
        }

        board[row][column] = value;
        assert board[row][column] == value : "Change is not successful.";
        return true;
    }

    /**
     * Clears an editable value
     *
     * @param row    row index from 0
     * @param column column index from 0
     * @return true if the request successful, false otherwise
     */
    protected boolean clearValue(int row, int column) {
        assert inRange(row, column) : "Row or column of the game board is out of bounds";

        int oldCellValue = getCellValue(row, column);

        if (!canBeEdit(row, column)) {
            assert board[row][column] == oldCellValue : "This cell is fixed, can't be modified";
            return false;
        }
        // Clearing an already empty editable cell.
        if (board[row][column] == 0) {
            return true; // Don't need to notify
        }
        board[row][column] = 0;
        assert board[row][column] == 0 : "Clear is not successful.";
        return true;
    }

    /**
     * Return whether a cell is empty
     *
     * @param row    row index from 0
     * @param column column index from 0
     * @return ture if the value of the cell is 0
     */
    protected boolean isEmpty(int row, int column) {
        assert inRange(row, column) : "Row or column of the game board is out of bounds";
        return board[row][column] == 0;
    }

    /**
     * A cell is marked invalid if its non-zero value is duplicated in its row, column, or 3x3 sub-grid.
     *
     * @return a boolean map
     */
    protected boolean[][] computeInvalidCells() {
        boolean[][] invalidCell = new boolean[SIZE][SIZE];

        // Check rows
        for (int row = 0; row < SIZE; row++) {
            int[] count = new int[10];
            for (int column = 0; column < SIZE; column++) {
                int value = board[row][column];
                if (value != 0) {
                    count[value]++;
                }
            }
            for (int column = 0; column < SIZE; column++) {
                int value = board[row][column];
                if (value != 0 && count[value] > 1) {
                    invalidCell[row][column] = true;
                }
            }
        }

        // Check columns
        for (int column = 0; column < SIZE; column++) {
            int[] count = new int[10];
            for (int row = 0; row < SIZE; row++) {
                int value = board[row][column];
                if (value != 0) {
                    count[value]++;
                }
            }
            for (int row = 0; row < SIZE; row++) {
                int value = board[row][column];
                if (value != 0 && count[value] > 1) {
                    invalidCell[row][column] = true;
                }
            }
        }

        // Check 3x3 sub-grids
        for (int boxRow = 0; boxRow < 3; boxRow++) {
            for (int boxColumn = 0; boxColumn < 3; boxColumn++) {
                int[] count = new int[10];
                int startRow = boxRow * 3;
                int startColumn = boxColumn * 3;

                for (int dr = 0; dr < 3; dr++) {
                    for (int dc = 0; dc < 3; dc++) {
                        int value = board[startRow + dr][startColumn + dc];
                        if (value != 0) {
                            count[value]++;
                        }
                    }
                }

                for (int dr = 0; dr < 3; dr++) {
                    for (int dc = 0; dc < 3; dc++) {
                        int value = board[startRow + dr][startColumn + dc];
                        if (value != 0 && count[value] > 1) {
                            invalidCell[startRow + dr][startColumn + dc] = true;
                        }
                    }
                }
            }
        }

        return invalidCell;
    }

    /**
     * Returns whether the current board has no duplicates in any row, column, or box.
     *
     * @return true if the board without duplication
     */
    protected boolean isBoardValid() {
        boolean[][] invalid = computeInvalidCells();
        for (int row = 0; row < SIZE; row++) {
            for (int column = 0; column < SIZE; column++) {
                if (invalid[row][column]) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Returns whether one cell is currently invalid because of duplication
     *
     * @param row    row index from 0
     * @param column column index from 0
     * @return true if the cell is not duplicated
     */
    protected boolean isCellInvalid(int row, int column) {
        assert inRange(row, column) : "Row or column of the game board is out of bounds";
        return computeInvalidCells()[row][column];
    }
}
