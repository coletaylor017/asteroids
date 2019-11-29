package asteroids.game;

import java.net.Socket;
import java.io.*;
import asteroids.game.*;

public class AsteroidsClient
{
    /* port to try to connect to */
    int port;
    
    /* The local representation of the game */
    Controller controller;
    
    ObjectOutputStream clientOut;
    
    ObjectInputStream clientIn;
    
    Socket socket;
    
    /*
     * Creates a new client for the Asteroids game. This client connects to
     * an AsteroidsServer and sends important game updates to said server.
     * The client also updates its local representation of the current game
     * whenever it receives information from the server.
     */
    public AsteroidsClient(int serverPort)
    {
        controller = new Controller("online-multiplayer", this);
        
        port = serverPort;
        try
        {
            // try to create connection to server at correct port
            socket = new Socket("localhost", port);
            
            // create an output stream to send GameUpdate objects to the server
            clientOut = new ObjectOutputStream(socket.getOutputStream());
            
            // create an input stream to read GameUpdate objects from server
            clientIn =  new ObjectInputStream(socket.getInputStream());
            
            // create a new update to the game state
            GameUpdate update = new GameUpdate("CONNECTION ESTABLISHED");
            
            // Send the update to the server
            clientOut.writeObject(update);
            
            // 'flush' just makes sure any un-sent output bytes actually get sent
            clientOut.flush();
        }
        catch (Exception e)
        {
            System.out.println("NEW EXCEPTION ON CLIENT SIDE (constructor): " + e);
        }
    }
    
    public void send(GameUpdate g)
    {
        try
        {
            clientOut.writeObject(g);
            clientOut.flush();
        }
        catch (Exception e)
        {
            System.out.println("NEW EXCEPTION ON CLIENT SIDE (send method): " + e);
        }
    }
    
    public void close ()
    {
        try
        {
            // close everything down
            clientOut.close();
            clientIn.close();
            socket.close();
        }
        catch (Exception e)
        {
            System.out.println("NEW EXCEPTION ON CLIENT SIDE (close method): " + e);
        }
    }
}
