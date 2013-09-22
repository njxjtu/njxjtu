/*
 * Created on Jul 19, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package fang;

import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

import fang.Keyboard;
import fang.Mouse;


/**
 * The GameConnections store the input and output
 * connections to the Client.
 * They are used to receive input from each Client
 * and send that input to all connected Clients.
 * @author Jam Jenkins
 */
public class GameConnections
{
    /**
     * all of the keyboards of the computers connected
     */
    private Keyboard[] keyboard;

    /**
     * all of the mouses of the computers connected
     */
    private Mouse[] mouse;

    /**
     * the state of the advancing of the game
     */
    private boolean isPaused = true;

    /** time between sending updates to the clients */
    public static final int TIME_BETWEEN_UPDATES = 40;

    /** how often to send messages when paused.
     * This represents the number of times slower
     * the heartbeat messages are than the normal
     * running game messages.*/
    public static final int HEART_BEAT=100;
    
    /** used for sending general messages between Clients */
    private Object[] message;
    
    /**since only one message can be sent per frame,
     * messages are queued until they can be sent if
     * messages accumulate
     */
    private LinkedList[] pendingMessages;
    
    /**since more keystrokes than one could
     * occur in the period between server
     * transmissions, this will keep sending
     * them once received if they build up
     */
    private LinkedList<Character>[] pendingKeystrokes;
    
    /** used to repeatedly write to the clients */
    private Timer timer;
    
    /** dirty bit indicating if the server has read new
     * information from the clients.  This is set to false
     * after each server message is sent and set to true
     * when any client information is read.
     */
    private boolean hasReadClients = true;

    /**
     * all connections inbound. For clients this is just 
     * a single connection with the server and it is used 
     * to receive the array of mouses and keyboards and 
     * the current time to keep consistent clocks. For
     * servers, there is one connection per client used 
     * to receive the individual keyboards and mouses.
     */
    protected ObjectInputStream[] in;

    /**
     * all connections outbound. For clients this is 
     * just a single connection with the server used 
     * to write this client's keyboard and mouse. For
     * servers, there is one connection per client 
     * used to write the keyboard and mouse arrays 
     * and the current time.
     */
    private ObjectOutputStream[] out;

    /** the state of the game connections to the server.
     * True indicates everything is connected and active.
     * False indicates disconnected or not yet connected
     * streams.
     */
    private boolean[] activeStreams;

    /** the global clock time in seconds.  Starts at zero. */
    private double currentTime=-1;

    /** the number of frames which have
     * been paused since the last transmission.
     * This variable is used to periodically
     * send blank messages to detect broken
     * lines of communication.  If all lines
     * of communication for this game are
     * closed, then the thread should terminate.
     */
    private int pauseCount;
    
    /**constructs a server without blocking.
     * @param gameName the name of the game being played
     * @param sessionName the name of the current session for a given game
     * @param players
     *            the number of clients connecting
     */
    @SuppressWarnings("unchecked")
	public GameConnections(String gameName, 
            String sessionName, int players)
    {
        in = new ObjectInputStream[players];
        out = new ObjectOutputStream[players];
        keyboard = new Keyboard[players];
        mouse = new Mouse[players];
        message = new Object[players];
        pendingMessages=new LinkedList[players];
        pendingKeystrokes=new LinkedList[players];
        activeStreams = new boolean[players];
        for (int i = 0; i < keyboard.length; i++)
        {
            keyboard[i] = new Keyboard();
            mouse[i] = new Mouse();
            message[i] = new Long(System.currentTimeMillis());
            activeStreams[i] = false;
            pendingMessages[i]=new LinkedList();
            pendingKeystrokes[i]=new LinkedList<Character>();
        }
    }

    /**determines if all connections are alive
     * @return true if all connections are active,
     * false otherwise
     */
    public boolean isFull()
    {
        for (boolean active : activeStreams)
            if (!active)
                return false;
        return true;
    }
    
    /**determines how many players are
     * left to join
     * @return the remaining connections
     * to be made to other players
     */
    private int getPlayersLeft()
    {
        int left=0;
        for (boolean active : activeStreams)
            if (!active)
                left++;
        return left;        
    }

    /**determines if any stream is active
     * @return true if any stream is available
     * for sending and receiving information,
     * false if all connections have been terminated
     */
    public boolean isActive()
    {
    	if(currentTime<0)
    		sendJoinedMessage(getPlayersLeft());
        for (boolean activity : activeStreams)
            if (activity)
                return true;
        return false;
    }

    /**determines where the next active stream should be added
     * @return the first available stream position
     */
    private int getFirstInactiveStream()
    {
        int i = 0;
        while (i < activeStreams.length && 
                activeStreams[i])
            i++;
        return i;
    }

