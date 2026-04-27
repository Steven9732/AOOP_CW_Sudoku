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

    /**
     * Store user movement for single-level undo
     */
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

    /**
     * Replaces any previous undo history with one newly recorded move.
     */
    private void recordSingleUndo(int row, int col, int oldValue, int newValue) {
        history.clear();
        history.push(new Move(row, col, oldValue, newValue));
    }

    /**
     * Constructor of Model class.
     */
    public Model() {
        this.puzzles = Collections.unmodifiableList(
                PuzzleLoader.loadPuzzlesFromFile("puzzles.txt")
        );
        if (this.puzzles.isEmpty()) {
            throw new IllegalArgumentException("Puzzles file is empty");
        }
        newGame();
    }

    /**
     * Replaces the current board with a new puzzle.
     */
    @Override
    public void newGame() {
        int index;
        // Random mode picks any puzzle
        if (randomPuzzleSelectionEnabled) {
            index = random.nextInt(this.puzzles.size());
        } else {
            // Non-random mode advances through the list in order
            index = (currentPuzzleIndex + 1) % puzzles.size();
        }
        newGame(index);
    }

    /**
     * Loads a specific puzzle by index.
     * @param currentPuzzleIndex index of the puzzle to load
     */
    private void newGame(int currentPuzzleIndex) {
        assert currentPuzzleIndex >= 0 && currentPuzzleIndex < puzzles.size() : "Puzzle index out of bounds";

        // Load the selected puzzle and rebuild all game state from it.
        Puzzle puzzle = puzzles.get(currentPuzzleIndex);
        int[][] givens = puzzle.givenGrid();
        this.currentPuzzleIndex = currentPuzzleIndex;
        this.initial = Board.deepCopy(givens);
        this.board = new Board(givens);

        // A new puzzle starts with a clean undo history and no pending completion event.
        history.clear();
        solved = false;
        completionEventPending = false;

        // Build a solved reference grid once.
        solution = Board.deepCopy(initial);
        if (!solveInPlace(solution)) {
            throw new IllegalStateException("Puzzle has no solution");
        }

        changed();
        assert postNewGameCheck(givens) : "Illegal game data loading.";
        assertInvariants();
    }

    /**
     * Marks the model as changed and notifies all observers.
     */
    private void changed() {
        setChanged();
        notifyObservers();
    }

    /**
     * Returns whether a cell address is inside the 9x9 board.
     * @param row row index from 0
     * @param column column index from 0
     * @return true if rows and columns are in the board
     */
    private static boolean inRange(int row, int column) {
        return row >= 0 && row < SIZE && column >= 0 && column < SIZE;
    }

    /**
     * Returns the current value stored in one cell
     * @param row row index from 0
     * @param column column index from 0
     * @return the current value, where 0 means empty
     */
    /*@
       @ requires 0 <= row && row < SIZE && 0 <= column && column < SIZE;
       @ ensures 0 <= \result && \result <= 9;
     */
    @Override
    public int getCellValue(int row, int column) {
        assert inRange(row, column) : "Row or column of the game board is out of bounds";
        int result = board.getCellValue(row, column);
        assert result >= 0 && result <= 9 :"Cell value must be between 0 and 9.";
        return result;
    }

    /**
     * Returns whether a cell is fixed by the original puzzle.
     * @param row row index from 0
     * @param column column index from 0
     * @return true if the cell is a given cell
     */
    /*@
      @ requires 0 <= row && row < SIZE && 0 <= column && column < SIZE;
      @ ensures \result <==> initial[row][column] != 0;
      @*/
    @Override
    public boolean isFixed(int row, int column) {
        assert inRange(row, column) : "Row or column of the game board is out of bounds";
        boolean result = board.isFixed(row, column);
        assert result == (initial[row][column] != 0) :"Fixed-state must match the initial givens.";
        return result;
    }

    /**
     * Returns whether a cell is editable by the player.
     * @param row row index from 0
     * @param column column index from 0
     * @return true if the cell is not fixed
     */
    /*@
      @ requires 0 <= row && row < SIZE && 0 <= column && column < SIZE;
      @ ensures \result <==> !isFixed(row, column);
      @*/
    @Override
    public boolean canBeEdit(int row, int column) {
        assert inRange(row, column) : "Row or column of the game board is out of bounds";
        boolean result = board.canBeEdit(row, column);
        assert result == !isFixed(row, column) : "Fixed cell cannot be edited.";
        return result;
    }

    /**
     * Writes a value into one editable cell.
     * @param row row index from 0
     * @param column column index from 0
     * @param value value to write
     * @return true if the request is accepted, otherwise false
     */
    /*@
      @ requires 0 <= row && row < SIZE && 0 <= column && column < SIZE;
      @ assignable board, history, solved, completionEventPending;
      @ ensures !\result ==> getCellValue(row, column) == \old(getCellValue(row, column));
      @ ensures \result && 0 <= value && value <= 9 && !isFixed(row, column) ==> getCellValue(row, column) == value;
      @ ensures \result && \old(getCellValue(row, column)) != value && 0 <= value && value <= 9 && !isFixed(row, column) ==> canUndo();
      @*/
    @Override
    public boolean setValue(int row, int column, int value) {
        assert inRange(row, column) : "Row or column of the game board is out of bounds";

        int oldCellValue = getCellValue(row, column);

        if (!board.setValue(row, column, value)) {
            assert getCellValue(row, column) == oldCellValue : "Rejected writes must not change the board.";
            return false;
        }
        // The request is accepted, but no real state change, so no undo record or notification is needed.
        if (oldCellValue == value) {
            assert getCellValue(row, column) == value : "Cell value should stay unchanged.";
            return true;
        }

        recordSingleUndo(row, column, oldCellValue, value);
        updateCompletionStateAfterBoardChange();
        changed();
        assert board.getCellValue(row, column) == value : "Change is not successful";
        assertInvariants();
        return true;
    }

    /**
     * Clears one editable cell.
     * @param row row index from 0
     * @param column column index from 0
     * @return true if the request is accepted, otherwise false
     */
    /*@
      @ requires 0 <= row && row < SIZE && 0 <= column && column < SIZE;
      @ assignable board, history, solved, completionEventPending;
      @ ensures !\result ==> getCellValue(row, column) == \old(getCellValue(row, column));
      @ ensures \result ==> getCellValue(row, column) == 0;
      @ ensures \result && \old(getCellValue(row, column)) != 0 ==> canUndo();
      @*/
    @Override
    public boolean clearValue(int row, int column) {
        assert inRange(row, column) : "Row or column of the game board is out of bounds";

        int  oldCellValue = getCellValue(row, column);

        if  (!board.clearValue(row, column)) {
            assert getCellValue(row, column) == oldCellValue : "Rejected clears must not change the board.";
            return false;
        }
        // Nothing changes, so there is no undo record and no observer notification.
        if  (oldCellValue == 0) {
            assert getCellValue(row, column) == 0 : "Empty cell should remain empty.";
            return true;
        }

        recordSingleUndo(row, column, oldCellValue, 0);
        updateCompletionStateAfterBoardChange();
        changed();
        assert board.getCellValue(row, column) == 0 : "Clear is not successful";
        assert canUndo() : "A real accepted clear must create one undo record.";
        assertInvariants();
        return true;
    }

    /**
     * Returns whether a cell is currently empty.
     * @param row row index from 0
     * @param column column index from 0
     * @return true if the cell value is 0
     */
    /*@
      @ requires 0 <= row && row < SIZE && 0 <= column && column < SIZE;
      @ ensures \result <==> getCellValue(row, column) == 0;
      @*/
    @Override
    public boolean isEmpty(int row, int column) {
        assert inRange(row, column) : "Row or column of the game board is out of bounds";
        boolean empty = board.isEmpty(row, column);
        assert empty == (getCellValue(row, column) == 0) : "Empty cell should be 0.";
        return empty;
    }

    /**
     * @return whether validation feedback is enabled.
     */
    /*@ ensures \result <==> validationFeedbackEnabled; @*/
    @Override
    public boolean isValidationFeedbackEnabled() {
        boolean result = validationFeedbackEnabled;
        assert result == validationFeedbackEnabled : "Returned validation flag must match the field.";
        return result;
    }

    /**
     * Enables or disables validation feedback.
     * @param validationFeedbackEnabled true to enable immediate invalid-cell feedback
     */
    /*@
      @ assignable validationFeedbackEnabled;
      @ ensures isValidationFeedbackEnabled() == validationFeedbackEnabled;
      @*/
    @Override
    public void setValidationFeedbackEnabled(boolean validationFeedbackEnabled) {
        if (this.validationFeedbackEnabled == validationFeedbackEnabled) {
            return;
        }

        this.validationFeedbackEnabled = validationFeedbackEnabled;
        changed();

        assert this.validationFeedbackEnabled == validationFeedbackEnabled
                : "Validation flag was not updated correctly.";
        assert isValidationFeedbackEnabled() == validationFeedbackEnabled;

        assertInvariants();
    }

    /**
     * @return whether the board currently has no duplicate non-zero values.
     */
    @Override
    public boolean isBoardValid() {
        return board.isBoardValid();
    }

    /**
     * Returns whether one cell is currently invalid because of duplication.
     * @param row row index from 0
     * @param column column index from 0
     * @return true if the cell is duplicated in its row, column, or box
     */
    /*@
      @ requires 0 <= row && row < SIZE && 0 <= column && column < SIZE;
      @ ensures isEmpty(row, column) ==> !\result;
      @*/
    @Override
    public boolean isCellInvalid(int row, int column) {
        assert inRange(row, column) : "Row or column of the game board is out of bounds";
        boolean result = board.isCellInvalid(row, column);
        assert !isEmpty(row, column) || !result : "Empty cells must never be marked invalid.";
        return result;
    }

    /**
     * Checks that the board was loaded exactly from the selected givens.
     */
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

    /**
     * Returns whether the puzzle is solved.
     * @return true if the puzzle is solved
     */
    @Override
    public boolean isSolved() {
        return solved;
    }

    /**
     * Consumes the one-shot completion event used by the UI.
     * @return true once after the puzzle becomes solved
     */
    /*@
      @ assignable completionEventPending;
      @ ensures \result <==> \old(completionEventPending);
      @ ensures !completionEventPending;
      @*/
    @Override
    public boolean consumeCompletionEvent() {
        boolean oldPending = completionEventPending;
        boolean result;
        if (completionEventPending) {
            completionEventPending = false;
            result = true;
        } else {
            result = false;
        }
        assert result == oldPending : "Return value must equal the old pending completion-event state.";
        assert !completionEventPending : "Completion event flag must be cleared after consumption.";
        return result;
    }

    /**
     * Returns whether the board contains no empty cells.
     */
    private boolean isBoardFull() {
        for  (int row = 0; row < SIZE; row++) {
            for (int column = 0; column < SIZE; column++) {
                if (board.getCellValue(row, column) == 0) {return false;}
            }
        }
        return true;
    }

    /**
     * Returns whether the current board is both full and valid.
     */
    private boolean isCompletedAndValid() {
        return isBoardFull() && isBoardValid();
    }

    /**
     * Updates solved-state flags after a board change.
     */
    private void updateCompletionStateAfterBoardChange() {
        boolean wasSolved = solved;
        solved = isCompletedAndValid();
        // Check whether the state is changed
        if (!wasSolved && solved) {
            completionEventPending = true;
        }
    }

    /**
     * Enables or disables random puzzle selection.
     * @param enabled true to load a random puzzle, false to load one by one
     */
    /*@
      @ assignable randomPuzzleSelectionEnabled;
      @ ensures isRandomPuzzleSelectionEnabled() == enabled;
      @*/
    @Override
    public void setRandomPuzzleSelectionEnabled(boolean enabled) {
        if (this.randomPuzzleSelectionEnabled == enabled) return; // Ensure the status is really changed
        this.randomPuzzleSelectionEnabled = enabled;
        changed();
        assertInvariants();
        assert this.randomPuzzleSelectionEnabled == enabled : "Random-selection flag was not updated correctly.";
    }

    /**
     * Enables or disables the hint feature.
     *
     * @param enabled true to allow hints
     */
    @Override
    public void setHintEnabled(boolean enabled) {
        if (this.hintEnabled == enabled) return; // Ensure the status is really changed
        this.hintEnabled = enabled;
        changed();
        assertInvariants();
    }

    /**
     * Reverts the most recent accepted board-changing action.
     * @return true if one move is undone, otherwise false
     */
    /*@
      @ assignable board, history, solved, completionEventPending;
      @ ensures !\old(canUndo()) ==> !\result;
      @ ensures \result ==> !canUndo();
      @*/
    @Override
    public boolean undo() {
        if (history.isEmpty()) {
            assert !canUndo() : "Undo must be unavailable when the history is empty.";
            return false;
        }

        // Get the previous value of the most recent accepted board change.
        Move latestMove = history.pop();
        boolean restored = board.setValue(latestMove.row, latestMove.col, latestMove.oldValue);
        assert restored : "There must have a record of action";
        updateCompletionStateAfterBoardChange();
        changed();
        assert getCellValue(latestMove.row, latestMove.col) == latestMove.oldValue : "Undo should restore the previous value.";
        assert !canUndo() : "Single-level undo must leave no further undo record after success.";
        assertInvariants();
        return true;
    }

    /**
     * Returns whether a single undo action is currently available.
     */
    @Override
    public boolean canUndo() {
        return !history.isEmpty();
    }

    /**
     * Restores the current puzzle to its initial state.
     * @return always true after the board is reset
     */
    /*@
      @ assignable board, history, solved, completionEventPending;
      @ ensures \result;
      @ ensures !isSolved();
      @ ensures !canUndo();
      @ ensures (\forall int r, c; 0 <= r && r < SIZE && 0 <= c && c < SIZE; getCellValue(r, c) == initial[r][c]);
      @*/
    @Override
    public boolean reset() {
        board = new Board(initial);
        history.clear();

        solved = false;
        completionEventPending = false;
        changed();

        assert !isSolved() : "Reset puzzle must not be solved.";
        assert !canUndo() : "Reset must clear undo history.";
        assert boardMatchesInitial() : "Reset must restore every cell to the initial puzzle.";
        assertInvariants();
        return true;
    }

    /**
     * Checks whether the board is the same as initialed
     * @return true if it matches
     */
    private boolean boardMatchesInitial() {
        for (int row = 0; row < SIZE; row++) {
            for (int column = 0; column < SIZE; column++) {
                if (board.getCellValue(row, column) != initial[row][column]) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Reveals the correct value for one eligible empty editable cell.
     * @param row zero-based row index
     * @param column zero-based column index
     * @return true if a hint is applied, otherwise false
     */
    /*@
      @ requires 0 <= row && row < SIZE && 0 <= column && column < SIZE;
      @ assignable board, history, solved, completionEventPending;
      @ ensures !\old(canApplyHint(row, column)) ==> !\result;
      @ ensures \result ==> getCellValue(row, column) == solution[row][column];
      @ ensures \result ==> canUndo();
      @*/
    @Override
    public boolean applyHint(int row, int column) {
        assert inRange(row, column) : "Row or column of the game board is out of bounds";

        if (!canApplyHint(row, column)) {
            return false;
        }

        int oldValue = board.getCellValue(row, column);
        int correctValue = solution[row][column];

        boolean changedCell = board.setValue(row, column, correctValue);
        assert changedCell : "A valid hint should always be applicable to the selected cell.";

        recordSingleUndo(row, column, oldValue, correctValue);
        updateCompletionStateAfterBoardChange();
        changed();
        assert getCellValue(row, column) == correctValue : "Hint should write the solved value.";
        assert canUndo() : "A successful hint must create one undo record.";
        assertInvariants();
        return true;
    }

    /**
     * Returns whether a hint may be applied to one cell.
     * @param row zero-based row index
     * @param column zero-based column index
     * @return true if hints are enabled and the cell is editable and empty
     */
    /*@
      @ ensures \result <==> (hintEnabled
      @                      && 0 <= row && row < SIZE
      @                      && 0 <= column && column < SIZE
      @                      && canBeEdit(row, column)
      @                      && isEmpty(row, column));
      @*/
    @Override
    public boolean canApplyHint(int row, int column) {
        boolean result = hintEnabled
                && solution != null
                && inRange(row, column)
                && board.canBeEdit(row, column)
                && board.isEmpty(row, column);
        assert result == (hintEnabled
                && solution != null
                && inRange(row, column)
                && board.canBeEdit(row, column)
                && board.isEmpty(row, column)) : "Hint applicability must match its defining condition.";

        return result;
    }

    /**
     * Returns whether placing a value at a position is safe in the candidate grid.
     */
    private static boolean isSafe(int[][] grid, int row, int column, int value) {
        // Check each row
        for (int c = 0; c < SIZE; c++) {
            if (grid[row][c] == value) {return false;}
        }
        // Check each column
        for (int r = 0; r < SIZE; r++) {
            if (grid[r][column] == value) {return false;}
        }
        //check each box
        int br = (row/3) * 3;
        int bc = (column/3) * 3;
        for (int dr = 0; dr < 3; dr++) {
            for (int dc = 0; dc < 3; dc++) {
                if (grid[br + dr][bc + dc] == value) {return false;}
            }
        }
        return true;
    }

    /**
     * Finds the next empty cell in a candidate grid.
     * @param grid 9x9 candidate grid
     * @return {row, column} for the next empty cell, or null when full
     */
    private static int[] findEmpty(int[][] grid) {
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                if (grid[r][c] == 0) return new int[]{r, c};
            }
        }
        return null;
    }

    /**
     * Solves a Sudoku grid in place.
     * @param grid 9x9 candidate grid
     * @return true if a solution is found
     */
    private static boolean solveInPlace(int[][] grid) {
        // Get all empty cells.
        int[] pos = findEmpty(grid);
        if (pos == null) return true; // solved

        int r = pos[0], c = pos[1]; // Undo the trial value and continue searching
        for (int v = 1; v <= 9; v++) {
            if (isSafe(grid, r, c, v)) {
                grid[r][c] = v;
                if (solveInPlace(grid)) return true;
                grid[r][c] = 0;
            }
        }
        return false;
    }

    /**
     * Runtime checks for the class invariants.
     */
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
