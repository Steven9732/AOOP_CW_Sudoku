package org.sudoku.model;

import java.util.ArrayList;
import java.util.Arrays;

final class Board {
    static final int SIZE = 9;

    private int[][] board;
    private boolean fixed[][];

    public Board(int[][] initial) {
        this.board = deepCopy(initial);
        this.fixed = fixedFromInitial(initial);
    }

    protected static int[][] deepCopy(int[][] src) {
        int[][] out = new int[src.length][];
        for (int i = 0; i < src.length; i++) {
            out[i] = Arrays.copyOf(src[i], src[i].length);
        }
        return out;
    }

    // Grid is modifiable if initial state is 0, otherwise is unmodifiable
    private static boolean[][] fixedFromInitial(int[][] initial) {
        boolean[][] fixedBoard = new boolean[SIZE][SIZE];
        for (int row = 0; row < SIZE; row++) {
            for (int column = 0; column < SIZE; column++) {
                fixedBoard[row][column] = initial[row][column] != 0;
            }
        }
        return fixedBoard;
    }

    // Ensure input value is in legal range
    private static boolean isValidDigit(int value) {
        return value >= 0 && value <= 9;
    }

    // Ensure the data is in the range of bounds
    private static boolean inRange(int row, int column) {
        return row >= 0 && row < SIZE && column >= 0 && column < SIZE;
    }

    // Get value of a specific cell
    public int getCellValue(int row, int column) {
        assert inRange(row, column) : "Row or column of the game board is out of bounds";
        return board[row][column];
    }

    // Check whether a specific cell is fixed
    public boolean isFixed(int row, int column) {
        assert inRange(row, column) :  "Row or column of the game board is out of bounds";
        return fixed[row][column];
    }

    // Check whether grids can be edited
    public boolean canBeEdit(int row, int column) {
        assert inRange(row, column) : "Row or column of the game board is out of bounds";
        return !fixed[row][column];
    }

    // Modify modifiable value
    public boolean setValue(int row, int column, int value) {
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
        if (board[row][column] == value) {
            assert oldCellValue == value;
            return true; // Don't need to notify
        }

        board[row][column] = value;
        assert board[row][column] == value : "Change is not successful.";
        return true;
    }

    // Erase value in selected cell
    public boolean clearValue(int row, int column) {
        assert inRange(row, column) : "Row or column of the game board is out of bounds";

        int  oldCellValue = getCellValue(row, column);

        if  (!canBeEdit(row, column)) {
            assert board[row][column] == oldCellValue : "This cell is fixed, can't be modified";
            return false;
        }
        if (board[row][column] == 0) {
            return true; // Don't need to notify
        }
        board[row][column] = 0;
        assert board[row][column] == 0 : "Clear is not successful.";
        return true;
    }

    // Return true if the cell is empty
    public boolean isEmpty(int row, int column) {
        assert inRange(row, column) : "Row or column of the game board is out of bounds";
        return board[row][column] == 0;
    }

    // Computes a 9x9 map marking cells that are part of any duplicate
    private boolean[][] computeInvalidCells() {
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

    // Ensure no duplicates number in any row, column and 3x3 grid
    public boolean isBoardValid() {
        boolean[][] invalid = computeInvalidCells();
        for  (int row = 0; row < SIZE; row++) {
            for  (int column = 0; column < SIZE; column++) {
                if (invalid[row][column]) {
                    return false;
                }
            }
        }
        return true;
    }

    // Returns true if input number is duplicated
    public boolean isCellInvalid(int row, int column) {
        assert inRange(row, column) : "Row or column of the game board is out of bounds";
        return computeInvalidCells()[row][column];
    }

    // Check whether the board is full
    private boolean isBoardFull() {
        for  (int row = 0; row < SIZE; row++) {
            for (int column = 0; column < SIZE; column++) {
                if (board[row][column] == 0) {return false;}
            }
        }
        return true;
    }
}
