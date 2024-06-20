package pacman.game;

import javax.swing.*;
import java.awt.*;
import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;

public class Board extends JPanel {

    private Dimension d; // Dimension of the board
    private final Font smallFont = new Font("Helvetica", Font.BOLD, 14); // Font for displaying text

    private Image ii; // Image for displaying graphics
    private final Color dotColor = new Color(192, 192, 0); // Color for dots in the maze
    private Color mazeColor; // Color for the maze walls

    private boolean inGame = true; // Flag to check if the game is ongoing
    private boolean dying = false; // Flag to check if Pac-Man is dying

    private final int BLOCK_SIZE = 24; // Size of each block in the maze
    private final int N_BLOCKS = 15; // Number of blocks in the maze
    private final int SCREEN_SIZE = N_BLOCKS * BLOCK_SIZE; // Total screen size
    private final int PAC_ANIM_DELAY = 2; // Delay for Pac-Man animation
    private final int PAC_ANIM_COUNT = 4; // Count for Pac-Man animation frames
    private final int MAX_GHOSTS = 12; // Maximum number of ghosts
    private final int PACMAN_SPEED = 6; // Speed of Pac-Man

    private int pacAnimCount = PAC_ANIM_DELAY; // Counter for Pac-Man animation delay
    private int pacAnimDir = 1; // Direction of Pac-Man animation
    private int getPacAnimPos = 0; // Position of Pac-Man animation frame
    private int n_ghosts = 6; // Number of ghosts in the current level
    private int pacsLeft, score; // Number of Pac-Man lives left and current score
    private int[] dx, dy; // Arrays for direction vectors
    private int[] ghost_x, ghost_y, ghost_dx, ghost_dy, ghostSpeed; // Arrays for ghost positions and speeds

    // Image variables
    private Image ghost;
    private Image pacman1, pacman2up, pacman2left, pacman2right, pacman2down;
    private Image pacman3up, pacman3left, pacman3right, pacman3down;
    private Image pacman4up, pacman4left, pacman4right, pacman4down;

    private int pacman_x, pacman_y, pacman_dy, pacman_dx; // Pac-Man's position and direction
    private int req_dx, req_dy, view_dx, view_dy; // Requested direction and current view direction

    private final short levelData[] = { // Level data for the maze
            19, 26, 26, 26, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 22,
            21, 0, 0, 0, 17, 16, 16, 16, 16, 16, 16, 16, 16, 16, 20,
            21, 0, 0, 0, 17, 16, 16, 16, 16, 16, 16, 16, 16, 16, 20,
            21, 0, 0, 0, 17, 16, 16, 24, 16, 16, 16, 16, 16, 16, 20,
            17, 18, 18, 18, 16, 16, 20, 0, 17, 16, 16, 16, 16, 16, 20,
            17, 16, 16, 16, 16, 16, 20, 0, 17, 16, 16, 16, 16, 24, 20,
            25, 16, 16, 16, 24, 24, 28, 0, 25, 24, 24, 16, 20, 0, 21,
            1, 17, 16, 20, 0, 0, 0, 0, 0, 0, 0, 17, 20, 0, 21,
            1, 17, 16, 16, 18, 18, 22, 0, 19, 18, 18, 16, 20, 0, 21,
            1, 17, 16, 16, 16, 16, 20, 0, 17, 16, 16, 16, 20, 0, 21,
            1, 17, 16, 16, 16, 16, 20, 0, 17, 16, 16, 16, 20, 0, 21,
            1, 17, 16, 16, 16, 16, 16, 18, 16, 16, 16, 16, 20, 0, 21,
            1, 17, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 20, 0, 21,
            1, 25, 24, 24, 24, 24, 24, 24, 24, 24, 16, 16, 16, 18, 20,
            9, 8, 8, 8, 8, 8, 8, 8, 8, 8, 25, 24, 24, 24, 28
    };

    private final int validSpeeds[] = {1, 2, 3, 4, 6, 8}; // Valid speeds for ghosts
    private final int maxSpeed = 6; // Maximum speed for ghosts

    private int currentSpeed = 3; // Current speed of ghosts
    private short[] screenData; // Array to store the current state of the maze
    private Timer timer; // Timer for game updates

    public Board() {
        loadImages();
        initVariables(); // Initialize game variables
        initBoard(); // Initialize the board
    }

