package org.sudoku.app;

import org.sudoku.model.SudokuModel;
import org.sudoku.model.Model;
import org.sudoku.controller.SudokuController;
import org.sudoku.view.SudokuFrame;

import javax.swing.SwingUtilities;

public class GUIMain {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SudokuModel model = new Model();
            SudokuFrame frame = new SudokuFrame();
            SudokuController controller = new SudokuController(model, frame);

            frame.bind(model, controller);

            frame.setVisible(true);
            frame.requestFocusInWindow();
        });
    }
}