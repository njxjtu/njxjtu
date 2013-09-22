package fang;

import java.awt.Dimension;
import java.awt.KeyboardFocusManager;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;

import javax.swing.JOptionPane;

/**This class adds mouse and 
 * keyboard interaction to 
 * FrameAdvancer and also provides
 * a method for sending Objects to
 * the other clients.  All games using
 * the FANG Engine extend this class.
 * @author Jam Jenkins
 */
public abstract class GameLoop 
    extends FrameAdvancer
{
    
    /** seed for random numbers */
	public static final long SEED = 1;
	
	/** used for random numbers */
    public Random random = new Random(SEED);

    /**
     * used to stream keyboard/mouse information to/from the server/client
     */
    private Client client;

    /**the array of players connected*/
    public Player[] player;

    /** which index belongs to this computer */
    private int id=-1;

    /** server name */
	private String server = "localhost";

    /** name of game */
	private String gameName = "default";

    /** name of session */
	private String sessionName = "default";

	/** number of payers */
    private int numPlayers = 1;

    /** field which can be used to mute/enable sound */
    private boolean audible = false;

    /** whether the game is still going
     * or has terminated */
    private boolean gameOver=false;
    
    /**the levels in the game*/
    private ArrayList<GameLevel> levels=
        new ArrayList<GameLevel>();
    
    /**the current position in the levels ArrayList*/
    private int levelIndex=-1;
    
    /**the current level*/
    private GameLevel currentLevel;
    
    /**whether the game should restart after
     * the next advanceFrame method completes*/
    private boolean restarting=false;
    
    /**whether the game should be proceeding
     * to the game over screen or not*/
    private boolean advancingToGameOver=false;
    
    /**
     * constructs a GameLoop with the 
     * default size AnimationCanvas (given in
     * FrameAdvancer).
     */
    public GameLoop()
    {
        super();
    }
    
    /**
     * constructs a GameLoop with the given size
     * 
     * @param width the horizontal span of the display in pixels
     * @param height the vertical span of the display in pixels 
     */
    public GameLoop(int width, int height)
    {
        super(new Dimension(width, height));
    }
    
    /** switches between mute and audible */
	public void toggleAudible()
    {
        super.toggleAudible();
        this.audible=!audible;
    }
    
	/**
	 * returns true if muted, false if not muted
	 * @return WhetherMuted
	 */
    public boolean isMuted()
    {
        return !audible;
    }
    
    /**
     * constructs a GameLoop with the given size
     * 
     * @param size
     *            the size of the AnimationCanvas
     */
    public GameLoop(Dimension size)
    {
        super(size);
    }
    
    /** starts session by connecting to the
     * server */
	public void begin()
    {
        if (client == null)
        {
            connect(server, getHash(), sessionName, numPlayers);
            if(client.isConnected())
            {
            	numPlayers=client.keyboard.length;
                startGame();
                if(currentLevel==null)
                {
                	currentLevel=getNextLevel();
                	if(currentLevel!=null)
                	{
                		currentLevel.setGameLoop(this);
                    	currentLevel.startLevel();
                	}
                }
            }
            else
            {
                String errorMessage="Cannot connect to "+server+"\n"+
                    "Server unavailable or "+gameName+" full.";
                JOptionPane.showMessageDialog(null, errorMessage, 
                    "Cannot Connect!", JOptionPane.ERROR_MESSAGE);
                stop();
            }
        }
    }
	
	/** starts game.  At this point the
     * number of players is now known.
     * This method should create and
     * add sprites to the canvas.
     * This method
     * is called for you by the FANG
     * Engine and should not be called
     * directly.*/
    public void startGame()
    {
    }
    
    /**This 
     * method is where the game logic
     * will be applied.  In general
     * the game will respond to user
     * input and other events such as
     * sprite collisions.  This method
     * is called for you by the FANG
     * Engine and should not be called
     * directly.
     * @param timePassed the duration
     * of time which has passed since
     * the last frame was calculated or
     * displayed. 
     */
    public void advanceFrame(double timePassed)
    {
    }
    
    /** clears the keyboard and mouse inputs,
     * advances the level when the current level
     * terminates, and also starts the game over 
     * if requested since the last advanceFrame*/
    public void postAdvanceFrame(double timePassed)
    {
        if(!restarting && currentLevel!=null)
        {
            currentLevel.advanceFrame(timePassed);
            currentLevel.postAdvanceFrame();
        }
        for(Player p: player)
        {
            p.getMouse().clearClicks();
            p.getKeyboard().clear();            
        }
        if(advancingToGameOver)
        {
            reallyGameOver();
            gameOver=true;
            advancingToGameOver=false;
        }
        if(restarting)
        {
            restarting=false;
            reallyStartOver();
        }
    }
    
    /**
     * sets the name of the server.  This is
     * the domain name of the server to which
     * the game will connect when starting.
     * @param server the domain name of the gaming
     * server to which this game will connect
     */
    public void setServerName(String server)
    {
        this.server = server;
    }

    /**
     * sets the name of the session.  This is
     * the name of the session which must match
     * with all players in a multi-player game.
     * The name of the session must be agreed
     * upon before players can connect to each
     * other.
     * @param sessionName the name of the session
     * used to connect with other players
     */
    public void setSessionName(String sessionName)
    {
        this.sessionName = sessionName;
    }
    
    /**gets the name of the session.  This is
     * the name of the session which must match
     * with all players in a multi-player game.
     * The name of the session must be agreed
     * upon before players can connect to each
     * other.
     * @return the name of the session
     * used to connect with other players
     */
    public String getSessionName()
    {
        return sessionName;
    }
    
    /**gets the name of the server.  This is
     * the domain name of the server to which
     * the game will connect when starting.
     * @return the domain name of the gaming
     * server to which this game will/has
     * connected
     */
    public String getServerName()
    {
        return server;
    }

    /**
     * sets the name of the game.  The name
     * of the game is used in conjunction with
     * the session name to help connect players
     * to each other.  To play in the same multi-
     * player game, the game name and session
     * name must match when connecting to the
     * server
     * @param gameName the name of this game
     */
    public void setGameName(String gameName)
    {
        this.gameName = gameName;
    }

    /**
     * gets the name of this game which
     * by default is the name of the class
     * @return the name of the game.  The name
     * of the game is used in conjunction with
     * the session name to help connect players
     * to each other.  To play in the same multi-
     * player game, the game name and session
     * name must match when connecting to the
     * server
     */
    public String getGameName()
    {
        return gameName;
    }
    
    /**
     * sets the number of players.  This method
     * is only used when the first person of a
     * multi-player game connects to the server.
     * For all players connecting to the same
     * game session, all that is required is that
     * the number of players be greater than 1.
     * The person to start the session decides
     * how many players to wait for before beginning
     * the game.
     * @param numPlayers the number of players
     * to wait for before starting the game.
     */
    public void setNumberOfPlayers(int numPlayers)
    {
        this.numPlayers = numPlayers;
    }
    
    /**gets the number of connected
     * players in this game.  This method
     * may only be called once the gaming
     * engine has called the startGame
     * method.
     * @return the number of players
     * who originally connected to the
     * game
     */
    public int getNumberOfPlayers()
    {
        if(id<0)
        {
            System.err.println(
                    "Error: cannot get the number of players until\n"+
                    "all players are connected and startGame\n"+
                    "is called.  Returning -1.");
            return -1;
        }
        return numPlayers;
    }

    /**
     * sends a message to all of the clients
     * @param localMessage a Serializable
     * message which is sent to all clients
     */
    public void sendMessage(Object localMessage)
    {
        client.setLocalMessage(localMessage);
    }
    
    /**
     * connects to the server and sets the 
     * game and session name and
     * sets the number of players
     * @param server the name of the domain
     * which hosts the gaming server
     * @param gameName the name of this game
     * which is used for determining which
     * players to connect to
     * @param sessionName the name of this
     * particular session of the game.  Both
     * the game name and session name must
     * match when connecting to a group of
     * players.
     * @param players how many players to
     * wait for before connecting
     */
    public void connect(String server, String gameName, String sessionName,
            int players)
    {
        Mouse mouse = new Mouse();
        Keyboard keyboard = new Keyboard();
        canvas.addMouseListener(mouse);
        canvas.addMouseMotionListener(mouse);
        KeyboardFocusManager.getCurrentKeyboardFocusManager()
                .addKeyEventDispatcher(keyboard);
        client = new Client(server, gameName, sessionName, players, this);
        client.setLocalMouse(mouse);
        client.setLocalKeyboard(keyboard);
        client.setLocalMessage(null);
        id = client.getID();
        Keyboard[] playerKeyboard = client.keyboard;
        Mouse[] playerMouse = client.mouse;
        Object[] messages = client.message;
        if(messages[0]!=null && messages[0] instanceof Long)
        	random.setSeed((Long)messages[0]);
        for(int i=0; i<messages.length; i++)
        	messages[i]=null;
        client.connect();
        player=new Player[playerKeyboard.length];
        for(int i=0; i<player.length; i++)
            player[i]=new Player(playerKeyboard[i],
                    playerMouse[i], i, messages);
    }

    /**disconnects from the server and
     * stops the advancing of frames.  This
     * method permanently stops the game.
     */
    public void stop()
    {
        super.stop();
        disconnect();
    }
    
    /** disconnects from server.  This
     * method permanently stops the game. */
    public void disconnect()
    {
        if(client!=null)
            client.disconnect();
    }

    /** 
     * returns true if paused, false if
     * the game is running
     * @return the state of the game
     * running/paused
     */
    public boolean isPaused()
    {
        return client.isPaused();
    }
    
    /** changes between paused and unpaused */
    public void pauseToggle()
    {
        client.pauseToggle();
    }
    
    /**changes the display between pause/resume
     * when a pause message is sent from the server.
     * This method should not be called by any
     * other class than the Client.
     */
    public void serverSaysPauseToggle()
    {
        super.pauseToggle();        
    }
    
    /**
     * gets the index of the current
     * player.  This index is the one
     * that corresponds to the playerMouse,
     * keyboard and messages arrays.  This method
     * should not be called before the gaming
     * engine calls the startGame method.
     * @return the index of the player
     */
    public int getID()
    {
        if(id<0)
            System.err.println(
                    "Error: cannot get the player id until\n"+
                    "all players are connected and startGame\n"+
                    "is called.  Returning -1.");
        return id;
    }
    
    /**gets the player object for a given
     * player number.
     * @param playerIndex the player number.
     * See getNumberOfPlayers to determine
     * how many players are connected to this
     * game.
     * @return the Player object which contains
     * that user's keyboard, mouse, and any
     * messages sent
     */
    public Player getPlayer(int playerIndex)
    {
        if(playerIndex<0 || playerIndex>=player.length)
        {
            System.err.println("No such player.  There are only");
            System.err.println(player.length+" players with indexes [0, "+(player.length-1)+"]");
            System.err.println("The requested index is "+playerIndex);
            return null;
        }
        return player[playerIndex];
    }
    
    /**gets the player object for a given
     * player name.
     * @param name String name associated
     * with the Player
     * @return the Player object which contains
     * that user's keyboard, mouse, and any
     * messages sent
     */
    public Player getPlayer(String name)
    {
        for(Player play: player)
            if(play.getName().equals(name))
                return play;
        System.err.println("No such player with name: "+name+".");
        System.err.println("Player names are: ");
        for(Player play: player)
            System.err.print(play.getName()+"\t");
        return null;        
    }
    
    /**
     * loads a resource relative to the
     * package of the game.  Be careful
     * to make sure your resource name
     * matches the case of the actual
     * resource.  Some file systems are
     * not case sensitive which will cause
     * the resource to load properly when
     * developing the game, but will not
     * load properly when deployed in a
     * jar file.
     * @param resource the name of the file
     * @return the resource as a URL
     */
    public URL getResource(String resource)
    {
        URL resourceURL=getClass().getResource(resource);
        return resourceURL;
    }
    
    /**determines if the game is over.  When the
     * game is over, the game over level (if there
     * is one) is currently running.
     * @return true if the game is over, false
     * otherwise
     */
    public boolean gameIsOver()
    {
        return gameOver; 
    }
     
    /**sets the game state to gameOver.  What this
     * does is sets the game over level (if there
     * is one) to active and the current leve (if
     * there is one) to inactive.  From the game over
     * level, it is possible to restart the game.
     * Calling this method does not permanently
     * halt the execution of the game.
     * @param gameOver true indicating the game over
     * status should be set to game over, false does
     * nothing*/
    public void setGameOver(boolean gameOver)
    {
        if(!this.gameOver && gameOver)
        {
            advancingToGameOver=true;
        }
    }
    
    private void reallyGameOver()
    {
        if(currentLevel!=null)
        {
            currentLevel.cleanUp();
            currentLevel.removeLevelObjects();
            if(!currentLevel.imagesCached())
                ImageSprite.clearCache();
            if(!currentLevel.soundsCached())
                Sound.cleanCache();
            GameLevel oldLevel=currentLevel;
            GameLevel level=getGameOverLevel();
            if(level!=null)
            {
                levelIndex=levels.size();
                currentLevel=level;
                currentLevel.initializePersistantState(
                        oldLevel.getPersistentAlarms(),
                        oldLevel.getPersistentSprites(),
                        oldLevel.getPersistentSounds());
                oldLevel.clearPersistence();
                currentLevel.setGameLoop(this);
                currentLevel.startLevel();
            }
        }
        this.gameOver=true;
    }
    
    /**adds a level to the game to be executed in
     * sequential order.  If the order of the levels
     * should be determined dynamically, call the
     * setNextLevel method instead.  No game should
     * call both setNextLevel and addLevel.
     * @param level the game level to add to the list
     * of levels to execute in order
     */
    public void addLevel(GameLevel level)
    {
    	level.setGameLoop(this);
        levels.add(level);
    	if(levels.size()==1)
    	{
    		levelIndex=-1;
    		advanceLevel();
    	}
    }
    
    /**
     * finishes the current level 
     * such that advanceFrame
     * will not be called again on
     * the current level.
     */
    public void finishLevel()
    {
        if(currentLevel!=null)
        	currentLevel.finishLevel();
    }
    
    /**dynamically sets the next level to be executed 
     * in the game.  Only the last call to this method
     * will be used in determining which level will be
     * executed after the current level ends. If you want
     * the game to advances through a sequence of levels
     * set out at the beginning of the game, use addLevel
     * instead.  Do not use both addLevel and setNextLevel
     * in any one game.
     * @param level the game level to insert into 
     * the list of levels to execute
     */
    public void setNextLevel(GameLevel level)
    {
    	boolean advance=false;
    	if(levels.size()==0)
    		advance=true;
    	levels.clear();
    	level.setGameLoop(this);
    	levels.add(level);
    	levelIndex=-1;
    	if(advance)
    	{
    		advanceLevel();
    	}
    }
    
    /**determines which level is next in the game.
     * If the getNextLevel
     * method is not overridden, then the game will
     * progress through the levels in the order in
     * which they are added.  In general, either the
     * levels should progress in the order in which
     * they are added by using the addLevel method, 
     * or they should be created dynamically by 
     * overriding this method, but not both.
     * @return the next level to begin.  Returning
     * null signals game over, and the getGameOver
     * level will be called
     */
    public GameLevel getNextLevel()
    {
        if(levelIndex+1<levels.size())
        {
            levelIndex++;
            return levels.get(levelIndex);
        }
        return null;
    }
    
    /**advances to the next level by clearing all
     * of the non-persistent state and getting the
     * next level to execute.
     */
    public void advanceLevel()
    {
        GameLevel oldLevel=currentLevel;
        currentLevel=getNextLevel();
        if(currentLevel==null)
            currentLevel=getGameOverLevel();
        if(currentLevel!=null)
        {
            if(oldLevel==null || !oldLevel.imagesCached())
                ImageSprite.clearCache();
            if(oldLevel==null || !oldLevel.soundsCached())
                Sound.cleanCache();
            if(oldLevel!=null)
            {
            	currentLevel.initializePersistantState(
                    oldLevel.getPersistentAlarms(),
                    oldLevel.getPersistentSprites(),
                    oldLevel.getPersistentSounds());
            	oldLevel.clearPersistence();
            }
            if(client!=null)
            {
            	currentLevel.setGameLoop(this);
            	currentLevel.startLevel();
            }
        }
        else
        {
            setGameOver(true);
        }
    }
    
    /**
     * gets the current level number.  If not
     * using levels, a negative number is returned.
     * @return the index of the current level, or 
     * a negative number if levels are not being used.
     */
    public int getLevelNumber()
    {
        return levelIndex;
    }
    
    /**starts the game over.  This consists
     * of removing all the sprites, sounds,
     * and alarms, then calling the startGame
     * method.  All of this will occur after
     * the next termination of the advanceFrame
     * method.  Therefore, the game should be
     * able to continue without error for at
     * least one more frame before the startGame
     * method is called.
     */
    public void startOver()
    {
        restarting=true;
    }
    
    /**the internal method that
     * actually performs the starting over
     */
    private void reallyStartOver()
    {
    	gameOver=false;
    	restoreCursor();
        resetTime();
        ImageSprite.clearCache();
        Sound.cleanCache();
        canvas.removeAllSprites();
        Sound.clearAll();
        cancelAllAlarms();
        client.clearInput();
        if(currentLevel!=null)
        {
            currentLevel.clearPersistence();
            currentLevel.cleanUp();
        }
        levelIndex=-1;
        currentLevel=getNextLevel();
        if(currentLevel!=null)
            currentLevel.startLevel();
        startGame();
    }
    
    /**gets the current player object.  This
     * method is equivalent to calling the
     * getPlayer method with getID as the player
     * number.
     * @return the Player object which contains
     * that user's keyboard, mouse, and any
     * messages sent
     */
    public Player getPlayer()
    {
        return player[getID()];
    }
    
    /**
     * gets the level to display at the
     * game over.  Overriding this method
     * allows for dynamically creating
     * different game endings.
     * @return the level to be displayed
     * at the end of the game
     */
    public GameLevel getGameOverLevel()
    {
        return null;
    }
}