    private void loadImages() {
        ghost = new ImageIcon("media/ghost.png").getImage();
        pacman1 = new ImageIcon("media/pacman.png").getImage();
        pacman2up = new ImageIcon("media/up1.png").getImage();
        pacman3up = new ImageIcon("media/up2.png").getImage();
        pacman4up = new ImageIcon("media/up3.png").getImage();
        pacman2down = new ImageIcon("media/down1.png").getImage();
        pacman3down = new ImageIcon("media/down2.png").getImage();
        pacman4down = new ImageIcon("media/down3.png").getImage();
        pacman2left = new ImageIcon("media/left1.png").getImage();
        pacman3left = new ImageIcon("media/left2.png").getImage();
        pacman4left = new ImageIcon("media/left3.png").getImage();
        pacman2right = new ImageIcon("media/right1.png").getImage();
        pacman3right = new ImageIcon("media/right2.png").getImage();
        pacman4right = new ImageIcon("media/right3.png").getImage();

    }

    private void initBoard() {
        setFocusable(true); // Make the board focusable to receive key events
        setBackground(Color.BLACK); // Set the background color to black
    }

    private void initVariables() {
        screenData = new short[N_BLOCKS * N_BLOCKS]; // Initialize the screen data array
        mazeColor = new Color(5, 100, 5); // Set the color of the maze
        d = new Dimension(400, 400); // Set the dimension of the board
        ghost_x = new int[MAX_GHOSTS]; // Initialize the ghost x positions
        ghost_dx = new int[MAX_GHOSTS]; // Initialize the ghost x directions
        ghost_y = new int[MAX_GHOSTS]; // Initialize the ghost y positions
        ghost_dy = new int[MAX_GHOSTS]; // Initialize the ghost y directions
        ghostSpeed = new int[MAX_GHOSTS]; // Initialize the ghost speeds
        dx = new int[4]; // Initialize the direction x array
        dy = new int[4]; // Initialize the direction y array
    }

    @Override
    public void addNotify() {
        super.addNotify(); // Ensure the superclass method is called
        initGame(); // Initialize the game
    }

    private void initGame() {
        pacsLeft = 3; // Set the initial number of lives
        score = 0; // Set the initial score
        initLevel(); // Initialize the level
        n_ghosts = 6; // Set the number of ghosts for the current level
        currentSpeed = 3; // Set the initial speed of ghosts
    }

    private void initLevel() {
        // Copy the level data into the screen data array
        for (int i = 0; i < N_BLOCKS * N_BLOCKS; i++) {
            screenData[i] = levelData[i];
        }
    }

    // Draw the maze on the board
    private void drawMaze(Graphics2D graphics2D) {
        short i = 0; // Initialize the index to track the position in screenData

        // Loop through each block's y-coordinate
        for (int y = 0; y < SCREEN_SIZE; y += BLOCK_SIZE) {
            // Loop through each block's x-coordinate
            for (int x = 0; x < SCREEN_SIZE; x += BLOCK_SIZE) {
                graphics2D.setColor(mazeColor); // Set the maze color
                graphics2D.setStroke(new BasicStroke(2)); // Set the stroke width for the walls

                // Check if the left wall (bit 0) should be drawn
                if ((screenData[i] & 1) != 0) {
                    graphics2D.drawLine(x, y, x, y + BLOCK_SIZE - 1); // Draw left wall
                }

                // Check if the top wall (bit 1) should be drawn
                if ((screenData[i] & 2) != 0) {
                    graphics2D.drawLine(x, y, x + BLOCK_SIZE - 1, y); // Draw top wall
                }

                // Check if the right wall (bit 2) should be drawn
                if ((screenData[i] & 4) != 0) {
                    graphics2D.drawLine(x + BLOCK_SIZE - 1, y, x + BLOCK_SIZE - 1, y + BLOCK_SIZE - 1); // Draw right wall
                }

                // Check if the bottom wall (bit 3) should be drawn
                if ((screenData[i] & 8) != 0) {
                    graphics2D.drawLine(x, y + BLOCK_SIZE - 1, x + BLOCK_SIZE - 1, y + BLOCK_SIZE - 1); // Draw bottom wall
                }

                // Check if there is a dot (bit 4) in the block
                if ((screenData[i] & 16) != 0) {
                    graphics2D.setColor(dotColor); // Set the color for the dot
                    graphics2D.fillRect(x + 11, y + 11, 2, 2); // Draw the dot at the center of the block
                }

                // Increment the index for screenData
                i++;
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g); // Ensure the superclass method is called

        doDrawing(g); // Perform custom drawing
    }

    // Perform custom drawing on the board
    private void doDrawing(Graphics g) {
        Graphics2D graphics2D = (Graphics2D) g; // Cast Graphics to Graphics2D

        graphics2D.setColor(Color.BLACK); // Set the background color to black
        graphics2D.fillRect(0, 0, d.width, d.height); // Fill the background

        drawMaze(graphics2D); // Draw the maze

        graphics2D.drawImage(ii, 5, 5, this); // Draw the image
        Toolkit.getDefaultToolkit().sync(); // Sync the toolkit
        graphics2D.dispose(); // Dispose of the graphics context
    }
}
