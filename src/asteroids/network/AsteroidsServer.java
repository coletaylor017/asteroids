package asteroids.network;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import asteroids.network.GameUpdate;
import java.io.*;
import asteroids.network.GameNetworkLoop;

public class AsteroidsServer
{
    /* List of all client threads currently active */
    ArrayList<GameNetworkLoop> threadList;

    /* Port that this server listens on */
    int port;

    /* A socket object that will be re-assigned each time the server connects to a new client */
    Socket socket;

    /* How long the server can run without clients connecting before it times out */
    private int timeoutTime;
    
    // the server socket that will accept client connetions
    ServerSocket ss;

    /*
     * Creates a server that connects to game clients, then receives important game events from those clients pertaining
     * to the Asteroids game. The server then propagates any newly changed game properties to every connected client.
     * The server does not host a "master copy" of the game; rather, AsteroidClients are responsible for instantiating a
     * new Controller and keeping the game state current by reading data from the server
     * 
     * I would have made the server host a "master copy" but the way Controller is set up is just not very conducive to
     * that.
     * 
     * In the future I will make all clients send data between each other with no server in between to reduce latency.
     */
    public AsteroidsServer (int serverPort)
    {
        // server will shut down after 30,000 ms of no connections
        timeoutTime = 20 * 1000;

        // time at which the server will timeout
        long shutdownTime = System.currentTimeMillis() + timeoutTime;

        port = serverPort;
        socket = null;

        /*
         * The task that the server will continue to do as long as it is active
         */
        try
        {
            // new ServerSocket waits for connection requests
            ss = new ServerSocket(port);

            System.out.println("Server up, waiting for connections...");

            // simple counter for naming threads
            int i = 1;

            // This loop will end in either of the following situations:
            // 1. A new connection is accepted after the timeout time
            // ss.close() is called from somewhere else, which will make
            // accept() throw a SocketException.
            while (shutdownTime > System.currentTimeMillis())
            {
                try
                {
                    // establish a connection represented by Socket s
                    socket = ss.accept();
                    System.out.println("Connection accepted.");
                    if (shutdownTime > System.currentTimeMillis())
                    {
                        // new thread for a client
                        GameNetworkLoop g = new GameNetworkLoop("thread-" + i, socket);
                        
                        threadList.add(g);

                        // start thread
                        g.start();
                    }
                }
                catch (IOException e)
                {
                    System.out.println("I/O error: " + e);
                }
                i++;
            }

             // Before shutting down the server, shut down each thread
             for (GameNetworkLoop gnl : threadList)
             {
                 // tell the game loop to stop
                 // Still subject to the problem that readObject() is blocking so the
                 // thread won't terminate until it gets another GameUpdate....
                 // Need to find a solution
                 System.out.println("Asking '" + gnl.getName() + "' to terminate.");
                 gnl.terminate();
                
                 // Wait till the thread stops
//                 gnl.join();
                 System.out.println("'" + gnl.getName() + "' succesfully terminated.");
                
                 // remove from list
                 threadList.remove(gnl);
                 System.out.println("Removed '" + gnl.getName() + "' from thread list.");
             }

            System.out.println("Server timeout reached, shutting game server down.");
            ss.close();
        }
        catch (Exception e)
        {
            System.out.println("NEW EXCEPTION ON ASTEROIDSSERVER FILE: ");
            e.printStackTrace();
        }
    }
    
    /* returns the ServerSocket for this server. */
    public ServerSocket getServerSocket ()
    {
        return ss;
    }
    
    /*
     * Removes the specified instance of GameNetworkLoop from the list of active threads.
     */
    public void removeFromList (GameNetworkLoop g)
    {
        threadList.remove(g);
    }
}
