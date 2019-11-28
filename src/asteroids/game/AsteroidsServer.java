package asteroids.game;

import java.net.ServerSocket;
import java.net.Socket;
import java.io.*;

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
            
            //Make an input stream to read incoming data
            DataInputStream serverIn = new DataInputStream(s.getInputStream());
            
            // Make an output stream so that server can send messages to client
            DataOutputStream serverOut = new DataOutputStream(s.getOutputStream());
            
            String inputString = serverIn.readUTF();
            System.out.println("Printing from server file: " + inputString);
            serverOut.writeUTF("From the server: you sent me '" + inputString 
                    + "'. Thanks! ^_^");
            
            serverOut.flush();
            
            // close everything down    
            s.close();
            ss.close();
            serverOut.close();
            serverIn.close();
        }
        catch (Exception e)
        {
            System.out.println("NEW EXCEPTION FROM SERVER: " + e);
        }
    }
}
