package asteroids.participants;

import static asteroids.game.Constants.ALIENSHIP_SCALE;
import static asteroids.game.Constants.BULLET_SPEED;
import static asteroids.game.Constants.RANDOM;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import asteroids.destroyers.*;
import asteroids.game.Controller;
import asteroids.game.Participant;
import asteroids.game.ParticipantCountdownTimer;

public class AlienShip extends Participant implements ShipDestroyer
{

    /* the shape for this ship's outline */
    private Shape outline;

    /* The controller this ship interacts with */
    private Controller controller;

    /* The size of this ship, 0 for small, 1 for medium */
    private int size;

    /*
     * Constructs a new alien ship for the given controller.
     */
    public AlienShip (int x, int y, int size, Controller controller)
    {
        this.controller = controller;
        this.size = size;
        setPosition(x, y);
        setRotation(0);
        
        // When this timer goes off, alien ship attacks
        new ParticipantCountdownTimer(this, 2000);

        // build outline
        Path2D.Double p = new Path2D.Double();
        // start at leftmost point
        p.moveTo(-14, 0);
        p.lineTo(-7, -5);
        p.lineTo(-4, -10);
        p.lineTo(4, -10);
        p.lineTo(7, -5);
        p.lineTo(-7, -5);
        p.lineTo(7, -5);
        p.lineTo(14, 0);
        // bottom part
        p.lineTo(7, 5);
        p.lineTo(-7, 5);
        p.lineTo(-14, 0);
        p.lineTo(14, 0);
        p.closePath();
        
        // scale to match size
        double scale = ALIENSHIP_SCALE[size];
        p.transform(AffineTransform.getScaleInstance(scale, scale));
        outline = p;
    }
    
    /*
     * Fire a bullet in the specified direction.
     */
    public void attack (double direction)
    {
        AlienBullet bullet = new AlienBullet(this.getX(), this.getY(), direction, BULLET_SPEED, controller);
        bullet.setGhostStatus(this.isGhost()); // set ghost status to match ship
        controller.addParticipant(bullet);
    }

    @Override
    protected Shape getOutline ()
    {
        return outline;
    }

    @Override
    public void collidedWith (Participant p)
    {
        // If it's the ship's own bullet, do nothing
        if (!(p instanceof AlienBullet))
        {
            // expire the ship
            Participant.expire(this);

            // let the controller know
            controller.alienShipDestroyed(size);
        }
    }

    /*
     * Runs when the attack timer runs out
     */
    @Override
    public void countdownComplete (Object obj)
    {
        if (size == 1)
        {
            // attack in a random direction
            attack(2 * Math.PI * RANDOM.nextDouble());
        }
        else if (size == 0)
        {
            // attack in general direction of ship
            double deltaX = controller.getShip().getX() - this.getX();
            double deltaY = controller.getShip().getY() - this.getY();
            double direction = Math.atan2(deltaY, deltaX);
            // offset +/- 5 degrees. 5 degrees = pi/36 radians = 0.087266 radians
            double offset = (0.087266) - (RANDOM.nextDouble() * 0.087266);
            attack(direction + offset);
        }
        
        // Restart the timer
        new ParticipantCountdownTimer(this, 2000);
    }
}
