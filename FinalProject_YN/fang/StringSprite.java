package fang;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;

import fang.Sprite;

/**
 * This class converts text into a sprite.
 * The text can contain multiple lines by
 * putting in newlines.  Also, different
 * true type fonts may be used.  During
 * development, the fonts may be loaded
 * automatically from the system, but when
 * publishing the game, all fonts used
 * should be copied into the tipgame.fonts
 * package and should have the extension
 * .ttf  In Windows, fonts can typically
 * be found in C:\WINDOWS\Fonts.  The files
 * corresponding to the fonts used can be 
 * copied into tipgame.fonts to make sure 
 * the target computers are able to 
 * properly display the font (since not
 * all computers have the same fonts
 * available by default).  This class
 * differs from the PrettyStringSprite in
 * that it is much faster and sometimes
 * does not represent the font as cleanly.
 * @author Jam Jenkins
 */

public class StringSprite extends Sprite
{
    /**deep refers to whether this method is being 
     * called by a user of the class or by this 
     * class internally.  Because StringSprites 
     * need to maintain consistent heights when 
     * changing text, scale must be handled
     * differently than in the normal way.  This 
     * is because the scale can actually change 
     * with a change in the text displayed.*/
    private int deep=0;
    
    /**the current sequence of characters*/
    private String text="";
    
    /**cache of letter samples for each font used*/
    private static final HashMap<Font, HashMap<Character, PrettyStringSprite>> CACHE=
        new HashMap<Font, HashMap<Character, PrettyStringSprite>>();
    
    /**sample letters in the current font used*/
    private HashMap<Character, PrettyStringSprite> letterRepository=
        new HashMap<Character, PrettyStringSprite>();
    
    /**base height of a line of text*/
    private double height;
    
    /**max width of any charater in this font*/
    private double width;
    
    /**the vertical margin between lines of text*/
    private static final double LEADING=0.2;
    
    /**the horizontal spacing between characters*/
    private static final double ADVANCE=0.1;
    
    /**the horizontal distance of aspace*/
    private static final double SPACE_WIDTH=0.2;
    
    /**how many character spaces a tab represents*/
    private static final double TAB_WIDTH=2.5;
    
    /**for left, center, and right
     * justification which is -1, 0,
     * and 1 respectively     */
    private Point2D.Double justify=new Point2D.Double(0, 0);
    
    /**how this text is rendered*/
    private Font font=new Font(null, Font.PLAIN, 12);
    
    /**whether to make all characters take the
     * same number of space or be variable width*/
    private boolean monospaced=false;
    
    /**true indicates the text should retain its original width and height,
     * false means the text should be expanded to fit a square*/
    private boolean keepAspect=true;
    
    /**the original rotation of the text*/
    private double baseRotate=0;
    
    /**makes a line at the base line of the text*/
    private boolean underlined=false;
    
    /**makes a Sprite containing a String 
     * resized so that the height is 1 pixel*/
    public StringSprite()
    {
        super();
        loadSampleLetters();
        setMaxDimension(1);
    }
    
    /**makes a Sprite containing a String 
     * resized so that the height is 1 pixel
     * @param text the String to display*/
    public StringSprite(String text)
    {
        super();
        loadSampleLetters();
        setText(text);
        setMaxDimension(1);
    }

    /**makes a Sprite containing a String 
     * resized so that the height is 1 pixel
     * @param text the String to display
     * @param keepAspect true indicates to
     * leave the aspect ratio of the text
     * unchanged while false means to shrink
     * or expand the text to fit into a 
     * square*/
    public StringSprite(String text,
            boolean keepAspect)
    {
        super();
        loadSampleLetters();
        setKeepAspect(keepAspect);
        setText(text);        
        setMaxDimension(1);
    }

    /**makes a Sprite containing a String 
     * resized so that the height is 1 pixel
     * @param text the String to display
     * @param aspect true indicates to
     * leave the aspect ratio of the text
     * unchanged while false means to shrink
     * or expand the text to fit into a 
     * square
     * @param baseRotate an initial offset
     * for the rotation which does not affect
     * its original orientation
     * */
    public StringSprite(String text,
            boolean aspect, double baseRotate)
    {
        super();
        this.baseRotate=baseRotate;
        loadSampleLetters();
        setKeepAspect(keepAspect);
        setText(text);        
        setMaxDimension(1);
    }
    
