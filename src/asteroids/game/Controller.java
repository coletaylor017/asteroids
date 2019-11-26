package asteroids.game;

import static asteroids.game.Constants.*;
import java.awt.event.*;
import java.util.Iterator;
import javax.swing.*;
import asteroids.participants.*;

/**
 * Controls a game of Asteroids.
 */
public class Controller implements KeyListener, ActionListener, Iterable<Participant>
{
    /** The state of all the Participants */
    private ParticipantState pstate;

    /** The ship (if one is active) or null (otherwise) */
    private Ship ship;

    /** When this timer goes off, it is time to refresh the animation */
    private Timer refreshTimer;

    /** indicates how many lives are left */
    // private String remainingLives = "Remaining Lives: ";

    /**
     * The time at which a transition to a new stage of the game should be made. A transition is scheduled a few seconds
     * in the future to give the user time to see what has happened before doing something like going to a new level or
     * resetting the current level.
     */
    private long transitionTime;

    /** Number of lives left */
    private int lives;

    /** Indicates the players current score */
    private int score;

    /** Indicates the current level */
    private int level;
    
    /* true when ship is firing */
    private boolean shooting;
    
    /** True if '</ A' are being pressed */
    private boolean turnLeft;

    /** True if '>/D' are being pressed */
    private boolean turnRight;
    
    /** True if UP/W are being pressed */
    private boolean moveForward;

    /** The game display */
    private Display display;
    
    /* "true" if the game is in enhanced mode, otherwise "false" */
    private String gameMode;
    
    /* Specifies if a two player game is taking place */
    private final boolean twoPlayerGame;
    
    /* Counter to keep track of number of bullets */
    private int numBullets;

    /**
     * Constructs a controller to coordinate the game and screen
     */
    public Controller (String version)
    {   
        // TODO: make input on startup screen to pick 1 or two players
        twoPlayerGame = false;
        
        // Number of bullets starts out at zero
        numBullets = 0;
        
        // if enhanced version requested, set enhanced to true
        gameMode = version;
        
        // Initialize the ParticipantState
        pstate = new ParticipantState();

        // Set up the refresh timer.
        refreshTimer = new Timer(FRAME_INTERVAL, this);

        // Clear the transitionTime
        transitionTime = Long.MAX_VALUE;

        // Record the display object
        display = new Display(this);

        // Bring up the splash screen and start the refresh timer
        splashScreen();
        display.setVisible(true);
        refreshTimer.start();
    }
    
    /*
     * Returns a string representing the current game mode.
     * Right now, can either be "classic" or "enhanced"
     * but could expand to more game modes in the future.
     */
    public String getGameMode ()
    {
        return gameMode;
    }

    /**
     * This makes it possible to use an enhanced for loop to iterate through the Participants being managed by a
     * Controller.
     */
    @Override
    public Iterator<Participant> iterator ()
    {
        return pstate.iterator();
    }

    /**
     * Returns the ship, or null if there isn't one
     */
    public Ship getShip ()
    {
        return ship;
    }

    /**
     * Configures the game screen to display the splash screen
     */
    private void splashScreen ()
    {
        // Clear the screen, reset the level, and display the legend
        clear();
        display.setLegend("Asteroids");

        placeAsteroids();
    }

    /**
     * The game is over. Displays a message to that effect.
     */
    private void finalScreen ()
    {
        display.setLegend(GAME_OVER);
        display.removeKeyListener(this);
    }

    /**
     * Place a new ship in the center of the screen. Remove any existing ship first.
     */
    private void placeShip ()
    {
        // Place a new ship
        Participant.expire(ship);
        ship = new Ship(SIZE / 2, SIZE / 2, -Math.PI / 2, this);
        addParticipant(ship);
        display.setLegend("");
    }

