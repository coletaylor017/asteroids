package asteroids.network;

import java.io.Serializable;

/*
 * A GameUpdate is an object sent between client and server and vice versa.
 * It represents changes in game state. It consists of an operation code,
 * which represents what has just happened in the latest state of the game.
 * Each code has a number of associated properties. For example, for the 
 * 'SHIPMOVE' code, an x and a y are sent along with it. For the BULLETSPAWN
 * code, an x and y are also sent. For SHIPDIE, there are no properties needed.
 * Valid codes are (thus far):
 * -SHIPSPAWN (x, y)
 * -SHIPMOVE (x, y, rotation)
 * -SHIPDIE
 * -BULLETSPAWN (x, y)
 * -BULLETMOVE (x, y, rotation)
 * -BULLETDIE
 * -ASTEROIDSPAWN (x, y)
 * -ASTEROIDMOVE (x, y)
 * -ASTEROIDDIE
 * -And a few more you can see down below
 * 
 */
public class GameUpdate implements Serializable
{
    /**
     * 
     */
    private static final long serialVersionUID = 1735716747784848307L;

    /* The operation code for the update, explained below */
    private String operationCode;
    
    /* Some operations have an associated x coordinate */
    private double x;
    
    /* Some operations have an associated y coordinate */
    private double y;
    
    /* Some operations have an associated rotation value */
    private double rotation;
    
    /*
     * Constructs a new GameUpdate with only a code and no additional parameters.
     * Appropriate to use for the following codes:
     * -SHIPDIE
     * -BULLETDIE
     * -ASTEROIDDIE
     * -STOPSERVER
     * -NEXTLEVEL
     * -RESTARTLEVEL
     */
    public GameUpdate (String operationCode)
    {
        this.operationCode = operationCode;
    }
    
    /*
     * Constructs a new GameUpdate with a code and two parameters: x and y.
     * Appropriate to use for the following codes:
     * -SHIPSPAWN
     * -BULLETSPAWN
     * -ASTEROIDSPAWN
     * -ASTEROIDMOVE
     */
    public GameUpdate(String operationCode, double x, double y)
    {
        this.operationCode = operationCode;
        this.x = x;
        this.y = y;
    }
    
    /*
     * Constructs a new GameUpdate with a code and three parameters: x, y, and rotation.
     * Appropriate to use for the following codes:
     * -SHIPMOVE
     * -BULLETMOVE
     */
    public GameUpdate(String operationCode, double x, double y, double rotation)
    {
        this.operationCode = operationCode;
        this.x = x;
        this.y = y;
        this.rotation = rotation;
    }
    
    public String getOperationCode ()
    {
        return operationCode;
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
}
