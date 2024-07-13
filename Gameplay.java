import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;

public class Gameplay extends JPanel implements KeyListener, ActionListener {
    private boolean play = false;
    private int score = 0;
    private int totalBricks = 21;
    private int level = 1;
    private int lives = 3;
    private Timer timer;
    private int delay = 8;

    private int playerX = 310;
    private int ballposX = playerX + 40;
    private int ballposY = 530;
    private int ballXdir = -1;
    private int ballYdir = -2;

    private MapGenerator map;
    private Font cloisterBlackFont;

    public Gameplay() {
        map = new MapGenerator(3, 7);
        addKeyListener(this);
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);
        timer = new Timer(delay, this);
        timer.start();
        loadCloisterBlackFont();
    }

    private void loadCloisterBlackFont() {
        try {
            cloisterBlackFont = Font.createFont(Font.TRUETYPE_FONT, new File("CloisterBlack.ttf")).deriveFont(48f);
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(cloisterBlackFont);
        } catch (IOException | FontFormatException e) {
            cloisterBlackFont = new Font("Serif", Font.BOLD, 48);
        }
    }

    public void paint(Graphics g) {
        // Background
        g.setColor(new Color(30, 30, 30));
        g.fillRect(1, 1, 692, 592);

        // Background Text
        g.setFont(cloisterBlackFont);
        g.setColor(Color.WHITE);
        g.drawString("Break the Wall !!", 150, 300);
        g.drawString("Never Give Up", 150, 350);

        // Drawing map
        map.draw((Graphics2D) g);

        // Borders
        g.setColor(Color.YELLOW);
        g.fillRect(0, 0, 3, 592);
        g.fillRect(0, 0, 692, 3);
        g.fillRect(691, 0, 3, 592);

        // Paddle with rounded corners
        drawRoundedRect(g, playerX, 550, 100, 8, new Color(192, 192, 192));

        // Ball
        draw3DOval(g, ballposX, ballposY, 20, 20, Color.RED);

        // Display Lives, Level, and Score at the top center
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        String status = "Lives: " + lives + " | Level: " + level + " | Score: " + score;
        int statusWidth = g.getFontMetrics().stringWidth(status);
        g.drawString(status, (692 - statusWidth) / 2, 30);

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

    private void drawRoundedRect(Graphics g, int x, int y, int width, int height, Color color) {
        g.setColor(color);
        g.fillRoundRect(x, y, width, height, 10, 10);
        g.setColor(color.darker());
        g.fillRoundRect(x + 2, y + 2, width - 2, height - 2, 10, 10);
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

            // Ball and wall collision
            if (ballposX < 0) {
                ballXdir = -ballXdir;
            }
            if (ballposY < 0) {
                ballYdir = -ballYdir;
            }
            if (ballposX > 670) {
                ballXdir = -ballXdir;
            }

            // Early paddle collision detection
            if (ballposY + 20 >= 550 && ballposX + 20 >= playerX && ballposX <= playerX + 100) {
                ballYdir = -ballYdir; // Bounce back
            }

            // Ball and brick collision
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

                            // Ball hits left or right of the brick
                            if (ballposX + 19 <= brickRect.x || ballposX + 1 >= brickRect.x + brickRect.width) {
                                ballXdir = -ballXdir;
                            } else {
                                ballYdir = -ballYdir;
                            }
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
                map = new MapGenerator(3 + level, 7 + level); // recreate the map with the new level
                resetBall();
            }
        }

        repaint();
    }

    public void resetBall() {
        ballposX = playerX + 40;
        ballposY = 530;
        ballXdir = -1;
        ballYdir = -2;
        play = false;
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
        // Start the game when any key is pressed
        if (!play && lives > 0) {
            play = true;
        }
        // Reset the game if Enter is pressed after game over
        if (e.getKeyCode() == KeyEvent.VK_ENTER && lives <= 0) {
            lives = 3;
            level = 1;
            score = 0;
            totalBricks = 21;
            map = new MapGenerator(3, 7);
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
