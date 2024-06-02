package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class GameClientGUI extends JFrame {
    SecondCustomDrawingPanel secondDrawingPanel;
    private GameClient client;
    private static final int CELL_SIZE = 30;
    CustomDrawingPanel drawingPanel;
    private final int rows = 10;
    private final int cols = 10;
    private final char[][] matrix = new char[rows][cols];
    private JTextField selectedTextField;
    public JPanel southPanel;
    private JTextField startTextField;
    private JTextField endTextField;
    private JTextField attackTextField;

    public JTextField getAttackTextField() {
        return attackTextField;
    }

    public GameClientGUI(GameClient client) {
        this.client = client;
        initMatrix();
        initCopy();
        initUI();
    }

    public JTextField getStartTextField() {
        return startTextField;
    }

    public JTextField getEndTextField() {
        return endTextField;
    }

    private void initMatrix() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                matrix[i][j] = 'X';
            }
        }
    }

    private void initCopy() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                matrixCopy[i][j] = 'Z';
            }
        }
    }

//    private void initUI() {
//        setTitle("Game Client");
//        setSize(300, 250);
//        setDefaultCloseOperation(EXIT_ON_CLOSE);
//        setLocationRelativeTo(null);
//
//        JPanel panel = new JPanel();
//        panel.setLayout(new GridLayout(2, 1));
//
//        JButton createGameButton = new JButton("Create Game");
//        createGameButton.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                client.sendMessage("create game");
//            }
//        });
//
//        JButton joinGameButton = new JButton("Join Game");
//        joinGameButton.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                String gameId = JOptionPane.showInputDialog(GameClientGUI.this, "Enter game code:");
//                if (gameId != null && !gameId.trim().isEmpty()) {
//                    client.sendMessage("join game " + gameId.trim());
//                }
//            }
//        });
//
//        panel.add(createGameButton);
//        panel.add(joinGameButton);
//
//        add(panel);
//    }

    private void initUI() {
        setTitle("Game Client");
        setSize(300, 250); // Increase the height to fit the new buttons
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(4, 1)); // Change to 4 rows

        JButton createGameButton = new JButton("Create Game");
        createGameButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                client.sendMessage("create game");
            }
        });

        JButton joinGameButton = new JButton("Join Game");
        joinGameButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String gameId = JOptionPane.showInputDialog(GameClientGUI.this, "Enter game code:");
                if (gameId != null && !gameId.trim().isEmpty()) {
                    client.sendMessage("join game " + gameId.trim());
                }
            }
        });

        JButton registerButton = new JButton("Register");
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JTextField usernameField = new JTextField();
                JTextField passwordField = new JPasswordField();
                Object[] message = {
                        "Username:", usernameField,
                        "Password:", passwordField
                };

                int option = JOptionPane.showConfirmDialog(null, message, "Register", JOptionPane.OK_CANCEL_OPTION);
                if (option == JOptionPane.OK_OPTION) {
                    String username = usernameField.getText();
                    String password = passwordField.getText();
                    if (!username.isEmpty() && !password.isEmpty()) {
                        client.sendMessage("register " + username + " " + password);
                    } else {
                        JOptionPane.showMessageDialog(GameClientGUI.this, "Username and password cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        JButton loginButton = new JButton("Login");
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JTextField usernameField = new JTextField();
                JTextField passwordField = new JPasswordField();
                Object[] message = {
                        "Username:", usernameField,
                        "Password:", passwordField
                };

                int option = JOptionPane.showConfirmDialog(null, message, "Login", JOptionPane.OK_CANCEL_OPTION);
                if (option == JOptionPane.OK_OPTION) {
                    String username = usernameField.getText();
                    String password = passwordField.getText();
                    if (!username.isEmpty() && !password.isEmpty()) {
                        client.sendMessage("login " + username + " " + password);
                    } else {
                        JOptionPane.showMessageDialog(GameClientGUI.this, "Username and password cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        panel.add(createGameButton);
        panel.add(joinGameButton);
        panel.add(registerButton);
        panel.add(loginButton);

        add(panel);
    }

    public void updateOpponentAttack(String[] boardState) {
        for (int i = 1; i < boardState.length; i++) {
            String[] row = boardState[i].trim().split(" ");
            for (int j = 1; j <= 10; j++) {
                matrixCopy[i - 1][j - 1] = row[j].charAt(0);
            }
        }
        secondDrawingPanel.repaint();
    }

    public void showGameCreatedPopup(String gameCode) {
        dispose();

        JFrame canvasFrame = new JFrame("Game");
        canvasFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        canvasFrame.setSize(850, 500);
        canvasFrame.setLocationRelativeTo(null);

        drawingPanel = new CustomDrawingPanel();
        secondDrawingPanel = new SecondCustomDrawingPanel();

        JPanel gridsPanel = new JPanel(new GridLayout(1, 2));
        gridsPanel.add(drawingPanel);
        gridsPanel.add(secondDrawingPanel);

        canvasFrame.add(gridsPanel, BorderLayout.CENTER);

        southPanel = new JPanel();
        canvasFrame.add(southPanel, BorderLayout.SOUTH);

        // Create and add the right panel
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BorderLayout());
        timerLabel = new JLabel("Time left: 15");
        timerLabel.setHorizontalAlignment(JLabel.CENTER);
        rightPanel.add(timerLabel, BorderLayout.CENTER);
        canvasFrame.add(rightPanel, BorderLayout.EAST);

        canvasFrame.setVisible(true);

        drawingPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int x = e.getX();
                int y = e.getY();

                int row = (y / CELL_SIZE) - 1;
                int col = (x / CELL_SIZE) - 1;

                if (row >= 0 && row < rows && col >= 0 && col < cols) {
                    String cellCoordinate = (row + 1) + String.valueOf((char) ('A' + col));
                    if (selectedTextField != null) {
                        selectedTextField.setText(cellCoordinate);
                    }
                }
            }
        });
    }

    private JLabel timerLabel;

    public JLabel getTimerLabel() {
        return timerLabel;
    }

    private Timer timer;
    private int timeLeft = 15;

    public void startTimer() {
        timeLeft = 15;
        timerLabel.setText("Time left: " + timeLeft);
        if (timer != null) {
            timer.stop();
        }
        timer = new Timer(1000, e -> {
            if (timeLeft > 0) {
                timeLeft--;
                timerLabel.setText("Time left: " + timeLeft);
            } else {
                ((Timer) e.getSource()).stop();
                SwingUtilities.invokeLater(() -> {
                    JFrame frame = new JFrame();
                    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    int result = JOptionPane.showOptionDialog(
                            frame,
                            "Time's up!",
                            "Time Up",
                            JOptionPane.DEFAULT_OPTION,
                            JOptionPane.INFORMATION_MESSAGE,
                            null,
                            new Object[]{"OK"},
                            "OK"
                    );
                    if (result == JOptionPane.OK_OPTION) {
                        client.sendMessage("timeup");
                        System.exit(0);
                    } else {
                        client.sendMessage("timeup");
                        System.exit(0);
                    }
                });
            }
        });
        timer.start();
    }

    private void pauseTimer() {
        if (timer != null) {
            timer.stop();
        }
    }

    public void resetBoard() {
        for(int i = 0; i < rows; i++){
            for(int j = 0; j < cols; j++){
                matrixCopy[i][j] = 'O';
            }
        }

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if(matrix[i][j] == 'S'){
                    matrixCopy[i][j] = 'S';
                }
                matrix[i][j] = 'X';
            }
        }
        drawingPanel.repaint();
        secondDrawingPanel.repaint();

        southPanel.removeAll();
        southPanel.revalidate();
        southPanel.repaint();
    }

    public void createSouthPanel(JPanel southPanel, boolean setShipMode) {
        // Clear existing components
        southPanel.removeAll();
        southPanel.revalidate();
        southPanel.repaint();

        if (setShipMode) {
            southPanel.setLayout(new GridLayout(3, 1));

            JLabel startLabel = new JLabel("Start Coordinate:");
            startTextField = new JTextField(10);
            JLabel endLabel = new JLabel("End Coordinate:");
            endTextField = new JTextField(10);
            JButton setShipButton = new JButton("Set Ship");

            startTextField.setPreferredSize(new Dimension(100, 25));
            endTextField.setPreferredSize(new Dimension(100, 25));
            setShipButton.setPreferredSize(new Dimension(120, 30));

            JPanel startPanel = new JPanel();
            startPanel.add(startLabel);
            startPanel.add(startTextField);

            JPanel endPanel = new JPanel();
            endPanel.add(endLabel);
            endPanel.add(endTextField);

            JPanel buttonPanel = new JPanel();
            buttonPanel.add(setShipButton);

            southPanel.add(startPanel);
            southPanel.add(endPanel);
            southPanel.add(buttonPanel);

            setShipButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String start = startTextField.getText();
                    String end = endTextField.getText();
                    if (start.isEmpty() || end.isEmpty()) {
                        JOptionPane.showMessageDialog(GameClientGUI.this, "Start and end coordinates cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
                    } else {
                        client.sendSetShipMessage(start, end);
                    }
                }
            });

            startTextField.addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    selectedTextField = startTextField;
                }
            });

            endTextField.addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    selectedTextField = endTextField;
                }
            });
        } else {
            southPanel.setLayout(new GridLayout(3, 1));

            JLabel attackLabel = new JLabel("Cell to Attack:");
            attackTextField = new JTextField(8);
            attackTextField.setPreferredSize(new Dimension(100, 25));
            JButton submitMoveButton = new JButton("Submit Move");
            submitMoveButton.setPreferredSize(new Dimension(120, 30));

            JPanel attackPanel = new JPanel();
            attackPanel.add(attackLabel);
            attackPanel.add(attackTextField);

            JPanel buttonPanel = new JPanel();
            buttonPanel.add(submitMoveButton);

            southPanel.add(attackPanel);
            southPanel.add(buttonPanel);

            submitMoveButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String attackCoordinate = attackTextField.getText();
                    if (attackCoordinate.isEmpty()) {
                        JOptionPane.showMessageDialog(GameClientGUI.this, "Attack coordinate cannot be empty.", "Invalid move", JOptionPane.ERROR_MESSAGE);
                    } else {
                        client.sendSubmitMoveMessage(attackCoordinate);
                        pauseTimer();
                    }
                }
            });

            attackTextField.addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    selectedTextField = attackTextField;
                }
            });
        }
    }

    public void updateMatrixWithShip(String start, String end) {
        int startRow = Integer.parseInt(start.substring(0, start.length() - 1)) - 1;
        int startCol = start.charAt(start.length() - 1) - 'A';
        int endRow = Integer.parseInt(end.substring(0, end.length() - 1)) - 1;
        int endCol = end.charAt(end.length() - 1) - 'A';

        if (startRow == endRow) {
            // horizontal ship
            if (startCol > endCol) {
                int temp = startCol;
                startCol = endCol;
                endCol = temp;
            }
        } else if (startCol == endCol) {
            // vert ship
            if (startRow > endRow) {
                int temp = startRow;
                startRow = endRow;
                endRow = temp;
            }
        }

        for (int i = startRow; i <= endRow; i++) {
            for (int j = startCol; j <= endCol; j++) {
                matrix[i][j] = 'S';
            }
        }

        drawingPanel.repaint();
    }

    public void updateMatrixWithAttack(String attackCoordinate, boolean hit) {
        int row = Integer.parseInt(attackCoordinate.substring(0, attackCoordinate.length() - 1)) - 1;
        int col = attackCoordinate.charAt(attackCoordinate.length() - 1) - 'A';
        matrix[row][col] = hit ? 'H' : 'M';

        drawingPanel.repaint();
    }

    private class CustomDrawingPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            // col labels (a-j)
            for (int j = 0; j < cols; j++) {
                g.drawString(String.valueOf((char) ('A' + j)), (j + 1) * CELL_SIZE + CELL_SIZE / 2 - 5, CELL_SIZE / 2);
            }

            // row label
            for (int i = 0; i < rows; i++) {
                g.drawString(String.valueOf(i + 1), CELL_SIZE / 2, (i + 1) * CELL_SIZE + CELL_SIZE / 2 + 5);
            }

            // draw cells
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    if (matrix[i][j] == 'X') {
                        g.setColor(new Color(173, 216, 230));
                        g.fillRect((j + 1) * CELL_SIZE, (i + 1) * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                        g.setColor(Color.BLACK);
                    }
                    if (matrix[i][j] == 'S') {
                        g.setColor(Color.GRAY);
                        g.fillRect((j + 1) * CELL_SIZE, (i + 1) * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                        g.setColor(Color.BLACK);
                    }
                    if (matrix[i][j] == 'H') {
                        g.setColor(Color.RED);
                        g.fillRect((j + 1) * CELL_SIZE, (i + 1) * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                        g.setColor(Color.BLACK);
                    }
                    if (matrix[i][j] == 'M') {
                        g.setColor(Color.YELLOW);
                        g.fillRect((j + 1) * CELL_SIZE, (i + 1) * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                        g.setColor(Color.BLACK);
                    }
                    g.drawRect((j + 1) * CELL_SIZE, (i + 1) * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                }
            }
        }
    }

    public final char[][] matrixCopy = new char[rows][cols];
    private class SecondCustomDrawingPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            // col labels (a-j)
            for (int j = 0; j < cols; j++) {
                g.drawString(String.valueOf((char) ('A' + j)), (j + 1) * CELL_SIZE + CELL_SIZE / 2 - 5, CELL_SIZE / 2);
            }

            // row label
            for (int i = 0; i < rows; i++) {
                g.drawString(String.valueOf(i + 1), CELL_SIZE / 2, (i + 1) * CELL_SIZE + CELL_SIZE / 2 + 5);
            }

            // draw cells
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    if (matrixCopy[i][j] == 'Z') {
                        g.setColor(Color.BLACK);
                        g.fillRect((j + 1) * CELL_SIZE, (i + 1) * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                        g.setColor(Color.BLACK);
                    }
                    if (matrixCopy[i][j] == 'O') {
                        g.setColor(new Color(173, 216, 230));
                        g.fillRect((j + 1) * CELL_SIZE, (i + 1) * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                        g.setColor(Color.BLACK);
                    }
                    if (matrixCopy[i][j] == 'S') {
                        g.setColor(Color.GREEN);
                        g.fillRect((j + 1) * CELL_SIZE, (i + 1) * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                        g.setColor(Color.BLACK);
                    }
                    if (matrixCopy[i][j] == 'X') {
                        g.setColor(Color.RED);
                        g.fillRect((j + 1) * CELL_SIZE, (i + 1) * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                        g.setColor(Color.BLACK);
                    }
                    g.drawRect((j + 1) * CELL_SIZE, (i + 1) * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                }
            }
        }
    }

}

