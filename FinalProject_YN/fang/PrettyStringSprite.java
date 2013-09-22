package fang;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.net.URL;
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
 * should be copied into the fang.resources
 * package and should have the extension
 * .ttf  In Windows, fonts can typically
 * be found in C:\WINDOWS\Fonts.  The files
 * corresponding to the fonts used can be 
 * copied into fang.resources to make sure 
 * the target computers are able to 
 * properly display the font (since not
 * all computers have the same fonts
 * available by default).  
 * @author Jam Jenkins
 */
public class PrettyStringSprite extends Sprite
{
    /**
     * true indicates the text should retain its original width and height,
     * false means the text should be expanded to fit a square
     */
    private boolean keepAspect;
    
    /**makes a line at the base line of the text*/
    private boolean underline=false;
    
    /**the bold and italics property*/
    private int fontStyle=Font.PLAIN;
    
    /**the name of the font family*/
    private String style=null;
    
    /**fonts loaded from tipgame.fonts*/
    private static HashMap<String, Font> customFonts=
        new HashMap<String, Font>();
    
    /**contains the family names of all the system wide 
     * fonts available*/
    private static HashMap<String, Font> availableFonts=
        getAvailableFonts();
        
    /**the base bounding box of all fonts previously used*/
    private static HashMap<String, Rectangle2D> fontSize=
        new HashMap<String, Rectangle2D>();
        
    /**margin above and below lines*/
    private static final double MARGIN=0.05;
    
    /**
     * the original rotation of the text.
     */
    private double baseRotation;

    /** the scaling done on the original
     * shape to make it fit into a 1x1 box.
     */
    private double originalSize=1;
    /**for left, center, and right
     * justification which is -1, 0,
     * and 1 respectively     */
    private Point justify=new Point();
    
    /**the current sequence of characters*/
    private String text;
    
    /**
     * makes a Sprite containing a String resized to fit within a square
     * 
     * @param text
     *            the String for the Sprite to contain
     */
    public PrettyStringSprite(String text)
    {
        this(text, false);
        keepAspect = false;
    }

    /**gets the names of all the system's available
     * font family names
     * @return the font family names
     */
    private static HashMap<String, Font> getAvailableFonts()
    {
        HashMap<String, Font> set=new HashMap<String, Font>();
        for(String name: GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames())
            set.put(name, null);
        return set;
    }

    /**
     * makes a Sprite containing a String
     * 
     * @param text
     *            the String for the Sprite to contain
     * @param aspect
     *            true indicates original width and height should be retained,
     *            false indicates the text should be resized to fit within a
     *            square
     */
    public PrettyStringSprite(String text, boolean aspect)
    {
        super();
        setText(text, aspect);
        setScale(1);
    }

    /**
     * makes a Sprite containing a String
     * 
     * @param text
     *            the String for the Sprite to contain
     * @param aspect
     *            true indicates original width and height should be retained,
     *            false indicates the text should be resized to fit within a
     *            square
     * @param baseRotate
     *            the original orientation
     */
    public PrettyStringSprite(String text, boolean aspect, double baseRotate)
    {
        this.baseRotation = baseRotate;
        setText(text, aspect);
        setScale(1);
    }

    /**
     * sets the height of an individual line
     * in pixels
     * @param height the height of one line of
     * text in pixels
     */
    public void setLineHeight(double height)
    {
        setScale(height*originalSize);
    }
    
    /**makes the size of this sprite such that the
     * height of all lines of the text is a 
     * given number of pixels high
     * @param height the pixels high of 
     * the lines of text
     */
    public void setHeight(double height)
    {
        setScale(height*originalSize/text.split("\n").length);
    }
    
