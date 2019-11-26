package asteroids.participants;

import java.awt.Shape;
import java.awt.geom.Line2D;
import asteroids.game.Controller;
import asteroids.game.Participant;
import asteroids.game.ParticipantCountdownTimer;

public class Particle extends Participant
{
    
    private Shape outline;
    
    private Controller controller;
    
    /* The length of the particle */
    private double length;
    
    /*
     * Construct  new Particle object at (x, y). A Particle is a Participant that floats
     * with a set velocity, has a limited life span, and doesn't have collision behavior.
     * The length of a dust particle should be set to 1, while the length of a debris particle
     * will be longer.
     * 
     * The Particle object leaves speed, direction, and lifespan open to input. 
     * This will allow it to be used later for things like ship thrust particles
     * and not just debris and dust.
     * 
     * For dust or debris, use a DestructionParticle object.
     */
    public Particle (double x, double y, double speed, double direction, double length, int lifespan, Controller controller)
    {
        
        this.controller = controller;
        setPosition(x, y);
        setVelocity(speed, direction);
        
        Line2D.Double line = new Line2D.Double(0, 0, 0, length);
        outline = line;
        
        new ParticipantCountdownTimer(this, lifespan);
    }
    
    @Override
    protected Shape getOutline ()
    {
        return outline;
    }
    
    /*
     * When time has elapsed, delete particle
     */
    @Override
    public void countdownComplete (Object payload)
    {
        Participant.expire(this);
    }

    @Override
    public void collidedWith (Participant p)
    {
        // nothing happens
    }
    
}
