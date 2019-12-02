package asteroids.participants;

import static asteroids.game.Constants.*;
import java.awt.Color;
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
//    private boolean hasFlame;
    
    /* Keeps track of if this ship is currently accelerating */
    private boolean accelerating;
    
    /* Keeps track of if this ship is currently shooting */
    private boolean shooting;
    
    /* Keeps track of if this ship is currently turning left */
    private boolean turningLeft;
    
    /* Keeps trak of if this ship is currently turning right */
    private boolean turningRight;
    
    /* Number of bullets onscreen that were fired by this particular ship */
    private int numBullets;
    
    /* The score of the player who controls this ship */
    private int score;
    
    /* Lives left of the player who controls this ship */
    private int lives;

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
        
        numBullets = 0;
        score = 0;
        lives = 0;
        

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
    }
    
    /* Get score of this ship */
    public int getScore()
    {
        return score;
    }
    
    /* Get lives of this ship */
    public int getLives()
    {
        return lives;
    }
    
    /* set this player's score */
    public void setScore(int newScore)
    {
        score = newScore;
    }
    
    /* set this player's lives */
    public void setLives(int newLives)
    {
        score = newLives;
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
        if (accelerating && controller.getGameMode() == "classic") // in advanced, the flame will be particles instead
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
    
    // SHIP STATE SETTERS 
    public void setTurningLeft (boolean state)
    {
        turningLeft = state;
    }
    
    public void setTurningRight (boolean state)
    {
        turningRight = state;
    }
    
    public void setAccelerating (boolean state)
    {
        accelerating = state;
    }
    
    public void setShooting (boolean shootingOn)
    {
        shooting = shootingOn;
    }
    
    // SHIP STATE GETTERS
    
    public boolean turningLeft ()
    {
        return turningLeft;
    }
    
    public boolean turningRight ()
    {
        return turningRight;
    }
    
    public boolean accelerating ()
    {
        return accelerating;
    }
    
    public boolean shooting ()
    {
        return shooting;
    }

    /**
     * Accelerates by SHIP_ACCELERATION
     * sets hasFlame to true
     */
    public void accelerate ()
    {
        accelerate(SHIP_ACCELERATION);
        
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
    
    /*
     * Sends a bullet from the this ship if there are fewer than eight bullets onscreen that belong to this ship.
     */
    public void attack ()
    {
        if (numBullets <= BULLET_LIMIT)
        {
            Bullet bullet = new Bullet(this.getXNose(), this.getYNose(), this.getRotation(), BULLET_SPEED, this);
            bullet.setColor(this.getColor()); // so that players can identify their own bullets
            controller.addParticipant(bullet);
            numBullets++;
        }
    }
    
    /*
     * Called when a bullet is destroyed.
     */
    public void bulletDestroyed ()
    {
        numBullets--;
    }
     
    /**
     * When a Ship collides with a ShipDestroyer, it expires
     */
    @Override
    public void collidedWith (Participant p)
    {
        if (p instanceof ShipDestroyer)
        {
            //spawn debris particles
            int[] debrisLengths = {7, 15, 30, 30};
            int i = 0;
            while (i < debrisLengths.length)
            {
                DestructionParticle d = new DestructionParticle(this.getX(), this.getY(), debrisLengths[i], controller);
                d.setColor(this.getColor()); // set debris color to that of this ship 
                controller.addParticipant(d);
                i++;
            }

            // Expire the ship from the game
            Participant.expire(this);
           

            // Tell the controller that this ship was destroyed
            controller.shipDestroyed(this);
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
