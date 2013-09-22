/*
 * Created on Jul 19, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package fang;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.Observable;
import java.util.Observer;

import javax.swing.Timer;


/**
 * The Client traps input from the user and
 * sends it to the Server asynchronously.
 * It also receives regular updates from the
 * Server for the mouse and keyboard inputs
 * and the current time, then updates and
 * refreshes the screen.
 * @author Jam Jenkins
 */

public class Client implements Observer
{
    /**the total number of sends from this client*/
    private long sends;
    /**the time of the last send*/
    private long sendTime=-1;
    /**the min, average, and max round trip times*/
    private long[] rtt=new long[3];
    
    /**
     * id of this client. Ids start at zero and 
     * go to the number of clients - 1.
     */
    private int id;

    /**
     * all of the keyboards of the computers connected
     */
    public Keyboard[] keyboard;

    /**
     * all of the mouses of the computers connected
     */
    public Mouse[] mouse;

    /**
     * used to receive general messages 
     * from the Server
     */
    public Object[] message;

    /**
     * the Keyboard trapping local keyboard events
     */
    private Keyboard localKeyboard;

    /**
     * the Mouse trapping local mouse events
     */
    private Mouse localMouse;

    /**
     * used to send general messages to the
     * Server in networked games
     */
    private Object localMessage;

    /**
     * the frameAdvancer is used for updating
     * the model and screen upon receiving server
     * information
     */
    private GameLoop frameAdvancer;

    /**
     * the inbound connection. This is just a single 
     * connection with the server and it is used to 
     * receive the array of mouses and keyboards and 
     * the current time to keep consistent clocks. For
     * servers, there is one connection per client 
     * used to receive the individual keyboards and 
     * mouses.
     */
    private ObjectInputStream in;

    /**
     * the outbound connection. This is just a single 
     * connection with the server used to write this 
     * client's keyboard and mouse. For servers, there 
     * is one connection per client used to write the 
     * keyboard and mouse arrays and the current time.
     */
    private ObjectOutputStream out;
    
    /** the timer used to poll for new server information*/
    private Timer readTimer;

    /** the name of the current game*/
    private String game;
    
    /** the name of the currently running session*/
    private String session;

    /** the domain the server is on */
    private String machine;

    /** the number of players */
    private int players;

    /** pause is toggled by settting sendPause to true*/
    private boolean sendPause;
    
    /** since server communication partially ceases 
     * during a puased game, this variable being set 
     * to true means the game is paused (not 
     * necessarily lost connections to server)
     */
    private boolean isPaused = true;

    /**set when the message for pausing
     * has been sent, but the pause message
     * from the server has not yet arrived*/
    private boolean pausing=false;
    
    /**indicates if any information at the
     * local Client has changed and needs
     * to be transmitted to the server
     */
    private boolean changed = true;
    
    /**
     * sets the domain, game and session name and
     * connects to the server.
     * 
     * @param machine
     *            the domain of the server
     * @param game the name of the game
     * @param session the name of the current session
     * @param players the number of players to wait for 
     * (only used for the first person starting the session)
     * @param frameAdvancer the model/view to update         
     */
    public Client(String machine, 
            String game, 
            String session, 
            int players,
            GameLoop frameAdvancer)
    {
        this.frameAdvancer=frameAdvancer;
        this.machine = machine;
        this.game = game;
        this.session = session;
        this.players = players;
        connectToServer();
        ReaderThread reader = new ReaderThread();
        readTimer = new Timer(GameConnections.TIME_BETWEEN_UPDATES/10, reader);
    }
    
    public static String[] getWaitingSessions(
    		String machine, 
            String game)
    {
    	LinkedList<String> waitingSessions=new LinkedList<String>();
    	try
    	{
	        Socket socket = new Socket(machine, Server.PORT);
	        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
	        ObjectInputStream in = new ObjectInputStream(
	        		new BufferedInputStream(socket.getInputStream(), 1000));
	        out.writeObject("List Games");
	        out.flush();
	        String allGames = (String) in.readObject();
	        for(String line: allGames.split("\n"))
	        {
	        	String[] elements=line.split(" ");
	        	String gameName=elements[1];
	        	String sessionName=elements[3];
	        	if(gameName.equals(game))
	        		waitingSessions.add(sessionName);
	        }
	        out.writeObject("Quit");
	        out.flush();
	        socket.close();
    	}
    	catch(Exception e)
    	{
    		
    	}
    	return waitingSessions.toArray(new String[0]);
    }

    /**clients can send a generic message to all
     * of the connected clients via the server.
     * The message being sent must be Serializable.
     * If the object being sent is not already
     * Serializable, all that is normally required
     * is that the object implement Serializable.
     * @param localMessage
     *            The localMessage to set.
     */
    public void setLocalMessage(Object localMessage)
    {
        this.localMessage = localMessage;
        update(null, null);
    }

    /**sets the Client's GameLoop.  This GameLoop
     * will be updated upon receiving transmissions
     * from the Server.
     * @param frameAdvancer
     *            The frameAdvancer to set.
     */
    public void setGameLoop(GameLoop frameAdvancer)
    {
        this.frameAdvancer = frameAdvancer;
    }

