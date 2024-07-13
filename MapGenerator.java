import java.awt.*;

public class MapGenerator {
    public int[][] map;
    public int brickWidth;
    public int brickHeight;

    private Color[] rowColors = {
            new Color(255, 255, 100), // Yellow-ish
            new Color(255, 100, 100), // Red-ish
            new Color(100, 100, 255), // Blue-ish
            new Color(100, 255, 100), // Green-ish
            new Color(255, 100, 255)  // Purple-ish
    };

    public MapGenerator(int rows, int cols) {
        map = new int[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                map[i][j] = 1; // Active bricks
            }
        }

        brickWidth = 540 / cols; // Width of each brick
        brickHeight = 150 / rows; // Height of each brick
    }

    public void draw(Graphics2D g) {
        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[0].length; j++) {
                if (map[i][j] > 0) {
                    Color brickColor = rowColors[i % rowColors.length];
                    g.setColor(brickColor);
                    g.fillRoundRect(j * brickWidth + 80, i * brickHeight + 50, brickWidth, brickHeight, 5, 5);
                    g.setColor(Color.BLACK); // Black outline color
                    g.drawRoundRect(j * brickWidth + 80, i * brickHeight + 50, brickWidth, brickHeight, 5, 5);
                }
            }
        }
    }

    public void setBrickValue(int value, int row, int col) {
        map[row][col] = value;
    }

    public int getTotalBricks() {
        int count = 0;
        for (int[] row : map) {
            for (int j : row) {
                count += j;
            }
        }
        return count;
    }
}
