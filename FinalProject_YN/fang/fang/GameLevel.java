package fang;

import java.net.URL;
import java.util.HashSet;
import java.util.Random;

/**This class mimicks the GameLoop.  Any game
 * which extends GameLoop can be made to extend
 * GameLevel instead and then can be incorporated
 * into a multi-level game.  See the examples on
 * the FANG Engine website.
 * @author Jam Jenkins
 */
public abstract class GameLevel
{    
    /**the place where sprites are drawn.
     * This is not initialized until startLevel
     * is called.  You will get a 
     * NullPointerException if you try to use
     * the canvas in the constructor.*/
    protected AnimationCanvas canvas;
    
    /**the GameLoop object used implement
     * most of the method calls.
     * This is not initialized until startLevel
     * is called.  You will get a 
     * NullPointerException if you try to use
     * the canvas in the constructor.*/
    private GameLoop gameLoop;
    
    /**the random number generator
     * use this generator for all random
     * number in order to maintain consistency
     * in multiplayer games.
     * This is not initialized until startLevel
     * is called.  You will get a 
     * NullPointerException if you try to use
     * the canvas in the constructor.*/
    protected Random random;
    
    /**the alarms which will persist across
     * levels.  When a level ends all alarms
     * in not in this collection will be
     * cancelled.*/
    private HashSet<Alarm> persistentAlarms=
        new HashSet<Alarm>();
    
    
    /**the sprites which will persist across
     * levels.  When a level ends all sprites
     * in not in this collection will be
     * removed from the canvas.*/
    private HashSet<Sprite> persistentSprites=
        new HashSet<Sprite>();
        
    /**the sounds which will persist across
     * levels.  When a level ends all sounds
     * not in this collection will be terminated.*/
    private HashSet<Sound> persistentSounds=
        new HashSet<Sound>();
    
    /**whether to clear the cached images or not.
     * By default, the cached is cleared at the
     * end of each level in order to avoid using
     * too much memory.*/
    private boolean cacheImages=false;
    
    /**whether to clear the cached sounds or not.
     * By default, the cached is cleared at the
     * end of each level in order to avoid using
     * too much memory.*/    
    private boolean cacheSounds=false;
    
    /**triggered when finishLevel is called*/
    private boolean finishingLevel=false;
    
    /**triggered when restartLevel is called*/
    private boolean restartingLevel=false;
    
    
    /**sets whether to clear the cached images or 
     * not.  By default, the cached is cleared at 
     * the end of each level in order to avoid 
     * using too much memory.  Keeping the images
     * in the cache can help the next level load
     * more quickly, but too many accumulated images
     * will cause the game to run out of memory.
     * @param cacheImages true indicates the images
     * currently in the cache should remain in the
     * cache, false means to clear them.
     */
    public void setCacheImages(boolean cacheImages)
    {
        this.cacheImages=cacheImages;
    }
    
    /**sets whether to clear the cached sounds or 
     * not.  By default, the cached is cleared at 
     * the end of each level in order to avoid 
     * using too much memory.  Keeping the sounds
     * in the cache can help the next level load
     * more quickly, but too many accumulated sounds
     * will cause the game to run out of memory.
     * @param cacheSounds true indicates the sounds
     * currently in the cache should remain in the
     * cache, false means to clear them.
     */
    public void setCacheSounds(boolean cacheSounds)
    {
        this.cacheSounds=cacheSounds;
    }
    
    /**gets whether to clear the cached images or 
     * not.  By default, the cached is cleared at 
     * the end of each level in order to avoid 
     * using too much memory.  Keeping the images
     * in the cache can help the next level load
     * more quickly, but too many accumulated images
     * will cause the game to run out of memory.
     * @return true if the images in cache will
     * remain in cache for the next level, false
     * means they will be cleared.
     */
    public boolean imagesCached()
    {
        return cacheImages;
    }
    
