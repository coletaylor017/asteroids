package asteroids.network;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * A panel to manage a currently running AsteroidsServer.
 */
@SuppressWarnings("serial")
public class ServerDisplay extends JFrame
{
    
    /* The currently running server. */
    private AsteroidsServer server;
    
    /* The local machine's IP */
    private String ip;
    
    /* The port the server is listening on */
    private int port;

    /**
     * Lays out the game and creates the controller
     */
    public ServerDisplay (AsteroidsServer s)
    {
        // Set server
        server = s;
        
        // Attempt to find local IP
        try(final DatagramSocket socket = new DatagramSocket()){
          socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
          ip = socket.getLocalAddress().getHostAddress();
        }
        catch (Exception e)
        {
            ip = "Error finding local IP";
            e.printStackTrace();
        }
        
        // set port
        port = 2020;
        
        // Title at the top
        setTitle("Asteroids Server Management");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Display current IP and port
        JLabel ipLabel = new JLabel("IP: " + ip);
        JLabel portLabel = new JLabel("Port: " + port);
        
        // The button that kills the server
        JButton killServer = new JButton("Kill server");
        killServer.setActionCommand("kill-server");

        // Organize everything
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));
        mainPanel.add(ipLabel);
        mainPanel.add(portLabel);
        mainPanel.add(killServer);
        
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
                    System.out.println("Server killed successfully. You should see an exception or two.");
                }
            }
        });
    }

}
