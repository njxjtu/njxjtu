package fang;

import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.awt.geom.*;
import java.util.LinkedList;

import fang.Keyboard;
import fang.Mouse;
import fang.Sprite;
import fang.Tracker;


/**This class enables text input
 * within the AnimationCanvas via
 * a special Sprite which responds
 * to keyboard input when the 
 * specified mouse is inside the box.
 * @author Jam Jenkins
 */
public class StringField
    extends Sprite
{
    /**margin from the edge of the inner box*/
    public static final double MARGIN=0.1;
    /**the inner box*/
    private Rectangle2D bigger;
    /**the outer box*/
    private Rectangle2D evenBigger;
    /**the text in the box*/
    private String text;
    
    /**whether the text being entered
     * has a line at the baseline or not*/
    private boolean underline=false;
    
    /**the bold and italics property*/
    private int fontStyle=Font.PLAIN;
    
    /**the font family name*/
    private String style=null;

    /**the tracker intermediary which traps
     * keyboard input when the mouse is over
     * the box*/
    private InputTracker inputTracker;
    
    /**the composite tracker this tracker holds*/
    private CompositeTracker composite;
        
    /**This is a special tracker which responds
     * to keyboard and mouse input and also
     * forwards the advancing of time to another
     * tracker which may be attached.  This
     * tracker controls the content of the box
     * while any other tracker attached to this
     * StringField will control the location,
     * rotation, and scaling.
     * @author Jam Jenkins
     */
    public class InputTracker extends TrackerAdapter
    {
        /**used to call methods on the outer class*/
        private StringField outer;
        /**the keyboards to which this tracker responds*/
        private LinkedList<Keyboard> keyboard=
            new LinkedList<Keyboard>();
        /**the mice to which this tracker responds*/
        private LinkedList<Mouse> mouse=
            new LinkedList<Mouse>();
        
        /**makes this Tracker.
         * @param outer the outer class
         */
        public InputTracker(StringField outer)
        {
            this.outer=outer;
        }
        
        /**enables the keyboard and mouse for input
         * @param k the keyboard to respond to
         * @param m the mouse used for location
         * in determining if the keyboard input
         * should be ignored or not
         */
        public void enable(Keyboard k, Mouse m)
        {
            keyboard.addLast(k);
            mouse.addLast(m);
        }
        
        /**disables the keyboard and mouse 
         * combination for input
         * @param k the keyboard to remove to
         * @param m the corresponding mouse used 
         * for location in determining if the 
         * keyboard input should be ignored or not
         */
        public void disable(Keyboard k, Mouse m)
        {
            Keyboard[] kArray=keyboard.toArray(new Keyboard[0]);
            Mouse[] mArray=mouse.toArray(new Mouse[0]);
            for(int i=kArray.length-1; i>=0; i--)
            {
                if(kArray[i]==k && mArray[i]==m)
                {
                    keyboard.remove(i);
                    mouse.remove(i);
                }
            }
        }
        
        /**disables all user input*/
        public void disable()
        {
            mouse.clear();
            keyboard.clear();
        }

        /**checks the location of the mouse to
         * see if it is over the box containing
         * the text.  If it is, then keyboard input
         * will change the contents of this sprite.
         * This method also forwards the advancing
         * of time to the auxilary tracker if
         * attached
         * @param time the time since the last
         * frame
         */
        public void advanceTime(double time)
        {
            if(mouse.isEmpty())
                return;
            int index=0;
            for(Mouse mouse: this.mouse)
            {
                Keyboard keyboard=this.keyboard.get(index);
                Point2D.Double position=mouse.getLocation();
                if(position!=null &&
                    outer.getBounds2D().contains(position))
                {
                    char key=keyboard.getLastKey();
                    if(key!=KeyEvent.CHAR_UNDEFINED)
                    {
                        if(key!=KeyEvent.VK_BACK_SPACE)
                            setText(getText()+keyboard.getLastKey());
                        else
                        {
                            if(text.length()>0)
                                setText(text.substring(0, text.length()-1));
                        }
                    }
                }
                index++;
            }
        }
    }
    
    /**
     * selects which mouse and which keyboard will
     * be allowed to edit the StringField
     * @param keyboard
     * @param mouse
     */
    public void enableInput(Keyboard keyboard, Mouse mouse)
    {
        inputTracker.enable(keyboard, mouse);
    }
    
    /**
     * this method is disabled for the
     * StringField.  Instead use the
     * addTracker method.
     * @param t the tracker to add
     */
    public void setTracker(Tracker t)
    {
        
    }    
    
    /**
     * adds the tracker to the sprite
     * @param t the tracker
     */
    public void addTracker(Tracker t)
    {
        composite.addTracker(t);
    }
    
    /**
     * gets all the added trackers to this sprite
     * @return the array of trackers which have been added
     */
    public Tracker[] getAddedTrackers()
    {
        Tracker[] tracker=composite.getAllTrackers();
        Tracker[] toReturn=new Tracker[tracker.length-1];
        int index=0;
        for(Tracker track: tracker)
        {
            if(track!=inputTracker)
            {
                toReturn[index]=track;
                index++;
            }
        }
        return toReturn;
    }
    
    /**
     * removes a tracker from this sprite
     * @param t the tracker to remove
     */
    public void removeTracker(Tracker t)
    {
        composite.removeTracker(t);
    }
        
    /**
     * disables all input
     */
    public void disableInput()
    {
        inputTracker.disable();
    }

    /**diables a keyboard mouse pair
     * @param keyboard the keyboard to ignore
     * @param mouse the corresponding mouse
     */
    public void disableInput(Keyboard keyboard, Mouse mouse)
    {
        inputTracker.disable(keyboard, mouse);
    }
    
    /**
     * creates a StringField with space
     * for a certain number of fixed
     * width characters
     * @param characters the number of
     * characters wide to make the StringField
     */
    public StringField(int characters)
    {
        super();
        text="";
        for(int i=0; i<characters; i++)
            text+="O";
        initialize();
        setText("");
    }

    /**
     * creates a StringField with space
     * for a certain number of fixed
     * width characters
     * @param characters the number of
     * characters wide to make the StringField
     */
    public StringField(int rows, int characters)
    {
        super();
        text="";
        for(int r=0; r<rows; r++)
        {
            for(int i=0; i<characters; i++)
                text+="O";
            if(r<rows-1)
                text+="\n";
        }
        initialize();
        setText("");
    }
    
    /**
     * creates a StringField with a
     * given starting text.  This starting
     * text indicates the default width of
     * the StringField.
     * @param text the starting String
     */
    public StringField(String text)
    {
        super();
        this.text=text;
        initialize();
    }
    
    /**
     * starts out the shape and sets up
     * the input tracker
     */
    private void initialize()
    {
        inputTracker=new InputTracker(this);
        composite=new CompositeTracker();
        composite.addTracker(inputTracker);
        super.setTracker(composite);
        PrettyStringSprite sprite=new PrettyStringSprite(text, true);
        sprite.setHeight(1);
        sprite.leftJustify();
        Rectangle2D box=sprite.getBounds2D();
        double margin=MARGIN*Math.min(box.getWidth(), box.getHeight());
        bigger=new Rectangle2D.Double(
                box.getX()-margin, 
                box.getY()-margin,
                box.getWidth()+2*margin,
                box.getHeight()+2*margin);
        evenBigger=new Rectangle2D.Double(
                box.getX()-2*margin, 
                box.getY()-2*margin,
                box.getWidth()+4*margin,
                box.getHeight()+4*margin);
        Area total=new Area();
        total.add(new Area(evenBigger));
        total.subtract(new Area(bigger));
        total.add(new Area(sprite.getShape()));
        total.intersect(new Area(evenBigger));
        setShape(total);        
    }
    
    /**sets the sequence of characters in
     * the StringField
     * @param text the new sequence of
     * characters
     */
    public void setText(String text)
    {
        if(this.text==text ||
                text!=null && this.text!=null &&
                this.text.equals(text))
            return;
        this.text=text;
        updateText();
    }

    /**changes the text in the box
     */
    private void updateText()
    {
        PrettyStringSprite sprite=new PrettyStringSprite(text, true);
        sprite.setHeight(1);
        sprite.leftJustify();
        sprite.topJustify();
        sprite.setFontFamilyName(style);
        sprite.setBold(isBold());
        sprite.setItalicized(isItalicized());
        sprite.setUnderlined(isUnderlined());
        sprite.setLocation(bigger.getMinX()+evenBigger.getHeight()/2-bigger.getHeight()/2, 
                bigger.getMinY()+evenBigger.getHeight()/2-bigger.getHeight()/2);
        Area total=new Area();
        total.add(new Area(evenBigger));
        total.subtract(new Area(bigger));
        if(sprite.getBounds2D().getWidth()>bigger.getWidth())
        {
            int lastLength=0;
            double maxLength=0;
            for(String line: text.split("\n"))
            {
                lastLength=line.length();
                maxLength=Math.max(lastLength, maxLength);
            }
            double offset=0;
            if(lastLength<maxLength)
                offset=-bigger.getWidth()*(1-(lastLength+1)/maxLength);
            sprite.setLocation(offset+bigger.getMaxX()-
                    (lastLength/maxLength)*sprite.getWidth(), sprite.getLocation().y);
        }
        if(sprite.getBounds2D().getHeight()>bigger.getHeight())
        {
            sprite.bottomJustify();
            sprite.setLocation(sprite.getLocation().x, bigger.getMaxY());
        }
        total.add(new Area(sprite.getShape()));
        total.intersect(new Area(evenBigger));
        setShape(total);        
    }
    
    /**
     * gets the contents of the StringField
     * @return the sequence of characters
     * in the StringField
     */
    public String getText()
    {
        return text;
    }
    
    /**sets the slant of the text
     * @param italics true indicates
     * slant, false is for no slant
     */
    public void setItalicized(boolean italics)
    {
        if(isItalicized()==italics)
            return;
        fontStyle=fontStyle | Font.ITALIC;
        updateText();
    }

    /**sets the thickness of the lettering
     * @param bold true indicates thick
     * lettering, false indicates normal
     * thickness
     */
    public void setBold(boolean bold)
    {
        if(isBold()==bold)
            return;
        fontStyle=fontStyle | Font.BOLD;
        updateText();
    }

    /**determines if the text is slanted
     * @return true if the text is slanted,
     * false otherwise
     */
    public boolean isItalicized()
    {
        return (fontStyle & Font.ITALIC)!=0;        
    }
    
    /**determines the thickness of the
     * lettering
     * @return true if the text has extra
     * thick lettering, false if the
     * thickness is normal
     */
    public boolean isBold()
    {
        return (fontStyle & Font.BOLD)!=0;
    }

    /**sets whether there should or should
     * not be a line at the baseline of the text
     * @param underline true indicates there
     * should be a line, false indicates no line
     * should be there
     */
    public void setUnderlined(boolean underline)
    {
        if(this.underline==underline)
            return;
        this.underline=underline;
        updateText();        
    }
    
    /**gets the family name of the font
     * @return the family name of the font
     */
    public String getStyle()
    {
        return style;
    }
    
    /**sets the family name of the font
     * @param style the family name of the font
     */
    public void setStyle(String style)
    {
        if(style==this.style || this.style!=null && style!=null && this.style.equals(style))
            return;
        this.style=style;
        updateText();
    }
    
    /**determines if there is a line
     * at the baseline of the text
     * @return true if there is a line,
     * false if there is not
     */
    public boolean isUnderlined()
    {
        return underline;
    }
}
