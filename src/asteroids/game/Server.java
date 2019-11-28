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
            
            String inputString = serverIn.readUTF();
            System.out.println(inputString);
            
            // close the server 
            ss.close();
        }
        catch (Exception e)
        {
            System.out.println("NEW EXCEPTION: " + e);
        }
    }
}
