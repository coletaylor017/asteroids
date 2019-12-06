package asteroids.game;

import static asteroids.game.Constants.*;
import static asteroids.network.NetworkConstants.*;
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
import asteroids.network.AsteroidsClient;
import asteroids.network.GameUpdate;
import asteroids.network.Player;
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

    /* When this timer goes off, an alien ship appears */
    private Timer alienShipTimer;

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

    /** The game display */
    private Display display;

    /** "classic" if the game is in enhanced mode, otherwise "enhanced" */
    private String gameMode;

    /* Specifies if a two player game is taking place on one machine. False for LAN games */
    private final boolean twoPlayerGame;

    /* Specifies whether beats should play */
    private boolean playSound;

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

    /* An way to iterate through all ships, INCLUDING this controller's user's ship */
    private ArrayList<Ship> shipList;

    /* The client that handle communication to and from the server */
    private AsteroidsClient client;

    /* A list to keep track of all remote players currently participating in an online game. */
    private ArrayList<Player> playerList;

    /* The player who uses this controller as their local representation of an online game */
    Player user;

    /* If this controller is the primary, it spawns asteroids for all the others. */
    boolean isPrimary;

    /* The ships that display the number of lvies left */
    private Ship[] livesShips;

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

        isPrimary = false;

        // initialize user
        user = new Player();
        System.out.println("Controller: new user id: " + user.getID());

        // initialize pstate
        pstate = new ParticipantState();

        // initialize client object
        client = aClient;

        shipList = new ArrayList<>();
        playerList = new ArrayList<>();

        // set game mode to "classic", "enhanced", or "online-multiplayer"
        gameMode = version;

        if (gameMode.equals("online-multiplayer"))
        {
            client.send(new GameUpdate(user, CONNECTIONESTABLISHED));
        }

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
        soundSwitch.start();

        // if in online multiplayer mode, let the server know a user has been added
        if (gameMode.equals("online-multiplayer"))
        {
            // Have the client request a current list of active players. The client will automatically call addPlayer()
            // for each one.
            client.send(new GameUpdate(user, GETPLAYERS));

            client.send(new GameUpdate(user, NEWPLAYER));
        }

        livesShips = new Ship[3];
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

    public void setPrimary (boolean p)
    {
        isPrimary = p;
    }

    public boolean isPrimary ()
    {
        return isPrimary;
    }

    /*
     * Returns user.
     */
    public Player getUser ()
    {
        return user;
    }

    /*
     * Returns the main ship.
     */
    public Ship getShip ()
    {
        return ship;
    }

    /*
     * Returns lives
     */
    public int getLives ()
    {
        return lives;
    }

    /*
     * Adds a new player to the game and give them a ship. This method is called from the AsteroidsClient when the
     * server informs it that a new player has joined the game.
     */
    public void addPlayer (Player plr)
    {
        playerList.add(plr);
        Ship newShip = new Ship(SIZE / 2, SIZE / 2, -Math.PI / 2, plr, this);
        newShip.setGhostStatus(true);
        shipList.add(newShip);
        addParticipant(newShip);
        plr.setShip(newShip);
    }

    /*
     * Removes the specified player and their ship from the game.
     */
    public void removePlayer (Player plr)
    {
        playerList.remove(plr);
        shipList.remove(plr.getShip());
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
        display.setScore("");
        display.setLevel("");
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
     * Place all ships in the center of the screen.
     */
    private void placeShips ()
    {
        display.setLegend("");
        // Place a new ship
        Participant.expire(ship);
        ship = new Ship(SIZE / 2, SIZE / 2, -Math.PI / 2, null, this);
        addParticipant(ship);

        shipList.add(ship);
        // display.setLegend("");

        if (!gameMode.equals("online-multiplayer"))
        {
            // Place a new ship.
            Participant.expire(ship);
            ship = new Ship(SIZE / 2, SIZE / 2, -Math.PI / 2, user, this);
            addParticipant(ship);
            shipList.add(ship);

            // if two player mode, place another ship
            if (gameMode.equals("enhanced"))
            {
                ship.setColor(Color.GREEN); // in a 2 player game, ships need separate colors to be told apart
                Participant.expire(ship2);
                ship2 = new Ship(SIZE / 2, SIZE / 2, -Math.PI / 2, null, this);
                ship2.setColor(Color.CYAN);
                addParticipant(ship2);
                shipList.add(ship2);
            }
        }
        else if (gameMode.equals("online-multiplayer"))
        {
            // if in an online multiplayer game, expire all ships
            for (Ship s : shipList)
            {
                Participant.expire(s);
            }

            shipList.clear();

            // Place the user's ship
            ship = new Ship(SIZE / 2, SIZE / 2, -Math.PI / 2, user, this);
            addParticipant(ship);
            shipList.add(ship);
            ship.setColor(Color.RED);
            user.setShip(ship);

            // Place new ships for all other players
            for (Player p : playerList)
            {
                Ship s = new Ship(SIZE / 2, SIZE / 2, -Math.PI / 2, p, this);
                s.setGhostStatus(true);
                addParticipant(s);
                shipList.add(s);
                p.setShip(s);
            }
        }
    }

    /**
     * Places an asteroid near one corner of the screen. Gives it a random velocity and rotation.
     */
    private void placeAsteroids ()
    {
        // If not online, just place asteroids. If online, check that this controller is set up as the primary
        if (!gameMode.equals("online-multiplayer") || (gameMode.equals("online-multiplayer") && isPrimary))
        {
            // Place four asteroids near the corners of the screen.
            // TOP LEFT
            addParticipant(new Asteroid(0, 2, (EDGE_OFFSET / 2) + RANDOM.nextInt(EDGE_OFFSET),
                    (EDGE_OFFSET / 2) + RANDOM.nextInt(EDGE_OFFSET), this));
            // TOP RIGHT
            addParticipant(new Asteroid(1, 2, SIZE - (EDGE_OFFSET / 2) - RANDOM.nextInt(EDGE_OFFSET),
                    (EDGE_OFFSET / 2) + RANDOM.nextInt(EDGE_OFFSET), this));
            // BOTTOM LEFT
            addParticipant(new Asteroid(1, 2, (EDGE_OFFSET / 2) + RANDOM.nextInt(EDGE_OFFSET),
                    SIZE - (EDGE_OFFSET / 2) - RANDOM.nextInt(EDGE_OFFSET), this));
            // BOTTOM RIGHT
            addParticipant(new Asteroid(1, 2, SIZE - (EDGE_OFFSET / 2) - RANDOM.nextInt(EDGE_OFFSET),
                    SIZE - (EDGE_OFFSET / 2) - RANDOM.nextInt(EDGE_OFFSET), this));
        }
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
    public void initialScreen ()
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

        // set lives, level, score
        lives = 3;
        level = 1;
        score = 0;

        // Place the ship, or ships if it's a two player game
        placeShips();

        // Add the ships for displaying lives
        for (int i = 0; i < lives; i++)
        {
            Ship s = new Ship(40 + 30 * i, LABEL_VERTICAL_OFFSET + 30, -Math.PI / 2, null, this);
            s.setInert(true);
            addParticipant(s);
            livesShips[i] = s;
        }

        // Display Level
        display.setLevel(level + "");
        // Display Score
        display.setScore(score + "");

        // Start listening to events (but don't listen twice)
        display.removeKeyListener(this);
        display.addKeyListener(this);

        // Give focus to the game screen
        display.requestFocusInWindow();
    }

    /**
     * Restarts the current level
     */
    public void restartLevel ()
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

        // in an online game, asteroid spawning is handled by the client to keep it uniform between players
        if (gameMode != "online-multiplayer")
        {
            // Place asteroids
            placeAsteroids();
        }

        // Place the ship(s)
        placeShips();

    }

    /**
     * Goes to the next level yo
     */
    public void nextLevel ()
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

        // Add the ships for displaying lives
        for (int i = 0; i < lives; i++)
        {
            Ship s = new Ship(40 + 30 * i, LABEL_VERTICAL_OFFSET + 30, -Math.PI / 2, null, this);
            s.setInert(true);
            addParticipant(s);
            livesShips[i] = s;
        }

        // in an online game, asteroid spawning is handled by the client to keep it uniform between players
        if (gameMode != "online-multiplayer")
        {
            // Place asteroids
            placeAsteroids();
        }

        level++;

        // Display new level
        display.setLevel(level + "");

        if (level > 1)
        {
            // Set the alien ship timer to a random time 5-10 seconds
            alienShipTimer = new Timer(RANDOM.nextInt(5000) + 5000, this);
            alienShipTimer.start();
        }

        // TODO: Make additional asteroid for each level.
        // each time randomizing position
        for (int i = level; i > 1; i--)
        {

            switch (new Random().nextInt(4))
            {
                case 0:
                    addParticipant(new Asteroid(0, 2, EDGE_OFFSET, EDGE_OFFSET, this));
                    break;
                case 1:
                    addParticipant(new Asteroid(1, 2, (SIZE - EDGE_OFFSET), EDGE_OFFSET, this));
                    break;
                case 2:
                    addParticipant(new Asteroid(1, 2, EDGE_OFFSET, SIZE - EDGE_OFFSET, this));
                    break;
                case 3:
                    addParticipant(new Asteroid(1, 2, SIZE - EDGE_OFFSET, SIZE - EDGE_OFFSET, this));
                    break;

            }
        }

        // TODO: Place alienShip

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
        fire.stop();
        thrust.stop();
        soundSwitch.stop();
        playSound = false;
        if (bangShip.isRunning())
        {
            bangShip.stop();
        }
        bangShip.setFramePosition(1);
        bangShip.start();

        if (gameMode.equals("online-multiplayer"))
        {
            client.send(new GameUpdate(user, SHIPDIE));
        }

        // remove the ship from shipList
        shipList.remove(s);

        // Null out the ship
        s = null;

        this.ship = null;

        // Decrement lives
        lives--;

        // Display lives
        Participant.expire(livesShips[lives]);

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
        display.setScore(score + "");

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
        display.setScore(score + "");

        if (countAsteroids() == 0)
        {
            soundSwitch.stop();
            scheduleTransition(END_DELAY);
        }
        else
        {
            // Reset and start the alien ship timer
            alienShipTimer = new Timer(RANDOM.nextInt(5000) + 5000, this);
            alienShipTimer.start();
        }
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
        else if (e.getSource() instanceof JButton)
        {
            if (e.getActionCommand().equals(START_LABEL))
            {
                initialScreen();
                if (gameMode.equals("online-multiplayer"))
                {
                    client.send(new GameUpdate(user, NEWGAME));
                }
            }
            else if (e.getActionCommand().equals("Kill client"))
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
                }
                if (s.shooting() && s != null)
                {
                    s.attack();
                }
                if (s != null)
                {
                    // if ship is moving, send its latest location to the server
                    if (gameMode.equals("online-multiplayer"))
                    {
                        if (s.getSpeed() > 0.000000000001)
                        {
                            client.send(new GameUpdate(user, SHIPMOVE, s.getX(), s.getY(), s.getRotation()));
                        }
                        if (s.shooting())
                        {
                            client.send(new GameUpdate(user, SHIPFIRE));
                        }
                    }
                    s.applyFriction(SHIP_FRICTION);
                }
            }

            // erase ships that are just there to display lives

            // Refresh screen
            display.refresh();
        }
        else if (e.getSource() == alienShipTimer)
        {
            // Time to spawn a new alien ship!
            if (level == 2)
            {
                // Spawn a new medium alien ship on edge of screen
                AlienShip alien = new AlienShip(SIZE / 2, SIZE - 50, 1, this);
                addParticipant(alien);
                if (bigSaucer.isRunning())
                {
                    bigSaucer.stop();
                }
                bigSaucer.setFramePosition(0);
                bigSaucer.loop(4);
            }

            else if (level >= 3)
            {
                // Spawn a small alien ship on edge of screen
                AlienShip alien = new AlienShip(SIZE / 2, SIZE - 50, 0, this);
                addParticipant(alien);
                if (smallSaucer.isRunning())
                {
                    smallSaucer.stop();
                }
                smallSaucer.setFramePosition(0);
                smallSaucer.loop(4);
            }

            // Stop the alien ship timer. It will be restarted when the alien dies
            alienShipTimer.stop();
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

            if (countAsteroids() == 0 && countAlienShip() == 0)
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

    private int countAlienShip ()
    {
        int count = 0;
        for (Participant p : this)
        {
            if (p instanceof AlienShip)
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
            thrust.setFramePosition(0);
            thrust.loop(10);
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
                fire.setFramePosition(0);
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
                ship.setShooting(true);
            }
        }

        if (e.getKeyCode() == KeyEvent.VK_W && ship != null)
        {
            if (!twoPlayerGame)
            {
                thrust.setFramePosition(0);
                thrust.loop(10);
                ship.setAccelerating(true);
            }
            else
            {
                thrust.setFramePosition(0);
                thrust.loop(10);
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