    /**
     * sets the height of an individual line
     * in pixels
     * @param height the height of one line of
     * text in pixels
     */
    public void setLineHeight(double height)
    {
        deep++;
        setScale(height);
        deep--;
    }
    
    /**makes the size of this sprite such that the
     * height of all lines of the text is a 
     * given number of pixels high
     * @param height the pixels high of 
     * the lines of text
     */
    public void setHeight(double height)
    {
        //setting the height of a blank
        //string doesn't really make since
        if(getText().equals(""))
        {
            setText("A");
            deep++;
            scale(height/getHeight());
            deep--;
            setText("");
        }
        else
        {
            deep++;
            scale(height/getHeight());
            deep--;
        }
    }    
    
    /**makes the size of this sprite such that the
     * width of the text is a given number of pixels
     * @param width the pixels wide of the original text
     */
    public void setWidth(double width)
    {
        //setting the height of a blank
        //string doesn't really make since
        if(getText().equals(""))
        {
            setText("A");
            deep++;
            scale(width/getWidth());
            deep--;
            setText("");
        }
        else
        {
            deep++;
            scale(width/getWidth());
            deep--;
        }
    }

    /**sets the actual scale while keeping
     * the location constant.  This
     * is necessary because of the
     * justification manipulations.
     * @param dimension the size in pixels
     */
    private void setMaxDimension(double dimension)
    {
        deep++;
        scale(dimension/getMaxDimension());
        deep--;
    }
    
    /**gets the actual scale in pixels.
     * @return scale the size in pixels
     */
    private double getMaxDimension()
    {
        deep++;
        double max=Math.max(getWidth(), getHeight());
        deep--;
        return max;
    }
    
    /**sets the scale while keeping
     * the location constant.  This
     * is necessary because of the
     * justification manipulations.
     * @param scale the size in pixels
     */
    public void setScale(double scale)
    {
        //deep refers to whether this
        //method is being called by
        //a user of the class or by
        //this class internally.  Because
        //StringSprites need to maintain
        //consistent heights when changing
        //text, scale must be handled
        //differently than in the normal
        //way.  This is because the scale
        //can actually change with a
        //change in the text displayed.
        if(deep>0)
            super.setScale(scale);
        else
            setMaxDimension(scale);
    }
    
    /**gets the scale in pixels.
     * @return scale the size in pixels
     */
    public double getScale()
    {
        //deep refers to whether this
        //method is being called by
        //a user of the class or by
        //this class internally.  Because
        //StringSprites need to maintain
        //consistent heights when changing
        //text, scale must be handled
        //differently than in the normal
        //way.  This is because the scale
        //can actually change with a
        //change in the text displayed.
        if(deep>0)  
            return super.getScale();
        return getMaxDimension();
    }
    
    /**sets the slant of the text
     * @param italics true indicates
     * slant, false is for no slant
     */
    public void setItalicized(boolean italics)
    {
        if(font.isItalic()==italics)
            return;
        if(italics)
            font=font.deriveFont(font.getStyle() & Font.ITALIC);
        else
            font=font.deriveFont(font.getStyle() & ~Font.ITALIC);
        loadSampleLetters();
    }

    /**sets the thickness of the lettering
     * @param bold true indicates thick
     * lettering, false indicates normal
     * thickness
     */
    public void setBold(boolean bold)
    {
        if(font.isBold()==bold)
            return;
        if(bold)
            font=font.deriveFont(font.getStyle() & Font.BOLD);
        else
            font=font.deriveFont(font.getStyle() & ~Font.BOLD);
        loadSampleLetters();
    }        

    /**determines if the text is slanted
     * @return true if the text is slanted,
     * false otherwise
     */
    public boolean isItalicized()
    {
        return font.isItalic();
    }

    /**determines the thickness of the
     * lettering
     * @return true if the text has extra
     * thick lettering, false if the
     * thickness is normal
     */
    public boolean isBold()
    {
        return font.isBold();
    }
    
    /**
     * sets whether the dimensions should
     * or should not be manipulated to fill
     * a square
     * @param keepAspect true indicates the
     * normal dimensions should remain
     * unchanged, false means the text should
     * be expanded to fit a square
     */
    public void setKeepAspect(boolean keepAspect)
    {
        this.keepAspect=keepAspect;
    }
    
