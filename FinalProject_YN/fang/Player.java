package fang;

/**
 * Collects player information such as
 * the mouse, keyboard, and message.
 * There is also a repository for the
 * player's score, number of lives, and
 * name.
 * @author Jam Jenkins
 */
public class Player
{
    /**storage for keyboard events generated
     * by the player*/
    private Keyboard keyboard;
    
    /**storage for mouse events generated
     * by the player*/    
    private Mouse mouse;
    
    /**storage for messages sent by
     * the players*/
    private Object[] message;
    
    /**this player's name*/
    private String name;
    
    /**the numeric score*/
    private int score;
    
    /**how many lives are left*/
    private int lives;
    
    /**the index of this player in the game*/
    private int playerNumber;

    /**
     * constructs a player
     * @param keyboard the place where keyboard
     * events will be stored
     * @param mouse the place where mouse
     * events will be stored
     * @param playerNumber
     * the index of the player
     * @param message the storage location
     * for all incoming messages
     */
    public Player(Keyboard keyboard, Mouse mouse,
            int playerNumber, Object[] message)
    {
        this.message=message;
        this.keyboard=keyboard;
        this.mouse=mouse;
        this.playerNumber=playerNumber;
        this.name="Player"+playerNumber;
    }
    
    /**gets the keyboard associated
     * with this player
     * @return the keyboard
     */
    public Keyboard getKeyboard()
    {
        return keyboard;
    }

    /**gets the lives left for this player
     * @return the lives left
     */
    public int getLives()
    {
        return lives;
    }
    
    /**sets the number of lives left
     * @param lives the lives left.
     */
    public void setLives(int lives)
    {
        this.lives = lives;
    }
    /**gets the mouse associated with
     * this player
     * @return the mouse
     */
    public Mouse getMouse()
    {
        return mouse;
    }
    
    /**gets the name of this player
     * @return the name.
     */
    public String getName()
    {
        return name;
    }
    
    /**sets the name of this player.
     * By default, players and named
     * 'Player N' where N is the player
     * index
     * @param name the name of the player
     */
    public void setName(String name)
    {
        this.name = name;
    }
    
    /**gets the player's score
     * @return the score
     */
    public int getScore()
    {
        return score;
    }
    
    /**sets the player's score
     * @param score the player's score
     */
    public void setScore(int score)
    {
        this.score = score;
    }
    
    /**
     * gets the player's index
     * @return the index of the player
     */
    public int getPlayerNumber()
    {
        return playerNumber;
    }
        
    /**determines if this player has sent
     * a message.  Generic messages can
     * be sent and received so long as
     * they implement Serializable.
     * @return true if a message can be
     * read from the getMessage and false
     * if no message has been received
     */
    public boolean hasMessage()
    {
        return message[playerNumber]!=null;
    }
    
    /**reads the message sent from
     * this player.  This message must
     * be Serializable.
     * @return the message from this player
     */
    public Object getMessage()
    {
        return message[playerNumber];
    }
    
}
