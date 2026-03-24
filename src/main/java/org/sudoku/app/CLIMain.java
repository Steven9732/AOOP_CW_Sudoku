package org.sudoku.app;

import org.sudoku.model.Model;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class CLIMain {
    public static void main(String[] args) throws Exception {
        Model model = new Model();

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
                        int row = parseInt(token[1], "row") -1;
                        int column = parseInt(token[2], "column") -1;
                        int value = parseInt(token[3], "value");
                        boolean ok = model.setValue(row, column, value);
                        stateChanged = ok;
                    }
                }
            } finally {

            }
        }

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

    // Show the commands
    private static void printCommands () {
            System.out.println("""
                Commands:
                  show
                  set row column value        (row, column, value are 1..9)
                  clear row column        (or: erase row column)
                  undo
                  hint
                  reset
                  new              (or: newgame)
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
}