    /**
     * gets whether the dimensions should
     * or should not be manipulated to fill
     * a square
     * @return true if the
     * normal dimensions remain
     * unchanged, false means the text is
     * being expanded to fit a square
     */
    public boolean getAspect()
    {
        return keepAspect;
    }
    
    /**makes the position represent the
     * left most position of the string
     */    
    public void leftJustify()
    {
        justify.x=-1;
    }
    
    /**makes the position represent the
     * right most position of the string
     */    
    public void rightJustify()
    {
        justify.x=1;
    }
    
    /**makes the position represent the
     * center position of the string.
     * StringSprites are center justified
     * by default.
     */    
    public void centerJustify()
    {
        justify.x=0;
        justify.y=0;
    }
    
    /**makes the position represent the
     * top most position of the string
     */    
    public void topJustify()
    {
        justify.y=-1;
    }
    
    /**makes the position represent the
     * bottom most position of the string
     */    
    public void bottomJustify()
    {
        justify.y=1;
    }
    
    /**sets whether there should or should
     * not be a line at the baseline of the text
     * @param underline true indicates there
     * should be a line, false indicates no line
     * should be there*/
    public void setUnderlined(boolean underline)
    {
        this.underlined=underline;
    }        
    
    /**gets the family name of the font
     * @return the family name of the font
     */
    public String getFontFamilyName()
    {
        return font.getFamily();
    }

    /**sets the family name of the font
     * @param familyName the family name of the font
     */
    public void setFontFamilyName(String familyName)
    {
        if(familyName==font.getFamily() ||
                familyName!=null && font.getFamily()!=null &&
                familyName.equals(font.getFamily()))
            return;
        font=PrettyStringSprite.getFont(font.getStyle(), familyName);
        font=font.deriveFont(font.getSize());
        loadSampleLetters();
    }
    
    /**determines if there is a line
     * at the baseline of the text
     * @return true if there is a line,
     * false if there is not
     */
    public boolean isUnderlined()
    {
        return underlined;
    }

    /**gets the full height that this
     * StringSprite could have with any
     * given text
     * @return the maximum height the
     * StringSprite will take for any
     * given sequence of characters
     */
    public double getLineHeight()
    {
        deep++;
        double h=height*getScale();
        deep--;
        return h;
    }

    /**
     * gets the bounds of the shape
     * @return the smallest rectangle
     * fitting around the text
     */
    public Rectangle2D getBounds2D()
    {
        return getShape().getBounds2D();
    }
    
    /**
     * gets the rotated bounds of the 
     * shape.  Note: unlike in other
     * Sprites, this does not give the
     * outline of the individual shapes
     * used to make the text.
     * @return the smallest rectangle
     * fitting around the text
     */
    public Shape getShape()
    {
        deep++;
        Rectangle2D.Double bounds;
        if(keepAspect)
            bounds=new Rectangle2D.Double(0, 0,
                    getUnscaledWidth(), getUnscaledHeight());
        else
            bounds=new Rectangle2D.Double(0, 0,
                    Math.max(getUnscaledWidth(), getUnscaledHeight()),
                    Math.max(getUnscaledWidth(), getUnscaledHeight()));
        if(justify.x==0)
            bounds.x=-bounds.getCenterX();
        else if(justify.x==1)
            bounds.x=-bounds.width;
        if(justify.y==0)
            bounds.y=-bounds.getCenterY();
        else if(justify.y==1)
            bounds.y=-bounds.height;
        GeneralPath path=new GeneralPath(bounds);
        path.transform(transform);
        deep--;
        return path;
    }
    
    /**
     * gets the style of the current text
     * @return the font being used to
     * generate the text
     */
    public Font getFont()
    {
        return font;
    }
    
    /**
     * sets the style of the current text
     * @param font the font being used to
     * generate the text
     */
    public void setFont(Font font)
    {
        this.font=font;
        loadSampleLetters();
    }
    
    /**loads all numbers and letters into
     * the letterRepository*/
    private void loadSampleLetters()
    {
        if(CACHE.containsKey(font))
        {
            letterRepository=CACHE.get(font);
        }
        else
        {
            letterRepository=
                new HashMap<Character, PrettyStringSprite>();
            for(int i=0; i<26; i++)
            {
                getSprite((char)('A'+i));
                getSprite((char)('a'+i));
                if(i<10)
                    getSprite((char)('0'+i));
            }
            CACHE.put(font, letterRepository);
        }
        for(PrettyStringSprite sprite: letterRepository.values())
        {
            width=Math.max(width, sprite.getWidth());
            height=sprite.getHeight();
        }
    }
    
