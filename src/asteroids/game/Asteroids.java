package asteroids.game;

import static asteroids.game.Constants.START_LABEL;
import static asteroids.game.Constants.TITLE;
import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import asteroids.network.AsteroidsClient;
import asteroids.network.AsteroidsServer;
import asteroids.network.IPPicker;
import asteroids.network.ServerDisplay;

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
        int choice = JOptionPane.showOptionDialog(null, "What version would you like to run?", "Choose a Version",
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
            // Set server
            AsteroidsServer as = new AsteroidsServer(2020);
            
            // Server management pane for the newly created server
            ServerDisplay s = new ServerDisplay(as);
            s.setVisible(true);
            s.requestFocusInWindow();
        }
        else if (choice == 3)
        {
            IPPicker picker = new IPPicker();
            picker.setVisible(true);
            picker.requestFocusInWindow();
        }
    }
}
