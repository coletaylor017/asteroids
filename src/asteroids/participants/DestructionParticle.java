package asteroids.participants;

import static asteroids.game.Constants.MAX_PARTICLE_SPEED;
import static asteroids.game.Constants.RANDOM;
import asteroids.participants.Particle;
import asteroids.game.Controller;

public class DestructionParticle extends Particle
{
    /*
     * A Particle that spawns when something is destroyed. It expands radially our from a set
     * point with a random life span, velocity, and direction.
     */
    public DestructionParticle (double x, double y, double length, Controller controller)
    {
        super(x, y, RANDOM.nextDouble() * MAX_PARTICLE_SPEED, RANDOM.nextDouble() * 2 * Math.PI, length, RANDOM.nextInt(1000) + 1500, controller);

// Was going to put theses before super constructor but it super needs to come first
// But, I'm leaving them here for clarification of the complex constructor call above
//        double direction = RANDOM.nextDouble() * 2 * Math.PI;
//        int lifespan = RANDOM.nextInt(1000) + 1500; // dust will last between 1.5 and 2.5 seconds
//        double speed = RANDOM.nextDouble() * MAX_PARTICLE_SPEED;
        
        
    }

}
