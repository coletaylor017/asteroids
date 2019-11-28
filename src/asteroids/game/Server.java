package asteroids.game;

import java.net.ServerSocket;
import java.net.Socket;
import java.io.*;

/*
 * Creates a server to handle input from a client.
 * Liberal amounts of code taken from the helpful tutorial at:
 * 'https://www.javatpoint.com/socket-programming'
 */
public class Server
{
    public static void main (String[] args)
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
