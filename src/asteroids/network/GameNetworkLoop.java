package asteroids.network;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class GameNetworkLoop implements Runnable
{
    /* Name of the thread */
    private String name;
    
    /* How long the thread can run without anything happening before it times out */
    private int timeoutTime;
    
    /* Socket that this thread will interact with */
    Socket socket;
    
    /* Flag to make the thread stop when we want */
    boolean running;

    public GameNetworkLoop (String name, Socket socket)
    {
//        running = true;
        this.name = name;
        this.socket = socket;
        
        // should time out after 60s of inactivity
        timeoutTime = 60*1000;
    }

    @SuppressWarnings("unused")
    @Override
    public void run ()
    {
        long shutdownTime = System.currentTimeMillis() + timeoutTime;
        System.out.println("GameNetowrkLoop thread '" + name + "' starting.");

        try
        {
            //Make an input stream to read incoming GameUpdate objects
            // This is a only way to get a buffered object input stream, since such an object doesn't exist 
            ObjectInputStream serverIn = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
            
            // askfj
            BufferedInputStream serverIn2 = new BufferedInputStream(new ObjectInputStream(socket.getInputStream()));

            
            // Make an output stream so that server can send GameUpdate objects to client
            ObjectOutputStream serverOut = new ObjectOutputStream(socket.getOutputStream());
            
            // Read incoming object, cast to type GameUpdate, and assign
            GameUpdate update = (GameUpdate) serverIn.readObject();
            
            // repeat until idle for timeoutTime milliseconds OR client tells this thread to self-destruct
            while (shutdownTime > System.currentTimeMillis() && !update.getOperationCode().equals("ENDCONNECTION"))
            {
                System.out.println("New game update: " + update.toString());
                System.out.println("Operation code: " + update.getOperationCode());
                System.out.println("X coord: " + update.getX());
                System.out.println("Y coord: " + update.getY());
                System.out.println("Rotation: " + update.getRotation());
                update = (GameUpdate) serverIn.readObject();
                
                // If there's something happening, reset shutdownTime
                if (false /* replace with indicator of input) */)
                {
                    shutdownTime = System.currentTimeMillis() + timeoutTime;
                }
            }
            
            System.out.println("Thread " + name + " shutting down.");
            
            // close everything down    
            serverOut.close();
            serverIn.close();
            socket.close();
        }
        catch (IOException i)
        {
            System.out.println("IO exception in server thread '" + name + "'. See stack trace below:");
            i.printStackTrace();
        }
        catch (Exception n)
        {
            System.out.println("Some non-io, non-interrupted exception in server thread '" + name + "'. See stack trace below:");
            n.printStackTrace();
        }

        System.out.println("End of run() method reached. Shutting socket down.");
    }
    
    public void terminate ()
    {
        running = false;
        try
        {
            socket.close();
        }
        catch (IOException i)
        {
            System.out.println("Error closing socket");
            i.printStackTrace();
        }
    }
}
