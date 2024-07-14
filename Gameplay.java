import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

public class Gameplay extends JPanel implements KeyListener, ActionListener {
    private boolean play = false; // Game state
    private boolean pause = false; // Pause state
    private boolean startScreen = true; // Start screen state
    private boolean countdownScreen = false; // Countdown screen state
    private int score = 0; // Player score
    private int highScore = 0; // High score
    private int totalBricks = 52; // Total number of bricks
    private int level = 1; // Current level
    private int lives = 3; // Player lives
    private Timer gameTimer; // Main game timer
    private Timer countdownTimer; // Countdown timer
    private int countdown = 3; // Countdown value
    private int delay = 4; // Timer delay

    private int playerX = 300; // Paddle position
    private int ballposX = playerX + 40; // Ball position X
    private int ballposY = 520; // Ball position Y
    private int ballXdir = -2; // Ball direction X
    private int ballYdir = -4; // Ball direction Y

    private MapGenerator map; // Brick map generator
    private Font cloisterBlackFont; // Custom font
    private Queue<Point> ballPath; // Ball trail

    public Gameplay() {
        map = new MapGenerator(4, 13); // Initialize the brick map
        addKeyListener(this);
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);
        gameTimer = new Timer(delay, this); // Initialize the game timer
        gameTimer.start();
        loadCloisterBlackFont(); // Load the custom font
        ballPath = new LinkedList<>();

