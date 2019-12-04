package asteroids.game;

import static asteroids.game.Constants.*;
import java.awt.event.*;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
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

    /* The second player's ship for a two player game */
    private Ship ship2;

    /** When this timer goes off, it is time to refresh the animation */
    private Timer refreshTimer;

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

    /** "classic" if the game is in enhanced mode, otherwise "enhanced" */
    private String gameMode;

    /* Specifies if a two player game is taking place */
    private final boolean twoPlayerGame;

    /* Specifies whether beats should play */
    private boolean playSound;

    /* An way to iterate through all ships */
    private ArrayList<Ship> shipList;

    /** Shooting sound */
    private Clip fire;

    /** Acceleration sound */
    private Clip thrust;

    /** Sound for large asteroid being destroyed */
    private Clip bangLarge;

    /** Sound for medium asteroid being destroyed */
    private Clip bangMedium;

    /** Sound for small asteroid being destroyed */
    private Clip bangSmall;

    /** Sound for ship being destroyed */
    private Clip bangShip;

    /** Sound for alien-ship being destroyed */
    private Clip bangAlienShip;

    /** Sound for big saucer */
    private Clip bigSaucer;

    /** Sound for small saucer */
    private Clip smallSaucer;

    /** Sound 1 */
    private Clip beat1;

    /** Sound 2 */
    private Clip beat2;

    /** Determines tempo of beats */
    private int longestBeat;

    /** timer for beats */
    private Timer soundSwitch;

    /**
     * Constructs a controller to coordinate the game and screen
     */
    public Controller (String version)
    {
        soundSwitch = new Timer(INITIAL_BEAT, this);
        longestBeat = INITIAL_BEAT;
        // create instance variables of sounds
        fire = createClip("/sounds/fire.wav");
        thrust = createClip("/sounds/thrust.wav");
        bangLarge = createClip("/sounds/bangLarge.wav");
        bangMedium = createClip("/sounds/bangMedium.wav");
        bangSmall = createClip("/sounds/bangSmall.wav");
        bangShip = createClip("/sounds/bangShip.wav");
        bangAlienShip = createClip("/sounds/bangAlienShip.wav");
        bigSaucer = createClip("/sounds/saucerBig.wav");
        smallSaucer = createClip("/sounds/saucerSmall.wav");
        beat1 = createClip("/sounds/beat1.wav");
        beat2 = createClip("/sounds/beat2.wav");

        shipList = new ArrayList<>();

        // if enhanced version requested, set enhanced to true
        gameMode = version;

        // TODO: make input on startup screen to pick 1 or two players
        // For now, enhanced mode will always be two player
        if (gameMode == "enhanced")
        {
            twoPlayerGame = true;
        }
        else
        {
            twoPlayerGame = false;
        }

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
        soundSwitch.start();
    }

    private Clip createClip (String string)
    {
        // Opening the sound file this way will work no matter how the
        // project is exported. The only restriction is that the
        // sound files must be stored in a package.
        try (BufferedInputStream sound = new BufferedInputStream(getClass().getResourceAsStream(string)))
        {
            // Create and return a Clip that will play a sound file. There are
            // various reasons that the creation attempt could fail. If it
            // fails, return null.
            Clip clip = AudioSystem.getClip();
            clip.open(AudioSystem.getAudioInputStream(sound));
            return clip;
        }
        catch (LineUnavailableException e)
        {
            return null;
        }
        catch (IOException e)
        {
            return null;
        }
        catch (UnsupportedAudioFileException e)
        {
            return null;
        }
    }

    /*
     * Returns a string representing the current game mode. Right now, can either be "classic" or "enhanced" but could
     * expand to more game modes in the future.
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
        soundSwitch.stop();
        playSound = false;
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
        // display.setLegend("");

        // if two player mode, place another ship
        if (twoPlayerGame)
        {
            ship.setColor(Color.GREEN); // in a 2 player game, ships need separate colors to be told apart

            Participant.expire(ship2);
            ship2 = new Ship(SIZE / 2, SIZE / 2, -Math.PI / 2, this);
            ship2.setColor(Color.CYAN);
            addParticipant(ship2);
            shipList.add(ship2);
            display.setLegend("");
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
        soundSwitch.stop();
        soundSwitch.setDelay(INITIAL_BEAT);
        playSound = true;
        longestBeat = INITIAL_BEAT;
        soundSwitch.start();
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
        // otherwise ship can start off moving or shooting in the next scene
        for (Ship s : shipList)
        {
            s.setAccelerating(false);
            s.setTurningLeft(false);
            s.setTurningRight(true);
            s.setShooting(false);
        }

        // Clear the screen
        // clear();

        // // Place asteroids
        // placeAsteroids();

        // TODO: Make additional asteroid for each level

        // Place the ship(s)
        placeShips();

    }

    /**
     * Goes to the next level yo
     */
    private void nextLevel ()
    {
        longestBeat = INITIAL_BEAT;
        soundSwitch.start();

        playSound = true;

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

        level++;

        // Display new level
        display.setLevel(level);

        // TODO: Make additional asteroid for each level.
        // each time randomizing position
        for (int i = level; i > 1; i--)
        {

            switch (new Random().nextInt(4))
            {
                case 0:
                    addParticipant(new Asteroid(0, 2, EDGE_OFFSET, EDGE_OFFSET, 3, this));
                    break;
                case 1:
                    addParticipant(new Asteroid(1, 2, (SIZE - EDGE_OFFSET), EDGE_OFFSET, 3, this));
                    break;
                case 2:
                    addParticipant(new Asteroid(1, 2, EDGE_OFFSET, SIZE - EDGE_OFFSET, 3, this));
                    break;
                case 3:
                    addParticipant(new Asteroid(1, 2, SIZE - EDGE_OFFSET, SIZE - EDGE_OFFSET, 3, this));
                    break;

            }
        }

        // TODO: Place alienShip
        // if (bigSaucer.isRunning())
        // {
        // bigSaucer.stop();
        // }
        // bigSaucer.setFramePosition(0);
        // bigSaucer.loop(10);

        // Place the ship(s)
        placeShips();

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
        soundSwitch.stop();
        playSound = false;
        if (bangShip.isRunning())
        {
            bangShip.stop();
        }
        bangShip.setFramePosition(1);
        bangShip.start();

        // remove the ship from shipList
        shipList.remove(s);

        // Null out the ship
        s = null;

        this.ship = null;

        // Decrement lives
        lives--;

        // Display lives
        display.setLives(lives);

        // Since the ship was destroyed, schedule a transition
        scheduleTransition(END_DELAY);
        soundSwitch.restart();

    }

    /**
     * An asteroid has been destroyed
     */
    public void asteroidDestroyed (int size)
    {
        // If all the asteroids are gone, schedule a transition
        if (countAsteroids() == 0)
        {
            soundSwitch.stop();
            scheduleTransition(END_DELAY);
        }
        // See if large asteroid has been destroyed
        if (size == 2)
        {
            if (bangLarge.isRunning())
            {
                bangLarge.stop();
            }
            bangLarge.setFramePosition(0);
            bangLarge.start();
        }
        // See if medium asteroid has been destroyed

        if (size == 1)
        {
            if (bangMedium.isRunning())
            {
                bangMedium.stop();
            }
            bangMedium.setFramePosition(0);
            bangMedium.start();
        }
        // Small asteroid has been destroyed
        else
        {
            if (bangSmall.isRunning())
            {
                bangSmall.stop();
            }
            bangSmall.setFramePosition(0);
            bangSmall.start();
        }

        // for 2 player mode, score is handled in the Asteroid class
        if (!twoPlayerGame)
        {
            score += ASTEROID_SCORE[size];
        }

        // Display new score
        display.setScore(score);

    }

    // TODO: implemented for score and sound
    public void alienShipDestroyed (int size)
    {
        if (bangAlienShip.isRunning())
        {
            bangAlienShip.stop();
        }
        bangAlienShip.setFramePosition(0);
        bangAlienShip.start();

        if (!twoPlayerGame)
        {
            score += ALIENSHIP_SCORE[size];
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
        if (soundSwitch == e.getSource())
        {
            if (ship != null)
            {
                longestBeat -= BEAT_DELTA;
                if (longestBeat < FASTEST_BEAT)
                {
                    longestBeat = FASTEST_BEAT;
                }
                soundSwitch.setDelay(longestBeat);
                if (playSound == true)
                {
                    beat1.setFramePosition(0);
                    beat1.start();

                }
                else
                {
                    beat2.setFramePosition(0);

                    beat2.start();
                }
                playSound = !playSound;
            }
        }
        if (e.getSource() instanceof JButton)
        {
            initialScreen();
        }

        // Time to refresh the screen and deal with keyboard input
        else if (e.getSource() == refreshTimer)
        {
            // It may be time to make a game transition
            performTransition();
            // Move the participants to their new locations
            pstate.moveParticipants();
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
                    thrust.setFramePosition(0);
                    thrust.loop(10);
                }
                if (s.shooting() && s != null)
                {
                    s.attack();
                }
                if (ship != null)
                {
                    s.applyFriction(SHIP_FRICTION);
                }
            }

            // // It may be time to make a game transition
            // performTransition();

            // // Move the participants to their new locations
            // pstate.moveParticipants();

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
                playSound = false;
                nextLevel();
            }
            if (ship == null && lives > 0)
            {
                placeShips();
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
            fire.setFramePosition(0);
            fire.loop(100);
            ship.setShooting(true);
        }
        if (e.getKeyCode() == KeyEvent.VK_UP && ship != null)
        {
            thrust.start();

            ship.setAccelerating(true);
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
                fire.loop(100);

                ship.setShooting(true);
            }
            else
            {
                fire.start();
                ship2.setShooting(true);
            }
        }
        if (e.getKeyCode() == KeyEvent.VK_SPACE && ship != null)
        {
            if (!twoPlayerGame)
            {
                fire.setFramePosition(0);
                fire.loop(100);
                ship.setShooting(true);
            }
        }

        if (e.getKeyCode() == KeyEvent.VK_W && ship != null)
        {
            if (!twoPlayerGame)
            {
                thrust.start();
                ship.setAccelerating(true);
            }
            else
            {
                thrust.start();
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
            thrust.stop();
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
                thrust.stop();
                ship.setAccelerating(false);
            }
            else
            {
                thrust.stop();
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
                fire.stop();
                ship.setShooting(false);
            }
            else
            {
                fire.stop();
                ship2.setShooting(false);
            }
        }
        if (e.getKeyCode() == KeyEvent.VK_SPACE && ship != null)
        {
            if (!twoPlayerGame)
            {
                fire.stop();
                ship.setShooting(false);
            }
        }
        if (e.getKeyCode() == KeyEvent.VK_DOWN && ship != null)
        {
            fire.stop();
            ship.setShooting(false);
        }
    }

}
