package asteroids.network;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class GameNetworkLoop extends Thread
{
    /* Name of the thread */
    private String name;

    /* Socket that this thread will interact with */
    Socket socket;

    /* Input stream for this socket */
    ObjectInputStream serverIn;

    /* The output stream for this socket */
    ObjectOutputStream serverOut;

    /* The server that created this gamenetworkloop */
    AsteroidsServer server;

    /* Lets the thread access this */
    GameNetworkLoop loop;

    public GameNetworkLoop (String name, Socket socket, AsteroidsServer server)
    {
        this.name = name;
        this.socket = socket;
        this.server = server;
        loop = this;
    }

    @Override
    public void run ()
    {
        System.out.println("GameNetowrkLoop thread '" + name + "' starting.");

        try
        {
            // Make an input stream to read incoming GameUpdate objects
            // This is a only way to get a buffered object input stream, since such an object doesn't exist
            serverIn = new ObjectInputStream(socket.getInputStream());

            // Make an output stream so that server can send GameUpdate objects to client
            serverOut = new ObjectOutputStream(socket.getOutputStream());

            // Add this connection to the server's list of active commections.
            server.addToSocketList(this);

            // Add new players to the local game for every already-existing player.
/*            for (int i = 0; i < server.getPlayerList().size(); i++)
            {
                serverOut.writeObject(new GameUpdate(server.getPlayerList().get(i), "NEWPLAYER"));
            }*/

            // get the first initialization broadcast from the client
            GameUpdate initialUpdate = (GameUpdate) serverIn.readObject();

            // Add the new player to the server's list of active players
            server.addToPlayerList(initialUpdate.getPlayer());

            Thread intake = new Thread()
            {
                public void run ()
                {
                    try
                    {
                        while (true)
                        {
                            GameUpdate update = (GameUpdate) serverIn.readObject();
                            System.out.println("New game update: " + update.getOperationCode());

                            // if the client is requesting a list of already-active players
                            if (update.getOperationCode().equals("GETPLAYERS"))
                            {
                                // send the current list
                                serverOut.writeObject(server.getPlayerList());
                            }
                            else // otherwise, assume it is a game update broadcast and push to all other game clients
                            {
                                server.broadcast(update, loop);
                            }
                        }
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }

                    // Once some exception has been thrown, thus escaping the while loop, do the following

                    // remove this connection and the player from the server's lists
                    server.removeFromPlayerList(initialUpdate.getPlayer());
                    server.removeFromSocketList(loop);

                    System.out.println("Thread " + name + " attempting to shut down.");
                    // close everything down
                    try
                    {
                        serverOut.close();
                        serverIn.close();
                        socket.close();
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                }
            };

            intake.start();

        }
        catch (Exception n)
        {
            n.printStackTrace();
        }
    }

    /*
     * Send a gameUpdate to this thread's client
     */
    public void write (GameUpdate g)
    {
        try
        {
            serverOut.writeObject(g);
            serverOut.flush();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