        // Countdown Timer
        countdownTimer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                countdown--; // Decrement countdown
                if (countdown <= 0) {
                    countdownTimer.stop(); // Stop countdown timer when it reaches 0
                    startGame(); // Start the game
                }
                repaint(); // Repaint the screen
            }
        });
    }

    // Load the custom font
    private void loadCloisterBlackFont() {
        try {
            cloisterBlackFont = Font.createFont(Font.TRUETYPE_FONT, new File("CloisterBlack.ttf")).deriveFont(24f);
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(cloisterBlackFont);
        } catch (IOException | FontFormatException e) {
            cloisterBlackFont = new Font("Serif", Font.BOLD, 24); // Fallback font
        }
    }

    // Paint the game components
    public void paint(Graphics g) {
        g.setColor(Color.BLACK);
        g.fillRect(1, 1, 700, 600);

        // Display the start screen
        if (startScreen) {
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 36));
            g.drawString("Break The Wall..", 210, 210);
            g.setFont(new Font("Arial", Font.BOLD, 36));
            g.drawString("Press Space to Start", 180, 250);
            return; // Return early to prevent drawing other components
        }

        // Display the countdown screen
        if (countdownScreen) {
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 36));
            g.drawString(countdown > 0 ? String.valueOf(countdown) : "Start", 330, 300);
            return; // Return early to prevent drawing other components
        }

        // Draw the brick map
        map.draw((Graphics2D) g);
        g.setColor(Color.YELLOW);
        g.fillRect(0, 0, 3, 600);
        g.fillRect(0, 0, 700, 3);
        g.fillRect(697, 0, 3, 600);

        // Draw the paddle
        draw3DRect(g, playerX, 550, 100, 8, new Color(192, 192, 192));

        // Draw the ball path
        g.setColor(Color.RED);
        for (Point p : ballPath) {
            g.fillOval(p.x, p.y, 10, 0);
        }

        // Draw the ball
        draw3DOval(g, ballposX, ballposY, 20, 20, Color.RED);

        // Display score, lives, level, and high score
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 16));
        g.drawString("Lives: " + lives + " | Level: " + level + " | Score: " + score + " | High Score: " + highScore, 180, 30);

        // Display game over message
        if (lives <= 0) {
            play = false;
            g.setColor(Color.RED);
            g.setFont(new Font("Arial", Font.BOLD, 36));
            g.drawString("Game Over", 230, 300);
            g.setFont(new Font("Arial", Font.BOLD, 24));
            g.drawString("Press Space to Restart", 200, 350);
        }

        // Display pause message
        if (pause) {
            g.setColor(Color.YELLOW);
            g.setFont(new Font("Arial", Font.BOLD, 36));
            g.drawString("Paused", 270, 300);
            g.setFont(new Font("Arial", Font.BOLD, 24));
            g.drawString("Press Space to Resume", 210, 350);
        }

        Toolkit.getDefaultToolkit().sync(); // Synchronize graphics
    }

    // Start the game after countdown
    private void startGame() {
        play = true;
        countdownScreen = false;
        ballposX = playerX + 40;
        ballposY = 520;
        ballXdir = -3;
        ballYdir = -6;
        repaint();
    }

    // Draw 3D rectangle for paddle
    private void draw3DRect(Graphics g, int x, int y, int width, int height, Color color) {
        g.setColor(color);
        g.fillRect(x, y, width, height);
        g.setColor(color.darker());
        g.fillRect(x + 2, y + 2, width - 2, height - 2);
    }

    // Draw 3D oval for ball
    private void draw3DOval(Graphics g, int x, int y, int width, int height, Color color) {
        g.setColor(color);
        g.fillOval(x, y, width, height);
        g.setColor(color.darker());
        g.fillOval(x + 2, y + 2, width - 2, height - 2);
    }

    // Main game logic
    @Override
    public void actionPerformed(ActionEvent e) {
        if (play && !pause) {
            ballposX += ballXdir;
            ballposY += ballYdir;

            ballPath.add(new Point(ballposX, ballposY));
            if (ballPath.size() > 10) {
                ballPath.poll();
            }

            if (ballposX < 0 || ballposX > 680) {
                ballXdir = -ballXdir;
            }
            if (ballposY < 0) {
                ballYdir = -ballYdir;
            }

            if (ballposY + 20 >= 550 && ballposX + 20 >= playerX && ballposX <= playerX + 100) {
                ballYdir = -ballYdir;
            }

            // Collision detection with bricks
            A:
            for (int i = 0; i < map.map.length; i++) {
                for (int j = 0; j < map.map[0].length; j++) {
                    if (map.map[i][j] > 0) {
                        int brickX = j * map.brickWidth + 80;
                        int brickY = i * map.brickHeight + 50;
                        int brickWidth = map.brickWidth;
                        int brickHeight = map.brickHeight;

                        Rectangle brickRect = new Rectangle(brickX, brickY, brickWidth, brickHeight);
                        Rectangle ballRect = new Rectangle(ballposX, ballposY, 20, 20);

                        if (ballRect.intersects(brickRect)) {
                            map.setBrickValue(0, i, j);
                            score += 5;

                            if (score > highScore) {
                                highScore = score;
                            }

                            if (ballposX + 19 <= brickRect.x || ballposX + 1 >= brickRect.x + brickRect.width) {
                                ballXdir = -ballXdir;
                            } else {
                                ballYdir = -ballYdir;
                            }
                            break A;
                        }
                    }
                }
            }

            if (ballposY > 570) {
                lives--;
                if (lives > 0) {
                    resetBall();
                } else {
                    play = false;
                }
            }

            if (map.getTotalBricks() == 0) {
                level++;
                totalBricks += 5;
                map = new MapGenerator(4, 13);
                resetBall();
            }
        }

        repaint();
    }

    public void resetBall() {
        ballposX = playerX + 40;
        ballposY = 520;
        ballXdir = -3;
        ballYdir = -6;
        play = false;
        ballPath.clear();
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            if (playerX >= 600) {
                playerX = 600;
            } else {
                moveRight();
            }
        }
        if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            if (playerX <= 10) {
                playerX = 10;
            } else {
                moveLeft();
            }
        }
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            if (startScreen) {
                startScreen = false;
                countdownScreen = true;
                countdown = 3;
                countdownTimer.start();
            } else if (countdownScreen) {
                countdownScreen = false;
                countdownTimer.stop();
                startGame();
            } else if (lives <= 0) {
                resetGame();
            } else {
                pause = !pause;
            }
        }
    }

    private void moveRight() {
        play = true;
        playerX += 80;
    }

    private void moveLeft() {
        play = true;
        playerX -= 80;
    }

    private void resetGame() {
        score = 0;
        lives = 3;
        level = 1;
        map = new MapGenerator(4, 13);
        startScreen = true;
        countdown = 3;
        countdownScreen = false;
        countdownTimer.stop();
        ballposX = playerX + 80;
        ballposY = 520;
        ballPath.clear();
        repaint();
    }
}
