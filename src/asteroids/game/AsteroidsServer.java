package asteroids.game;

import java.net.ServerSocket;
import java.net.Socket;
import java.io.*;
import asteroids.game.GameUpdate;

public class AsteroidsServer
{
    /*
     * Creates a server that connects to game clients, then receives
     * important game events from those clients pertaining to the Asteroids game.
     * The server then performs game logic and propagates any newly changed game
     * properties to every connected client.
     */
    public AsteroidsServer()
    {   
        try
        {
            // new ServerSocket waits for connection requests
            ServerSocket ss = new ServerSocket(2020);
            
            // establish a connection represented by Socket s
            Socket s = ss.accept();
            
            //Make an input stream to read incoming GameUpdate objects
            ObjectInputStream serverIn = new ObjectInputStream(s.getInputStream());
            
            // Make an output stream so that server can send GameUpdate objects to client
            ObjectOutputStream serverOut = new ObjectOutputStream(s.getOutputStream());
            
            // Read incoming object, cast to type GameUpdate, and assign
            GameUpdate update = (GameUpdate) serverIn.readObject();
            
            
            
            System.out.println("New game update: " + update.toString());
            System.out.println("Operation code: " + update.getOperationCode());
            System.out.println("X coord: " + update.getX());
            System.out.println("Y coord: " + update.getY());
//            serverOut.writeUTF("From the server: you sent me '" + update.toString() 
//                    + "'. Thanks! ^_^");
//            
//            serverOut.flush();
            
            // close everything down    
            s.close();
            ss.close();
            serverOut.close();
            serverIn.close();
        }
        catch (Exception e)
        {
            System.out.println("NEW EXCEPTION ON SERVER SIDE: " + e);
        }
    }
}
