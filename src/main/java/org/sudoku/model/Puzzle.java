package org.sudoku.model;

import java.util.Arrays;

final class Puzzle {
    private static final int SIZE = 9;
    private final int[][] givens;

    Puzzle(int[][] givens) {
        this.givens = deepCopy(givens);
        assert is9x9(this.givens) : "The given board is 9x9";
    }

    int[][] givenGrid() {
        return deepCopy(givens);
    }

    private static int[][] deepCopy(int[][] src) {
        int[][] out = new int[src.length][];
        for (int i = 0; i < src.length; i++) {
            out[i] = Arrays.copyOf(src[i], src[i].length);
        }
        return out;
    }

    private static boolean is9x9(int[][] givens) {
        if (givens == null || givens.length != SIZE) {return false;}
        for (int row = 0; row < SIZE; row++) {
            if (givens[row] == null || givens[row].length != SIZE) {return false;}
            for (int col = 0; col < SIZE; col++) {
                int value = givens[row][col];
                if (value < 0 || value > 9) {return false;}
            }
        }
        return true;
    }
}
