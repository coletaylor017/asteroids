package asteroids.participants;

import java.awt.Shape;
import java.awt.geom.Path2D;
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
    public AlienShip (int x, int y, int size, Controller controller)
    {
        this.controller = controller;
        this.size = size;
        setPosition(x, y);
        setRotation(0);

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

        outline = p;
    }

    @Override
    protected Shape getOutline ()
    {
        return outline;
    }

    @Override
    public void collidedWith (Participant p)
    {
        // expire the ship
        Participant.expire(this);

        // let the controller know
        controller.alienShipDestroyed(size);
    }
}
