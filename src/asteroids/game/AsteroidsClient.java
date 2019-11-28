package asteroids.game;

import java.net.Socket;
import java.io.*;
import asteroids.game.GameUpdate;

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
            
            // create an output stream to send GameUpdate objects to the server
            ObjectOutputStream clientOut = new ObjectOutputStream(s.getOutputStream());
            
            // create an input stream to read GameUpdate objects from server
            ObjectInputStream clientIn =  new ObjectInputStream(s.getInputStream());
            
            // craete a new update to the game state
            GameUpdate update = new GameUpdate("SHIPMOVE", 200, 300);
            
            // Send the update to the server
            clientOut.writeObject(update);
            
            // 'flush' just makes sure any un-sent output bytes actually get sent
            clientOut.flush();
            
//            String response = clientIn.readUTF();
            
//            System.out.println("I'm the client, and the server responded with this: '" + response + "'");
            
            // close everything down
            clientOut.close();
            clientIn.close();
            s.close();
            
        }
        catch (Exception e)
        {
            System.out.println("NEW EXCEPTION ON CLIENT SIDE: " + e);
        }
    }
}
