import javax.swing.JFrame;

public class GameFrame extends JFrame {
    public GameFrame() {
        setTitle("Brick Breaker Game");

        setResizable(false); // window wont be resizable
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // close the application when the window is closed

        add(new GamePanel());
        pack(); // fit window size to its contents
        setVisible(true); // make window visible on the screen
        setLocationRelativeTo(null);
        // GamePanel.requestFocusInWindow();

    }
}