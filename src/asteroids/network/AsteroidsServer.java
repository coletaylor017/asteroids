package asteroids.network;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import asteroids.network.GameUpdate;
import java.io.*;
import asteroids.network.GameNetworkLoop;

public class AsteroidsServer
{
    /* List of all connections currently active */
    ArrayList<Socket> socketList;

    /* Port that this server listens on */
    int port;

    /* A socket object that will be re-assigned each time the server connects to a new client */
    Socket socket;

    /* How long the server can run without clients connecting before it times out */
    private int timeoutTime;

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
        timeoutTime = 30 * 1000;

        // time at which the server will timeout
        long shutdownTime = System.currentTimeMillis() + timeoutTime;

        port = serverPort;
        socket = null;

        try
        {
            // new ServerSocket waits for connection requests
            ServerSocket ss = new ServerSocket(port);

            System.out.println("Server up, waiting for connections...");

            // simple counter for naming threads
            int i = 1;

            while (shutdownTime > System.currentTimeMillis())
            {
                try
                {
                    // establish a connection represented by Socket s
                    socket = ss.accept();
                    System.out.println("Connection accepted.");
                }
                catch (IOException e)
                {
                    System.out.println("I/O error: " + e);
                }
                // new thread for a client
                new GameNetworkLoop("thread-" + i, socket).start();
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
}
