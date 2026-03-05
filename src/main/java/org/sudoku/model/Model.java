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
    public boolean canEdit(int row, int column) {
        assert inRange(row, column) : "Row or column of the game board is out of bounds";
        return !fixed[row][column];
    }

    // Modify modifiable value
    public boolean setValue(int row, int column, int value) {
        assert inRange(row, column) : "Row or column of the game board is out of bounds";

        int oldCellValue = getCellValue(row, column);

        if (!canEdit(row, column)) {
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

        if  (!canEdit(row, column)) {
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
