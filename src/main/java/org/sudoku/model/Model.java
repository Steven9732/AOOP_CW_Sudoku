package org.sudoku.model;

import java.util.*;

@SuppressWarnings("deprecation")
public final class Model extends Observable implements SudokuModel {
    public static final int SIZE = 9;

    private Board board;

    private final List<Puzzle> puzzles; // Numbers loaded from puzzles.txt
    private final Random random = new Random();

    private int[][] initial; // Initial game board
    private int currentPuzzleIndex = -1;
    private boolean validationFeedbackEnabled = true;
    private boolean hintEnabled = true; // Hint flag
    private boolean randomPuzzleSelectionEnabled = true; // Puzzle selection flag

    private boolean solved = false;
    private boolean completionEventPending = false;

    private final Deque<Move> history = new ArrayDeque<>();
    private int[][] solution; // The solution for current puzzle

    // Used for cell modification recording
    private static final class Move {
        final int row, col;
        final int oldValue, newValue;

        Move(int row, int col, int oldValue, int newValue) {
            this.row = row;
            this.col = col;
            this.oldValue = oldValue;
            this.newValue = newValue;
        }
    }

    public Model() {
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
        int index;
        if (randomPuzzleSelectionEnabled) {
            index = random.nextInt(this.puzzles.size());
        } else {
            index = (currentPuzzleIndex + 1) % puzzles.size();
        }
        newGame(index);
    }

