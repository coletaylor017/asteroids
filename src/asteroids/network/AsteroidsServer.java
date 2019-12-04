package asteroids.network;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.io.*;
import asteroids.network.GameNetworkLoop;

public class AsteroidsServer
{

    /* Port that this server listens on */
    private int port;

    /* the server socket that will accept client connections */
    private ServerSocket ss;
    
    /* List to keep track of all connected clients */
    private ArrayList<GameNetworkLoop> socketList;
    // TODO: Do something so that dead sockets are removed from this list
    
    /* List to keep track of all active Player objects */
    private ArrayList<Player> playerList;
    // TODO: Do something so that dead players are removed from this list

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
        // Initialize stuff
        port = serverPort;
        socketList = new ArrayList<>();
        playerList = new ArrayList<>();

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
        
        System.out.println("Running connection accepter.");
        new ConnectionAccepter(this).start();
        // making the above leaves a thread open for broadcasting messages
    }
    
    /*
     * Adds a socket to the list of active sockets
     */
    public void addToSocketList (GameNetworkLoop g)
    {
        socketList.add(g);
    }
    
    /*
     * Adds a player to the list of active player
     */
    public void addToPlayerList (Player player)
    {
        playerList.add(player);
        System.out.println("AsteroisServer added player ID '" + player.getID() + "' to the list.");
    }
    
    /*
     * Returns playerList
     */
    public ArrayList<Player> getPlayerList ()
    {
        return playerList;
    }
        
    /*
     * Removes specified player from list
     */
    public void removeFromPlayerList (Player p)
    {
        playerList.remove(p);
    }
    
    /*
     * Removes specified connection from list
     */
    public void removeFromSocketList (GameNetworkLoop g)
    {
        socketList.remove(g);
    }
    
    /*
     * Send a game update to all clients except the one specified, which should be the socket where the message originated.
     */
    public void broadcast (GameUpdate update, GameNetworkLoop sender)
    {
        for (GameNetworkLoop l : socketList)
        {
            // Don't write to the one that sent the message
            if (!l.equals(sender))
            {
                l.write(update);
            }
        }
    }
    
    /*
     * Returns the length of the array of connections
     */
    public int getSocketCount ()
    {
        return socketList.size();
    }

    /* returns the ServerSocket for this server. */
    public ServerSocket getServerSocket ()
    {
        return ss;
    }

    /* Starts the server's main process */
    private class ConnectionAccepter extends Thread
    {
        /* The server to build netowrk loops with */
        AsteroidsServer aServer;
        
        /*
         * Make a new connection accepter with the indicated server
         */
        public ConnectionAccepter (AsteroidsServer a)
        {
            aServer = a;
        }
        
        @Override
        public void run () {
            try
            {
                // initialize a simple counter for naming threads
                int i = 0;
    
                // This loop will end only when ss.close() is called from somewhere else, which will make
                // ss.accept() throw a SocketException.
                while (true)
                {
                    System.out.println("Waiting for connections...");
                    // establish a connection represented by Socket s
                    Socket socket = ss.accept();
                    System.out.println("Connection accepted.");
    
                    // new thread for the new client connection
                    new GameNetworkLoop("" + i, socket, aServer).start();
    
                    i++;
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

    }
}
