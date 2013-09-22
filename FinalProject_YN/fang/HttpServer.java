/*
 * Created on Jul 19, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package fang;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * This establishes incoming connections and
 * directs them to the appropriate GameConnection.
 * The Server can be run continually on a dedicated
 * computer to serve all multiclient games.
 * @author Jam Jenkins
 */
public class HttpServer extends Thread
{
    /**
     * port on which to run the server
     */
    public static int PORT = 1554;

    /**
     * map of currently running games to the GameConnections
     */
    private HashMap<String, GameConnections> games = 
        new HashMap<String, GameConnections>();

    /**
     * true indicates still accepting new connections,
     * false indicates accepting no more connections
     */
    private boolean connected = true;

    /**
     * the output stream used for applets
     */
    private ObjectOutputStream pipedOut;

    /**
     * the input stream used for applets
     */
    private ObjectInputStream pipedIn;

    /**
     * the socket listening for new connections
     */
    private ServerSocket serverSocket;

    /**the constructor to use when using pipes*/
    public HttpServer(Object dummy)
    {
    }
    
    /**the constructor to use in applications
     * @throws IOException */
    public HttpServer() throws IOException
    {
    	serverSocket = new HttpServerSocket(PORT);
    }

    /**makes a piped input stream to use in applets
     * @return the piped input stream connected to the server
     */
    public ObjectInputStream getPipedInput()
    {
        try
        {
            PipedOutputStream out = new PipedOutputStream();
            PipedInputStream in = new PipedInputStream();
            out.connect(in);
            pipedOut = new ObjectOutputStream(out);
            return new ObjectInputStream(in);
        } catch (IOException ioe)
        {
            ioe.printStackTrace();
            pipedOut = null;
            return null;
        }
    }

    /**makes a piped output stream to use in applets
     * @return the piped output stream connected to the server
     */
    public ObjectOutputStream getPipedOutput()
    {
        try
        {
            PipedInputStream in = new PipedInputStream();
            PipedOutputStream out = new PipedOutputStream();
            in.connect(out);
            ObjectOutputStream toReturn = new ObjectOutputStream(out);
            pipedIn = new ObjectInputStream(in);
            return toReturn;
        } catch (IOException ioe)
        {
            ioe.printStackTrace();
            pipedIn = null;
            return null;
        }
    }

