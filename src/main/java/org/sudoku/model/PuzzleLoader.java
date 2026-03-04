package org.sudoku.model;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

final class PuzzleLoader {

    private static final int SIZE = 9;
    private static final int CELLS = 81;

    private PuzzleLoader() {}

    static List<Puzzle> loadPuzzlesFromFile(String filename) {
        List<String> lines = readAllLinesClasspathFitst(filename);
        List<Puzzle> puzzles = new ArrayList<>();

        for (String rawLine : lines) {
            String line = rawLine.trim();
            String first81Digits = line.substring(0, CELLS);
            int [][] givens = new int[SIZE][SIZE];
            for (int i = 0; i < CELLS; i++) {
                givens[i / SIZE][i % SIZE] = first81Digits.charAt(i) - '0';
            }
            puzzles.add(new Puzzle(givens));
        }
        return puzzles;
    }

    private static List<String> readAllLinesClasspathFitst(String filename) {
        InputStream inputStream = PuzzleLoader.class.getClassLoader().getResourceAsStream(filename);
        if (inputStream == null) {
            throw new IllegalArgumentException("File " + filename + " not found");
        }
        try{
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            List<String> outStream = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                outStream.add(line);
            } return outStream;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