    /**sends the information the client 
     * needs to start communications:
     * <ol>
     * <li>keyboard array
     * <li>mouse array
     * <li>message array
     * </ol>
     * This method sends the arrays as complete 
     * objects the first time so that the existing
     * objects can simply be updated on later 
     * transmissions.
     * @throws IOException if there is an interruption
     * in the connection to the client
     */
    private void sendFirstClientMessage()
        throws IOException
    {
        for (ObjectOutputStream o : out)
        {
            o.writeObject(keyboard);
            o.writeObject(mouse);
            o.writeObject(message);
            o.flush();
            o.reset();
        }        
    }

    /**sends the number of players left to join
     * @param left the number of players left to 
     * join before the game goes live
     */
    private void sendJoinedMessage(int left)
    {
        for (int j=0; j<out.length; j++)
        {
            ObjectOutputStream o=out[j];
            if(!activeStreams[j])
                continue;
            try
            {
                o.writeInt(left);
                o.flush();
                o.reset();
            }
            catch(IOException ioe)
            {
                handleBrokenConnection(j);
            }
        }    
    }

    
    /**handles exceptions of broken connections.  If the
     * game has yet to start, then it just clears the active
     * stream to make room for another to connect.  If all
     * of the streams were active, then consistency can never
     * be regained with a re-connection, so all connections
     * are terminated at the beginning of the game.
     * @param clientIndex the index of the failed connection
     */
    private void handleBrokenConnection(int clientIndex)
    {
        if (isFull())
        {
            for (int j = 0; j < out.length; j++)
            {
                try
                {
                    out[j].close();
                    in[j].close();
                } catch (IOException io)
                {
                }
                activeStreams[j] = false;
                out[j] = null;
                in[j] = null;
            }
        } 
        else if (clientIndex < activeStreams.length)
            activeStreams[clientIndex] = false;    
    }
    
    /**
     * adds a connection to the server's list of clients.
     * This method does not block.
     * @param outStream the stream to the client
     * @param inStream the stream from the client
     * @throws Exception
     *             if an error occurs while establishing the initial connections
     */
    public boolean addConnection(
            ObjectOutputStream outStream,
            ObjectInputStream inStream)
    {
        //don't allow to join games in session
        if(currentTime>0)
        {
            try
            {
                outStream.writeObject("Game already in play.");
                outStream.flush();
            }
            catch(IOException ioe)
            {
                ioe.printStackTrace();
            }
            return false;
        }
        int activationIndex = getFirstInactiveStream();
        if (activationIndex == out.length)
        {
            try
            {
                outStream.writeObject("Game already full.");
                outStream.flush();
            }
            catch(IOException ioe)
            {
                ioe.printStackTrace();
            }
            return false;
        }
        try
        {
            activeStreams[activationIndex] = true;
            out[activationIndex] = outStream;
            in[activationIndex] = inStream;
            out[activationIndex].writeObject("Success");
            out[activationIndex].flush();
            out[activationIndex].writeInt(activationIndex);
            out[activationIndex].flush();
            //this write discovers any disconnected clients
            sendJoinedMessage(1);
            //so this write gives the correct number
            sendJoinedMessage(getPlayersLeft());
            if (isFull())
            {
            	currentTime=0;
                sendFirstClientMessage();
                startThreads();
            }
            return true;
        } catch (IOException ioe)
        {
            ioe.printStackTrace();
            handleBrokenConnection(activationIndex);
            return false;
        }
    }
    

    /**
     * reads any available new information from the
     * clients.  This method does not block.
     */
    private void readAvailable()
    {
        for(int i=0; i<out.length; i++)
        {
            if(readAvailable(i))
            {
                hasReadClients=true;
            }
        }        
    }

    /**writes an update to the client.  If no reads from
     * clients have been made since last transmission, only
     * the new time is sent.  Otherwise all arrays are transmitted.
     * Note: the writes are not flushed in this method.
     * @param clientIndex the client to write to
     * @throws IOException if an error occurs during writing
     */
    private void writeClient(int clientIndex)
        throws IOException
    {
        out[clientIndex].reset();
        out[clientIndex].writeDouble(currentTime);
        //write out the arrays only if there
        //is new information
        if(!hasReadClients)
        {
            out[clientIndex].writeBoolean(false);
        }
        else
        {
            out[clientIndex].writeBoolean(true);
            for (int j = 0; j < keyboard.length; j++)
            {
                keyboard[j].writeExternal(out[clientIndex]);
                mouse[j].writeExternal(out[clientIndex]);
                if (message[j] == null)
                    out[clientIndex].writeBoolean(false);
                else
                {
                    out[clientIndex].writeBoolean(true);
                    out[clientIndex].writeObject(message[j]);
                }
            }
            out[clientIndex].writeBoolean(isPaused);
        }        
    }
    