    /**sets the scale while keeping
     * the location constant.  This
     * is necessary because of the
     * justification manipulations.
     * @param scale the size in pixels
     */
    public void setScale(double scale)
    {
        if(scale==getScale())
            return;
        Point2D.Double loc=getLocation();
        super.setScale(scale);
        setLocation(loc);
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
        double lineHeight=getLineHeight();
        updateText();
        setHeight(lineHeight);
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
        double lineHeight=getLineHeight();
        updateText();
        setHeight(lineHeight);
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
    
    /**makes the position represent the
     * left most position of the string
     */    
    public void leftJustify()
    {
        if(justify.x==-1)
            return;
        if(justify.x==0)
            translate(getWidth()/2, 0);
        else if(justify.x==1)
            translate(getWidth(), 0);
        justify.x=-1;
        if(text.split("\n").length>1)
        {
            double lineHeight=getLineHeight();
            updateText();
            setHeight(lineHeight);
        }
    }

    /**makes the position represent the
     * top most position of the string
     */    
    public void topJustify()
    {
        if(justify.y==-1)
            return;
        if(justify.y==0)
            translate(0, getHeight()/2);
        else if(justify.y==1)
            translate(0, getHeight());
        justify.y=-1;
    }

    /**makes the position represent the
     * bottom most position of the string
     */    
    public void bottomJustify()
    {
        if(justify.y==1)
            return;
        if(justify.y==0)
            translate(0, -getHeight()/2);
        else if(justify.y==-1)
            translate(0, -getHeight());
        justify.y=1;
    }
    
    /**makes the position represent the
     * right most position of the string
     */    
    public void rightJustify()
    {
        if(justify.x==1)
            return;
        if(justify.x==0)
            translate(-getWidth()/2, 0);
        else if(justify.x==-1)
            translate(-getWidth(), 0);
        justify.x=1;
        if(text.split("\n").length>1)
        {
            double lineHeight=getLineHeight();
            updateText();
            setHeight(lineHeight);
        }
    }
    
    /**makes the position represent the
     * center position of the string.
     * StringSprites are center justified
     * by default.
     */    
    public void centerJustify()
    {
        if(justify.x==-1)
        {
            translate(getWidth()/2, 0);
            if(text.split("\n").length>1)
                updateText();            
        }
        else if(justify.x==1)
        {
            translate(getWidth()/2, 0);
            if(text.split("\n").length>1)
                updateText();
        }
        justify.x=0;
        if(justify.y==-1)
            translate(0, getHeight()/2);
        else if(justify.y==1)
            translate(0, getHeight()/2);
        justify.y=0;
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
    public String getFontFamilyName()
    {
        return style;
    }
    
    /**sets the family name of the font
     * @param style the family name of the font
     */
    public void setFontFamilyName(String style)
    {
        if(style==this.style || this.style!=null && style!=null && this.style.equals(style))
            return;
        this.style=style;
        double lineHeight=getLineHeight();
        updateText();
        setHeight(lineHeight);
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
    
    /**currently disabled to prevent
     * loosing height information by
     * rescaling
     */
    protected void normalize()
    {
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
        return getScale()/originalSize;
    }
                
    /**moves the StringSprite from its current
     * location
     * @param x the amount to move from the current
     * horizontal position
     * @param y the amount to move from the current
     * vertical position
     */
    public void translate(double x, double y)
    {
        Point2D.Double original=getLocation();
        setLocation(original.x+x, original.y+y);
    }
    
    
    /**moves the StringSprite from its current
     * location to a new location
     * @param x the new horizontal position
     * @param y the new vertical position
     */
    public void setLocation(double x, double y)
    {
        super.setLocation(
                x-justify.x*getWidth()/2, 
                y-justify.y*getHeight()/2);
    }
    
    /**
     * returns the position of this StringSprite.
     * The location will correspond to the leftmost,
     * center, or rightmost point depending on the
     * justification.
     * @return the position of the StringSprite
     */
    public Point2D.Double getLocation()
    {
        Point2D.Double location=super.getLocation();
        location.x+=justify.x*getWidth()/2;
        location.y+=justify.y*getHeight()/2;
        return location;
    }
    
    /**
     * gets the horizontal span in pixels of
     * this StringSprite
     * @return the horizontal span
     */
    public double getWidth()
    {
        double originalRotation=getRotation();
        setRotation(0);
        double original=getShape().getBounds2D().getWidth();
        setRotation(originalRotation);
        return original;
    }
    
    /**gets the smallest rectangle which will
     * surrond the shape
     * @return the minimal bounding rectangle*/
    public Rectangle2D getBounds2D()
    {
        Point2D.Double location=getLocation();
        double w=getWidth();
        double h=getHeight();
        double x;
        double y;
        if(justify.x==-1)
            x=location.x;
        else if(justify.x==0)
            x=location.x-w/2;
        else
            x=location.x-w;
        if(justify.y==-1)
            y=location.y;
        else if(justify.y==0)
            y=location.y-h/2;
        else
            y=location.y-h;
        return new Rectangle2D.Double(x, y, w, h);
    }

    /**
     * gets the vertical span in pixels of
     * this StringSprite
     * @return the vertical span
     */
    public double getHeight()
    {
        return getLineHeight()*text.split("\n").length;
    }
    
    /**makes the size of this sprite such that the
     * width of the text is a given number of pixels
     * @param width the pixels wide of the original text
     */
    public void setWidth(double width)
    {
        scale(width/getWidth());
    }
    
    /**makes the font from the font family name
     * @param fontStyle the style (PLAIN/BOLD/ITALIC)
     * @param fontFamily the font's family name
     * @return the font, or the default font if
     * the family name font is not available
     */
    public static Font getFont(int fontStyle, String fontFamily)
    {
    	if(fontFamily==null)
            return new Font(null, fontStyle, 12);

    	if(availableFonts.containsKey(fontFamily))
        {
            if(availableFonts.get(fontFamily)==null)
            {
                availableFonts.put(fontFamily, new Font(fontFamily, fontStyle, 12));
            }
            return availableFonts.get(fontFamily).deriveFont(fontStyle);
        }
        if(customFonts.containsKey(fontFamily))
            return customFonts.get(fontFamily).deriveFont(fontStyle);
        URL url=PrettyStringSprite.class.getResource("resources/"+fontFamily+".ttf");
        try
        {
            Font base=Font.createFont(Font.TRUETYPE_FONT, 
                    url.openStream());
            if(!base.getFamily().equals(fontFamily))
                System.err.println("Must match font file with font name\n"+
                        "Font file: fang/resources/"+fontFamily+".ttf\n"+
                        "Font name: fang/resources/"+base.getFamily()+"\n"+
                        "Please rename fang/resources/"+fontFamily+".ttf to " +
                        "fang/resources/"+base.getFamily()+".ttf");                  
            customFonts.put(fontFamily, base);
            return base.deriveFont(fontStyle);
        } catch (Exception e)
        {
        	System.err.println("Font family "+fontFamily+" not found.\n"+
        			"Put "+fontFamily+".ttf in fang/resources package.\n"+
        			"Also check for spelling and case sensitivity errors.");
            return new Font(null, fontStyle, 12);
        }        
    }
    
    /**makes the font from the font family name
     * @param fontStyle the style (PLAIN/BOLD/ITALIC)
     * @return the font, or the default font if
     * the family name font is not available
     */
    private Font getFont(int fontStyle)
    {
        return getFont(fontStyle, style);
    }
    
    /**this method updates the text shape
     * and is only called when this shape
     * would change from what it was before
     */
    private void updateText()
    {
        if(text.length()==0)
        {
            setShape(new GeneralPath());
            originalSize=1;
        }
        double originalWidth=getWidth();
        double originalHeight=getHeight();
        String[] lines=text.split("\n");
        Rectangle2D boundsBase;
        Font font;
        if(fontSize.containsKey(style))
        {
            Rectangle2D temp=fontSize.get(style);
            boundsBase=new Rectangle2D.Double(
                    temp.getMinX(), temp.getMinY(),
                    temp.getWidth(), temp.getHeight());
        }
        else
        {
            font=getFont(Font.PLAIN);       
            GlyphVector glyphBase = font.createGlyphVector(
                new FontRenderContext(null,
                false, true), "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789");
            GeneralPath sBase = new GeneralPath(
                glyphBase.getOutline());
            Rectangle2D temp=sBase.getBounds2D();
            fontSize.put(style, temp);
            boundsBase=new Rectangle2D.Double(
                    temp.getMinX(), temp.getMinY(),
                    temp.getWidth(), temp.getHeight());
        }
        double vertOffset=-boundsBase.getMinY();
        boundsBase.setRect(
                boundsBase.getMinX(),
                boundsBase.getMinY()+vertOffset,
                boundsBase.getWidth(),
                boundsBase.getHeight());
        Area total=new Area();
        double y=MARGIN;
        Rectangle2D bounds;
        font=getFont(fontStyle);
        for(String line: lines)
        {
            GlyphVector glyph = font.createGlyphVector(
                new FontRenderContext(null,
                false, true), line);
            GeneralPath s = new GeneralPath(
                    glyph.getOutline());
            s.transform(AffineTransform.getTranslateInstance(
                    0, vertOffset));
            bounds = s.getBounds2D();
            if(underline)
            {
                double vert=boundsBase.getMaxY()-
                    boundsBase.getHeight()/12;
                Rectangle2D.Double uline=new Rectangle2D.Double(
                        0, vert, 
                        bounds.getWidth(), 
                        boundsBase.getHeight()/12);
                Area area=new Area(s);
                area.add(new Area(uline));
                s=new GeneralPath(area);
            }
            if(justify.x==0)
                s.transform(AffineTransform.getTranslateInstance(
                        -s.getBounds2D().getCenterX(),
                        y));
            if(justify.x==-1)
                s.transform(AffineTransform.getTranslateInstance(
                        -s.getBounds2D().getMinX(),
                        y));
            if(justify.x==1)
                s.transform(AffineTransform.getTranslateInstance(
                        -s.getBounds2D().getMaxX(),
                        y));
            total.add(new Area(s));
            y+=boundsBase.getHeight()+2*MARGIN;
        }
        double maxHeight=lines.length*
            (boundsBase.getHeight()+2*MARGIN);
        total.transform(AffineTransform.getTranslateInstance(
                -total.getBounds2D().getCenterX(), -maxHeight/2));
        total.transform(AffineTransform.getScaleInstance(
                1/(boundsBase.getHeight()+2*MARGIN),
                1/(boundsBase.getHeight()+2*MARGIN)));
        originalSize=Math.max(
                total.getBounds2D().getWidth(),
                total.getBounds2D().getHeight());
        if(originalSize<=0)
            originalSize=1;
        total.transform(AffineTransform.getScaleInstance(
                1/originalSize,
                1/originalSize));
        if (!keepAspect)
        {
            double maxDimension=Math.max(
                    total.getBounds2D().getWidth(),
                    total.getBounds2D().getHeight());
            if(maxDimension==total.getBounds2D().getHeight())
            {
                total.transform(AffineTransform.getScaleInstance(
                        1/originalSize, 1));
            }
            else
            {
                total.transform(AffineTransform.getScaleInstance(
                        1, 1/originalSize));                
            }
        }
        setShape(total);
        double resultingWidth=getWidth();
        double resultingHeight=getHeight();
        double widthDifference=resultingWidth-originalWidth;
        double heightDifference=resultingHeight-originalHeight;
        translate(-justify.x*widthDifference/2, 
                -justify.y*heightDifference/2);
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
        if(this.text!=null && this.text.equals(text) && 
                aspect==keepAspect && this.underline==underline)
            return;
        this.underline=underline;
        this.keepAspect=aspect;
        this.text=text;
        double height=getLineHeight();
        updateText();
        setLineHeight(height);
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
        setText(text, aspect, underline);
    }
    
    

    /**
     * sets the text of the Sprite
     * 
     * @param text
     *            the String for the Sprite to contain
     */
    public void setText(String text)
    {
        setText(text, keepAspect, underline);
    }

    /**gets the rotation of this sprite.  The rotation
     * returned is minus the original base rotation
     * @see fang.Sprite#getRotation()
     */
    public double getRotation()
    {
        double adjusted = super.getRotation() - baseRotation;
        while (adjusted < 0)
            adjusted += Math.PI * 2;
        return super.getRotation();
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
    
    /**
     * gets a tight rectangle which
     * fits around the text.  The rectangle
     * does not have to be aligned along
     * the x-axis and y-axis.
     * @return the rectangle surrounding
     * the shape
     */
    public Shape getRotatedBoundingBox()
    {
        double originalRotation=getRotation();
        setRotation(0);
        Rectangle2D original=getShape().getBounds2D();
        setRotation(originalRotation);
        GeneralPath path=new GeneralPath(original);
        path.transform(AffineTransform.getRotateInstance(originalRotation,
                original.getCenterX(), original.getCenterY()));
        return path;
    }
}
