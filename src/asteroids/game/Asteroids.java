package asteroids.game;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import asteroids.game.AsteroidsServer;
import asteroids.game.AsteroidsClient;

/**
 * The main class for the application.
 */
public class Asteroids
{
    /**
     * Launches a dialog that lets the user choose between a classic and an enhanced game of Asteroids.
     */
    public static void main (String[] args)
    {
        SwingUtilities.invokeLater( () -> chooseVersion());
    }

    /**
     * Interacts with the user to determine whether to run classic Asteroids or enhanced Asteroids.
     */
    private static void chooseVersion ()
    {
        String[] options = { "Classic", "One keyboard, two-player game", "Start LAN server", "Join LAN server" };
        int choice = JOptionPane.showOptionDialog(null, "What version would you liek to run?", "Choose a Version",
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
        if (choice == 0)
        {
            new Controller("classic");
        }
        else if (choice == 1)
        {
            new Controller("enhanced");
        }
        else if (choice == 2)
        {
            new AsteroidsServer();
        }
        else if (choice == 3)
        {
            new AsteroidsClient();
        }

    }
}
