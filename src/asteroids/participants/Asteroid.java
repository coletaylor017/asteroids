package asteroids.participants;

import static asteroids.game.Constants.*;
import static asteroids.network.NetworkConstants.*;
import java.awt.Shape;
import java.awt.geom.*;
import java.util.Random;
import asteroids.destroyers.AsteroidDestroyer;
import asteroids.destroyers.ShipDestroyer;
import asteroids.game.Constants;
import asteroids.game.Controller;
import asteroids.game.Participant;
import asteroids.network.GameUpdate;

/**
 * Represents asteroids
 */
public class Asteroid extends Participant implements ShipDestroyer
{
    /** The size of the asteroid (0 = small, 1 = medium, 2 = large) */
    private int size;

    /** The outline of the asteroid */
    private Shape outline;

    /** The game controller */
    private Controller controller;

    /* A unique id for this asteroid */
    private long id;

    /**
     * Throws an IllegalArgumentException if size or variety is out of range.
     * 
     * Creates an asteroid of the specified variety (0 through 3) and size (0 = small, 1 = medium, 2 = large) and
     * positions it at the provided coordinates with a random rotation. Its velocity has the given speed but is in a
     * random direction.
     */
    public Asteroid (int variety, int size, double x, double y, int speed, Controller controller)
    {
        // Generate a random ID
        int randomInt = Constants.RANDOM.nextInt(1000 * 1000);

        // Combine with current epoch to guarantee a unique ID for every player
        id = Long.parseLong((System.currentTimeMillis() + "") + randomInt);

        // Make sure size and variety are valid
        if (size < 0 || size > 2)
        {
            throw new IllegalArgumentException("Invalid asteroid size: " + size);
        }
        else if (variety < 0 || variety > 3)
        {
            throw new IllegalArgumentException();
        }

        // Create the asteroid set speed for different varieties
        // if (variety == 2)
        // {
        // speed = MAXIMUM_LARGE_ASTEROID_SPEED;
        // }
        // if (variety == 1)
        // {
        // speed = MAXIMUM_MEDIUM_ASTEROID_SPEED;
        // }
        // else
        // {
        // speed = MAXIMUM_SMALL_ASTEROID_SPEED;
        // }
        this.controller = controller;
        this.size = size;
        setPosition(x, y);
        setVelocity(speed, RANDOM.nextDouble() * 2 * Math.PI);
        setRotation(2 * Math.PI * RANDOM.nextDouble());
        createAsteroidOutline(variety, size);

        // If online, send this spawn to the server
        if (controller.getGameMode().equals("online-multiplayer"))
        {
            controller.getClient().send(new GameUpdate(controller.getUser(), ASTEROIDSPAWN, this.id, this.size,
                    this.outline, this.getX(), this.getY(), this.getRotation(), this.getSpeed(), this.getDirection()));
        }
    }

    /*
     * A second constructor for explicitly declaring direction and rotation. This constructor is used for asteroids who
     * are spawned by someone else's controller.
     */
    public Asteroid (long id, int size, Shape outline, double x, double y, double rotation, double speed,
            double direction, Controller controller)
    {
        this.id = id;
        this.outline = outline;

        // Create the asteroid
        this.controller = controller;
        this.size = size;
        setPosition(x, y);
        setVelocity(speed, direction);
        setRotation(rotation);
    }

    public long getID ()
    {
        return id;
    }

    @Override
    protected Shape getOutline ()
    {
        return outline;
    }

    /**
     * Creates the outline of the asteroid based on its variety and size.
     */
    private void createAsteroidOutline (int variety, int size)
    {
        // This will contain the outline
        Path2D.Double poly = new Path2D.Double();

        // Fill out according to variety
        if (variety == 0)
        {
            poly.moveTo(0, -30);
            poly.lineTo(28, -15);
            poly.lineTo(20, 20);
            poly.lineTo(4, 8);
            poly.lineTo(-1, 30);
            poly.lineTo(-12, 15);
            poly.lineTo(-5, 2);
            poly.lineTo(-25, 7);
            poly.lineTo(-10, -25);
            poly.closePath();
        }
        else if (variety == 1)
        {
            poly.moveTo(10, -28);
            poly.lineTo(7, -16);
            poly.lineTo(30, -9);
            poly.lineTo(30, 9);
            poly.lineTo(10, 13);
            poly.lineTo(5, 30);
            poly.lineTo(-8, 28);
            poly.lineTo(-6, 6);
            poly.lineTo(-27, 12);
            poly.lineTo(-30, -11);
            poly.lineTo(-6, -15);
            poly.lineTo(-6, -28);
            poly.closePath();
        }
        else if (variety == 2)
        {
            poly.moveTo(10, -30);
            poly.lineTo(30, 0);
            poly.lineTo(15, 30);
            poly.lineTo(0, 15);
            poly.lineTo(-15, 30);
            poly.lineTo(-30, 0);
            poly.lineTo(-10, -30);
            poly.closePath();
        }
        else
        {
            poly.moveTo(30, -18);
            poly.lineTo(5, 5);
            poly.lineTo(30, 15);
            poly.lineTo(15, 30);
            poly.lineTo(0, 25);
            poly.lineTo(-15, 30);
            poly.lineTo(-25, 8);
            poly.lineTo(-10, -25);
            poly.lineTo(0, -30);
            poly.lineTo(10, -30);
            poly.closePath();
        }

        // Scale to the desired size
        double scale = ASTEROID_SCALE[size];
        poly.transform(AffineTransform.getScaleInstance(scale, scale));

        // Save the outline
        outline = poly;
    }

    /**
     * Returns the size of the asteroid
     */
    public int getSize ()
    {
        return size;
    }

    /**
     * When an Asteroid collides with an AsteroidDestroyer, it expires.
     */
    @Override
    public void collidedWith (Participant p)
    {
        // TODO: Add multiplayer score increase
        // Only run collision code as long as the collidee isn't an outside player on multiplayer
        if (!p.isGhost() && p instanceof AsteroidDestroyer)
        {
            if (controller.getGameMode().equals("online-multiplayer"))
            {
                // Let other online players know
                controller.getClient()
                        .send(new GameUpdate(controller.getUser(), ASTEROIDDIE, this.id, this.size, this.getOutline(),
                                this.getX(), this.getY(), this.getRotation(), this.getSpeed(), this.getDirection()));
            }

            // spawn two new asteroids only if this is not a small asteroid
            if (this.size != 0)
            {
                Random r = new Random();
                controller.addParticipant(
                        new Asteroid(r.nextInt(4), this.getSize() - 1, this.getX(), this.getY(), 3, controller));
                controller.addParticipant(
                        new Asteroid(r.nextInt(4), this.getSize() - 1, this.getX(), this.getY(), 3, controller));
            }

            // spawn dust
            int dustCount = RANDOM.nextInt(4) + 4;
            for (int i = 0; i < dustCount; i++)
            {
                controller.addParticipant(new DestructionParticle(this.getX(), this.getY(), 1, controller));
            }

            // Expire the asteroid
            Participant.expire(this);

            // Inform the controller
            controller.asteroidDestroyed(size);
        }
    }
}
