package asteroids.participants;

import static asteroids.game.Constants.BULLET_DURATION;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import asteroids.destroyers.*;
import asteroids.game.Controller;
import asteroids.game.Participant;
import asteroids.game.ParticipantCountdownTimer;
import asteroids.network.GameUpdate;

public class Bullet extends Participant implements AsteroidDestroyer
{
    /** Shape of bullet to detect collisions **/
    private Shape outline;

    /** The Ship that fired this bullet **/
    private Ship owner;
    
    /* Controller for this bullet */
    private Controller controller;
    
    /*
     * Constructs a bullet at initial point (x, y), facing indicated direction,
     * with indicated initial speed. Specify game controller when constructing.
     */
    public Bullet (double x, double y, double direction, double initSpeed, Ship owner, Controller controller)
    {
        this.owner = owner;
        setPosition(x, y);
        setVelocity(initSpeed, direction);
        
        Ellipse2D.Double el = new Ellipse2D.Double(0, 0, 1, 1);
        outline = el;
        
        // To make the bullet expire after the correct duration
        new ParticipantCountdownTimer(this, BULLET_DURATION);
    }
    
    /* Returns the ship that this bullet eminated from */
    public Ship getOwner()
    {
        return owner;
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
        owner.bulletDestroyed();
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
            owner.bulletDestroyed();
        }
    }
    
    /**
     * Customizes the base move method by sending move data to the server
     */
    @Override
    public void move ()
    {
        // yeah, changes need to be made so the server can identify which bullet is moving
//        controller.getClient().send(new GameUpdate("BULLETMOVE", this.getX(), this.getY(), this.getRotation()));
        super.move();
    }
}
