package asteroids.network;

import java.net.ServerSocket;
import java.net.Socket;
import java.io.*;
import asteroids.network.GameNetworkLoop;

public class AsteroidsServer
{

    /* Port that this server listens on */
    int port;

    /* the server socket that will accept client connections */
    ServerSocket ss;

    /*
     * Creates a server that connects to game clients, then receives important game events from those clients pertaining
     * to the Asteroids game. The server then propagates any newly changed game properties to every connected client.
     * The server does not host a "master copy" of the game; rather, AsteroidClients are responsible for instantiating a
     * new Controller and keeping the game state current by reading data from the server.
     * 
     * For each new client the server connects to, it creates a new GameNetworkLoop so that that client can be handled
     * on its own thread without blocking any others.
     */
    public AsteroidsServer (int serverPort)
    {
        port = serverPort;
        
        // new ServerSocket waits for connection requests
        try
        {
            ss = new ServerSocket(port);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        System.out.println("Server created.");
    }

    /* returns the ServerSocket for this server. */
    public ServerSocket getServerSocket ()
    {
        return ss;
    }

    /* Starts the server's main process */
    public void start ()
    {
        try
        {
            // initialize a simple counter for naming threads
            int i = 0;

            // This loop will end only when ss.close() is called from somewhere else, which will make
            // ss.accept() throw a SocketException.
            while (true)
            {
                try
                {
                    System.out.println("Waiting for connections...");
                    // establish a connection represented by Socket s
                    Socket socket = ss.accept();
                    System.out.println("Connection accepted.");

                    // new thread for the new client connection
                    new GameNetworkLoop("" + i, socket).start();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
                i++;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
