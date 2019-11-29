package asteroids.network;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class GameNetworkLoop implements Runnable
{
    /* Thread object to be created */
    private Thread thread;

    /* Name of the thread */
    private String name;
    
    /* How long the thread can run without anything happening before it times out */
    private int timeoutTime;
    
    /* Socket that this thread will interact with */
    Socket socket;

    public GameNetworkLoop (String name, Socket socket)
    {
        this.name = name;
        this.socket = socket;
        
        // should time out after 30,000 ms of inactivity
        timeoutTime = 25*1000;
    }

    @SuppressWarnings("unused")
    @Override
    public void run ()
    {
        long shutdownTime = System.currentTimeMillis() + timeoutTime;
        System.out.println("Beginning of run loop reached.");

        try
        {
            //Make an input stream to read incoming GameUpdate objects
            ObjectInputStream serverIn = new ObjectInputStream(socket.getInputStream());
            
            // Make an output stream so that server can send GameUpdate objects to client
            ObjectOutputStream serverOut = new ObjectOutputStream(socket.getOutputStream());
            
            // Read incoming object, cast to type GameUpdate, and assign
            GameUpdate update = (GameUpdate) serverIn.readObject();
            
            // repeat until idle for timeoutTime milliseconds OR client tells this thread to self-destruct
            while (shutdownTime > System.currentTimeMillis() && !update.getOperationCode().equals("ENDCONNECTION"))
            {
                // pause execution for two seconds, as a test
//                Thread.sleep(2000);
//                System.out.println("Continuing to run thread");
                
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
            
            // close everything down    
            socket.close();
            serverOut.close();
            serverIn.close();
        }
//        catch (InterruptedException e)
//        {
//            System.out.println("Thread '" + name + "' interrupted. See stack trace below:");
//            e.printStackTrace();
//        }
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
    
    public void start ()
    {
        // don't start the thread is it's already running
        if (thread == null)
        {
            // create a new thread that can run this's run() method
            thread = new Thread(this, name);
            
            // make the thread start running the run() method
            System.out.println("Starting thread");
            thread.start();
        }
    }

}