    /**
     * starts pipe communications
     */
    public void startPipes()
    {
        if (pipedOut != null && pipedIn != null)
        {
            new Dispatcher(pipedOut, pipedIn).start();
        } else
            System.err.println("cannot start pipes");
        // seems like a bug in the JDK, but this
        // thread cannot end, or the Pipe breaks.
        synchronized (this)
        {
            try
            {
                wait();
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    /**
     * connects to a client and forwards the connections
     * to the proper GameConnection
     * 
     * @throws Exception
     *             if an error occurs while establishing the initial connections
     */
    public void connect()
    {

    	while (connected)
    	{
    		Socket socket=null;
    		try
    		{
			java.lang.System.out.println("accepting connection");
    			socket= serverSocket.accept();
			java.lang.System.out.println("accepted connection");
    		}
    		catch (IOException ioe)
    		{
    			ioe.printStackTrace();
    			break;
    		}
    		try
    		{
			java.lang.System.out.println("server making out");
    			OutputStream oStream=socket.getOutputStream();
			oStream.write("?PNG".getBytes());
			oStream.write(13);
			oStream.write(10);
			oStream.write(26);
			oStream.write(10);
			oStream.flush();
			ObjectOutputStream out = new ObjectOutputStream(oStream);
			//out.writeObject("hello\n");
			out.flush();
			java.lang.System.out.println("server making in");
    			ObjectInputStream in = new ObjectInputStream(
    				new BufferedInputStream(socket.getInputStream(), 100));
			java.lang.System.out.println("server dispatching");
    			new Dispatcher(out, in).start();
    		}
    		catch (IOException ioe)
    		{
    			ioe.printStackTrace();
    		}
    	}
    }

    /**
     * stops the loop from accepting new connections
     */
    public void disconnect()
    {
        connected = false;
    }

    /**starts the pipes for applets, or the server for applications
     * @see java.lang.Runnable#run()
     */
    public void run()
    {
        if (pipedIn == null && pipedOut == null)
            connect();
        else
            startPipes();
    }

    /**communicates with the client to determine which game
     * to join
     * @author Jam Jenkins
     */
    class Dispatcher extends Thread
    {
        /**
         * the output stream to the client
         */
        ObjectOutputStream out;

        /**
         * the input stream from the client
         */
        ObjectInputStream in;

        /**stores the streams for communication
         * @param out the stream to the client
         * @param in the stream from the client
         */
        public Dispatcher(ObjectOutputStream out, ObjectInputStream in)
        {
            this.out = out;
            this.in = in;
        }

        /**responds to a join command.  The format of the join command is:
         * Join [gameName] [sessionName] [players]
         * players is optional.  If not specified, 2 players is assumed.
         * @param command the line in the format described above
         * @return true if successful, false otherwise.  Joining can be
         * unsuccessful if the format is not correct or if the game trying
         * to join is full.
         * @throws IOException if the communication chanel becomes corrupt
         */
        private boolean joinGame(String command) throws IOException
        {
            String[] parts = command.split(" ");
            String gameName = parts[1];
            String sessionName = parts[2];
            String id = "Game: " + gameName + " Session: " + sessionName;
            int players = 2;
            if (parts.length > 3)
                players = Math.max(1, Integer.parseInt(parts[3]));
            GameConnections connections = games.get(id);
            if (connections == null)
            {
                connections = new GameConnections(gameName, sessionName,
                        players);
                games.put(id, connections);
            }
            if (connections.isFull())
            {
                out.writeObject("Game already full");
                out.flush();
                return false;
            }
            boolean results=connections.addConnection(out, in);
            if(connections.isFull())
                games.remove(id);
            return results;
        }

        /**sends a list of the games currently in progress to the client.
         * The format of the list is:
         * Game: [gameName] Session: [sessionName]
         * @throws IOException
         */
        private void listGames() throws IOException
        {
            String[] gameIDs = games.keySet().toArray(new String[0]);
            String sum = "happy\n";
            for (String name : gameIDs)
            {
                if(!games.get(name).isFull())
                    sum += name + "\n";
            }
	java.lang.System.out.println("writing "+sum);
            out.writeObject(sum);
            out.flush();
	java.lang.System.out.println("flushed "+sum);
        }

        /**
         * gets rid of games no longer in progress
         */
        private void cleanEmptyGames()
        {
            Set<Map.Entry<String, GameConnections>> gameSet = games.entrySet();
            LinkedList<String> toRemove = new LinkedList<String>();
            for (Map.Entry<String, GameConnections> game : gameSet)
            {
                if (!game.getValue().isActive())
                    toRemove.add(game.getKey());
            }
            for (String key : toRemove)
            {
                games.remove(key);
            }
        }

        /**reads commands from the client and resonds appropriately
         * the two commands are
         * List Games
         * and
         * Join [gameName] [sessionName] [players]
         * @see java.lang.Runnable#run()
         */
        public void run()
        {
            try
            {
                String command;
                while (true)
                {
		java.lang.System.out.println("awaiting command");
                    command = (String) in.readObject();
		java.lang.System.out.println("performing: "+command);
                    cleanEmptyGames();
                    if (command.startsWith("Join"))
                    {
                        if (joinGame(command))
                            break;
                    } else if (command.startsWith("List Games"))
                    {
                        listGames();
                    }else if(command.startsWith("Quit"))
                    	return;
                }
                // seems like a bug in the JDK, but this
                // thread cannot end, or the Pipe breaks.
                synchronized (this)
                {
                    int players=Integer.parseInt(command.split(" ")[3]);
                    if(players==1)
                    {
                        wait();
                    }
                }
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    /**starts the server accepting new connections
     * @param argv not used
     * @throws IOException 
     */
    public static void main(String[] argv) throws IOException
    {
        new HttpServer().connect();
    }
}
