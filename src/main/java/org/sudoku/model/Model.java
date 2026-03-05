package org.sudoku.model;

import java.util.*;

@SuppressWarnings("deprecation")
public final class Model extends Observable {
    public static final int SIZE = 9;

    private final List<Puzzle> puzzles; // Numbers loaded from puzzles.txt
    private final Random random = new Random();

    private int[][] board; // Game boaed
    private boolean[][] fixed; // Immutable grids during game time
    private int[][] initial; // Initial game board
    private int currentPuzzleIndex = -1;
    private boolean validationFeedbackEnabled = true;

    Model() {
        this.puzzles = Collections.unmodifiableList(
                PuzzleLoader.loadPuzzlesFromFile("puzzles.txt")
        );
        if (this.puzzles.isEmpty()) {
            throw new IllegalArgumentException("Puzzles file is empty");
        }
        newGame();
    }

    // Load a new random puzzle
    public void newGame() {
        newGame(random.nextInt(puzzles.size()));
    }

    private void newGame(int currentPuzzleIndex) {
        assert currentPuzzleIndex >= 0 && currentPuzzleIndex < puzzles.size() : "Puzzle index out of bounds";

        Puzzle puzzle = puzzles.get(currentPuzzleIndex);
        int[][] givens = puzzle.givenGrid();
        this.currentPuzzleIndex = currentPuzzleIndex;
        this.initial = deepCopy(givens); // Backup board for restart
        this.board = deepCopy(givens); // Initial state of gameboard
        this.fixed = fixedFromInitial(initial); // Determine which grids can be changed during game time

        changed(); // Notify observers when the status of model being changed
        assert postNewGameCheck(givens) : "Illegal game data loading.";
        assertInvariants();
    }

    private static int[][] deepCopy(int[][] src) {
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

    // Notify observers when the status of model being changed
    private void changed() {
        setChanged();
        notifyObservers();
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
        changed();
        assert board[row][column] == value : "Change is not successful";
        assertInvariants();
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
        changed();
        assert board[row][column] == 0 : "Clear is not successful";
        assertInvariants();
        return true;
    }

    // Return true if the cell is empty
    public boolean isEmpty(int row, int column) {
        assert inRange(row, column) : "Row or column of the game board is out of bounds";
        return board[row][column] == 0;
    }

    public boolean isValidationFeedbackEnabled() {
        return validationFeedbackEnabled;
    }

    // To determine whether the cell with wrong answer need to be highlighted
    public void setValidationFeedbackEnabled(boolean validationFeedbackEnabled) {
        if (this.validationFeedbackEnabled == validationFeedbackEnabled) return;
        this.validationFeedbackEnabled = validationFeedbackEnabled;
        changed();
        assertInvariants();
    }

    // Computes a 9x9 map marking cells that are part of any duplicate
    private boolean[][] computeInvalidCells() {
        boolean[][] invalidCell = new boolean[SIZE][SIZE];

        // rows
        for (int row = 0; row < SIZE; row++) {
            int[] count = new int[10];
            for (int column = 0; column < SIZE; column++) {
                int value = board[row][column];
                if (value != 0) {
                    count[column]++;
                }
            }
            for (int column = 0; column < SIZE; column++) {
                int value = board[row][column];
                if (value != 0 && count[column] > 1) {
                    invalidCell[row][column] = true;
                }
            }
        }

        // Columns
        for (int column = 0; column < SIZE; column++) {
            int[] count = new int[10];
            for (int row = 0; row < SIZE; row++) {
                int value = board[row][column];
                if (value != 0) {
                    count[column]++;
                }
            }
            for (int row = 0; row < SIZE; row++) {
                int value = board[row][column];
                if (value != 0 && count[column] > 1) {
                    invalidCell[row][column] = true;
                }
            }
        }

        // 3x3 subgrid
        for (int boxR = 0; boxR < 3; boxR++) {
            for (int boxC = 0; boxC < 3; boxC++) {
                int[] count = new int[10];
                int r0 = boxR * 3;
                int c0 = boxC * 3;
                for (int rowInBox = 0; rowInBox < 3; rowInBox++) {
                    for (int columnInBox = 0; columnInBox < 3; columnInBox++) {
                        int value = board[r0 + rowInBox][c0 + columnInBox];
                        if (value != 0) {
                            count[value]++;
                        }
                    }
                }
                for (int rowInBox = 0; rowInBox < 3;  rowInBox++) {
                    for (int columnInBox = 0; columnInBox < 3; columnInBox++) {
                        int value = board[r0 + rowInBox][c0 + columnInBox];
                        if  (value != 0 && count[value] > 1) {
                            invalidCell[rowInBox][columnInBox] = true;
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
    public boolean isCellValid(int row, int column) {
        assert inRange(row, column) : "Row or column of the game board is out of bounds";
        return computeInvalidCells()[row][column];
    }

    // Check whether game is initialize correctly
    private boolean postNewGameCheck(int[][] givens) {
        for  (int row = 0; row < SIZE; row++) {
            for (int column = 0; column < SIZE; column++) {
                if (board[row][column] != givens[row][column]) {return  false;}
                if (initial[row][column] != givens[row][column]) {return false;}
                if (fixed[row][column] != (givens[row][column] != 0)) {return false;}
            }
        }
        return true;
    }

    // Check invariants
    private void assertInvariants() {
        assert board != null && board.length == SIZE : "The game board's row number should be 9";
        assert initial != null && initial.length == SIZE : "The initial board's row number should be 9";
        assert fixed  != null && fixed.length == SIZE : "The fixed board's row number should be 9";

        for  (int row = 0; row < SIZE; row++) {
            assert board[row] != null && board[row].length == SIZE : "The game board's column number should be 9";
            assert initial[row] != null && initial[row].length == SIZE : "The initial board's column number should be 9";
            assert fixed[row] != null && fixed[row].length == SIZE : "The fixed board's column number should be 9";
            for (int column = 0; column < SIZE; column++) {
                int valueOfBoard = board[row][column];
                int valueOfInitial = initial[row][column];
                assert valueOfBoard >= 0 && valueOfBoard <=9 : "The range of the number in the game board should between 0 and 9.";
                assert valueOfInitial >= 0 && valueOfInitial <= 9 :  "The range of the number in the game board should between 0 and 9.";
                assert fixed[row][column] == (valueOfInitial != 0) : "fixed cells must correspond to non-zero givens.";
                assert !fixed[row][column] || board[row][column] == initial[row][column] : "Fixed cells can never be changed.";
            }
        }
        assert currentPuzzleIndex >= -1 && currentPuzzleIndex < puzzles.size() : "Puzzle index should be in range.";
    }
}
