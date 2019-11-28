package asteroids.game;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.io.*;
import asteroids.game.GameUpdate;

public class AsteroidsServer
{
    /* List of all connections currently active */
    ArrayList<Socket> socketList;
    
    /* Port that this server listens on */
    int port;

    /*
     * Creates a server that connects to game clients, then receives
     * important game events from those clients pertaining to the Asteroids game.
     * The server then propagates any newly changed game
     * properties to every connected client.
     * The server does not host a "master copy" of the game; rather, AsteroidClients
     * are responsible for instantiating a new Controller and keeping the game state
     * current by reading data from the server
     * 
     * I would have made the server host a "master copy" but the way Controller is set up is just not
     * very conducive to that.
     * 
     * In the future I will make all clients send data between each other with no server in between to 
     * reduce latency.
     */
    public AsteroidsServer(int serverPort)
    {   
        port = serverPort;
        
        try
        {
            // new ServerSocket waits for connection requests
            ServerSocket ss = new ServerSocket(port);
            
            // establish a connection represented by Socket s
            Socket s = ss.accept();
            
            //Make an input stream to read incoming GameUpdate objects
            ObjectInputStream serverIn = new ObjectInputStream(s.getInputStream());
            
            // Make an output stream so that server can send GameUpdate objects to client
            ObjectOutputStream serverOut = new ObjectOutputStream(s.getOutputStream());
            
            // Read incoming object, cast to type GameUpdate, and assign
            GameUpdate update = (GameUpdate) serverIn.readObject();
            
            // Run until the client requests for the server to stop
            while (!update.getOperationCode().equals("STOPSERVER"))
            {
                System.out.println("New game update: " + update.toString());
                System.out.println("Operation code: " + update.getOperationCode());
                System.out.println("X coord: " + update.getX());
                System.out.println("Y coord: " + update.getY());
                System.out.println("Rotation: " + update.getRotation());
                update = (GameUpdate) serverIn.readObject();

            }
            

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
