package asteroids.network;

import java.io.Serializable;
import asteroids.participants.Ship;
import asteroids.game.Constants;

/*
 * Defines a player in an online game. A player has a ship which they own,
 * a score, a number of lives that can vary, and an id.
 */
public class Player implements Serializable
{
    /**
     * Necessary to create a serializable object
     */
    private static final long serialVersionUID = 1L;

    /* ID that will be sent along with this player's actions to the server */
    private long id;
    
    /* Ship that this player owns */
    private Ship playerShip;
    
    /* Number of lives this player currently has */
    private int lives;
    
    /* The player's score */
    private int score;
    
    /*
     * Constructs a new player with the given ship.
     */
    public Player ()
    {
        // Generate a random ID
        int randomInt = Constants.RANDOM.nextInt(1000*1000);
        
        // Combine with current epoch to guarantee a unique ID for every player
        id = Long.parseLong((System.currentTimeMillis() + "") + randomInt);
        
        score = 0;
        lives = 3;
    }
    
    /*
     * Returns the player's ID.
     */
    public long getID ()
    {
        return id;
    }
    
    /*
     * Returns the Ship owned by this player.
     */
    public Ship getShip ()
    {
        return playerShip;
    }
    
    /*
     * Sets the ship that belongs to this player.
     */
    public void setShip (Ship ship)
    {
        playerShip = ship;
    }
    
    /*
     * Returns the player's score.
     */
    public int getScore ()
    {
        return score;
    }
    
    /*
     * Sets the player's score
     */
    public void setScore (int s)
    {
        score = s;
    }
    
    /*
     * Returns number of lives
     */
    public int getLives ()
    {
        return lives;
    }
    
    /*
     * Set number of lives
     */
    public void setLives (int l)
    {
        lives = l;
    }
}
