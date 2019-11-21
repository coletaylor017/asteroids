package asteroids.participants;

import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import asteroids.destroyers.*;
import asteroids.game.Controller;
import asteroids.game.Participant;

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
    public Bullet (double x, double y, double direction, double initSpeed, Controller controller)
    {
        this.controller = controller;
        setPosition(x, y);
        setVelocity(initSpeed, direction);
        
//        Ellipse2D.Double el = new Ellipse2D.Double(10, 10, 0, 0);
//        outline = el;
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
        return outline;
    }
    
    /*
     * When time has elapsed, delete bullet
     */
    @Override
    public void countdownComplete (Object payload)
    {
        controller.bulletDestroyed();
        Participant.expire(this);
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
