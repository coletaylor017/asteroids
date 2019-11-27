package asteroids.participants;

import static asteroids.game.Constants.*;
import java.awt.Shape;
import java.awt.geom.*;
import asteroids.destroyers.*;
import asteroids.game.Controller;
import asteroids.game.Participant;
import asteroids.game.ParticipantCountdownTimer;


/**
 * Represents ships
 */
public class Ship extends Participant implements AsteroidDestroyer
{
    /** The outline of the ship */
    private Shape outline;
    
    /** Outline of the shape of rocket with flame */
    private Shape wFlame;
    
    /** determines whether accelerate() is active,
     * if so   */
    private boolean hasFlame;
    

    /** Game controller */
    private Controller controller;

    /**
     * Constructs a ship at the specified coordinates that is pointed in the given direction.
     */
    public Ship (int x, int y, double direction, Controller controller)
    {
        this.controller = controller;
        setPosition(x, y);
        setRotation(direction);

        Path2D.Double shipWFlame = new Path2D.Double();
        shipWFlame.moveTo(21, 0);
        shipWFlame.lineTo(-21, 12);
        shipWFlame.lineTo(-14, 10);
        shipWFlame.lineTo(-30, 0);
        shipWFlame.lineTo(-14, -10);
        shipWFlame.lineTo(-14, 10);
        shipWFlame.lineTo(-14, -10);
        shipWFlame.lineTo(-21, -12);
        shipWFlame.closePath();
        
        wFlame =shipWFlame;
        
        
        

        Path2D.Double poly = new Path2D.Double();
        poly.moveTo(21, 0);
        poly.lineTo(-21, 12);
        poly.lineTo(-14, 10);
        poly.lineTo(-14, -10);
        poly.lineTo(-21, -12);
        poly.closePath();

        outline = poly;
      
      
          

        // Schedule an acceleration in two seconds
        // new ParticipantCountdownTimer(this, "move", 2000);
    }

    /**
     * Returns the x-coordinate of the point on the screen where the ship's nose is located.
     */
    public double getXNose ()
    {
        Point2D.Double point = new Point2D.Double(20, 0);
        transformPoint(point);
        return point.getX();
    }

    /**
     * Returns the x-coordinate of the point on the screen where the ship's nose is located.
     */
    public double getYNose ()
    {
        Point2D.Double point = new Point2D.Double(20, 0);
        transformPoint(point);
        return point.getY();
    }

    @Override
    protected Shape getOutline ()
    {
        if (hasFlame==true && controller.getGameMode() == "classic") // in advanced, the flame will be particles instead
        {
            return wFlame;
        }
        return outline;
    }

    /**
     * Customizes the base move method by imposing friction
     */
    @Override
    public void move ()
    {
        applyFriction(SHIP_FRICTION);
        super.move();
    }

    /**
     * Turns right by Pi/16 radians
     */
    public void turnRight ()
    {
        rotate(Math.PI / 16);
    }

    /**
     * Turns left by Pi/16 radians
     */
    public void turnLeft ()
    {
        rotate(-Math.PI / 16);
    }

    /**
     * Accelerates by SHIP_ACCELERATION
     * sets hasFlame to true
     */
    public void accelerate ()
    {
        accelerate(SHIP_ACCELERATION);
        hasFlame=true;
        
        // add some little exhaust particles
        if (controller.getGameMode() == "enhanced")
        {
            for (int i = 0; i < 2; i++)
            {
                double offset = -5 + RANDOM.nextInt(10);
                Line2D.Double blastParticleShape = new Line2D.Double(-10, offset, -11, offset);
                int duration = RANDOM.nextInt(250) + 500; // randomize each particle's lifespan
                controller.addParticipant(new Particle(this.getX(), this.getY(), 1, this.getRotation() - Math.PI, this.getRotation(), blastParticleShape, duration, controller));
            }
        }
        
    }
    
    /** Removes flame */
    public void turnFlameOff() 
    {
        hasFlame=false;
    }
     

    /**
     * When a Ship collides with a ShipDestroyer, it expires
     */
    @Override
    public void collidedWith (Participant p)
    {
        if (p instanceof ShipDestroyer)
        {
            // spawn debris particles
            controller.addParticipant(new DestructionParticle(this.getX(), this.getY(), 30, controller));
            controller.addParticipant(new DestructionParticle(this.getX(), this.getY(), 15, controller));
            controller.addParticipant(new DestructionParticle(this.getX(), this.getY(), 30, controller));
            controller.addParticipant(new DestructionParticle(this.getX(), this.getY(), 7, controller));

            // Expire the ship from the game
            Participant.expire(this);

            // Tell the controller the ship was destroyed
            controller.shipDestroyed();
        }
    }

    /**
     * This method is invoked when a ParticipantCountdownTimer completes its countdown.
     */
    @Override
    public void countdownComplete (Object payload)
    {
        // Give a burst of acceleration, then schedule another
        // burst for 200 msecs from now.
        if (payload.equals("move"))
        {
            accelerate();
            new ParticipantCountdownTimer(this, "move", 200);
        }
    }
}