    /**gets whether to clear the cached sounds or 
     * not.  By default, the cached is cleared at 
     * the end of each level in order to avoid 
     * using too much memory.  Keeping the sounds
     * in the cache can help the next level load
     * more quickly, but too many accumulated sounds
     * will cause the game to run out of memory.
     * @return true if the sounds in cache will
     * remain in cache for the next level, false
     * means they will be cleared.
     */
    public boolean soundsCached()
    {
        return cacheSounds;
    }

    /**copies all of the peristent alarms,
     * sprites, and sounds into this level's
     * persistence collections
     * @param alarms the alarms which came
     * from the last level
     * @param sprites the sprites which came
     * from the last level
     * @param sounds the sounds which came
     * from the last lelel
     */
    public void initializePersistantState(
            Alarm[] alarms, Sprite[] sprites,
            Sound[] sounds)
    {
        for(Alarm alarm: alarms)
            persistentAlarms.add(alarm);
        for(Sprite sprite: sprites)
            persistentSprites.add(sprite);
        for(Sound sound: sounds)
            persistentSounds.add(sound);
    }
    
    /**sets the GameLoop object this level
     * will use to implement the majority
     * of its methods.
     * @param loop the GameLoop object of
     * the main game
     */
    public void setGameLoop(GameLoop loop)
    {
        gameLoop=loop;
        canvas=gameLoop.canvas;
        random=gameLoop.random;
    }
    
    /** switches between mute and audible */
    public void toggleAudible()
    {
        gameLoop.toggleAudible();
    }
      
    /**
     * returns true if muted, false if not muted
     * @return WhetherMuted
     */
    public boolean isMuted()
    {
        return gameLoop.isMuted();
    }
    
    /**
     * sends a message to all of the clients
     * @param localMessage a Serializable
     * message which is sent to all clients
     */
    public void sendMessage(Object localMessage)
    {
        gameLoop.sendMessage(localMessage);
    }
        
    /** 
     * returns true if paused, false if
     * the game is running
     * @return the state of the game
     * running/paused
     */
    public boolean isPaused()
    {
        return gameLoop.isPaused();
    }
    
