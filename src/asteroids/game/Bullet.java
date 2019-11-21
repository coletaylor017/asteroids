package asteroids.game;

import java.awt.Shape;
import asteroids.destroyers.*;

public class Bullet extends Participant implements AsteroidDestroyer
{
    /** Shape of bullet to detect collisions **/
    private Shape outline;

    /** Game controller **/
    private Controller controller;
    
    /*
     * Constructs a bullet at initial point (x, y), facing indicated direction,
     * with indicated initial speed. Specify game controller when constructing.
     */
    public Bullet (int x, int y, double direction, double initSpeed, Controller controller)
    {
        this.controller = controller;
        setPosition(x, y);
        setRotation(direction);
        setSpeed(initSpeed);
        
        
    }

    @Override
    protected Shape getOutline ()
    {
        return outline;
    }

    /*
     * When a bullet collides with a ShipDestroyer, it deletes itself
     */
    @Override
    public void collidedWith (Participant p)
    {
        if (p instanceof ShipDestroyer)
        {
            // delete bullet
            Participant.expire(this);
            
            // inform controller
            controller.bulletDestroyed();
        }
    }

}
