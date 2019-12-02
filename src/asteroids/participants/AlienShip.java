package asteroids.participants;

import java.awt.Shape;
import asteroids.destroyers.ShipDestroyer;
import asteroids.game.Participant;

public class AlienShip extends Participant implements ShipDestroyer
{

    @Override
    protected Shape getOutline ()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void collidedWith (Participant p)
    {
        // TODO Auto-generated method stub
        
    }
}