    /**sets the Client's local Keyboard.  This is
     * the keyboard which will be used for generating
     * messages to the Server about the newly pressed
     * key.
     * @param localKeyboard
     *            The localKeyboard to set.
     */
    public void setLocalKeyboard(Keyboard localKeyboard)
    {
        this.localKeyboard = localKeyboard;
    }

    /**sets the Client's local Mouse.  Ths is the
     * mouse which will be used for generating
     * messages to the Server when the mouse changes
     * position or is clicked.
     * @param localMouse
     *            The localMouse to set.
     */
    public void setLocalMouse(Mouse localMouse)
    {
        this.localMouse = localMouse;
        localMouse.setCanvas(frameAdvancer.getCanvas());
    }

    /**tries to connect to the designated machine and port
     * @throws Exception if no server is running on the
     * machine and port
     */
    private void tryToConnect() throws Exception
    {
        Socket socket = new Socket(machine, Server.PORT);
        out = new ObjectOutputStream(socket.getOutputStream());
        in = new ObjectInputStream(new BufferedInputStream(socket
                .getInputStream(), 1000));
        out.writeObject("Join " + game + " " + session + " " + players);
        out.flush();
        String success = (String) in.readObject();
        if (!success.equals("Success"))
        {
            out.close();
            in.close();
            out=null;
            in=null;
            return;
        }
        id = in.readInt();
        int left=in.readInt();
        while(left>0)
        {
            left=in.readInt();
            if(left==1)
            	frameAdvancer.setLoadMessage("Waiting for "+
            			left+" player to join.");
            else
            	frameAdvancer.setLoadMessage("Waiting for "+
            			left+" players to join.");
        }
        frameAdvancer.setLoadMessage("Loading Game...");
        keyboard = (Keyboard[]) in.readObject();
        mouse = (Mouse[]) in.readObject();
        message = (Object[]) in.readObject();
        setMouseBoundary();
    }

    /**starts piped input and output to the local server
     * used during one-player games.
     * @param server the local server
     * @throws Exception if the connection cannot be established
     */
    private void tryToConnect(Server server) throws Exception
    {
        out = server.getPipedOutput();
        in = server.getPipedInput();
        server.start();
        out.writeObject("Join " + game + " " + session + " " + players);
        out.flush();
        Object success = in.readObject();
        if (!success.equals("Success"))
        {
            out.close();
            in.close();
            out=null;
            in=null;
            return;
        }
        id = in.readInt();
        int left=in.readInt();
        while(left>0)
        {
            left=in.readInt();
            frameAdvancer.setLoadMessage("Waiting for "+
                left+" players to join.");
        }
        frameAdvancer.setLoadMessage("Loading Game...");
        keyboard = (Keyboard[]) in.readObject();
        mouse = (Mouse[]) in.readObject();
        message = (Object[]) in.readObject();
        setMouseBoundary();
    }
    
    /**
     * the Mouse is not allowed to send positions
     * outside of the boundary of the AnimationCanvas
     * even if these MouseEvents are trapped.  This
     * method allows each Mouse to keep track of it's
     * boundaries to avoid out of bounds mice.
     */
    private void setMouseBoundary()
    {
        for(Mouse m: mouse)
            m.setCanvas(frameAdvancer.getCanvas());
    }

    /**determines if this Client has open
     * communications to the Server
     * @return true if there are open communications,
     * false otherwise
     */
    public boolean isConnected()
    {
        return out!=null;
    }
    
    /**gets the name of the localhost on the network
     * @return the name of the localhost on the network
     */
    public static String getHostname()
    {
        try
        {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException uhe)
        {
            return "localhost";
        }
    }
    
    /**
     * tries to connect to an existing server.  If no
     * server exists, a server is started on the local
     * machine.  If a one player game is started, pipes
     * are set up to connect since outside connections
     * will not be necessary.  If a multiplayer game is
     * started, then a server is started on the local
     * machine which can accept outside connections.
     *
     */
    private void connectToServer()
    {
        try
        {
        	if (players==1)
        	{
   				Server server = new Server(null);
   				tryToConnect(server);
        	}
        	else
        	{
        		if (machine.equals("localhost") ||
        			machine.equals(getHostname()))
        		{
        			try
        			{
        				new Server().start();
        			}
        			catch(Exception e){}
        		}
        		tryToConnect();
        	}
        } catch (Exception ioe)
        {
            ioe.printStackTrace();
        }
    }

    /**
     * If the game is paused, sends nothing unless
     * it is the message to resume.
     * If the game is not paused, writes this client's:
     * <ol>
     * <li>keyboard
     * <li>mouse
     * <li>message
     * </ol>
     * if they have changed.
     * @throws IOException if the connection to the server
     * is disrupted
     */
    private void write() throws IOException
    {
        if (isPaused && !sendPause)
            return;
        out.writeBoolean(changed);
        if(changed)
            changed=false;
        else
            return;
        localKeyboard.writeExternal(out);
        localMouse.writeExternal(out);
        localKeyboard.clear();
        localMouse.clearClicks();
        if (!localMouse.pressed())
            localMouse.clear();
        if (localMessage == null)
            out.writeBoolean(false);
        else
        {
            out.writeBoolean(true);
            out.writeObject(localMessage);
            localMessage=null;
        }
        out.writeBoolean(sendPause);
        sendPause = false;
        out.flush();
        out.reset();
        sendTime=System.currentTimeMillis();
        sends++;
    }

