package asteroids.network;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class GameNetworkLoop extends Thread
{
    /* Name of the thread */
    private String name;
    
    /* Socket that this thread will interact with */
    Socket socket;

    public GameNetworkLoop (String name, Socket socket)
    {
        this.name = name;
        this.socket = socket;
    }

    @Override
    public void run ()
    {
        System.out.println("GameNetowrkLoop thread '" + name + "' starting.");

        try
        {
            //Make an input stream to read incoming GameUpdate objects
            // This is a only way to get a buffered object input stream, since such an object doesn't exist 
            ObjectInputStream serverIn = new ObjectInputStream(socket.getInputStream());

            // Make an output stream so that server can send GameUpdate objects to client
            ObjectOutputStream serverOut = new ObjectOutputStream(socket.getOutputStream());
            
            // Read incoming object, cast to type GameUpdate, and assign
            GameUpdate update = (GameUpdate) serverIn.readObject();
            
            // repeat until client tells this thread to stop via a GameUpdate with code="ENDCONNECTION"
            while (!update.getOperationCode().equals("ENDCONNECTION"))
            {
                serverOut.writeObject(update);
                serverOut.flush();
                System.out.println("New game update: " + update.toString());
                System.out.println("Operation code: " + update.getOperationCode());
                System.out.println("X coord: " + update.getX());
                System.out.println("Y coord: " + update.getY());
                System.out.println("Rotation: " + update.getRotation());
                update = (GameUpdate) serverIn.readObject();
            }
            
            System.out.println("Thread " + name + " attempting to shut down.");
            
            // close everything down    
            serverOut.close();
            serverIn.close();
            socket.close();
        }
        catch (Exception n)
        {
            n.printStackTrace();
        }

        System.out.println("GameNetworkLoop thread '" + name + "' has finished its run() method. You should see an exception.");
    }
}
