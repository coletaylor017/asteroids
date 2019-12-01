package asteroids.game;

import static asteroids.game.Constants.*;
import java.awt.event.*;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.*;
import asteroids.network.AsteroidsClient;
import asteroids.network.GameUpdate;
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

    /* The second player's ship for a two player game */
    private Ship ship2;

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
    // TODO: Implement in Ship instead of Controller so each player can have their own number of lives
    private int lives;

    /** Indicates the players current score */
    // TODO: Implement in Ship instead of Controller so each player can have their own score
    private int score;

    /** Indicates the current level */
    private int level;

    /** The game display */
    private Display display;

    /* "classic" if the game is in enhanced mode, otherwise "enhanced" */
    private String gameMode;

    /* Specifies if a two player game is taking place on one machine. False for LAN games */
    private final boolean twoPlayerGame;

    /* An way to iterate through all ships */
    private ArrayList<Ship> shipList;

    /* The client that handle communication to and from the server */
    private AsteroidsClient client;

    /*
     * Constructs a controller to coordinate the game and screen
     */
    public Controller (String version)
    {
        this(version, null);
    }

    /**
     * Constructs a controller made to work with an AsteroidClient instance
     */
    public Controller (String version, AsteroidsClient aClient)
    {
        // initialize pstate
        pstate = new ParticipantState();

        // initialize client object
        client = aClient;

        shipList = new ArrayList<>();
        // set game mode to "classic", "enhanced", or "local-multiplayer"
        gameMode = version;

        // For now, enhanced mode will equal two player
        twoPlayerGame = gameMode.equals("enhanced");

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
     * Returns a string representing the current game mode. Right now, can either be "classic" or "enhanced" but could
     * expand to more game modes in the future.
     */
    public String getGameMode ()
    {
        return gameMode;
    }

    /* Get the Client instance associated with this controller */
    public AsteroidsClient getClient ()
    {
        return client;
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
     * Returns the list of ships
     */
    public ArrayList<Ship> getShipList ()
    {
        return shipList;
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
     * Place all ships in the center of the screen. Remove any existing ships first.
     */
    private void placeShips ()
    {
        // Place a new ship
        Participant.expire(ship);
        ship = new Ship(SIZE / 2, SIZE / 2, -Math.PI / 2, this);
        addParticipant(ship);
        shipList.add(ship);
        display.setLegend("");

        // if two player mode, place another ship
        if (!gameMode.equals("classic"))
        {
            ship.setColor(Color.GREEN); // in a 2 player game, ships need separate colors to be told apart

            if (gameMode.equals("enhanced")) // for 2 player local mode, make another ship
            {
                Participant.expire(ship2);
                ship2 = new Ship(SIZE / 2, SIZE / 2, -Math.PI / 2, this);
                ship2.setColor(Color.CYAN);
                addParticipant(ship2);
                shipList.add(ship2);
                display.setLegend("");
            }
        }

        // if in online multiplayer mode, let the server know a ship has been placed
        if (gameMode.equals("online-multiplayer"))
        {
            client.send(new GameUpdate("SHIPSPAWN"));
        }
    }

    /**
     * Places an asteroid near one corner of the screen. Gives it a random velocity and rotation.
     */
    private void placeAsteroids ()
    {
        // Place four asteroids near the corners of the screen.
        // TOP LEFT
        addParticipant(new Asteroid(0, 2, EDGE_OFFSET, EDGE_OFFSET, 3, this));
        // TOP RIGHT
        addParticipant(new Asteroid(1, 2, (SIZE - EDGE_OFFSET), EDGE_OFFSET, 3, this));
        // BOTTOM LEFT
        addParticipant(new Asteroid(1, 2, EDGE_OFFSET, SIZE - EDGE_OFFSET, 3, this));
        // BOTTOM RIGHT
        addParticipant(new Asteroid(1, 2, SIZE - EDGE_OFFSET, SIZE - EDGE_OFFSET, 3, this));
    }

    /**
     * Clears the screen so that nothing is displayed
     */
    private void clear ()
    {
        pstate.clear();
        display.setLegend("");
        for (@SuppressWarnings("unused")
        Ship s : shipList) // not 100% sure why this error pops up
        {
            s = null;
        }
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

        // Place the ship, or ships if it's a two player game
        placeShips();

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
     * Restarts the current level
     */
    private void restartLevel ()
    {
        // Kill the server
        // TODO: remove this code
//        if (gameMode.equals("online-multiplayer"))
//        {
//            client.send(new GameUpdate("STOPSERVER"));
//        }
//
//        // close down the client
//        client.close();

        // otherwise ship can start off moving or shooting in the next scene
        for (Ship s : shipList)
        {
            s.setAccelerating(false);
            s.setTurningLeft(false);
            s.setTurningRight(true);
            s.setShooting(false);
        }

        // Clear the screen
        clear();

        // Place asteroids
        placeAsteroids();

        // TODO: Make additional asteroid for each level

        // Place the ship(s)
        placeShips();

        // display.setLives(lives);
    }

    /**
     * Goes to the next level yo
     */
    private void nextLevel ()
    {
        // otherwise ship can start off moving in the next scene
        for (Ship s : shipList)
        {
            s.setAccelerating(false);
            s.setTurningLeft(false);
            s.setTurningRight(false);
            s.setShooting(false);
        }

        // Clear the screen
        clear();

        // Place asteroids
        placeAsteroids();

        // TODO: Make additional asteroid for each level

        // Place the ship(s)
        placeShips();

        level++;

        // Display new level
        display.setLevel(level);

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
    public void shipDestroyed (Ship s)
    {
        if (gameMode.equals("online-multiplayer"))
        {
            client.send(new GameUpdate("SHIPDIE"));
        }

        // remove the ship from shipList
        shipList.remove(s);

        // Null out the ship
        s = null;

        // Decrement lives
        lives--;

        // Display lives
        display.setLives(lives);

        // Since the ship was destroyed, schedule a transition
        scheduleTransition(END_DELAY);

    }

    /**
     * An asteroid has been destroyed
     */
    public void asteroidDestroyed (int size)
    {
        // If all the asteroids are gone, schedule a transition
        if (countAsteroids() == 0)
        {
            scheduleTransition(END_DELAY);
        }
        // for 2 player mode, score is handled in the Asteroid class
        if (!twoPlayerGame)
        {
            score += ASTEROID_SCORE[size];
        }

        // Display new score
        display.setScore(score);

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
            if (e.getActionCommand().contentEquals(START_LABEL))
            {
                initialScreen();
            }
            else if (e.getActionCommand().contentEquals("Kill client"))
            {
                /*
                 * terminate the client program. I think this should also throw an exception, thus ending the
                 * GameNetworkLoop thread handling this socket's connection.
                 */
                client.close();
            }
        }

        // Time to refresh the screen and deal with keyboard input
        else if (e.getSource() == refreshTimer)
        {
            for (Ship s : shipList)
            {
                if (s.turningLeft() && s != null)
                {
                    s.turnLeft();
                }
                if (s.turningRight() && s != null)
                {
                    s.turnRight();
                }
                if (s.accelerating() && s != null)
                {
                    s.accelerate();
                }
                if (s.shooting() && s != null)
                {
                    s.attack();
                }
                if (s != null)
                {
                    // if ship is moving, send its latest location to the server
                    if (gameMode.equals("online-multiplayer") && s.getSpeed() > 0.000000000001) // getSpeed returns a
                                                                                                // double so we have to
                                                                                                // use this inequality
                    {
                        client.send(new GameUpdate("SHIPMOVE", s.getX(), s.getY(), s.getRotation()));
                    }
                    s.applyFriction(SHIP_FRICTION);
                }
            }

            // It may be time to make a game transition
            performTransition();

            // Move the participants to their new locations
            pstate.moveParticipants();

            // Refresh screen
            display.refresh();
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

            if (countAsteroids() == 0)
            {
                nextLevel();
            }
            else if (shipList.size() == 0) // if both players have died
            {
                restartLevel();
            }

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

        // TODO: SHIP 1
        if (e.getKeyCode() == KeyEvent.VK_RIGHT && ship != null)
        {
            ship.setTurningRight(true);
        }

        if (e.getKeyCode() == KeyEvent.VK_LEFT && ship != null)
        {
            ship.setTurningLeft(true);
        }

        if (e.getKeyCode() == KeyEvent.VK_DOWN && ship != null)
        {
            ship.setShooting(true);
        }
        if (e.getKeyCode() == KeyEvent.VK_UP && ship != null)
        {
            ship.setAccelerating(true);
            // client.send(new GameUpdate("SHIPMOVE", ship.getX(), ship.getY()));
        }

        // TODO: SHIP 2
        if (e.getKeyCode() == KeyEvent.VK_D && ship != null)
        {
            if (!twoPlayerGame)
            {
                ship.setTurningRight(true);
            }
            else
            {
                ship2.setTurningRight(true);
            }
        }

        if (e.getKeyCode() == KeyEvent.VK_A && ship != null)
        {
            if (!twoPlayerGame)
            {
                ship.setTurningLeft(true);
            }
            else
            {
                ship2.setTurningLeft(true);
            }
        }
        if (e.getKeyCode() == KeyEvent.VK_S && ship != null)
        {
            // s key will not fire player 1's ship in two-player mode
            if (!twoPlayerGame)
            {
                ship.setShooting(true);
            }
            else
            {
                ship2.setShooting(true);
            }
        }
        if (e.getKeyCode() == KeyEvent.VK_SPACE && ship != null)
        {
            if (!twoPlayerGame)
            {
                ship.setShooting(true);
            }
        }

        if (e.getKeyCode() == KeyEvent.VK_W && ship != null)
        {
            if (!twoPlayerGame)
            {
                ship.setAccelerating(true);
            }
            else
            {
                ship2.setAccelerating(true);
            }
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
        // TODO: PLAYER1
        // UP KEY
        if (e.getKeyCode() == KeyEvent.VK_UP && ship != null)
        {
            ship.setAccelerating(false);
        }

        // LEFT KEY
        if (e.getKeyCode() == KeyEvent.VK_LEFT && ship != null)
        {
            ship.setTurningLeft(false);
        }

        // RIGHT KEY
        if (e.getKeyCode() == KeyEvent.VK_RIGHT && ship != null)
        {
            ship.setTurningRight(false);
        }

        // TODO: PLAYER2
        // W KEY
        if (e.getKeyCode() == KeyEvent.VK_W && ship != null)
        {
            if (!twoPlayerGame)
            {
                ship.setAccelerating(false);
            }
            else
            {
                ship2.setAccelerating(false);
            }
        }

        // A KEY
        if (e.getKeyCode() == KeyEvent.VK_A && ship != null)
        {
            if (!twoPlayerGame)
            {
                ship.setTurningLeft(false);
            }
            else
            {
                ship2.setTurningLeft(false);
            }
        }

        // D KEY
        if (e.getKeyCode() == KeyEvent.VK_D && ship != null)
        {
            if (!twoPlayerGame)
            {
                ship.setTurningRight(false);
            }
            else
            {
                ship2.setTurningRight(false);
            }
        }
        if (e.getKeyCode() == KeyEvent.VK_S && ship != null)
        {
            if (!twoPlayerGame)
            {
                ship.setShooting(false);
            }
            else
            {
                ship2.setShooting(false);
            }
        }
        if (e.getKeyCode() == KeyEvent.VK_SPACE && ship != null)
        {
            if (!twoPlayerGame)
            {
                ship.setShooting(false);
            }
        }
        if (e.getKeyCode() == KeyEvent.VK_DOWN && ship != null)
        {
            ship.setShooting(false);
        }
    }

}