    /** changes between paused and unpaused */
    public void pauseToggle()
    {
        gameLoop.pauseToggle();
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
     * @param filename the name of the file
     * @return the resource as a URL
     */
    public URL getResource(String filename)
    {
        URL resourceURL=getClass().getResource(filename);
        return resourceURL;
    }
    
    /** starts the level.  At this point the
     * number of players is now known.
     * This method should create and
     * add sprites to the canvas.  This method
     * is called for you by the FANG
     * Engine and should not be called
     * directly.*/
    public abstract void startLevel();
    
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
    public abstract void advanceFrame(double timePassed);
    
    public void postAdvanceFrame()
    {
        if(restartingLevel)
            reallyRestartLevel();
        if(finishingLevel)
            reallyFinishLevel();
    }
    
    /**this method will be called by the
     * FANG Engine just before terminating
     * the level in case the level needs
     * an opportunity to perform some final
     * operations*/
    public void cleanUp()
    {
    }
    
    /**sets the game state to game over.
     * This not only ends the level, but
     * also ends the game.*/
    protected final void finishGame()
    {
        gameLoop.setGameOver(true);
    }
    
    /**removes all the non-persistent
     * sprites, alarms, and sounds
     */
    protected void removeLevelObjects()
    {
        Sprite[] allSprites=canvas.getAllSprites();
        for(Sprite sprite: allSprites)
        {
            if(!persistentSprites.contains(sprite))
                canvas.removeSprite(sprite);
        }
        Sound.clearAllExcept(persistentSounds.toArray(new Sound[0]));
        Alarm[] allAlarms=gameLoop.getAlarms();
        for(Alarm alarm: allAlarms)
        {
            if(!persistentAlarms.contains(alarm))
                gameLoop.cancelAlarm(alarm);
        }        
    }
      
    /**
     * sets the next level which will execute
     * once this level has finished.
     * @param level the next level
     */
    public void setNextLevel(GameLevel level)
    {
    	gameLoop.setNextLevel(level);
    }
    
    /**
     * finishes this level such that advanceFrame
     * will not be called again.
     */
    public void finishLevel()
    {
        finishingLevel=true;
    }
    
    /**gets the name of this class
     * @return the name of the class*/
    public String toString()
    {
    	return getClass().getName();
    }
    
    /**finishes this level in the game.
     * This cleans up the non-persistent
     * alarms, sprites and sounds, then
     * advances to the next level of the
     * game. 
     */
    private void reallyFinishLevel()
    {
        finishingLevel=false;
        cleanUp();
        removeLevelObjects();
        gameLoop.advanceLevel();
    }
    
    /**begins this level again by 
     * the next advanceFrame*/
    public void startOverLevel()
    {
        restartingLevel=true;
    }
    
    /**begins this level again immediately*/
    private void reallyRestartLevel()
    {
        restartingLevel=false;
        cleanUp();
        removeLevelObjects();
        startLevel();
    }
    
    /**adds an alarm to the collection of
     * alarms which will persist after the
     * level ends.
     * @param alarm the alarm to keep after
     * the level terminates
     */
    public void persist(Alarm alarm)
    {
        persistentAlarms.add(alarm);
    }
    
    /**adds a sprite to the collection of
     * sprites which will persist after the
     * level ends.
     * @param sprite the sprite to keep after
     * the level terminates
     */
    public void persist(Sprite sprite)
    {
        persistentSprites.add(sprite);
    }
    
    /**adds a sound to the collection of
     * sounds which will persist after the
     * level ends.
     * @param sound the sound to keep after
     * the level terminates
     */
    public void persist(Sound sound)
    {
        persistentSounds.add(sound);
    }

    
    /**removes the alarm from the collection of
     * alarms which will persist after the
     * level ends.
     * @param alarm the alarm to remove from
     * the persistent collection
     */
    public void clearPersistence(Alarm alarm)
    {
        persistentAlarms.remove(alarm);
    }
    
    /**removes the sprite from the collection of
     * sprites which will persist after the
     * level ends.
     * @param sprite the sprite to remove from
     * the persistent collection
     */
    public void clearPersistence(Sprite sprite)
    {
        persistentSprites.remove(sprite);
    }
    
    /**removes the sound from the collection of
     * sounds which will persist after the
     * level ends.
     * @param sound the sound to remove from
     * the persistent collection
     */
    public void clearPersistence(Sound sound)
    {
        persistentSounds.remove(sound);
    }
    
    /**clears all peristent objects
     * in this level from the collections
     * of persistent objects
     */
    public void clearPersistence()
    {
        persistentAlarms.clear();
        persistentSprites.clear();
        persistentSounds.clear();
    }
    
    /**
     * gets all the alarms which are
     * set to persist after the level ends.
     * @return the array of alarms
     */
    public Alarm[] getPersistentAlarms()
    {
        return persistentAlarms.toArray(new Alarm[0]);
    }

    /**
     * gets all the sprites which are
     * set to persist after the level ends.
     * @return the array of sprites
     */
    public Sprite[] getPersistentSprites()
    {
        return persistentSprites.toArray(new Sprite[0]);
    }

    /**
     * gets all the sounds which are
     * set to persist after the level ends.
     * @return the array of sounds
     */
    public Sound[] getPersistentSounds()
    {
        return persistentSounds.toArray(new Sound[0]);
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
        return gameLoop.getNumberOfPlayers();
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
        return gameLoop.getID();
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
    protected final Player getPlayer(int playerIndex)
    {
        return gameLoop.getPlayer(playerIndex);
    }
    
    /**gets the player object for a given
     * player name.
     * @param name String name associated
     * with the Player
     * @return the Player object which contains
     * that user's keyboard, mouse, and any
     * messages sent
     */
    protected final Player getPlayer(String name)
    {
        return gameLoop.getPlayer(name);
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
        return gameLoop.getPlayer();
    }

    /**
     * sets and alarm to go off relative to 
     * the current time. For example, this
     * method can be used to set off an alarm 
     * in 5 seconds from the current time.
     * 
     * @param alarm
     *            the class to call the alarm method on
     * @param relative
     *            the time from now in seconds to call the alarm method
     */

    public void scheduleRelative(Alarm alarm, double relative)
    {
        gameLoop.scheduleRelative(alarm, relative);
    }

    /**
     * sets and alarm to go off at a time 
     * relative to the beginning of time
     * (zero). For example, this method can 
     * be used to set off an alarm 30
     * seconds from the beginning time.
     * 
     * @param alarm
     *            the class to call the alarm method on
     * @param absolute
     *            the time in seconds to call the alarm method
     */
    public void scheduleAbsolute(Alarm alarm, double absolute)
    {
        gameLoop.scheduleAbsolute(alarm, absolute);
    }
    
    /**
     * removes all pending alarms on this object. 
     * If there are no alarms with
     * this object as the target, the method call is ignored.
     * 
     * @param alarm
     *            the object that is the target of a pending alarm
     */
    public void cancelAlarm(Alarm alarm)
    {
        gameLoop.cancelAlarm(alarm);
    }
    
    /**
     * removes all pending alarms. 
     * If there are no pending alarms, 
     * the method call is ignored.
     */
    public void cancelAllAlarms()
    {
        gameLoop.cancelAllAlarms();
    }
    
    /**gets the list of alarms scheduled to go
     * off in the future.
     * @return the array of alarms in the order
     * which they would go off
     */
    public Alarm[] getAlarms()
    {
        return gameLoop.getAlarms();
    }
    
    /**indicates whether this alarm is
     * part of the collection of alarms
     * which will persist after the level
     * terminates
     * @param alarm the alarm to check
     * @return true indicates the alarm
     * will persist, false means it will
     * be removed upon termination of the
     * level
     */
    public boolean isPeristent(Alarm alarm)
    {
        return persistentAlarms.contains(alarm);
    }
    
    /**indicates whether this sprite is
     * part of the collection of sprites
     * which will persist after the level
     * terminates
     * @param sprite the sprite to check
     * @return true indicates the sprite
     * will persist, false means it will
     * be removed upon termination of the
     * level
     */
    public boolean isPeristent(Sprite sprite)
    {
        return persistentSprites.contains(sprite);
    }
    
    /**indicates whether this sound is
     * part of the collection of sounds
     * which will persist after the level
     * terminates
     * @param sound the alarm to check
     * @return true indicates the sound
     * will persist, false means it will
     * be removed upon termination of the
     * level
     */
    public boolean isPeristent(Sound sound)
    {
        return persistentSounds.contains(sound);
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
        gameLoop.startOver();
    }
    
    /**sets the help screen.  If no help
     * screen is set, the default help
     * screen asks the user to email the
     * person who made the game and ask
     * them to make a help screen.  To
     * avoid receiving emails asking for
     * a help screen, provide one from
     * the start.  The html help screen
     * does not offer full html support,
     * it just displays the formatted text
     * between the body tags.  Therefore
     * you should avoid using advanced
     * formatting, java script and other
     * advanced html features as they may
     * not display properly.
     * @param help the path to the html
     * help screen which will be displayed
     */
    public void setHelp(String help)
    {
        gameLoop.setHelp(help);
    }
    
    /**gets the time in seconds since
     * the game has started
     * @return the time in seconds
     */
    public double getTime()
    {
        return gameLoop.getTime();
    }
    
    /**
     * makes the current time zero
     */
    public void resetTime()
    {
        gameLoop.resetTime();
    }

}
