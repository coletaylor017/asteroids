package asteroids.game;

import java.awt.Shape;
import asteroids.destroyers.*;

public class Bullet extends Participant implements AsteroidDestroyer
{
    /** Shape of bullet to detect collisions **/
    private Shape outline;
    
    /** Game controller **/
    private Controller controller;

    @Override
    protected Shape getOutline ()
    {
        return outline;
    }

    @Override
    public void collidedWith (Participant p)
    {
        
        
    }

}
