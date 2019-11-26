package asteroids.participants;

import java.awt.Shape;
import java.awt.geom.Line2D;
import asteroids.destroyers.*;
import asteroids.game.Controller;
import asteroids.game.Participant;

public class Particle extends Participant
{
    
    private Shape outline;
    
    private Controller controller;
    
    /* The type of debris: debris or dust, potentially more in the future */
    private String type;
    
    /*
     * Construct  new Particle object at (x, y). A Particle is a Participant that floats
     * with a set velocity, has a limited life span, and doesn't have collision behavior.
     * The length of a dust particle should be set to 1, while the length of a debris particle
     * will be longer.
     */
    public Particle (double x, double y, double length, double direction, double speed, Controller controller)
    {
        this.controller = controller;
        setPosition(x, y);
        setVelocity(speed, direction);
        
        Path2D.Double poly = new Path2D.Double();
    }
    
    @Override
    protected Shape getOutline ()
    {
        return null;
    }

    @Override
    public void collidedWith (Participant p)
    {
        // nothing happens
    }
    
    

}