    /**
     * Places an asteroid near one corner of the screen. Gives it a random velocity and rotation.
     */
    private void placeAsteroids ()
    {  
        // Place four asteroids near the corners of the screen.
        //TOP LEFT
        addParticipant(new Asteroid(0, 2, EDGE_OFFSET, EDGE_OFFSET, 3, this));
        //TOP RIGHT
        addParticipant(new Asteroid(1, 2, (SIZE - EDGE_OFFSET), EDGE_OFFSET, 3, this));
        //BOTTOM LEFT
        addParticipant(new Asteroid(1,2, EDGE_OFFSET, SIZE-EDGE_OFFSET, 3, this));
        //BOTTOM RIGHT
        addParticipant(new Asteroid(1,2, SIZE-EDGE_OFFSET, SIZE - EDGE_OFFSET, 3, this));
    }

    /**
     * Clears the screen so that nothing is displayed
     */
    private void clear ()
    {
        pstate.clear();
        display.setLegend("");
        ship = null;
    }

    /**
     * Sets things up and begins a new game.
     */
    private void initialScreen ()
    {
        // Clear the screen
        clear();

        // Place asteroids
        placeAsteroids();

        // Place the ship
        placeShip();

        // Reset statistics

        // set lives, level, score
        lives = 3;
        level = 1;
        score = 0;

        // Display Lives
        display.setLives(lives);
        // Display Level
        display.setLevel(level);
        // Display Score
        display.setScore(score);

        // Start listening to events (but don't listen twice)
        display.removeKeyListener(this);
        display.addKeyListener(this);

        // Give focus to the game screen
        display.requestFocusInWindow();
    }

    /**
     * Adds a new Participant
     */
    public void addParticipant (Participant p)
    {
        pstate.addParticipant(p);
    }

    /**
     * The ship has been destroyed
     */
    public void shipDestroyed ()
    {
        // Null out the ship
        ship = null;

        // Display a legend
        display.setLegend("Ouch!");

        // Decrement lives
        lives--;

        // Display lives
        display.setLives(lives);

        // Since the ship was destroyed, schedule a transition
        scheduleTransition(END_DELAY);
        
        initialScreen();
    }

    /**
     * An asteroid has been destroyed
     */
    public void asteroidDestroyed ()
    {
        // If all the asteroids are gone, schedule a transition
        if (countAsteroids() == 0)
        {
            scheduleTransition(END_DELAY);
        }
        // Increment score by 20
        score += 20;
        // Display new score
        display.setScore(score);
        
        
        
        // TODO: MAKE NEW DEBRIS OBJECT AT LOCATION OF DESTROYED ASTEROID
        
        // TODO: PUT ASTEROID DUPLICATION CODE HERE
    }
    
    /*
     * Called when a bullet is destroyed.
     */
    public void bulletDestroyed ()
    {
        numBullets--;
    }

    /**
     * Schedules a transition m msecs in the future
     */
    private void scheduleTransition (int m)
    {
        transitionTime = System.currentTimeMillis() + m;
    }

    /**
     * This method will be invoked because of button presses and timer events.
     */
    @Override
    public void actionPerformed (ActionEvent e)
    {
        // The start button has been pressed. Stop whatever we're doing
        // and bring up the initial screen
       
        if (e.getSource() instanceof JButton)
        {
            initialScreen();
        }

        // Time to refresh the screen and deal with keyboard input
        else if (e.getSource() == refreshTimer)
        {
            
            if (turnLeft == true)
            {
                ship.turnLeft();
            }
            if (turnRight == true)
            {
                ship.turnRight();
            }
            if (moveForward == true)
            {
                ship.accelerate();
            }
            
            // It may be time to make a game transition
            performTransition();

            // Move the participants to their new locations
            pstate.moveParticipants();

            // Refresh screen
            display.refresh();
            
            if (shooting)
            {
                attack(ship);
            }
        }

    }

    /**
     * If the transition time has been reached, transition to a new state
     */
    private void performTransition ()
    {
        // Do something only if the time has been reached
        if (transitionTime <= System.currentTimeMillis())
        {
            // Clear the transition time
            transitionTime = Long.MAX_VALUE;

            // If there are no lives left, the game is over. Show the final
            // screen.
            if (lives <= 0)
            {
                finalScreen();
            }
        }
    }

