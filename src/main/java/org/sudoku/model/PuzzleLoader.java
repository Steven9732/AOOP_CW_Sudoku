package org.sudoku.model;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

final class PuzzleLoader {

    private static final int SIZE = 9;
    private static final int CELLS = 81;

    private PuzzleLoader() {}

    /**
     * Loads all puzzles from one classpath text file.
     * @param filename filename classpath file name
     * @return list of parsed puzzles
     */
    static List<Puzzle> loadPuzzlesFromFile(String filename) {
        assert filename != null && !filename.isBlank() : "Filename must not be null or blank.";
        List<String> lines = readAllLinesClasspathFirst(filename);
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

    /**
     * Reads all lines from one text resource in the classpath.
     */
    private static List<String> readAllLinesClasspathFirst(String filename) {
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
