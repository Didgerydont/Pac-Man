package pacman.game;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;
import java.io.File;

public class Board extends JPanel implements ActionListener {

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
    private int pacAnimPos = 0; // Position of Pac-Man animation frame
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
        ghost = new ImageIcon(getClass().getResource("/pacman/media/ghost.png")).getImage();
        pacman1 = new ImageIcon(getClass().getResource("/pacman/media/pacman.png")).getImage();
        pacman2up = new ImageIcon(getClass().getResource("/pacman/media/up1.png")).getImage();
        pacman3up = new ImageIcon(getClass().getResource("/pacman/media/up2.png")).getImage();
        pacman4up = new ImageIcon(getClass().getResource("/pacman/media/up3.png")).getImage();
        pacman2down = new ImageIcon(getClass().getResource("/pacman/media/down1.png")).getImage();
        pacman3down = new ImageIcon(getClass().getResource("/pacman/media/down2.png")).getImage();
        pacman4down = new ImageIcon(getClass().getResource("/pacman/media/down3.png")).getImage();
        pacman2left = new ImageIcon(getClass().getResource("/pacman/media/left1.png")).getImage();
        pacman3left = new ImageIcon(getClass().getResource("/pacman/media/left2.png")).getImage();
        pacman4left = new ImageIcon(getClass().getResource("/pacman/media/left3.png")).getImage();
        pacman2right = new ImageIcon(getClass().getResource("/pacman/media/right1.png")).getImage();
        pacman3right = new ImageIcon(getClass().getResource("/pacman/media/right2.png")).getImage();
        pacman4right = new ImageIcon(getClass().getResource("/pacman/media/right3.png")).getImage();

    }

    private void initBoard() {

        addKeyListener(new TAdapter());
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
        timer = new Timer(40, this);
        timer.start();
    }

    @Override
    public void addNotify() {
        super.addNotify(); // Ensure the superclass method is called
        initGame(); // Initialize the game
    }

    private void doAnim() {

        pacAnimCount --;

        if(pacAnimCount <= 0) {
            pacAnimCount = PAC_ANIM_DELAY;
            pacAnimPos = pacAnimPos + pacAnimDir;

            if (pacAnimPos == (pacAnimCount -1) || pacAnimPos == 0) {
                pacAnimDir = -pacAnimDir;
            }
        }
    }

    private void playGame(Graphics2D graphics2D) {
        if(dying) {
            death();
        } else {
            movePacman();
            moveGhosts(graphics2D);
            checkMaze();
            drawPacman(graphics2D);
        }

    }

    public void playMusic(String musicLocation) {

        try {

            File musicPath = new File(musicLocation);
            if(musicPath.exists()) {
                AudioInputStream audioInput = AudioSystem.getAudioInputStream(musicPath);
                Clip clip = AudioSystem.getClip();
                clip.open(audioInput);
                clip.start();
                clip.loop(Clip.LOOP_CONTINUOUSLY);
            } else {
                System.out.println("Cannot fint the Audio File");
            }
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    private void showIntroScreen(Graphics2D g2d) {

        g2d.setColor(new Color(0,32,48));
        g2d.fillRect(50, SCREEN_SIZE/2 -30, SCREEN_SIZE - 100, 50);
        g2d.setColor(Color.white);
        g2d.drawRect(50, SCREEN_SIZE/2 - 30, SCREEN_SIZE - 100, 50);

        String s = "Press s to Start.";
        Font small = new Font("Helvetica", Font.BOLD, 14);
        FontMetrics metr = this.getFontMetrics(small);

        g2d.setColor(Color.white);
        g2d.setFont(small);
        g2d.drawString(s, (SCREEN_SIZE - metr.stringWidth(s))/2, SCREEN_SIZE/2);
    }


    private void death() {
        pacsLeft--;

        if(pacsLeft == 0) {
            inGame = false;
        }
        continueLevel();
    }

    private void checkMaze() {

        short i =0;
        boolean finished = true;

        while (i< N_BLOCKS*N_BLOCKS && finished) {

            if ((screenData[i] & 48) != 0) {
                finished = false;
            }
            i++;
        }

        if(finished) {
            score += 50;

            if(n_ghosts < MAX_GHOSTS) {
                n_ghosts ++;
            }
            if (currentSpeed < maxSpeed) {
                currentSpeed++;
            }

            initLevel();
        }
    }

    private void drawScore(Graphics2D g) {
        int i;
        String s;

        g.setFont(smallFont);
        g.setColor(new Color(96,128,255));
        s = "Score: " + score;
        g.drawString(s, SCREEN_SIZE /2 + 96, SCREEN_SIZE + 16);

        for(i = 0; i<pacsLeft; i++) {
            g.drawImage(pacman3left,  i*28 + 8, SCREEN_SIZE+1, this);
        }

    }

    private void moveGhosts(Graphics2D graphics2D) {
        short i ;
        int pos;
        int count;

        for(i=0; i<n_ghosts; i++) {
            if(ghost_x[i] % BLOCK_SIZE == 0 && ghost_y[i] % BLOCK_SIZE == 0) {
                pos = ghost_x[i] / BLOCK_SIZE + N_BLOCKS*(int) (ghost_y[i] / BLOCK_SIZE);

                count = 0;

                if((screenData[pos] & 1) == 0 && ghost_dx[i] != 1) {
                    dx[count] = -1;
                    dy[count] = 0;
                    count++;
                }

                if((screenData[pos] & 2) == 0 && ghost_dy[i] != 1) {
                    dx[count] = 0;
                    dy[count] = -1;
                    count++;
                }

                if((screenData[pos] & 4) == 0 && ghost_dx[i] != -1) {
                    dx[count] = 1;
                    dy[count] = 0;
                    count++;
                }

                if((screenData[pos] & 8) == 0 && ghost_dy[i] != -1) {
                    dx[count] = 0;
                    dy[count] = 1;
                    count++;
                }

                if(count == 0) {
                    if((screenData[pos] & 15) == 15) {
                        ghost_dx[i] = 0;
                        ghost_dy[i] = 0;
                    } else {
                        ghost_dx[i] = -ghost_dx[i];
                        ghost_dy[i] = -ghost_dy[i];
                    }
                } else {
                    count = (int) (Math.random()* count);

                    if(count > 3) {
                        count = 3;
                    }

                    ghost_dx[i] = dx[count];
                    ghost_dy[i] = dy[count];
                }
            }

            ghost_x[i] = ghost_x[i] + (ghost_dx[i]*ghostSpeed[i]);
            ghost_y[i] = ghost_y[i] + (ghost_dy[i]*ghostSpeed[i]);
            drawGhost(graphics2D, ghost_x[i] +1, ghost_y[i]+1);

            if(pacman_x > (ghost_x[i] -12) && pacman_x < (ghost_x[i] +12)
                    && pacman_y > (ghost_y[i] -12) && pacman_y < (ghost_y[i] +12)
                    &&inGame) {
                dying = true;
            }
        }
    }

    private void drawGhost(Graphics2D graphics2D, int x, int y) {

        graphics2D.drawImage(ghost, x, y, this);
    }

    private void movePacman() {

        int pos;
        int ch;

        if(req_dx == -pacman_dx && req_dy == pacman_dy) {
            pacman_dx = req_dx;
            pacman_dy = req_dy;
            view_dx = pacman_dx;
            view_dy = pacman_dy;

        }

        if(pacman_x % BLOCK_SIZE == 0 && pacman_y % BLOCK_SIZE == 0) {
            pos = pacman_x/BLOCK_SIZE + N_BLOCKS*(int)(pacman_y/BLOCK_SIZE);
            ch = screenData[pos];

            if ((ch & 16) != 0) {
                screenData[pos] = (short)(ch & 15);
                score++;
            }
            if(req_dx != 0 || req_dy != 0) {
                if(!((req_dx==-1 && req_dy == 0 && (ch & 1) !=0) || (req_dx == 1 && req_dy == 0 && (ch & 4) != 0)
                        || (req_dx == 0 && req_dy ==-1 && (ch & 2) != 0) || (req_dx == 0 && req_dy == 1 && (ch & 8) != 0))) {
                    pacman_dx = req_dx;
                    pacman_dy = req_dy;
                    view_dx = pacman_dx;
                    view_dy = pacman_dy;
                }

            }
            if ((pacman_dx == -1 && pacman_dy== 0 && (ch & 1) != 0) || (pacman_dx == 1 && pacman_dy == 0 && (ch & 4) != 0)
                    || (pacman_dx == 0 && pacman_dy ==-1 && (ch & 2) != 0) || (pacman_dx == 0 && pacman_dy == 1 && (ch & 8) != 0)){
                pacman_dx = 0;
                pacman_dy = 0;

            }


        }
        pacman_x = pacman_x + PACMAN_SPEED*pacman_dx;
        pacman_y = pacman_y + PACMAN_SPEED*pacman_dy;

    }

    private void drawPacman(Graphics2D graphics2D) {

        if (view_dx == -1) {
            drawPacmanLeft(graphics2D);
        } else if (view_dx == 1) {
            drawPacmanRight(graphics2D);
        } else if (view_dy == -1) {
            drawPacmanUp(graphics2D);
        } else {
            drawPacmanDown(graphics2D);
        }

    }

    private void drawPacmanUp(Graphics2D graphics2D) {
        switch (pacAnimPos) {
            case 1:
                graphics2D.drawImage(pacman2up, pacman_x + 1, pacman_y + 1, this);
                break;
            case 2:
                graphics2D.drawImage(pacman3up, pacman_x + 1, pacman_y + 1, this);
                break;
            case 3:
                graphics2D.drawImage(pacman4up, pacman_x + 1, pacman_y + 1, this);
                break;
            default:
                graphics2D.drawImage(pacman1, pacman_x + 1, pacman_y + 1, this);
                break;
        }
    }

    private void drawPacmanDown(Graphics2D graphics2D) {
        switch (pacAnimPos) {
            case 1:
                graphics2D.drawImage(pacman2down, pacman_x + 1, pacman_y + 1, this);
                break;
            case 2:
                graphics2D.drawImage(pacman3down, pacman_x + 1, pacman_y + 1, this);
                break;
            case 3:
                graphics2D.drawImage(pacman4down, pacman_x + 1, pacman_y + 1, this);
                break;
            default:
                graphics2D.drawImage(pacman1, pacman_x + 1, pacman_y + 1, this);
                break;
        }
    }

    private void drawPacmanLeft(Graphics2D graphics2D) {
        switch (pacAnimPos) {
            case 1:
                graphics2D.drawImage(pacman2left, pacman_x + 1, pacman_y + 1, this);
                break;
            case 2:
                graphics2D.drawImage(pacman3left, pacman_x + 1, pacman_y + 1, this);
                break;
            case 3:
                graphics2D.drawImage(pacman4left, pacman_x + 1, pacman_y + 1, this);
                break;
            default:
                graphics2D.drawImage(pacman1, pacman_x + 1, pacman_y + 1, this);
                break;
        }
    }

    private void drawPacmanRight(Graphics2D graphics2D) {
        switch (pacAnimPos) {
            case 1:
                graphics2D.drawImage(pacman2right, pacman_x + 1, pacman_y + 1, this);
                break;
            case 2:
                graphics2D.drawImage(pacman3right, pacman_x + 1, pacman_y + 1, this);
                break;
            case 3:
                graphics2D.drawImage(pacman4right, pacman_x + 1, pacman_y + 1, this);
                break;
            default:
                graphics2D.drawImage(pacman1, pacman_x + 1, pacman_y + 1, this);
                break;
        }
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
        continueLevel();
    }

    private void continueLevel() {
        short i;
        int dx = 1;
        int random;

        for(i=0;i<n_ghosts;i++) {

            ghost_y[i] = 4*BLOCK_SIZE;
            ghost_x[i] = 4*BLOCK_SIZE;
            ghost_dy[i] = 0;
            ghost_dx[i] = dx;
            dx = -dx;
            random = (int) (Math.random()*(currentSpeed +1));

            if(random>currentSpeed) {
                random = currentSpeed;
            }
            ghostSpeed[i] = validSpeeds[random];
        }

        pacman_x = 7*BLOCK_SIZE;
        pacman_y = 11*BLOCK_SIZE;
        pacman_dx = 0;
        pacman_dy = 0;
        req_dx = 0;
        req_dy = 0;
        view_dx = -1;
        view_dy = 0;
        dying = false;
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

        doAnim();

        drawMaze(graphics2D); // Draw the maze
        drawScore(graphics2D);
        playGame(graphics2D);

        graphics2D.drawImage(ii, 5, 5, this); // Draw the image
        Toolkit.getDefaultToolkit().sync(); // Sync the toolkit
        graphics2D.dispose(); // Dispose of the graphics context
    }

    class TAdapter extends KeyAdapter{
        @Override
        public void keyPressed(KeyEvent e){
            int key = e.getKeyCode();

            if(inGame) {
                if(key == KeyEvent.VK_LEFT) {
                    req_dx=-1;
                    req_dy = 0;
                } else if(key == KeyEvent.VK_RIGHT) {
                    req_dx=1;
                    req_dy = 0;
                } else if(key == KeyEvent.VK_UP) {
                    req_dx=0;
                    req_dy = -1;
                }     else if(key == KeyEvent.VK_DOWN) {
                    req_dx=0;
                    req_dy = 1;
                }     else if (key == KeyEvent.VK_ESCAPE && timer.isRunning()) {
                    inGame = false;
                }     else if (key == KeyEvent.VK_P) {
                    if (timer.isRunning()) {
                        timer.stop();
                    } else {
                        timer.start();
                    }
                }
            } else {
                if (key =='s' || key =='S') {
                    inGame = true;
                    initGame();
                }
            }
        }
        @Override
        public void keyReleased(KeyEvent e) {
            int key = e.getKeyCode();

            if(key == Event.LEFT || key == Event.RIGHT || key == Event.UP || key == Event.DOWN) {
                req_dx = 0;
                req_dy = 0;
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        repaint();
    }
}
