package org.sudoku.view;

import org.sudoku.controller.SudokuController;
import org.sudoku.model.SudokuModel;

import javax.swing.*;
import java.awt.*;
import java.util.Observable;
import java.util.Observer;

@SuppressWarnings("deprecation")
public final class SudokuFrame extends JFrame implements Observer {
    private SudokuModel model;
    private SudokuController controller;

    private final BoardPanel boardPanel = new BoardPanel();

    // 5 buttons
    private final JButton NewGameButton = new JButton("New Game");
    private final JButton ResetButton = new JButton("Reset");
    private final JButton UndoButton = new JButton("Undo");
    private final JButton EraseButton = new JButton("Erase");
    private final JButton HintButton = new JButton("Hint");

    // GUI runtime toggles
    private final JCheckBox ValidationCB = new JCheckBox("Validation feedback", true);
    private final JCheckBox HintCB = new JCheckBox("Hint enabled", true);
    private final JCheckBox RandomCB = new JCheckBox("Random puzzles", true);

    // Digital Button
    private final JButton[] digitButtons = new JButton[9];

    // Status line
    private final JLabel status = new JLabel(" ");

    public SudokuFrame() {
        super("Sudoku");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1000, 800));
        setLocationRelativeTo(null);

        // Left
        JPanel left = new JPanel(new BorderLayout());
        left.add(boardPanel, BorderLayout.CENTER);
        left.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        // Right
        JPanel right = new JPanel();
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        right.setBorder(BorderFactory.createEmptyBorder(12, 0, 12, 12));

        // Add buttons to right
        right.add(sectionTitle("Actions"));
        right.add(row(NewGameButton, ResetButton));
        right.add(Box.createVerticalStrut(12));
        right.add(row(UndoButton, EraseButton));
        right.add(Box.createVerticalStrut(12));
        right.add(row(HintButton));

        right.add(Box.createVerticalStrut(16));
        right.add(sectionTitle("Settings"));
        right.add(ValidationCB);
        right.add(HintCB);
        right.add(RandomCB);

        right.add(Box.createVerticalStrut(16));
        right.add(sectionTitle("Keypad"));
        right.add(makeKeypad());

        right.add(Box.createVerticalStrut(16));
        right.add(sectionTitle("Status"));
        right.add(status);

        // Root layout
        setLayout(new BorderLayout());
        add(left, BorderLayout.CENTER);
        add(right, BorderLayout.EAST);

        NewGameButton.setFocusable(false);
        ResetButton.setFocusable(false);
        UndoButton.setFocusable(false);
        EraseButton.setFocusable(false);
        HintButton.setFocusable(false);

        ValidationCB.setFocusable(false);
        HintCB.setFocusable(false);
        RandomCB.setFocusable(false);

        setFocusable(true);
    }

    public void bind(SudokuModel model, SudokuController controller) {
        this.model = model;
        this.controller = controller;
        model.addObserver(this);
    }

    @Override
    public void update(Observable o, Object arg) {
        if (controller == null) {
            return;
        }
        SwingUtilities.invokeLater(() -> {
            boolean solvedNow = controller.handleModelChanged();
            if (solvedNow) {
                JOptionPane.showMessageDialog(this, "Solved", "Sudoku", JOptionPane.INFORMATION_MESSAGE);
            }
        });
    }

    private static JLabel sectionTitle(String title) {
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 13f));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 6, 0));
        return titleLabel;
    }

    private static JPanel row(JComponent... components) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        for (JComponent component : components) {
            p.add(component);
        }
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        return p;
    }

    private JPanel makeKeypad() {
        JPanel p = new JPanel(new GridLayout(3, 3, 6, 6));
        p.setAlignmentX(Component.LEFT_ALIGNMENT + Component.RIGHT_ALIGNMENT);
        for (int i = 0; i < 9; i++) {
            JButton b = new JButton(String.valueOf(i + 1));
            b.setFocusable(false);
            digitButtons[i] = b;
            p.add(b);
        }
        return p;
    }

    public BoardPanel getBoardPanel() { return boardPanel; }

    public JButton getNewGameButton() { return NewGameButton; }

    public JButton getResetButton() { return ResetButton; }

    public JButton getUndoButton() { return UndoButton; }

    public JButton getEraseButton() { return EraseButton; }

    public JButton getHintButton() { return HintButton; }

    public JCheckBox getValidationCheckBox() { return ValidationCB; }

    public JCheckBox getHintCheckBox() { return HintCB; }

    public JCheckBox getRandomCheckBox() { return RandomCB; }

    public JButton getDigitButton(int digital) { return digitButtons[digital - 1]; }

    public void setStatusText(String text) { status.setText(text); }
}