    public void clearInput()
    {
        localKeyboard.clear();
        localMouse.clearClicks();
        for (int i = 0; i < keyboard.length; i++)
        {
            keyboard[i].clear();
            mouse[i].clearClicks();
            message[i] = null;
        }        
    }
    
    /**
     * if the game was paused, this method will
     * set it running again.  If the game was not
     * paused, this method will pause the game.
     */
    public void pauseToggle()
    {
    	if(pausing)
    		return;
        sendPause = true;
        update(null, null);
        pausing=true;
    }

    /**
     * reads in from the server:
     * <ol>
     * <li>current time
     * <li>if the are changed since the last 
     * message received it also reads
     * <ol> 
     * <li>keyboard array
     * <li>mouse array
     * <li>message array
     * </ol>
     * </ol>
     * Sends an immediate acknowledgement upon receiving the
     * entire transmission, then updates the GameLoop with
     * the current time and the new information from the
     * keyboard, mouse, and messages from each client.
     * @throws IOException if the connection to the server is disupted
     * @throws ClassNotFoundException if there are class loading problems
     */
    private boolean read() throws IOException, ClassNotFoundException
    {
        if (in.available() <= 0)
            return false;
        double timeAbsolute = in.readDouble();
        if(in.readBoolean())
        {
            for (int i = 0; i < keyboard.length; i++)
            {
                keyboard[i].clear();
                mouse[i].clearClicks();
                keyboard[i].readExternal(in);
                mouse[i].readExternal(in);
                if (in.readBoolean())
                {
                    message[i] = in.readObject();
                }
                else
                {
                    message[i] = null;
                }
            }
            boolean wasPaused=isPaused;
            isPaused = in.readBoolean();
            if(isPaused!=wasPaused)
            {
                pausing=false;
                frameAdvancer.serverSaysPauseToggle();
            }
        }
        update(null, null);
        frameAdvancer.updateModel(timeAbsolute);
        if(sendTime>0)
        {
            long currentTime=System.currentTimeMillis();
            rtt[0]=Math.min(rtt[0], currentTime-sendTime);
            rtt[1]+=currentTime-sendTime;
            rtt[2]=Math.max(rtt[2], currentTime-sendTime);
            sendTime=-1;
            //if(sends%100==0)
            //    System.out.println("rtt- min: "+rtt[0]+
            //        "avg: "+rtt[1]/sends+
            //        "max: "+rtt[2]);
        }
        return true;
    }

    /**reads repeatedly from the server and refreshes
     * the screen.  The screen is refreshed once all
     * reads have been exhausted and at least one read
     * has taken place.
     * @author Jam Jenkins
     */
    class ReaderThread implements ActionListener
    {
        public void actionPerformed(ActionEvent ae)
        {
            try
            {
                if(!read())
                    return;
                while(read())
                    ;
                frameAdvancer.refreshScreen();
            } 
            catch (Exception e)
            {
                disconnect();
                e.printStackTrace();
            }
        }
    }

    /**
     * Connects to the server then goes into an infinite 
     * loop sending and receiving consistency information 
     * from and to the server. See class comment for 
     * further details
     * 
     * @see java.lang.Runnable#run()
     */
    public void connect()
    {
        localKeyboard.setObserver(this);
        localMouse.setObserver(this);
        readTimer.start();
    }

    /**
     * disconnects and terminates sending and receiving
     * informations to and from the server
     */
    public void disconnect()
    {
        isPaused=true;
        readTimer.stop();
        try
        {
            in.close();
            out.close();
        } catch (IOException ioe)
        {
        }
    }

    /**determines if the game is currently advancing
     * @return true if the game has temporarily stopped
     * advancing, false otherwise
     */
    public boolean isPaused()
    {
        return isPaused;
    }
    
    /**gets the id of the current client.  This id
     * corresponds to the index of this client in 
     * the array of mouse and keyboard information 
     * received from the client.
     * @return the id of the current client, also
     * the same as the index of this client in the
     * playerMouse and playerKeyboard arrays
     */
    public int getID()
    {
        return id;
    }
    
    /**
     * this method is called when there is new information
     * to send to the server.  It is called when new keyboard
     * and/or mouse information is received, when the game
     * is paused or resumed, or when a message is sent.
     * @param arg0 not used
     * @param arg1 not used
     * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
     */
    public void update(Observable arg0, Object arg1)
    {
        try
        {
            if(pausing || isPaused)
            {
                localKeyboard.clear();
                localMouse.clearClicks();
            }
            changed=true;
            write();
        } catch (IOException e)
        {
            disconnect();
            e.printStackTrace();
        }
    }
}
