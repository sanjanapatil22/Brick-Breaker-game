import javax.swing.JPanel;
import javax.swing.Timer;

import java.awt.*;
import java.awt.event.*;
import java.util.Random;

import javax.sound.sampled.*; // for sounds
import java.io.*;

public class GamePanel extends JPanel implements KeyListener, ActionListener {

    private boolean play = false;
    private int score = 0;
    private int row = 3;
    private int col = 7;
    private int totalBricks = row * col;
    private Timer timer;
    private int delay = 5; // game refresh speed in milliseconds
    private int playerX = 310; // paddles X position

    Random rand = new Random();

    // Assuming the game width is 700 and height is 600
    private int ballPosX = 100 + rand.nextInt(500); // between 100 and 600
    private int ballPosY = 300 + rand.nextInt(100); // between 300 and 400

    // ball movement direction
    private int ballDirX = -1;
    private int ballDirY = -2;

    private MapGenerator map;

    private boolean moveLeft = false;
    private boolean moveRight = false;

    private boolean start = true;
    private boolean gameOverSoundPlayed = false;
    private boolean gameStartSoundPlayed = false;
    private boolean gameOverWinnerSoundPlayed = false;

    private Thread backgroundMusicThread;
    private volatile boolean isMusicPlaying = false;

    // draw all bricks before ball and paddle

