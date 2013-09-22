package fang;

import java.awt.event.KeyEvent;
import java.io.Externalizable;
import java.util.Observer;
import java.awt.KeyEventDispatcher;

/**
 * This class uses polling rather 
 * than events for keyboard input.
 * 
 * @author Jam Jenkins
 */
public class Keyboard implements 
    Externalizable, KeyEventDispatcher
{
    /**
     * used for serialization versioning
     */
    private static final long serialVersionUID = 1L;
    /** key pressed */
	private char key;
	/** observer, used for signalling updates*/
    private Observer observer;

    /**writes the key to the output stream
     * @param out the output stream to write to
     */
    public void writeExternal(java.io.ObjectOutput out)
            throws java.io.IOException
    {
        out.writeChar(key);
    }

    /**reads the key from the input stream
     * @param in the input stream to read from
     */
    public void readExternal(java.io.ObjectInput in)
            throws java.io.IOException
    {
        char temp = in.readChar();
        if (temp != KeyEvent.CHAR_UNDEFINED)
            key = temp;
    }

    /** Creates a new instance of Keyboard */
    public Keyboard()
    {
        clear();
    }

    /** returns the key that is pressed in string form */
    public String toString()
    {
        return key + " is last pressed";
    }

    /** clear all pending keyboard events */
    public void clear()
    {
        key = KeyEvent.CHAR_UNDEFINED;
    }

    /**
     * gets the last key pressed. Subsequent 
     * calls to getLastKey will return the
     * same value until another key is pressed 
     * or the key events have been cleared.
     * 
     * @return the last key pressed
     */
    public char getLastKey()
    {
        return key;
    }
    
    /**determines if getLastKey will
     * return a key pressed.  getLastKey
     * returns an undefined char when no
     * key has been pressed.
     * @return true if a key has been
     * pressed, false otherwise
     */
    public boolean keyPressed()
    {
        return key!=KeyEvent.CHAR_UNDEFINED;
    }
    
    /**sets the last key pressed
     * @param key the last key pressed
     */
    public void setLastKey(char key)
    {
        this.key=key;
        if(observer!=null)
            observer.update(null, null);
    }

    /**sets key and updates the observer
     * @param e the event to store
     * @return false
     */
    public boolean dispatchKeyEvent(KeyEvent e)
    {
        if (e.getID() == KeyEvent.KEY_PRESSED)
        {
            key = e.getKeyChar();
            if(observer!=null)
                observer.update(null, null);
        }
        return false;
    }
    
    /**
     * sets observer
     * @param observer
     */
    public void setObserver(Observer observer)
    {
        this.observer=observer;
    }
}
