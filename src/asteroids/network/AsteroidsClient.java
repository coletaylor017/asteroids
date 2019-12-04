package asteroids.network;

import java.net.Socket;
import java.util.ArrayList;
import java.io.*;
import asteroids.game.*;
import asteroids.participants.Ship;
import asteroids.participants.Asteroid;
import static asteroids.network.NetworkConstants.*;

public class AsteroidsClient
{
    /* port to try to connect to */
    private int port;

    /* The local representation of the game */
    private static Controller controller;

    /* Outbound messages to the server go through here */
    private ObjectOutputStream clientOut;

    /* Incoming messages from the server can be read here */
    private static ObjectInputStream clientIn;

    /* The socket this client connects through */
    private Socket socket;

    /*
     * Creates a new client for the Asteroids game. This client connects to an AsteroidsServer and is used to send
     * important game updates from controller to said server. The client also interprets info from the server and
     * invokes methods on controller to update the game state according to those updates.
     */
    public AsteroidsClient (int serverPort)
    {

        port = serverPort;
        try
        {
            // try to create connection to server at correct port. If unsuccessful, an exception is thrown.
            socket = new Socket("localhost", port);

            // initialize the output stream
            clientOut = new ObjectOutputStream(socket.getOutputStream());

            // initialize the input stream
            clientIn = new ObjectInputStream(socket.getInputStream());

            /* create a new controller to build the user's local game */
            controller = new Controller("online-multiplayer", this);

            // handle messages from the server with a separate thread
            Thread thread = new Thread(new MessageGetter(clientIn));
            thread.start();
        }
        catch (Exception e)
        {
            System.out.println("NEW EXCEPTION ON CLIENT SIDE (constructor): ");
            e.printStackTrace();
        }
    }

    public void send (GameUpdate g)
    {
        try
        {
            clientOut.writeObject(g);
            clientOut.flush();
        }
        catch (Exception e)
        {
            System.out.println("NEW EXCEPTION ON CLIENT SIDE (send method): " + e);
        }
    }

    public void close ()
    {
        try
        {
            // close everything down
            clientOut.close();
            clientIn.close();
            socket.close();
        }
        catch (Exception e)
        {
            System.out.println("NEW EXCEPTION ON CLIENT SIDE (close method): " + e);
        }
    }

    static private class MessageGetter implements Runnable
    {
        /* The socket that this MessageGetter will connect through */
        ObjectInputStream ois;

        public MessageGetter (ObjectInputStream s)
        {
            ois = s;
        }

        @SuppressWarnings("unchecked")
        @Override
        public void run ()
        {
            try
            {
                while (true)
                {
                    Object objectIn = clientIn.readObject();
                    if (objectIn instanceof GameUpdate) // if the object is a game state update
                    {
                        GameUpdate update = (GameUpdate) objectIn;
                        String opCode = update.getOperationCode();
                        System.out.println("New update from the server. Update code: " + update.getOperationCode());
                        
                        // TODO: organize loosely by frequency to avoid unnecessary checking
                        if (opCode.equals(NEWPLAYER))
                        {
                            // Add a new player with the correct ID
                            controller.addPlayer(update.getPlayer());
                            System.out.println("Added new player via NEWPLAYER call");
                        }
                        else if (opCode.equals(SHIPMOVE))
                        {
                            Ship s = update.getPlayer().getShip();

                            // Orient the ship
                            s.setPosition(update.getX(), update.getY());
                            s.setRotation(update.getRotation());
                        }
                        else if (opCode.equals(SHIPFIRE))
                        {
                            // Ask that the ship fire a bullet!
                            update.getPlayer().getShip().attack();
                        }
                        else if (opCode.equals(SHIPDIE))
                        {
                            // Kill ship and remove it from list but leave playerlist alone
                        }
                        else if (opCode.equals(ASTEROIDSPAWN))
                        {
                            Asteroid a = new Asteroid(
                                update.getID(),
                                update.getSize(),
                                update.getOutline(),
                                update.getX(),
                                update.getY(),
                                update.getRotation(),
                                update.getSpeed(),
                                update.getDirection(),
                                controller
                            );
                            
                            controller.addParticipant(a);
                        }
                        else if (opCode.equals(ASTEROIDDIE))
                        {
                            // Find the right asteroid
                            long targetID = update.getID();
                            System.out.println(targetID);
                            for (Participant p : controller)
                            {
                                if (p instanceof Asteroid)
                                {
                                    Asteroid a = (Asteroid) p;
                                    System.out.println(a.getID());
                                    if (a.getID() == targetID)
                                    {
                                        Participant.expire(p);
                                        controller.asteroidDestroyed(update.getSize());
                                    }
                                }
                            }
                        }
                        else if (opCode.equals(RESTARTLEVEL))
                        {
                            controller.restartLevel();
                        }
                        else if (opCode.equals(NEXTLEVEL))
                        {
                            controller.nextLevel();
                        }
                        else if (opCode.equals(NEWGAME))
                        {
                            controller.initialScreen();
                        }
                    }
                    else if (objectIn instanceof ArrayList<?>) // In this case, assume server is returning a list of
                                                               // active players
                    {
                        // cast to proper object type
                        ArrayList<Player> players = (ArrayList<Player>) objectIn;
                        System.out.println("Printing player array list");
                        for (Player p : players)
                        {
                            System.out.println(p.getID());
                            if (p.getID() != controller.getUser().getID()) // Don't add player if it's the controller's
                                                                           // own user. Only add other players.
                            {
                                controller.addPlayer(p);
                                System.out.println("Client: Added new player (above) via player arraylist");
                            }
                        }
                        System.out.println("Done");
                        
                        // If this is the first person to join, set their controller as the primary
                        if (players.size() == 1)
                        {
                            controller.setPrimary(true);
                        }
                    }
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }
}
