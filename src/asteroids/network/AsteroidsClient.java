package asteroids.network;

import java.net.Socket;
import static asteroids.game.Constants.SIZE;
import java.io.*;
import asteroids.game.*;
import asteroids.participants.Ship;

public class AsteroidsClient
{
    /* port to try to connect to */
    int port;
    
    /* The local representation of the game */
    static Controller controller;
    
    /* Outbound messages to the server go through here */
    ObjectOutputStream clientOut;
    
    /* Incoming messages from the server can be read here */
    static ObjectInputStream clientIn;
    
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

            // handle messages from the server

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
    
    static private class MessageGetter implements Runnable
    {
        /* The socket that this MessageGetter will connect through */
        ObjectInputStream ois;
        
        public MessageGetter (ObjectInputStream s)
        {
            ois = s;
        }
        
        @Override
        public void run ()
        {
            try
            {
                while (true)
                {
                    GameUpdate update = (GameUpdate) clientIn.readObject();
                    if (update.getOperationCode().contentEquals("SHIPSPAWN"))
                    {
                        // let the controller know
                    }
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }
}
