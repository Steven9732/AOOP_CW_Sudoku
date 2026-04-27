package org.sudoku.app;

import org.sudoku.model.SudokuModel;
import org.sudoku.model.Model;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class CLIMain {
    public static void main(String[] args) throws Exception {
        SudokuModel model = new Model();

        System.out.println("This is a Sudoku Game");
        System.out.println("Type 'help' to see commands.");

        printBoard(model);

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        while (true) {
            System.out.print("> ");
            String line = br.readLine();
            if (line == null) break;

            line = line.trim();
            if (line.isEmpty()) continue;
            String[] token = line.split("\\s+");
            String command = token[0].toLowerCase();

            try {
                boolean stateChanged = false;
                switch (command) {
                    case "help" -> printCommands(); // To show all commands
                    case "show" ->  printBoard(model); //
                    case "set" -> {
                        requiredInputLength(token, 4);
                        int row = parseInt(token[1], "row") -1;
                        int column = parseInt(token[2], "column") -1;
                        int value = parseInt(token[3], "value");
                        boolean set = model.setValue(row, column, value);
                        if (!set) {
                            System.out.println("Move rejected. Only editable cells can be changed, and values must be 1..9.");
                        }
                        stateChanged = set;
                    }
                    case "clear", "erase" -> {
                        requiredInputLength(token, 3);
                        int  row = parseInt(token[1], "row") -1;
                        int column = parseInt(token[2], "column") -1;
                        boolean cleared = model.clearValue(row, column);
                        if (!cleared) {
                            System.out.println("Clear rejected. Pre-filled cells cannot be cleared.");
                        }
                        stateChanged = cleared;
                    }
                    case "undo" -> {
                        boolean undo = model.undo();
                        if (!undo) {
                            System.out.println("Undo rejected. There is no move to undo.");
                        }
                        stateChanged = undo;

                    }
                    case "hint" -> {
                        requiredInputLength(token, 3);
                        int row = parseInt(token[1], "row") - 1;
                        int column = parseInt(token[2], "column") - 1;
                        boolean applyHint = model.applyHint(row, column);
                        if (!applyHint) {
                            System.out.println("Hint rejected. Select an empty editable cell and make sure hints are enabled.");
                        }
                        stateChanged = applyHint;
                    }
                    case "reset" -> {
                        boolean reset = model.reset();
                        stateChanged = reset;
                    }
                    case "new", "newgame" -> {
                        model.newGame();
                        stateChanged = true;
                    }
                    case "quit" -> {
                        return;
                    }
                    default -> {System.out.println("Type 'help' to see commands.");}
                }
                if (stateChanged) {
                    printBoard(model);

                    if (model.consumeCompletionEvent()) {
                        System.out.println("Congratulations! You have successfully solved the game.");
                    }

                    if (model.isValidationFeedbackEnabled() && !model.isBoardValid()) {
                        System.out.println("Warning: duplicates exist.");
                        printInvalidCells(model);
                    }
                }
            } catch (IllegalArgumentException ex) {
                System.out.println("Input Error: " + ex.getMessage());
            }
        }

    }

    // Print game board
    private static void printBoard (SudokuModel model) {
        for (int row = 0; row < SudokuModel.SIZE; row++) {
            if (row % 3 == 0) System.out.println("+------+------+------+");
            for (int col = 0; col < SudokuModel.SIZE; col++) {
                if (col % 3 == 0) System.out.print("|");
                int value = model.getCellValue(row, col);
                System.out.print(value == 0 ? ". " : (value + " "));
            }
            System.out.println("|");
        }
        System.out.println("+------+------+------+");
    }

    // Show the commands
    private static void printCommands () {
        System.out.println("""
            Commands:
              help          (show all commands)
              show
              set row column value        (row, column, value are 1..9)
              clear row column            (or: erase row column)
              undo
              hint row column
              reset
              new                         (or: newgame)
              quit
            """);
    }

    // Parse string to integer
    private static int parseInt(String input, String name) {
        int x;
        try {
            x = Integer.parseInt(input);
        }  catch (NumberFormatException e) {
            throw new IllegalArgumentException(name + " is not a number");
        }
        if (x < 1 || x > 9) throw new IllegalArgumentException(name + " must be in 1 to 9.");
        return x;
    }

    private static void printInvalidCells(SudokuModel model) {
        StringBuilder sb = new StringBuilder("Invalid cells: ");
        boolean any = false;
        for (int r = 0; r < SudokuModel.SIZE; r++) {
            for (int c = 0; c < SudokuModel.SIZE; c++) {
                if (model.getCellValue(r, c) != 0 && model.isCellInvalid(r, c) && !model.isFixed(r, c)) {
                    any = true;
                    sb.append("(").append(r + 1).append(",").append(c + 1).append(") ");
                }
            }
        }
        System.out.println(any ? sb.toString() : "Invalid cells: none");
    }

    private static void requiredInputLength(String[] token, int length) {
        throw new IllegalArgumentException("Expected input length " + length + " tokens but got " + token.length + ".");
    }
}
