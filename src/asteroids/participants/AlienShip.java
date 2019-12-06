package asteroids.participants;

import static asteroids.game.Constants.*;
import java.awt.Shape;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.io.BufferedInputStream;
import java.io.IOException;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
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

    /* Indicates is this alien ship is moving or not */
    boolean moving;

    /** Shooting sound */
    private Clip alienShot;

    /*
     * Constructs a new alien ship for the given controller.
     */
    public AlienShip (int x, int y, int size, Controller controller)
    {
        alienShot = createClip("/sounds/fire.wav");

        this.controller = controller;
        this.size = size;
        setRotation(0);
        if (size == 0)
        {
            setVelocity(3, 0);
        }
        else if (size == 1)
        {
            setVelocity(2, 0);
        }
        moving = false;

        // appear randomly on either one edge or the other
        setPosition(SIZE * RANDOM.nextInt(2), SIZE * RANDOM.nextDouble());

        // When this timer goes off, alien ship attacks
        new ParticipantCountdownTimer(this, new String("attack"), 2000);
        new ParticipantCountdownTimer(this, new String("turn"), 1000);

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

    /**
     * Creates an audio clip from a sound file.
     */
    public Clip createClip (String soundFile)
    {
        // Opening the sound file this way will work no matter how the
        // project is exported. The only restriction is that the
        // sound files must be stored in a package.
        try (BufferedInputStream sound = new BufferedInputStream(getClass().getResourceAsStream(soundFile)))
        {
            // Create and return a Clip that will play a sound file. There are
            // various reasons that the creation attempt could fail. If it
            // fails, return null.
            Clip clip = AudioSystem.getClip();
            clip.open(AudioSystem.getAudioInputStream(sound));
            return clip;
        }
        catch (LineUnavailableException e)
        {
            return null;
        }
        catch (IOException e)
        {
            return null;
        }
        catch (UnsupportedAudioFileException e)
        {
            return null;
        }
    }

    /*
     * Fire a bullet in the specified direction.
     */
    public void attack (double direction)
    {
        AlienBullet bullet = new AlienBullet(this.getX(), this.getY(), direction, BULLET_SPEED, controller);
        bullet.setGhostStatus(this.isGhost()); // set ghost status to match ship
        controller.addParticipant(bullet);
        alienShot.loop(1);

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

        // Check what the timer is telling us to do
        String instruction = (String) obj;
        if (instruction.equals("attack"))
        {
            if (size == 1)
            {
                // attack in a random direction
                attack(2 * Math.PI * RANDOM.nextDouble());
                alienShot.loop(1);
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
                alienShot.loop(1);
            }

            // Restart the timer
            new ParticipantCountdownTimer(this, new String("attack"), 2000);
        }
        else if (instruction.equals("turn"))
        {
            setDirection((RANDOM.nextInt(3) - 1) * -1);

            // Restart the timer
            new ParticipantCountdownTimer(this, new String("turn"), 1000);
        }
    }
}
