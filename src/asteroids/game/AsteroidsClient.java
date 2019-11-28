package asteroids.game;

import java.net.Socket;
import java.io.*;

public class AsteroidsClient
{
    /*
     * Creates a new client for the Asteroids game. This client connects to
     * an AsteroidsServer and sends important game updates to said server.
     * The client also updates its local representation of the current game
     * whenever it receives information from the server.
     */
    public AsteroidsClient()
    {
        try
        {
            // try to create connection to server at correct port
            Socket s = new Socket("localhost", 2020);
            
            // create an output stream to send data to the server
            DataOutputStream clientOut = new DataOutputStream(s.getOutputStream());
            
            // create an input stream to read from server
            DataInputStream clientIn =  new DataInputStream(s.getInputStream());
            
            clientOut.writeUTF("Hello, server!");
            
            // 'flush' just makes sure any un-sent output bytes actually get sent
            clientOut.flush();
            
            String response = clientIn.readUTF();
            
            System.out.println("I'm the client, and the server responded with this: '" + response + "'");
            
            // close everything down
            clientOut.close();
            clientIn.close();
            s.close();
            
        }
        catch (Exception e)
        {
            System.out.println("NEW EXCEPTION FROM CLIENT: " + e);
        }
    }
}
