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
    private boolean play = false;
    private int score = 0;
    private int highScore = 0;
    private int totalBricks = 52;
    private int level = 1;
    private int lives = 3;
    private Timer timer;
    private int delay = 8;

    private int playerX = 300;
    private int ballposX = playerX + 40;
    private int ballposY = 520;
    private int ballXdir = -1;
    private int ballYdir = -2;

    private MapGenerator map;
    private Font cloisterBlackFont;
    private Queue<Point> ballPath;

    public Gameplay() {
        map = new MapGenerator(4, 13);
        addKeyListener(this);
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);
        timer = new Timer(delay, this);
        timer.start();
        loadCloisterBlackFont();
        ballPath = new LinkedList<>();
    }

    private void loadCloisterBlackFont() {
        try {
            cloisterBlackFont = Font.createFont(Font.TRUETYPE_FONT, new File("CloisterBlack.ttf")).deriveFont(24f);
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(cloisterBlackFont);
        } catch (IOException | FontFormatException e) {
            cloisterBlackFont = new Font("Serif", Font.BOLD, 24);
        }
    }

    public void paint(Graphics g) {
        // Background
        g.setColor(Color.BLACK);
        g.fillRect(1, 1, 700, 600);

        // Background Text
        //g.setFont(cloisterBlackFont);
        //g.setColor(Color.WHITE);
        //g.drawString("Break the Wall !!", 150, 300);
        //g.drawString("Never Give Up", 150, 350);

        // Drawing map
        map.draw((Graphics2D) g);

        // Borders
        g.setColor(Color.YELLOW);
        g.fillRect(0, 0, 3, 600);
        g.fillRect(0, 0, 700, 3);
        g.fillRect(697, 0, 3, 600);

        // Paddle
        draw3DRect(g, playerX, 550, 100, 8, new Color(192, 192, 192));

        // Draw the ball path
        g.setColor(Color.RED);
        for (Point p : ballPath) {
            g.fillOval(p.x, p.y, 10, 10);
        }

        // Ball
        draw3DOval(g, ballposX, ballposY, 20, 20, Color.RED);

        // Score, lives, level, and high score
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 16));
        g.drawString("Lives: " + lives + " | Level: " + level + " | Score: " + score + " | High Score: " + highScore, 150, 30);

        // Game Over
        if (lives <= 0) {
            play = false;
            g.setColor(Color.RED);
            g.setFont(new Font("Arial", Font.BOLD, 36));
            g.drawString("Game Over", 230, 300);
            g.setFont(new Font("Arial", Font.BOLD, 24));
            g.drawString("Press Enter to Restart", 200, 350);
        }

        Toolkit.getDefaultToolkit().sync();
    }

    private void draw3DRect(Graphics g, int x, int y, int width, int height, Color color) {
        g.setColor(color);
        g.fillRect(x, y, width, height);
        g.setColor(color.darker());
        g.fillRect(x + 2, y + 2, width - 2, height - 2);
    }

    private void draw3DOval(Graphics g, int x, int y, int width, int height, Color color) {
        g.setColor(color);
        g.fillOval(x, y, width, height);
        g.setColor(color.darker());
        g.fillOval(x + 2, y + 2, width - 2, height - 2);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        timer.start();
        if (play) {
            ballposX += ballXdir;
            ballposY += ballYdir;

            // Add ball position to the path and maintain a size limit
            ballPath.add(new Point(ballposX, ballposY));
            if (ballPath.size() > 10) { // Limit path size
                ballPath.poll(); // Remove the oldest position
            }

            // Ball and wall collision
            if (ballposX < 0) {
                ballXdir = -ballXdir;
            }
            if (ballposY < 0) {
                ballYdir = -ballYdir;
            }
            if (ballposX > 680) {
                ballXdir = -ballXdir;
            }

            // Paddle collision detection
            if (ballposY + 20 >= 550 && ballposX + 20 >= playerX && ballposX <= playerX + 100) {
                ballYdir = -ballYdir;
            }

            // Ball and brick collision
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

                            // Ball hits left or right of the brick
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

            // Reset if the ball falls below the paddle
            if (ballposY > 570) {
                lives--;
                if (lives > 0) {
                    resetBall();
                } else {
                    play = false;
                }
            }

            // Check if all bricks are destroyed
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
        ballXdir = -1;
        ballYdir = -2;
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
        if (!play && lives > 0) {
            play = true;
        }
        if (e.getKeyCode() == KeyEvent.VK_ENTER && lives <= 0) {
            lives = 3;
            level = 1;
            score = 0;
            totalBricks = 52;
            map = new MapGenerator(4, 13);
            resetBall();
            repaint();
        }
    }

    public void moveRight() {
        playerX += 20;
        if (!play) {
            ballposX += 20;
        }
    }

    public void moveLeft() {
        playerX -= 20;
        if (!play) {
            ballposX -= 20;
        }
    }
}
