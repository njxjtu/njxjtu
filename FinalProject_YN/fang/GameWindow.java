package fang;

import java.awt.*;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;

/**
 * Driver for the GameLoop. Extending this 
 * class will put the AnimationCanvas of
 * the the GameLoop into a JFrame with 
 * controls at the bottom for
 * starting/pausing, muting, showing a 
 * help screen, and quitting.
 * 
 * @author Jam Jenkins
 */
public abstract class GameWindow 
    extends JApplet 
    implements ActionListener,
        WindowStateListener
{
    /** frame for the GUI */
    protected JFrame frame;
    
    /** the message displayed while loading*/
    protected JLabel loadingMessage;
    
    /** labels for connecting in multiplayer games*/
    protected JTextField sessionLabel, serverLabel;
    
    /** the layout for the applet/application */
    protected CardLayout cards;
    
    /** the container need for flipping cards */
    protected Container container;

    /** control buttons */
    protected JButton pause, quit, helpPlay, mute, connectButton;
    
    /** input component for the session name*/
    protected FunComboBox sessionComboBox;
    
    /** input component for the server name*/    
    protected JTextField serverField;
    
    /** input component for the number of players*/
    protected JTextField playersSpinner;

    /** title at the top of the frame */
    protected JLabel title;

    /** display for help information */
    protected HTMLDisplay help;

    /** determines if the game should have its own frame */
    protected boolean hasFrame;

    /** the number of players*/
    public int players=1;
    
    /** the ability to change the number of players */
    public boolean playersSelectable=true;
    
    /** the ability to change the server name*/
    public boolean serverSelectable=true;
    
    /** the ability to change the session name*/
    public boolean sessionSelectable=true;
    
    public static boolean displayControlButtons=true;
    
    public JPanel controlPanel;
    
    /** determines if the game will be displayed
     * full screen or not.  This variable is only
     * used for applications and must be set prior
     * to calling runAsApplication
     */
    public boolean fullScreen=false;
    
    /**
     * the hash of the compiled class code
     */
    private int hash=0;    
    
    /**
     * makes the components, layout, and sets the name of the game
     */
    public GameWindow()
    {
        hasFrame=false;
    }

    /**
     * sets hasFrame to parameter hasFrame.
     * When running as an application, the
     * game appears in a frame, but when
     * running as an applet, it runs in the
     * provided space in the browser.
     * @param hasFrame true indicates the game
     * should open in a new window and false
     * indicates the game should run in the
     * space provided by the applet
     */
    public void setUseFrame(boolean hasFrame)
    {
        this.hasFrame = hasFrame;        
    }
    
    /** runs as an application.  This opens up
     * the game in a new window. */
    public void runAsApplication()
    {
        setUseFrame(true);
        init();
        if(frame.getTitle().equals(""))
            frame.setTitle(getGameName());
    }
    
    /**
     * abstract method, sets the name of the game
     * @param name
     * 		name of game
     */
    public abstract void setGameName(String name);
    
    /**gets the name of the game
     * @return the name of the game
     */
    public abstract String getGameName();
    
    /**
     * Sets the title at the top of the JFrame
     * and/or applet
     * 
     * @param topTitle
     *            the text for the title
     */
    public void setTitle(String topTitle)
    {
        title.setText(topTitle);
        if(frame!=null)
            frame.setTitle(topTitle);
    }

    /**
     * sets the number of players to wait 
     * for when starting a new game. Calls to
     * this method are ignored for already 
     * started game sessions.
     * 
     * @param players
     *            the number of players to 
     *            wait for before starting the game
     */

    public abstract void setNumberOfPlayers(int players);
    
    /**
     * where this game's server resides
     * 
     * @param server
     *            the name of the computer 
     *            where the game was originally started
     */
    public abstract void setServerName(String server);

    /**
     * sets the name of this particular instance of the game
     * 
     * @param session
     *            the name of this particular instance of the game
     */
    public abstract void setSessionName(String session);

    /** responds to control button presses */
    public void actionPerformed(ActionEvent e)
    {
        Object cause = e.getSource();
        if (cause == pause)
        {
            pauseToggle();
        } else if (cause == quit)
        {
            exitGame();
        } else if (cause == helpPlay)
        {
            if(help==null)
                setDefaultHelp();
            help.setVisible(true);
        } else if (cause == mute)
        {
            toggleAudible();
        }
    }

    /** toggles the sound between on and muted */
    public void toggleAudible()
    {
        if (mute.getText().equals("Mute"))
        {
            mute.setText("Sound On");
            Sound.mute();
        } else
        {
            mute.setText("Mute");
            Sound.turnSoundOn();
        }
    }
    
    /** quits the application */
    private void exitGame()
    {
        disconnect();
        if(frame!=null)
        {
            frame.dispose();
        }
        try
        {
            System.exit(0);
        }
        catch(Exception e){}
    }

    /** abstract method, disconnects */
    public abstract void disconnect();
    
    /** toggles pause */
    public void pauseToggle()
    {
        if(pause.getText().equals("Start") ||
                pause.getText().equals("Resume"))
        {
            pause.setText("Pause");
            Sound.resume();
        }
        else
        {
            pause.setText("Resume");
            Sound.pause();
        }
    }

    /**
     * this method must be called in order
     * to set the help screen.  The filename
     * is relative to the class which makes
     * the call.  Only what is between the
     * body tags will be displayed, and the
     * full html specs are not supported.
     * Therefore use only simple html in order
     * to make the help display properly.
     * @param filename the relative file name
     * of the help html file 
     */
    public void setHelp(String filename)
    {
        if(help==null)
        {
            setDefaultHelp();
        }
        help.setContent(filename, getClass().getResource(filename));
    }
    
    public void setHelpText(String helpText)
    {
    	help.setText(helpText);
    }
    
    /**sets the default help screen to
     * say that the player should email
     * the author of the game asking
     * him/her to make a help screen
     */
    protected void setDefaultHelp()
    {
        help = new HTMLDisplay("resources/DefaultHelp.html", GameWindow.class.getResource("resources/DefaultHelp.html"));
        URL style=getHelpStyleSheet();
        if(style!=null)
            help.setStyleSheet(style);
    }

    /**gets the stylesheet to format the
     * help screen.  The default stylesheet
     * is black with blue lettering.
     * @return the stylesheet to use
     */
    protected URL getHelpStyleSheet()
    {
        return null;
    }
    
    /**
     * if the size is not specified it is 400 x 400
     * 
     * @return the size 400 x 400
     */
    protected Dimension getDefaultSize()
    {
        return new Dimension(400, 400);
    }

    /**
     * exits the application when the window 
     * closes and also terminates the
     * server connections
     * 
     * @see java.awt.event.WindowStateListener#windowStateChanged(java.awt.event.WindowEvent)
     */
    public void windowStateChanged(WindowEvent event)
    {
        if (event.getNewState() == WindowEvent.WINDOW_CLOSING)
        {
            exitGame();
        }
    }

    /** initialize all components */
    private void makeComponents()
    {
        Dimension size=getSize();
        if(hasFrame)
        {
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsDevice gs = ge.getDefaultScreenDevice();
            frame = new JFrame("", gs.getDefaultConfiguration());
            frame.addWindowListener(new WindowCloser());
            size=getCanvas().getSize();
        }
        size.width=2*size.width/3;
        size.height=2*size.height/3;
        pause = new FunButton("Start", size);
        pause.addActionListener(this);
        quit = new FunButton("Quit", size);
        quit.addActionListener(this);
        helpPlay = new FunButton("Help", size);
        helpPlay.addActionListener(this);
        mute = new FunButton("Sound On", size);
        mute.addActionListener(this);
        if(hasFrame)
            size=getCanvas().getSize();
        else
            size=getSize();
        title = new JLabel();
        FunPainter.setProperties(size, title);
        loadingMessage=new JLabel("Loading");
        FunPainter.setProperties(size, loadingMessage);
        if(help==null)
        {
            setDefaultHelp();
        }
    }

    /**gets the name of the session.  The
     * session name is used to determine 
     * instance of the game to join when
     * connecting to the server.
     * @return name of the sesson
     */
    public abstract String getSessionName();
    
    /**gets the name of the server to connect
     * to when joining a game.  For applets,
     * this must be the server from which it
     * was downloaded.  Applications are not
     * restricted in the network connections
     * they can make, so they can actually
     * connect to other servers.
     * @return the name of the server
     */
    public abstract String getServerName();
    
    /** position all components */
    private void layoutComponents()
    {
        if (hasFrame)
            container = frame.getContentPane();
        else
            container = this.getRootPane().getContentPane();
        cards=new CardLayout();
        container.setLayout(cards);
        container.add(getGamePanel(), "Game");
        container.add(getConnectionPanel(), "Connecting");
        container.add(getStatusPanel(), "Loading Game");
        
    }
    
    /**
     * gets the primary connected game panel
     * @return the game panel with the animation
     * canvas and control buttons
     */
    private JPanel getGamePanel()
    {
        JPanel panel=new FunPanel();
        panel.setLayout(new BorderLayout());
        panel.add(getCanvas(), BorderLayout.CENTER);
        controlPanel=getControlPanel();
        if(displayControlButtons)
            panel.add(controlPanel, BorderLayout.SOUTH);
        panel.add(title, BorderLayout.NORTH);
        return panel;
    }
    
    /**
     * gets the loading/connecting panel which
     * comes before the game panel
     * @return the loading/connecting panel
     */
    private JPanel getStatusPanel()
    {
        JPanel big=new FunPanel(new BorderLayout());
        JPanel panel=new FunPanel(new GridLayout(7, 1));
        JLabel label;
        Dimension size;
        if(hasFrame)
            size=getCanvas().getSize();
        else
            size=getSize();
        label=new JLabel(getGameName());
        FunPainter.setProperties(size, label);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setFont(label.getFont().deriveFont(label.getFont().getSize()*1.5f));
        panel.add(label);
        label=new JLabel("Name of this game:");
        FunPainter.setProperties(size, label);
        panel.add(label);
        sessionLabel=new FunTextField(getSessionName(), size);
        sessionLabel.setEnabled(false);
        panel.add(sessionLabel);
        JPanel playerPanel=new FunPanel(new BorderLayout());
        label=new JLabel("Number of players:");
        FunPainter.setProperties(size, label);
        playerPanel.add(label, BorderLayout.WEST);
        JTextField playersSpinner=new FunTextField(size);
        playersSpinner.setText(playersSpinner.getText());
        playersSpinner.setEnabled(false);
        playerPanel.add(playersSpinner, BorderLayout.CENTER);
        panel.add(playerPanel);
        label=new JLabel("Name of game server:");
        FunPainter.setProperties(size, label);
        panel.add(label);
        serverLabel=new FunTextField(getServerName(), size);
        serverLabel.setEnabled(false);
        panel.add(serverLabel);
        loadingMessage.setFont(loadingMessage.getFont().deriveFont(Font.ITALIC | Font.BOLD));
        panel.add(loadingMessage);
        label=new JLabel("Game Engine by Jam");
        FunPainter.setProperties(size, label);
        label.setFont(new Font("monospaced", Font.BOLD, label.getFont().getSize()*3/2));
        label.setForeground(new Color(1.0f, 0.0f, 0.0f, 0.3f));
        big.add(panel, BorderLayout.CENTER);
        big.add(label, BorderLayout.SOUTH);        
        return big;
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
     * gets the connection panel where the
     * user can enter the session name,
     * server name, and number of players
     * @return the first panel displayed
     */
    private JPanel getConnectionPanel()
    {
        JPanel big=new FunPanel(new BorderLayout());
        FunPanel controls=new FunPanel(new GridLayout(7, 1));
        JLabel label;
        Dimension size;
        if(hasFrame)
            size=getCanvas().getSize();
        else
            size=getSize();
        label=new JLabel(getGameName());
        FunPainter.setProperties(size, label);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setFont(label.getFont().deriveFont(label.getFont().getSize()*1.5f));
        controls.add(label);
        label=new JLabel("Name of this game:");
        FunPainter.setProperties(size, label);
        controls.add(label);
        sessionComboBox=new FunComboBox(new String[]{"default"}, size);
        sessionComboBox.addPopupMenuListener(new ShowPopup());
        sessionComboBox.setEditable(true);
        controls.add(sessionComboBox);
        sessionComboBox.setEnabled(sessionSelectable);
//        sessionComboBox.addActionListener(new JComboListener()); // TODO: c added this
        JPanel playerPanel=new FunPanel(new BorderLayout());
        label=new JLabel("Number of players:");
        FunPainter.setProperties(size, label);
        playerPanel.add(label, BorderLayout.WEST);
        playersSpinner=new FunTextField(""+players, size);
        playerPanel.add(playersSpinner, BorderLayout.CENTER);
        playersSpinner.setEnabled(playersSelectable);
        controls.add(playerPanel);
        label=new JLabel("Name of game server:");
        FunPainter.setProperties(size, label);
        controls.add(label);
        serverField=new FunTextField(size);
        try
        {
            if (getServerName().length() == 0 ||
                    getServerName().equals("localhost"))
            {
                if (getCodeBase() == null || getCodeBase().getHost() == null
                        || getCodeBase().getHost().equals("")
                        || getCodeBase().getHost().equals("localhost")
                        || getCodeBase().getHost().equals(getHostname()))
                {
                    serverField.setText(getHostname());
                    setServerName(getHostname());
                    serverField.setEditable(true);
                } else
                {
                    serverField.setText(getCodeBase().getHost());
                    setServerName(getCodeBase().getHost());
                    serverField.setEditable(false);
                }
            }
            else
            {
                serverField.setText(getServerName());
            }
        }
        catch(NullPointerException npe)
        {
            serverField.setText(getHostname());
            serverField.setEditable(true);            
        }
        serverField.setEnabled(serverSelectable);
        controls.add(serverField);  
        connectButton=new FunButton("Connect & Start Game", size);
        connectButton.addActionListener(new StartGame());
        controls.add(connectButton);
        label=new JLabel("Game Engine by Jam");
        FunPainter.setProperties(size, label);
        label.setFont(new Font("monospaced", Font.BOLD, label.getFont().getSize()*3/2));
        label.setForeground(new Color(1.0f, 0.0f, 0.0f, 0.3f));
        big.add(controls, BorderLayout.CENTER);
        big.add(label, BorderLayout.SOUTH);        
        return big;
    }

    /** abstract method to return canvas */
    public abstract AnimationCanvas getCanvas();
    
    /** layout the bottom control panel */
    private JPanel getControlPanel()
    {
        JPanel panel = new FunPanel(new GridLayout(1, 4));
        panel.add(pause);
        panel.add(mute);
        panel.add(helpPlay);
        panel.add(quit);
        return panel;
    }

    /**
     * sets a custom loading message
     * @param message the text for the JLabel
     */
    public void setLoadMessage(String message)
    {
        loadingMessage.setText(message);
    }
    
    /**starts the game.  This method blocks until
     * the game is connected.
     */
    public abstract void begin();
    
    /**reads the applet's parameters if there are any*/
    private void readParameters()
    {
        try
        {
            if(getParameter("players")!=null)
            {
                players=new Integer(getParameter("players"));
                playersSpinner.setText(""+players);
                if(getParameter("players-selectable")!=null)
                {
                    if(getParameter("players-selectable").toLowerCase().trim().equals("false"))
                        playersSelectable=false;
                    else
                        playersSelectable=true;
                }
                else
                    playersSelectable=false;
            }
            else
            {
                players=1;
            }
            if(getParameter("session")!=null)
            {
                setSessionName(getParameter("session"));
            }
            if(getParameter("session-selectable")!=null)
            {
                if(getParameter("session-selectable").trim().toLowerCase().equals("false"))
                {
                    sessionSelectable=false;
                }
            }
        }
        catch(NullPointerException npe)
        {
        }
    }
    
    /**hashes the compiled source code
     * in order to make sure that the
     * only game which one can connect
     * to is the exact same game.
     * @return a relatively unique id
     * based upon the compiled source
     */
    public String getHash()
    {
    	if(hash!=0)
    		return ""+hash;
        try
        {
            String fullName = this.getClass().getName();
            String shortName = fullName.substring(fullName.lastIndexOf(".") + 1);
            Class c=getClass();
            URL url=c.getResource(shortName+".class");
            byte[] full=new byte[4];
            int read=-1;
            int hash=0;
            InputStream in=url.openStream();
            do
            {
            	int offset=0;
            	do
            	{
                	read=in.read(full, offset, 4-offset);
                	if(read<0)
                		return ""+hash;
                	else
                		offset+=read;
            	}while(offset<4);
            	for(int i=0; i<offset; i++)
            		hash=hash^(full[i]<<(i*8));
            }while(read>=0);
            return ""+hash;
        } catch (IOException e)
        {
            e.printStackTrace();
            return "Game";
        }
    }
    
    /** sets the frame visible */
    public void init()
    {
        String fullName = this.getClass().getName();
        String shortName = fullName.substring(fullName.lastIndexOf(".") + 1);
        setGameName(shortName);
        readParameters();
        makeComponents();
        layoutComponents();
        cards.next(container);
        if(hasFrame)
        {
            if (fullScreen &&
                    frame.getGraphicsConfiguration().getDevice().isFullScreenSupported()) 
            {
                frame.setUndecorated(true);
                frame.getGraphicsConfiguration().getDevice().setFullScreenWindow(frame);
                frame.validate();
            }
            if(!fullScreen)
                frame.pack();
            frame.setVisible(true);
        }
        if(players==1)
        {
            cards.next(container);
            setSessionName("default");
            setServerName("localhost");
            setNumberOfPlayers(1);
            new StartGame().start();
        }
        else if(!playersSelectable &&
                !sessionSelectable &&
                !serverSelectable)
        {
            connectButton.getActionListeners()[0].actionPerformed(null);
        }
        updateAvailableSessions();
    }
    
    /**This class refreshes the list
     * of available sessions when it
     * is displayed.
     * @author Jam Jenkins
     */
    class ShowPopup implements PopupMenuListener
    {
		public void popupMenuWillBecomeVisible(PopupMenuEvent arg0) {
			updateAvailableSessions();
			validate();
		}

		public void popupMenuWillBecomeInvisible(PopupMenuEvent arg0){}
		public void popupMenuCanceled(PopupMenuEvent arg0) {}
    }
   
    /**updates the list of available sessions
     * running on the server
     */
    private void updateAvailableSessions()
    {
    	sessionComboBox.removeAllItems();
        String[] all=Client.getWaitingSessions(getServerName(), getHash());
        for(String s: all)
        {
      		sessionComboBox.addItem(s);            	
        }
    }
    
    /**
     * This class starts the game when the user clicks
     * on the connect and start game button at the
     * bottom.  The starting of the game is in a
     * separate thread because it will block while
     * connecting and the AWT thread should not be
     * delayed.
     * @author Jam Jenkins
     */
    class StartGame extends Thread implements ActionListener
    {
        /**starts the game and keeps the thread alive.
         * The Pipes used for communications in 1-player
         * games are only active so long as the thread
         * that started them is active, so this thread
         * must stay alive so long as the game is
         * being played.
         */
        public void run()
        {
            begin();
        	cards.next(container);
            FunPanel.stopUpdating();
            //the thread which starts the game
            //must stay alive to keep the piped
            //communications open
            synchronized(this)
            {
                try
                {
                    wait();
                }
                catch(InterruptedException e){}
            }
        }
        
        /**starts the game*/
        public void actionPerformed(ActionEvent arg0)
        {
            if(Integer.parseInt(playersSpinner.getText())==1)
            {
                setSessionName("default");
                setServerName("localhost");
                setNumberOfPlayers(1);
                cards.next(container);
                start();
                return;
            }
            serverLabel.setText(serverField.getText());
            setServerName(serverField.getText());
            sessionComboBox.actionPerformed(null);
            if(((String)sessionComboBox.getSelectedItem()).trim().length()==0)
                sessionComboBox.setSelectedItem("default");
            sessionLabel.setText(((String)sessionComboBox.getSelectedItem()).trim());
            setSessionName(((String)sessionComboBox.getSelectedItem()).trim());
            setNumberOfPlayers(Integer.parseInt(playersSpinner.getText()));
            cards.next(container);
            start();
        }
    }

    /** abstract method, starts game */
    public abstract void startGame();
    
    /** sets the frame invisible and pauses the game */
    public void stop()
    {
        if(frame!=null)
            frame.dispose();
        
    }
    
    /**exits the game when the X is clicked on*/
    private class WindowCloser extends WindowAdapter
    {
        public void windowClosing(WindowEvent e)
        {
            exitGame();
        }
    }
}
