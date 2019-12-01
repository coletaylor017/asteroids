package asteroids.game;

import javax.swing.*;
import asteroids.network.AsteroidsClient;
import static asteroids.game.Constants.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;


/**
 * Defines the top-level appearance of an Asteroids game.
 */
@SuppressWarnings("serial")
public class Display extends JFrame
{
    
    /** The area where the action takes place */
    private Screen screen;

    /**
     * Lays out the game and creates the controller
     */
    public Display (Controller controller)
    {
        // Title at the top
        setTitle(TITLE);

        // Just close the JFrame on close, because before the app is shut down, we need to
        // run a function to shut down the client and server
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // The main playing area and the controller
        screen = new Screen(controller);

        // This panel contains the screen to prevent the screen from being
        // resized
        JPanel screenPanel = new JPanel();
        screenPanel.setLayout(new GridBagLayout());
        screenPanel.add(screen);

        // This panel contains buttons and labels
        JPanel controls = new JPanel();

        // The button that starts the game
        JButton startGame = new JButton(START_LABEL);
        controls.add(startGame);
        
        if (controller.getGameMode().equals("online-multiplayer"))
        {
            JButton killClient = new JButton("Kill client");
            controls.add(killClient);
            killClient.addActionListener(controller);
        }

        // Organize everything
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.add(screenPanel, "Center");
        mainPanel.add(controls, "North");
        setContentPane(mainPanel);
        pack();

        // Connect the controller to the start button
        startGame.addActionListener(controller);
        
    }

    /**
     * Called when it is time to update the screen display. This is what drives the animation.
     */
    public void refresh ()
    {
        screen.repaint();
    }

    /**
     * Sets the large legend
     */
    public void setLegend (String s)
    {
        screen.setLegend(s);
    }

    /** Sets the lives label */
    public void setLives (int s)
    {
        screen.setLives(s);

    }

    /** Sets the Score label */
    public void setScore (int s)
    {
        screen.setScoreLabel(s);
    }

    /** Sets the Level Label */
    public void setLevel (int s)
    {
        screen.setLevelLabel(s);
    }

}
