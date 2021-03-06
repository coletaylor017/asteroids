package asteroids.network;

import java.awt.Shape;
import java.io.Serializable;

/*
 * A GameUpdate is an object sent between client and server and vice versa.
 * It represents changes in game state. It consists of an operation code,
 * which represents what has just happened in the latest state of the game,
 * and an ID that indicates which player performed the action.
 * Each code has a number of associated properties. For example, for the 
 * 'SHIPMOVE' code, an x and a y are sent along with it. For the BULLETSPAWN
 * code, an x and y are also sent. For SHIPDIE, there are no properties needed.
 * Valid codes are (thus far):
 * -SHIPMOVE (x, y, rotation)
 * -SHIPDIE
 * -SHIPFIRE
 * -BULLETMOVE (x, y, rotation)
 * -BULLETDIE
 * -ASTEROIDSPAWN (x, y)
 * -ASTEROIDDIE
 * -And a few more you can see down below
 * 
 */
public class GameUpdate implements Serializable
{
    /**
     * Necessary to create a serializable object
     */
    private static final long serialVersionUID = 1735716747784848307L;

    /* The operation code for the update, explained below */
    private String operationCode;
    
    /* The player that performed the action */
    private Player originPlayer;
    
    /* Some operations have an associated x coordinate */
    private double x;
    
    /* Some operations have an associated y coordinate */
    private double y;
    
    /* Some operations have an associated rotation value */
    private double rotation;
    
    /* Some operations even have an associated speed! */
    private double speed;
    
    private double direction;
    
    private int size;
    
    private Shape outline;
    
    private long id;
    
    /*
     * Constructs a new GameUpdate with only a code and no additional parameters.
     * Appropriate to use for the following codes:
     * -SHIPDIE
     * -ASTEROIDDIE
     * -NEXTLEVEL
     * -RESTARTLEVEL
     * -SHIPSPAWN
     */
    public GameUpdate (Player p, String operationCode)
    {
        originPlayer = p; // TODO: change to an ID for faster messages?
        this.operationCode = operationCode;
    }
    
    /*
     * Constructs a new GameUpdate with a code and two parameters: x and y.
     * Appropriate to use for the following codes:
     * -ASTEROIDSPAWN
     */
    public GameUpdate(Player p, String operationCode, double x, double y)
    {
        originPlayer = p; // TODO: change to an ID for faster messages?
        this.operationCode = operationCode;
        this.x = x;
        this.y = y;
    }
    
    /*
     * Constructs a new GameUpdate with a code and three parameters: x, y, and rotation.
     * Appropriate to use for the following codes:
     * -SHIPMOVE
     */
    public GameUpdate(Player p, String operationCode, double x, double y, double rotation)
    {
        originPlayer = p; // TODO: change to an ID for faster messages?
        this.operationCode = operationCode;
        this.x = x;
        this.y = y;
        this.rotation = rotation;
    }
    
    /*
     * Constructs a new GameUpdate with a code and seven parameters.
     * Appropriate to use for the following codes:
     * -ASTEROIDSPAWN
     */
    public GameUpdate(Player p, String operationCode, long id, int size, Shape outline, double x, double y, double rotation, double speed, double direction)
    {
        originPlayer = p; // TODO: change to an ID for faster messages?
        this.operationCode = operationCode;
        this.id = id;
        this.x = x;
        this.y = y;
        this.rotation = rotation;
        this.speed = speed;
        this.outline = outline;
        this.direction = direction;
        this.size = size;
    }
    
    /*
     * Turns this game update into a code represented by a String, making it much
     * faster to send over an internet connection than the object it starts out as.
     * 
     * The string is comprised of the following chunks, separated by the tilde (`)
     * character:
     * -a 1-digit type indicator, so this string is only as long as it needs to be.
     *  0 means this gameUpdate only has two fields, i.e. it was constructed using
     *  the first constructor above. 1 means the second constructor, 2 the third, 
     *  and 3 the fourth.
     *  
     * The string also consists of the following, in the following order:
     * 
     * For types 0, 1, 2, and 3:
     * -a 17-digit player ID
     * -a 1-letter operation code
     * For types 1, 2, and 3:
     * -double for X
     * -double for Y
     * For types 2 and 3:
     * -double for rotation
     * For type 3:
     * -A double for speed
     * -A double for direction
     * -An int for size
     * -An int for variety
     */
    public String encode ()
    {
        return "";
    }
    
    public String getOperationCode ()
    {
        return operationCode;
    }
    
    public Player getPlayer ()
    {
        return originPlayer;
    }
    
    public long getID ()
    {
        return id;
    }
    
    public double getX ()
    {
        return x;
    }
    
    public double getY ()
    {
        return y;
    }
    
    public double getRotation ()
    {
        return rotation;
    }
    
    public double getSpeed ()
    {
        return speed;
    }
    
    public int getSize ()
    {
        return size;
    }
    
    public double getDirection ()
    {
        return direction;
    }
    
    public Shape getOutline ()
    {
        return outline;
    }
}