    /**updates the text
     * @param text the new text
     * @param aspect false means the text
     * should be expanded to fit a square,
     * true indicates the text should 
     * retain its original shape
     * @param underline true indicates there
     * should be a line at the baseline,
     * false indicates no line
     */
    public void setText(String text, boolean aspect,
            boolean underline)
    {
        this.underlined=underline;
        this.keepAspect=aspect;
        this.text=text;
    }
    
    /**
     * sets the text of the Sprite
     * 
     * @param text
     *            the String for the Sprite to contain
     * @param aspect
     *            true indicates original width and height should be retained,
     *            false indicates the text should be resized to fit within a
     *            square
     */
    public void setText(String text, boolean aspect)
    {
        setText(text, aspect, underlined);
    }

    /**
     * sets the text of the Sprite
     * 
     * @param text
     *            the String for the Sprite to contain
     */
    public void setText(String text)
    {
        setText(text, keepAspect, underlined);
    }
        
    /**gets the PrettyStringSprite corresponding to
     * the character given
     * @param letter the letter to translate
     * @return the corresponding PrettyStringSprite
     */
    private PrettyStringSprite getSprite(char letter)
    {
        if(!letterRepository.containsKey(letter))
        {
            PrettyStringSprite letterSprite=new PrettyStringSprite(""+letter, true);
            letterSprite.setFontFamilyName(font.getFamily());
            letterSprite.setBold(font.isBold());
            letterSprite.setItalicized(font.isItalic());
            letterSprite.setHeight(1);
            letterSprite.setColor(color);
            height=letterSprite.getHeight();
            letterRepository.put(letter, letterSprite);
        }
        return letterRepository.get(letter);
    }
    
    /**gets the height of this Sprite in pixels
     * @return the vertical span in pixels*/
    public double getHeight()
    {
        deep++;
        double h=getScale()*getUnscaledHeight();
        deep--;
        return h;
    }

    /**gets the height of this Sprite in pixels
     * before adding the transform
     * @return the untransformed vertical span 
     * in pixels*/
    private double getUnscaledHeight()
    {
        return height*(1+LEADING)*text.split("\n").length-LEADING*height;
    }
    
    /**gets the width of a given line of text
     * in this sprite.  Invalid lines return
     * width 0.
     * @param index the line number with 0
     * being the first line
     * @return the horizontal span of the given
     * line in pixels*/
    public double getLineWidth(int index)
    {
        deep++;
        double w=getScale()*getUnscaledLineWidth(index);
        deep--;
        return w;
    }
    
    /**gets the untransformed width of a given 
     * line of text in this sprite.  Invalid 
     * lines return width 0.
     * @param index the line number with 0
     * being the first line
     * @return the untransformed horizontal 
     * span of the given line in pixels*/
    private double getUnscaledLineWidth(int index)
    {
        if(index<0 || index>=text.split("\n").length)
            return 0;
        String line=text.split("\n")[index];
        double lineWidth=-ADVANCE*width;
        for(char letter: line.toCharArray())
        {
            if(letter=='\t')
            {
                if(monospaced)
                    lineWidth+=Math.round(TAB_WIDTH)*(width+ADVANCE*width);
                else
                    lineWidth+=TAB_WIDTH*(width+ADVANCE*width);
            }
            else if(letter==' ')
            {
                if(monospaced)
                    lineWidth+=width+ADVANCE*width;
                else
                    lineWidth+=SPACE_WIDTH*width+ADVANCE*width;
            }
            else
            {
                if(monospaced)
                    lineWidth+=width+ADVANCE*width;
                else
                    lineWidth+=getSprite(letter).getWidth()+ADVANCE*width;
            }
        }
        return lineWidth;
    }
    
    /**sets whether this text should be represented
     * with fixed or variable width text. 
     * @param monospaced true indicates fixed width
     * text, false means variable width text
     */
    public void setMonospaced(boolean monospaced)
    {
        this.monospaced=monospaced;
    }
    
    /**sets the color of this sprite
     * @param color the shade of the sprite*/
    public void setColor(Color color)
    {
        this.color=color;
        for(PrettyStringSprite sprite: letterRepository.values())
            sprite.setColor(color);
    }
        