    /**writes an update to all client.  If no reads from
     * clients have been made since last transmission, only
     * the new time is sent.  Otherwise all arrays are transmitted.
     * Note: the writes are not flushed in this method.
     */
    private void writeClients()
    {
        for (int i = 0; i < out.length; i++)
        {
            try
            {
                if(activeStreams[i])
                    writeClient(i);
            } catch (IOException ioe)
            {
                activeStreams[i] = false;
            }
        } 
        hasReadClients=false;
    }
    
    /**
     * flushes what's currently in the output streams to the
     * clients.  This method is provided to make a best effort
     * at notifying all clients of the new information at the
     * same time.
     */
    private void flushClients()
    {
        for (int i = 0; i < out.length; i++)
        {
            try
            {
                out[i].flush();
                //out[i].reset();
            } catch (IOException ioe)
            {
                // ioe.printStackTrace();
                activeStreams[i] = false;
            }
        }
    }
    
    /**
     * clears all keys typed and mouses clicked to
     * make sure they are only sent once.
     */
    private void clearInputDevices()
    {
        for (int j = 0; j < keyboard.length; j++)
        {
            mouse[j].clearClicks();
            if(pendingKeystrokes[j].isEmpty())
                keyboard[j].clear();
            else
            {
                keyboard[j].setLastKey(pendingKeystrokes[j].removeFirst());
                hasReadClients=true;
            }
            if (!mouse[j].pressed())
                mouse[j].clear();
            if(pendingMessages[j].isEmpty())
                message[j]=null;
            else
            {
                message[j]=pendingMessages[j].removeFirst();
                hasReadClients=true;
            }
        }
    }
    
    /**
     * writes to all clients if the game is not paused
     */
    public void write()
    {
       readAvailable();
       //periodically send a message if the
       //game is paused to make sure the lines
       //of communication are still open
       if(isPaused)
       {
           pauseCount++;
           if(pauseCount%HEART_BEAT==0)
           {
               pauseCount=0;
               writeClients();
               flushClients();
           }
           return;
       }
       pauseCount=0;
       currentTime += TIME_BETWEEN_UPDATES / 1000.0;
       writeClients();
       flushClients();
       clearInputDevices();
    }

    /**reads what's available from the given client without
     * blocking
     * @param clientIndex the client to read from
     * @return true if new information was read, false if
     * new information was not available from the client
     */
    @SuppressWarnings("unchecked")
	public boolean readAvailable(int clientIndex)
    {
        try
        {
            boolean changed=false;
            if(in[clientIndex].available() <= 0)
                return changed;
            while(in[clientIndex].available() > 0)
            {
                if(!in[clientIndex].readBoolean())
                    continue;
                if(keyboard[clientIndex].getLastKey()!=KeyEvent.CHAR_UNDEFINED)
                {
                    
                    char oldKey=keyboard[clientIndex].getLastKey();
                    keyboard[clientIndex].setLastKey(KeyEvent.CHAR_UNDEFINED);
                    keyboard[clientIndex].readExternal(in[clientIndex]);
                    if(keyboard[clientIndex].getLastKey()!=KeyEvent.CHAR_UNDEFINED)
                    {
                        pendingKeystrokes[clientIndex].addLast(keyboard[clientIndex].getLastKey());
                    }
                    keyboard[clientIndex].setLastKey(oldKey);
                }
                else
                    keyboard[clientIndex].readExternal(in[clientIndex]);
                mouse[clientIndex].readExternal(in[clientIndex]);
                if (in[clientIndex].readBoolean())
                {
                    Object object=in[clientIndex].readObject();
                    if(message[clientIndex]!=null)
                        pendingMessages[clientIndex].addLast(object);
                    else
                        message[clientIndex] = object;
                }
                if (in[clientIndex].readBoolean())
                {
                    isPaused = !isPaused;
                }
                changed=true;
            }
            return changed;
        }
        catch (Exception e)
        {
            activeStreams[clientIndex] = false;
            e.printStackTrace();
            return false;
        }
    }
        

    /**used to repeatedly write to the clients
     * @author Jam Jenkins
     */
    class WriterThread extends TimerTask
    {
        /**writes to the clients once*/
        public void run()
        {
            write();
            if (!isActive())
            {
                synchronized(in)
                {
                    in.notifyAll();
                }
                this.cancel();
            }
        }
    }

    /**
     * necessary to keep the timer alive.
     * If the current thread were to die,
     * then all corresponding times would
     * be cancelled.
     */
    private void keepThreadAlive()
    {
        synchronized(in)
        {
            try
            {
                in.wait();
            }
            catch(InterruptedException ioe)
            {
            }
        }
    }
    
    /**
     * starts the writing to the clients.  This method
     * will never terminate.
     */
    public void startThreads()
    {
        currentTime = 0;
        timer=new java.util.Timer(true);
        timer.scheduleAtFixedRate(new WriterThread(), 0,
                TIME_BETWEEN_UPDATES);
        //must keep thread alive to make sure
        //the timer continues to be active
        keepThreadAlive();
    }
}
