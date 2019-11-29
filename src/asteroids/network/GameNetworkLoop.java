package asteroids.network;

import java.net.Socket;

public class GameNetworkLoop implements Runnable
{
    /* Thread object to be created */
    private Thread thread;

    /* Name of the thread */
    private String name;
    
    /* How long the server can run without anything happening before it times out */
    private int timeoutTime;
    
    /* Socket that this thread will interact with */
    Socket socket;

    public GameNetworkLoop (String name, Socket socket)
    {
        this.name = name;
        this.socket = socket;
        
        // should time out after 10,000 ms of inactivity
        timeoutTime = 10*1000;
    }

    @SuppressWarnings("unused")
    @Override
    public void run ()
    {
        long shutdownTime = System.currentTimeMillis() + timeoutTime;
        System.out.println("Beginning of run loop reached.");

        try
        {
            while (shutdownTime > System.currentTimeMillis())
            {
                // pause execution for two seconds, as a test
                Thread.sleep(2000);
                System.out.println("Continuing to run thread");
                
                // If there's something happening, reset shutdownTime
                if (false /* replace with indicator of input) */)
                {
                    shutdownTime = System.currentTimeMillis() + timeoutTime;
                }
            }
        }
        catch (InterruptedException e)
        {
            System.out.println("Thread " + name + " interrupted. See stack trace below:");
            e.printStackTrace();
        }

        System.out.println("End of run() method reached.");
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
