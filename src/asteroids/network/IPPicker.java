package asteroids.network;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

@SuppressWarnings("serial")
public class IPPicker extends JFrame
{
    /**
     * Lets user specify game server to connect to.
     */
    public IPPicker ()
    {
        // just for access inside actionListener below
        IPPicker ipPicker = this;
        
        // Title at the top
        setTitle("Server Connection Configuration");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JLabel ipLabel = new JLabel("IP:");
        JTextField ipField = new JTextField();
        JLabel portLabel = new JLabel("Port:");
        JTextField portField = new JTextField();

        JButton connectButton = new JButton("Connect to game");
        connectButton.setActionCommand("connect");

        // Organize everything
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));
        mainPanel.add(ipLabel);
        mainPanel.add(ipField);
        mainPanel.add(portLabel);
        mainPanel.add(portField);
        mainPanel.add(connectButton);


        setContentPane(mainPanel);
        pack();
        
        connectButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed (ActionEvent e)
            {
                if (e.getActionCommand().equals("connect"))
                {
                    System.out.println("Trying to create new client");
                    new AsteroidsClient(ipField.getText(), Integer.parseInt(portField.getText()));
                    ipPicker.dispose();
                }
            }
        });
    }
}