    private void newGame(int currentPuzzleIndex) {
        assert currentPuzzleIndex >= 0 && currentPuzzleIndex < puzzles.size() : "Puzzle index out of bounds";

        Puzzle puzzle = puzzles.get(currentPuzzleIndex);
        int[][] givens = puzzle.givenGrid();
        this.currentPuzzleIndex = currentPuzzleIndex;
        this.initial = Board.deepCopy(givens);
        this.board = new Board(givens);

        history.clear();
        solved = false;
        completionEventPending = false;

        solution = Board.deepCopy(initial);
        if (!solveInPlace(solution)) {
            throw new IllegalStateException("Puzzle has no solution");
        }

        changed();
        assert postNewGameCheck(givens) : "Illegal game data loading.";
        assertInvariants();
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
    @Override
    public int getCellValue(int row, int column) {
        return board.getCellValue(row, column);
    }

    // Check whether a specific cell is fixed
    @Override
    public boolean isFixed(int row, int column) {
        return board.isFixed(row, column);
    }

    // Check whether grids can be edited
    public boolean canBeEdit(int row, int column) {
        return board.canBeEdit(row, column);
    }

    // Modify modifiable value
    public boolean setValue(int row, int column, int value) {
        assert inRange(row, column) : "Row or column of the game board is out of bounds";

        int oldCellValue = getCellValue(row, column);

        if (!board.setValue(row, column, value)) {
            return false;
        }
        if (oldCellValue == value) {
            return true;
        }

        history.push(new Move(row, column, oldCellValue, value));
        updateCompletionStateAfterBoardChange();
        changed();
        assert board.getCellValue(row, column) == value : "Change is not successful";
        assertInvariants();
        return true;
    }

    // Erase value in selected cell
    public boolean clearValue(int row, int column) {
        assert inRange(row, column) : "Row or column of the game board is out of bounds";

        int  oldCellValue = getCellValue(row, column);

        if  (!board.clearValue(row, column)) {
            return false;
        }
        if  (oldCellValue == 0) {
            return true;
        }

        history.push(new Move(row, column, oldCellValue, 0));
        updateCompletionStateAfterBoardChange();
        changed();
        assert board.getCellValue(row, column) == 0 : "Clear is not successful";
        assertInvariants();
        return true;
    }

    // Return true if the cell is empty
    public boolean isEmpty(int row, int column) {
        return board.isEmpty(row, column);
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

    // Ensure no duplicates number in any row, column and 3x3 grid
    @Override
    public boolean isBoardValid() {
        return board.isBoardValid();
    }

    // Returns true if input number is duplicated
    public boolean isCellInvalid(int row, int column) {
        return board.isCellInvalid(row, column);
    }

    // Check whether game is initialize correctly
    private boolean postNewGameCheck(int[][] givens) {
        for  (int row = 0; row < SIZE; row++) {
            for (int column = 0; column < SIZE; column++) {
                if (board.getCellValue(row, column) != givens[row][column]) {return  false;}
                if (initial[row][column] != givens[row][column]) {return false;}
                if (board.isFixed(row, column) != (givens[row][column] != 0)) {return false;}
            }
        }
        return true;
    }

    // Check whether the current puzzle is solved
    public boolean isSolved() {
        return solved;
    }

    // Return true when puzzle is solved
    public boolean consumeCompletionEvent() {
        if (completionEventPending) {
            completionEventPending = false;
            return true;
        }
        return false;
    }

    // Check whether the board is full
    private boolean isBoardFull() {
        for  (int row = 0; row < SIZE; row++) {
            for (int column = 0; column < SIZE; column++) {
                if (board.getCellValue(row, column) == 0) {return false;}
            }
        }
        return true;
    }

    // Ensure the board is full and valid
    private boolean isCompletedAndValid() {
        return isBoardFull() && isBoardValid();
    }

    // Updates status of current game
    private void updateCompletionStateAfterBoardChange() {
        boolean wasSolved = solved;
        solved = isCompletedAndValid();
        // Check whether the state is changed
        if (!wasSolved && solved) {
            completionEventPending = true;
        }
    }

    public void setRandomPuzzleSelectionEnabled(boolean enabled) {
        if (this.randomPuzzleSelectionEnabled == enabled) return; // Ensure the status is really changed
        this.randomPuzzleSelectionEnabled = enabled;
        changed();
        assertInvariants();
    }

    // Get the status of the puzzle selection
    public boolean isRandomPuzzleSelectionEnabled() {return randomPuzzleSelectionEnabled;}

    // Get the status of whether hint is enabled
    public boolean isHintEnabled() {return hintEnabled;}

    public void setHintEnabled(boolean enabled) {
        if (this.hintEnabled == enabled) return; // Ensure the status is really changed
        this.hintEnabled = enabled;
        changed();
        assertInvariants();
    }

    // Undo the last step
    public boolean undo() {
        if (history.isEmpty()) return false;

        Move lastestMove = history.pop();
        board.setValue(lastestMove.row, lastestMove.col, lastestMove.oldValue);
        updateCompletionStateAfterBoardChange();
        changed();
        assertInvariants();
        return true;
    }

    @Override
    public boolean canUndo() {
        return !history.isEmpty();
    }

    // Set the game to the initial state
    @Override
    public boolean reset() {
        board = new Board(initial);
        history.clear();

        solved = false;
        completionEventPending = false;
        changed();
        assertInvariants();
        return true;
    }

    // Give hint to player
    @Override
    public boolean applyHint() {
        if (!hintEnabled) return false;
        if (solution == null) return false;

        for (int row = 0; row < SIZE; row++) {
            for (int column = 0; column < SIZE; column++) {
                if (board.canBeEdit(row, column) && board.isEmpty(row, column)) {
                    int oldValue = board.getCellValue(row, column);
                    int correctValue = solution[row][column];

                    history.push(new Move(row, column, oldValue, correctValue));
                    board.setValue(row, column, correctValue);

                    updateCompletionStateAfterBoardChange();
                    changed();
                    assertInvariants();
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean canApplyHint() {
        if (!hintEnabled) return false;
        if (solution == null) return false;
        for (int row = 0; row < SIZE; row++) {
            for (int column = 0; column < SIZE; column++) {
                if (board.canBeEdit(row, column) && board.isEmpty(row, column)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean isSafe(int[][] grid, int row, int column, int value) {
        // Check each row
        for (int c = 0; c < SIZE; c++) {
            if (grid[row][c] == value) {return false;}
        }
        // Check each column
        for (int r = 0; r < SIZE; r++) {
            if (grid[r][column] == value) {return false;}
        }
        //chcek each box
        int br = (row/3) * 3;
        int bc = (column/3) * 3;
        for (int dr = 0; dr < 3; dr++) {
            for (int dc = 0; dc < 3; dc++) {
                if (grid[br + dr][bc + dc] == value) {return false;}
            }
        }
        return true;
    }

    private static int[] findEmpty(int[][] grid) {
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                if (grid[r][c] == 0) return new int[]{r, c};
            }
        }
        return null;
    }

    private static boolean solveInPlace(int[][] grid) {
        int[] pos = findEmpty(grid);
        if (pos == null) return true; // solved

        int r = pos[0], c = pos[1];
        for (int v = 1; v <= 9; v++) {
            if (isSafe(grid, r, c, v)) {
                grid[r][c] = v;
                if (solveInPlace(grid)) return true;
                grid[r][c] = 0;
            }
        }
        return false;
    }

    // Check invariants
    private void assertInvariants() {
        assert board != null : "The game board must not be null";
        assert initial != null && initial.length == SIZE : "The initial board's row number should be 9";
        assert solution != null && solution.length == SIZE : "The solution board's row number should be 9";

        for (int row = 0; row < SIZE; row++) {
            assert initial[row] != null && initial[row].length == SIZE : "The initial board's column number should be 9";
            assert solution[row] != null && solution[row].length == SIZE : "The solution board's column number should be 9";

            for (int column = 0; column < SIZE; column++) {
                int valueOfBoard = board.getCellValue(row, column);
                int valueOfInitial = initial[row][column];
                int valueOfSolution = solution[row][column];

                assert valueOfBoard >= 0 && valueOfBoard <= 9 : "The range of the number in the game board should be between 0 and 9.";
                assert valueOfInitial >= 0 && valueOfInitial <= 9 : "The range of the number in the initial board should be between 0 and 9.";
                assert valueOfSolution >= 1 && valueOfSolution <= 9 : "The range of the number in the solution board should be between 1 and 9.";

                assert board.isFixed(row, column) == (valueOfInitial != 0) : "Fixed cells must correspond to non-zero givens.";
                assert !board.isFixed(row, column) || board.getCellValue(row, column) == initial[row][column] : "Fixed cells can never be changed.";
                assert board.getCellValue(row, column) != 0 || !board.isCellInvalid(row, column) : "Empty cells are never invalid.";
            }
        }

        assert currentPuzzleIndex >= -1 && currentPuzzleIndex < puzzles.size() : "Puzzle index should be in range.";

        if (solved) {
            assert isBoardFull() : "The pre-condition of solved all cells in board is full.";
            assert isBoardValid() : "The pre-condition of solved all cells in board is valid.";
        }
    }
}
