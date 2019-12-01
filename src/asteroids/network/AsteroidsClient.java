package asteroids.network;

import java.net.Socket;
import java.io.*;
import asteroids.game.*;

public class AsteroidsClient
{
    /* port to try to connect to */
    int port;
    
    /* The local representation of the game */
    Controller controller;
    
    /* Outbound messages to the server go through here */
    ObjectOutputStream clientOut;
    
    /* Incoming messages from the server can be read here */
    ObjectInputStream clientIn;
    
    /* The socket this client connects through */
    Socket socket;
    
    /*
     * Creates a new client for the Asteroids game. This client connects to
     * an AsteroidsServer and is used to send important game updates from controller to said server.
     * The client also interprets info from the server and invokes methods on controller to 
     * update the game state according to those updates. 
     */
    public AsteroidsClient(int serverPort)
    {
        
        port = serverPort;
        try
        {
            // try to create connection to server at correct port. If unsuccessful, an exception is thrown.
            socket = new Socket("localhost", port);
            
            // initialize the output stream
            clientOut = new ObjectOutputStream(socket.getOutputStream());
            
            // initialize the input stream
            clientIn =  new ObjectInputStream(socket.getInputStream());
            
            /* create a new controller to build the user's local game */
            controller = new Controller("online-multiplayer", this);
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