    /**
     * Returns the number of asteroids that are active participants
     */
    private int countAsteroids ()
    {
        int count = 0;
        for (Participant p : this)
        {
            if (p instanceof Asteroid)
            {
                count++;
            }
        }
        return count;
    }

    /**
     * If a key of interest is pressed, record that it is down.
     */
    @Override
    public void keyPressed (KeyEvent e)
    {

        //TODO: SHIP 1
        if (e.getKeyCode() == KeyEvent.VK_RIGHT && ship != null)
        {
            turnRight = true;
        }

        if (e.getKeyCode() == KeyEvent.VK_LEFT && ship != null)
        {
            turnLeft = true;
        }

        if (e.getKeyCode() == KeyEvent.VK_DOWN && ship != null)
        {
            // Down key always fires ship 1
            shooting = true;
        }
        if (e.getKeyCode() == KeyEvent.VK_UP && ship != null)
        {
            moveForward = true;
        }

        //TODO: SHIP 2
        if (e.getKeyCode() == KeyEvent.VK_D && ship != null)
        {
            turnRight = true;
        }

        if (e.getKeyCode() == KeyEvent.VK_A && ship != null)
        {
            turnLeft = true;
        }
        if (e.getKeyCode() == KeyEvent.VK_S && ship != null)
        {
            // s key will not fire player 1's ship in two-player mode
            if (!twoPlayerGame)
            {
                shooting = true;
            }
            else
            {
//                attack(ship2); comment out when two player game is in place
            }
        }
        if (e.getKeyCode() == KeyEvent.VK_SPACE && ship != null)
        {
            if (!twoPlayerGame)
            {
                shooting = true;
            }
        }

        if (e.getKeyCode() == KeyEvent.VK_W && ship != null)
        {
           moveForward = true;
        }

    }
    
    /*
     * Sends a bullet from the specified ship. This way,
     * one can specify which ship is firing when in two 
     * player mode.
     */
    public void attack (Ship shooter)
    {
        if (numBullets <= BULLET_LIMIT)
        {
            Bullet bullet = new Bullet(shooter.getXNose(), shooter.getYNose(), shooter.getRotation(), BULLET_SPEED, this);
            addParticipant(bullet);
            numBullets++;
            new ParticipantCountdownTimer(bullet, BULLET_DURATION);
        }
    }

    @Override
    public void keyTyped (KeyEvent e)
    {
    }

    /**
     * If a key of interest is pressed stop whatever action was initiated from keyPressed
     */
    @Override
    public void keyReleased (KeyEvent e)
    {
        //TODO: PLAYER1
        //UP KEY
        if (e.getKeyCode() == KeyEvent.VK_UP && ship != null)
        {
            moveForward =false;
            ship.turnFlameOff();
            ship.applyFriction(SHIP_FRICTION);
        }
        
        //LEFT KEY
        if (e.getKeyCode() == KeyEvent.VK_LEFT && ship != null)
        {
            turnLeft = false;
        }       
        
        //RIGHT KEY
        if (e.getKeyCode() == KeyEvent.VK_RIGHT && ship != null)
        {
            turnRight = false;
        }
        
        //TODO: PLAYER2
        //W KEY
        if (e.getKeyCode() == KeyEvent.VK_W && ship != null)
        {
            moveForward =false;
            ship.turnFlameOff();
            ship.applyFriction(SHIP_FRICTION);
        }
        
        //A KEY
        if (e.getKeyCode() == KeyEvent.VK_A && ship != null)
        {
            turnLeft = false;
        }
        
        //D KEY
        if (e.getKeyCode() == KeyEvent.VK_D && ship != null)
        {
            turnRight = false;
        }
        if (e.getKeyCode() == KeyEvent.VK_S && ship != null)
        {
            shooting = false;
        }
        if (e.getKeyCode() == KeyEvent.VK_SPACE && ship != null)
        {
            shooting = false;
        }
        if (e.getKeyCode() == KeyEvent.VK_DOWN && ship != null)
        {
            shooting = false;
        }
    }

}
