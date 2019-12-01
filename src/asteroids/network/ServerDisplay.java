package asteroids.network;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

/**
 * A panel to manage a currently running AsteroidsServer.
 */
@SuppressWarnings("serial")
public class ServerDisplay extends JFrame
{
    
    /* The currently running server. */
    AsteroidsServer server;

    /**
     * Lays out the game and creates the controller
     */
    public ServerDisplay (AsteroidsServer s)
    {
        // Set server
        server = s;
        
        // Title at the top
        setTitle("Asteroids Server Management");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // This panel contains buttons and labels
        JPanel controls = new JPanel();

        // The button that starts the game
        JButton killServer = new JButton("Kill server");
        killServer.setActionCommand("kill-server");
        controls.add(killServer);

        // Organize everything
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.add(controls, "Center");
        setContentPane(mainPanel);
        pack();

        killServer.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed (ActionEvent e)
            {
                if (e.getActionCommand().equals("kill-server"))
                {
                    // Close the serverSocker, throwing an exception and ending server execution
                    System.out.println("Attempting to kill server... ");
                    try
                    {
                        server.getServerSocket().close();
                    }
                    catch (IOException e1)
                    {
                        e1.printStackTrace();
                    }
                    System.out.println("Server killed successfully");
                }
            }
        });
    }

}
