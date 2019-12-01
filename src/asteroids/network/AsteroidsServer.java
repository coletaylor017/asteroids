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
        port = serverPort;
        Socket socket = null;

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

            // This loop will end only  when ss.close() is called from somewhere else, which will make
            // accept() throw a SocketException.
            while (true)
            {
                try
                {
                    // establish a connection represented by Socket s
                    socket = ss.accept();
                    System.out.println("Connection accepted.");

                    // new thread for a client
                    new GameNetworkLoop("" + i, socket).start();
                }
                catch (IOException e)
                {
                    System.out.println("I/O error: " + e);
                    ss.close();
                    System.out.println("Closed server socket successfully.");
                }
                i++;
            }
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
