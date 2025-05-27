import java.awt.*;

public class MapGenerator {
    public int[][] map; // brick grid: 1 = visible, 0 = broken
    public int brickWidth;
    public int brickHeight;
    public boolean[][] justHitBrick;

    public MapGenerator(int row, int col) {
        map = new int[row][col];
        justHitBrick = new boolean[row][col];

        for (int i = 0; i < row; i++) {
            for (int j = 0; j < col; j++) {
                map[i][j] = 1; // 1 means brick is present
            }
        }

        brickWidth = 600 / col;
        brickHeight = 150 / row;
    }

    // Draw all bricks
    public void draw(Graphics2D g) {

        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[0].length; j++) {
                if (map[i][j] > 0) {
                    int brickX = j * brickWidth + 50;
                    int brickY = i * brickHeight + 50;

                    // Base and highlight colors in pinks
                    Color brickBase = new Color(0xffc1d9); // soft baby pink
                    Color brickHighlight = new Color(0xff8ebf); // medium pink

                    // Cast to Graphics2D
                    Graphics2D g2d = (Graphics2D) g;

                    // Shadow for depth (soft purple-pink tint)
                    g2d.setColor(new Color(100, 0, 60, 30));
                    g2d.fillRoundRect(brickX + 2, brickY + 2, brickWidth, brickHeight, 10, 10);

                    // Gradient pink brick fill
                    GradientPaint brickGradient = new GradientPaint(
                            brickX, brickY, brickHighlight,
                            brickX + brickWidth, brickY + brickHeight, brickBase);
                    g2d.setPaint(brickGradient);
                    g2d.fillRoundRect(brickX, brickY, brickWidth, brickHeight, 10, 10);

                    // Bold pink border for brick
                    g2d.setStroke(new BasicStroke(1.5f));
                    g2d.setColor(new Color(0xb30059)); // rich rose border
                    g2d.drawRoundRect(brickX, brickY, brickWidth, brickHeight, 10, 10);

                    // Optional crack effect
                }
            }
        }
    }

    public void showCrack(int row, int col) {
        justHitBrick[row][col] = true;
    }

    public void clearCrack(int row, int col) {
        justHitBrick[row][col] = false;
    }

    // Set a brick as broken
    public void setBrickValue(int value, int row, int col) {
        map[row][col] = value;
    }
}