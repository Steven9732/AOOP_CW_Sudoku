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
        this.fixed = fixedFromInitial(initial);
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
}
