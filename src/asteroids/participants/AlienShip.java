package asteroids.participants;

import java.awt.Shape;
import asteroids.destroyers.*;
import asteroids.game.Controller;
import asteroids.game.Participant;

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
    public AlienShip (int size, Controller controller)
    {
        this.controller = controller;
        this.size = size;
    }

    @Override
    protected Shape getOutline ()
    {
        return outline;
    }

    @Override
    public void collidedWith (Participant p)
    {
        if (p instanceof AsteroidDestroyer)
        {
            // expire the ship
            Participant.expire(this);
            
            // let the controller know
            controller.alienShipDestroyed(size);
        }
    }
}
