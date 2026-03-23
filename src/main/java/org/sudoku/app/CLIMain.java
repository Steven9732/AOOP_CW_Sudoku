package org.sudoku.app;

import org.sudoku.model.Model;

public class CLIMain {
    public static void main(String[] args) {
        Model model = new Model();

        System.out.println("This is a Sudoku Game");
        System.out.println("Type 'help' to see commands.");

        printBoard(model);

    }

    // Print game board
    private static void printBoard (Model model) {
        for (int row = 0; row < model.SIZE; row++) {
            if (row % 3 == 0) System.out.println("+------+------+------+");
            for (int col = 0; col < model.SIZE; col++) {
                if (col % 3 == 0) System.out.print("|");
                int value = model.getCellValue(row, col);
                System.out.print(value == 0 ? ". " : (value + " "));
            }
            System.out.println("|");
        }
        System.out.println("+------+------+------+");
    }
}