    /**gets the width of this Sprite in pixels
     * @return the horizontal span in pixels*/
    public double getWidth()
    {
        deep++;
        double w=getScale()*getUnscaledWidth();
        deep--;
        return w;
    }
        
    /**gets the width of this Sprite in pixels
     * before adding the transform
     * @return the untransformed horizontal span 
     * in pixels*/
    private double getUnscaledWidth()
    {
        double lineWidth=getUnscaledLineWidth(0);
        for(int i=1; i<text.split("\n").length; i++)
            lineWidth=Math.max(lineWidth, getUnscaledLineWidth(i));
        return lineWidth;
    }     
    
    /**gets the rotation of this sprite.  The rotation
     * returned is minus the original base rotation
     * @see fang.Sprite#getRotation()
     */
    public double getRotation()
    {
        deep++;
        double adjusted = super.getRotation() - baseRotate;
        while (adjusted < 0)
            adjusted += Math.PI * 2;
        double r=super.getRotation();
        deep--;
        return r;
    }
    
    private void draw(Graphics2D brush)
    {
        int lineNumber=0;
        double lateralMovement=-getUnscaledLineWidth(lineNumber)/2*
            (justify.x+1);
        brush.translate(lateralMovement, 0);
        for(char letter: text.toCharArray())
        {
            if(letter=='\n')
            {
                brush.translate(
                        -lateralMovement, 
                        height+LEADING*height);
                lineNumber++;
                lateralMovement=-getUnscaledLineWidth(lineNumber)/2*
                    (justify.x+1);
                brush.translate(lateralMovement, 0);
                continue;
            }
            if(letter=='\t')
            {
                double spaces=TAB_WIDTH;
                if(monospaced)
                    spaces=Math.round(spaces);
                brush.transform(AffineTransform.getTranslateInstance(
                        (width+width*ADVANCE)*spaces, 0));
                lateralMovement+=(width+width*ADVANCE)*spaces;
                continue;
            }            
            if(letter==' ')
            {
                double fractionalWidth=SPACE_WIDTH;
                if(monospaced)
                    fractionalWidth=1;
                brush.transform(AffineTransform.getTranslateInstance(
                        width*ADVANCE+width*fractionalWidth, 0));
                lateralMovement+=width*ADVANCE+width*fractionalWidth;
                continue;
            }
            if(monospaced)
                lateralMovement+=width;
            else
                lateralMovement+=getSprite(letter).getWidth();
            lateralMovement+=width*ADVANCE;
            if(monospaced)
                brush.translate(width/2, 0);
            else
                brush.translate(getSprite(letter).getWidth()/2, 0);
            if(underlined)
            {
                double uWidth=width*ADVANCE;
                if(monospaced)
                    uWidth+=width;
                else
                    uWidth+=getSprite(letter).getWidth();
                Rectangle2D.Double line=new Rectangle2D.Double(
                        -uWidth/2, 5/12.0, uWidth, 1/12.0);
                brush.fill(line);
            }
            getSprite(letter).setColor(color);
            getSprite(letter).paint(brush);
            if(monospaced)
                brush.translate(width/2, 0);
            else
                brush.translate(getSprite(letter).getWidth()/2, 0);
            brush.translate(width*ADVANCE, 0);            
        }        
    }
    
    /**scales the StringSprite by a given factor
     * relative to it's current scale.
     * @param s the scaling factor
     */
    public void scale(double s)
    {
        if(deep>0)
            super.scale(s);
        else
            setMaxDimension(s*getMaxDimension());
    }

    
    /**
     * paints the text by transforming the brush
     * and placing the characters from the letterRepository
     * @param brush the drawing instrument
     */
    public void paint(Graphics2D brush)
    {
        AffineTransform original=brush.getTransform();
        brush.transform(transform);
        if(!keepAspect)
            brush.transform(AffineTransform.getScaleInstance(
                    getMaxDimension()/getWidth(),
                    getMaxDimension()/getHeight()));
        brush.setColor(color);
        brush.translate(0,
                -getUnscaledHeight()/2.0*(justify.y+1)+height/2.0);
        draw(brush);
        brush.setTransform(original);
    }
    
    /**
     * gets the characters currently
     * in this StringSprite
     * @return the sequence of characters
     * in the sprite
     */
    public String getText()
    {
        return text;
    }
}
