package asteroids.participants;

import java.awt.Shape;
import java.awt.geom.Path2D;
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
     * Construct  new Particle object. A Particle is a Participant that floats
     * with a set velocity, has a limited life span, and doesn't have collision behavior.
     */
    public Particle (double x, double y, double direction, double speed, Controller controller)
    {
        this.controller = controller;
        setPosition(x, y);
        setVelocity(speed, direction);
        
        Path2D.Double poly = new Path2D.Double();
        poly.moveTo(1, 1);
        poly.lineTo(1, 0);
        poly.lineTo(0, 0);
        poly.lineTo(0, 1);
        poly.closePath();
        
        outline = poly;
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