    public GamePanel() {
        setPreferredSize(new Dimension(700, 600));

        // Set background color to black
        setBackground(Color.BLACK);

        // This makes sure the panel can receive keyboard input in the future
        setFocusable(true);
        requestFocusInWindow();

        addKeyListener(this); // listens for key input

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                requestFocusInWindow(); // regain focus on click
            }
        });

        requestFocusInWindow(); // Ensure panel gets keyboard focus
        map = new MapGenerator(row, col); // 3 rows, 7 columns
        totalBricks = row * col;

        // timer calls action performed every 8ms
        timer = new Timer(delay, this);
        timer.start();

    }

    public void playBackgroundMusicStreaming(String filepath) {
        isMusicPlaying = true;

        backgroundMusicThread = new Thread(() -> {
            try {
                do {
                    File soundFile = new File(filepath);
                    AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundFile);
                    AudioFormat format = audioIn.getFormat();
                    DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
                    SourceDataLine sourceLine = (SourceDataLine) AudioSystem.getLine(info);
                    sourceLine.open(format);
                    sourceLine.start();

                    byte[] buffer = new byte[4096];
                    int bytesRead;

                    while (isMusicPlaying && (bytesRead = audioIn.read(buffer, 0, buffer.length)) != -1) {
                        sourceLine.write(buffer, 0, bytesRead);
                    }

                    sourceLine.drain();
                    sourceLine.stop();
                    sourceLine.close();
                    audioIn.close();
                } while (isMusicPlaying); // Loop playback
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        backgroundMusicThread.start();
    }

    public void stopBackgroundMusic() {
        isMusicPlaying = false;
    }

    public void playSound(String soundFile) {
        new Thread(() -> {
            try {
                // Try loading from resources first
                InputStream audioSrc = getClass().getResourceAsStream(soundFile);
                if (audioSrc == null) {
                    // Fall back to file system
                    File soundPath = new File(soundFile);
                    if (!soundPath.exists()) {
                        System.err.println("Sound file not found: " + soundFile);
                        return;
                    }
                    audioSrc = new FileInputStream(soundPath);
                }

                AudioInputStream audioInput = AudioSystem.getAudioInputStream(
                        new BufferedInputStream(audioSrc));
                Clip clip = AudioSystem.getClip();
                clip.open(audioInput);
                clip.setFramePosition(0);
                clip.start();

            } catch (Exception e) {
                System.err.println("Error playing sound: " + soundFile);
                e.printStackTrace();
            }
        }).start();
    }

    @Override
    public void paintComponent(Graphics g) {
        // Always call super first to ensure the panel is cleared before redrawing
        super.paintComponent(g);

        // background
        Color backgroundColor = new Color(0xffe3ec);
        g.setColor(backgroundColor);
        g.fillRect(0, 0, 700, 600);

        map.draw((Graphics2D) g);

        // Paddle
        Color paddleColor = new Color(0xd27d92);
        g.setColor(paddleColor);
        g.fillRoundRect(playerX, 550, 100, 10, 10, 10);

        // Ball
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(new Color(150, 0, 60, 60));
        g2d.fillOval(ballPosX + 4, ballPosY + 4, 20, 20);

        GradientPaint gradient = new GradientPaint(
                ballPosX, ballPosY, new Color(0xffa6c1), // light pink
                ballPosX + 20, ballPosY + 20, new Color(0xff597b) // darker rose pink
        );
        g2d.setPaint(gradient);
        g2d.fillOval(ballPosX, ballPosY, 20, 20);

        // START screen
        if (start) {
            g.setColor(new Color(0x800040)); // rich plum
            g.setFont(new Font("serif", Font.BOLD, 30));
            g.drawString("Welcome to Brick Breaker!", 165, 300);

            g.setFont(new Font("serif", Font.BOLD, 20));
            g.drawString("Press Enter to Start", 245, 350);

            if (!gameStartSoundPlayed) {
                stopBackgroundMusic();
                playSound("/Users/sanjanapatil/Documents/sanjana/cs/java/BrickBreakerGame/sounds/game-start1.wav");
                gameStartSoundPlayed = true;
            }

        } else if (!play && totalBricks > 0) {
            // Game Over screen
            g.setColor(new Color(0xb30059));
            g.setFont(new Font("serif", Font.BOLD, 30));
            g.drawString("Game Over, Score: " + score, 190, 250);
            g.drawString("HAHA, GO HOME LOSER", 140, 300);

            g.setFont(new Font("serif", Font.BOLD, 20));
            g.drawString("Press Enter to Restart", 230, 350);

            stopBackgroundMusic();

            if (!gameOverSoundPlayed) {
                playSound("/Users/sanjanapatil/Documents/sanjana/cs/java/BrickBreakerGame/sounds/gameover1.wav");
                gameOverSoundPlayed = true;
            }
        }

        // Victory screen
        if (totalBricks == 0) {
            play = false;
            ballDirX = 0;
            ballDirY = 0;

            g.setColor(new Color(0x99004c)); // dark rose
            g.setFont(new Font("serif", Font.BOLD, 30));
            g.drawString("You Won! Score: " + score, 200, 250);

            g.setFont(new Font("serif", Font.BOLD, 20));
            g.drawString("Press Enter to Restart", 240, 300);
            stopBackgroundMusic();
            if (!gameOverWinnerSoundPlayed) {
                playSound("/Users/sanjanapatil/Documents/sanjana/cs/java/BrickBreakerGame/sounds/game-end-winner.wav");
                gameOverWinnerSoundPlayed = true;
            }
        }

        // Score
        g.setColor(new Color(0x800040)); // plum pink
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("Score: " + score, 550, 30);
    }

    // automatically called repeatedly by timer
    @Override
    public void actionPerformed(ActionEvent e) {
        if (play) {
            timer.start();
            if (moveRight && playerX < 600) {
                playerX += 4;
            }
            if (moveLeft && playerX > 5) {
                playerX -= 4;
            }

            // Move the ball
            ballPosX += ballDirX;
            ballPosY += ballDirY;

            // Check for brick collisions
            A: for (int i = 0; i < map.map.length; i++) {
                for (int j = 0; j < map.map[0].length; j++) {
                    if (map.map[i][j] > 0) {
                        int brickX = j * map.brickWidth + 50;
                        int brickY = i * map.brickHeight + 50;
                        int brickWidth = map.brickWidth;
                        int brickHeight = map.brickHeight;

                        Rectangle brickRect = new Rectangle(brickX, brickY, brickWidth, brickHeight);
                        Rectangle ballRect = new Rectangle(ballPosX, ballPosY, 20, 20);

                        if (ballRect.intersects(brickRect)) {
                            map.setBrickValue(0, i, j);
                            System.out.println("brick hit");
                            Toolkit.getDefaultToolkit().beep(); // Plays a system beep

                            totalBricks--;
                            score += 5;

                            // Bounce off: detect hit direction
                            if (ballPosX + 19 <= brickX || ballPosX + 1 >= brickX + brickWidth) {
                                ballDirX = -ballDirX;
                            } else {
                                ballDirY = -ballDirY;
                            }

                            break A; // break both loops after 1 collision
                        }
                    }
                }
            }

            // Bounce off left wall
            if (ballPosX < 0) {
                ballDirX = -ballDirX;
            }

            // Bounce off top
            if (ballPosY < 0) {
                ballDirY = -ballDirY;
            }

            // Bounce off right wall
            if (ballPosX > 670) {
                ballDirX = -ballDirX;
            }

            // Paddle collision
            if (ballPosY > 530 && ballPosX >= playerX - 10 && ballPosX <= playerX + 100) {
                ballDirY = -ballDirY;
            }

            // Missed the paddle — game over
            if (ballPosY > 555) {
                play = false;
                ballDirX = 0;
                ballDirY = 0;
                timer.stop(); // stop the timer when game over
                repaint();
            }

        }
        repaint();
    } // key pressed

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            moveRight = true;
        }
        if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            moveLeft = true;
        }
        // Restart game
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            System.out.println("ENTER pressed"); // debug check
            if (!play) {
                play = true;
                start = false;
                gameOverSoundPlayed = false;
                gameStartSoundPlayed = false;
                gameOverWinnerSoundPlayed = false;
                // position the ball randomly each time.
                ballPosX = 100 + rand.nextInt(500);
                ballPosY = 300 + rand.nextInt(100);
                ballDirX = -1;
                ballDirY = -2;
                playerX = 310;
                score = 0;
                totalBricks = row * col;
                map = new MapGenerator(row, col);
                requestFocusInWindow();
                repaint();

                timer = new Timer(delay, this); // Re-create and assign
                timer.start();// restart timer if stopped
                stopBackgroundMusic(); // stop any previous instance
                playBackgroundMusicStreaming("/Users/sanjanapatil/Documents/sanjana/cs/java/BrickBreakerGame/sounds/mixkit-dirty-thinkin-989.wav");
                repaint();
            }
        }
        requestFocusInWindow();
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            moveRight = false;
        }
        if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            moveLeft = false;
        }
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(700, 600); // set your desired canvas size
    }
}