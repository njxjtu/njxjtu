package fang;

import static fang.ErrorConsole.*;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.TreeMap;

import javax.swing.JComponent;

/**
 * This class is a JPanel that contains Sprites.<br><br>
 * The license below must remain in the software
 * unaltered.  The watermark must remain unaltered
 * and visible in all games created using this
 * software.  Removing the watermark or license
 * shown below is a violation of federal copyright
 * law that can be subject to monetary penalties.
 * <!-- Creative Commons License -->
<a rel="license" href="http://creativecommons.org/licenses/by-sa/2.5/"><img alt="Creative Commons License" border="0" src="http://creativecommons.org/images/public/somerights20.gif" /></a><br />
This work is licensed under a <a rel="license" href="http://creativecommons.org/licenses/by-sa/2.5/">Creative Commons Attribution-ShareAlike 2.5 License</a>.
<!-- /Creative Commons License -->


<!--

<rdf:RDF xmlns="http://web.resource.org/cc/"
    xmlns:dc="http://purl.org/dc/elements/1.1/"
    xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#">
<Work rdf:about="">
   <license rdf:resource="http://creativecommons.org/licenses/by-sa/2.5/" />
</Work>

<License rdf:about="http://creativecommons.org/licenses/by-sa/2.5/">
   <permits rdf:resource="http://web.resource.org/cc/Reproduction" />
   <permits rdf:resource="http://web.resource.org/cc/Distribution" />
   <requires rdf:resource="http://web.resource.org/cc/Notice" />
   <requires rdf:resource="http://web.resource.org/cc/Attribution" />
   <permits rdf:resource="http://web.resource.org/cc/DerivativeWorks" />
   <requires rdf:resource="http://web.resource.org/cc/ShareAlike" />
</License>

</rdf:RDF>

-->

 * 
 * @author Jam Jenkins
 */

public class AnimationCanvas 
    extends JComponent
{
    /**
     * used for serialization versioning
     */
    private static final long serialVersionUID = 1L;
    /**
     * the ordered collection of Sprites. The last added Sprite will appear on
     * top.
     */
    private TreeMap<Double, LinkedHashSet<Sprite>> sprites;
    
    /**reverse map for sprites*/
    private HashMap<Sprite, Double> reverseSprites;
    
    /** used for the transparent text at the bottom right*/
    private StringSprite[] waterMark;
    /** the color to use when clearing the background */
    private static final Color DEFAULT_BACKGROUND = Color.BLACK;
    /** default size of canvas */
	private static final Dimension DEFAULT_SIZE=new Dimension(400, 400);
    
    /** width/height aspect ratio */
    private double aspect=1;
    
    /** constructs an empty canvas with the default size*/
    public AnimationCanvas()
    {
        this(DEFAULT_SIZE);
    }

    /** constructs an empty canvas with a give size 
     * @param size the width and height to make the
     * AnimationCanvas.  This width and height specify
     * the target aspect ratio and preferred size.
     * If the size of the AnimationCanvas expands or
     * shrinks, it will maintain the original aspect
     * ratio of width to height.*/
    public AnimationCanvas(Dimension size)
    {
        super();
        aspect=size.getWidth()/size.getHeight();
        setSize(size);
        sprites = new TreeMap<Double, LinkedHashSet<Sprite>>();
        reverseSprites = new HashMap<Sprite, Double>();
        setFocusable(true);
        setOpaque(true);
        setBackground(DEFAULT_BACKGROUND);
        setIgnoreRepaint(true);
        makeWaterMark();
    }
    
    public void restoreCursor()
    {
    	this.setCursor(Cursor.getDefaultCursor());
    }
    
    /**removes the cursor from the game canvas*/
    public void removeCursor()
    {
        Toolkit toolkit=getToolkit();
        Image image=toolkit.createImage(new byte[0]);
        setCursor(getToolkit().createCustomCursor(image, new Point(), "none"));
    }
    
    /**sets the cursor for the game engine
     * @param url the image to display
     * as the cursor */
    public void setCursor(URL url)
    {
        Toolkit toolkit=getToolkit();
        Image image=toolkit.createImage(url);
        setCursor(getToolkit().createCustomCursor(image, new Point(), "none"));        
    }
    
    /**
     * gets the preferred size as the current size
     * @return the current size
     */
    public Dimension getPreferredSize()
    {
        return getSize();
    }

    /**
     * makes the semi-transparent string sprites
     * appearing at the lower right of the canvas.
     * This watermark must not be removed or
     * altered.  See the class comment concerning
     * copyright protection.
     */
    private void makeWaterMark()
    {
        double size=0.0375;
        Point2D.Double bottomRight=new Point2D.Double(1, 1);
        if(aspect>1) bottomRight.x=aspect;
        else bottomRight.y=1.0/aspect;
        waterMark=new StringSprite[2];
        waterMark[0]=new StringSprite("FANG Engine by Jam", true);//new TriangleSprite();
        waterMark[0].rightJustify();
        waterMark[0].bottomJustify();
        waterMark[0].setLineHeight(size);
        waterMark[0].setLocation(
                bottomRight.x-4.0/getWidth(),
                bottomRight.y-4.0/getHeight());
        waterMark[0].setColor(new Color(1.0f, 1.0f, 1.0f, 0.25f));
        waterMark[1]=new StringSprite("FANG Engine by Jam", true);//new TriangleSprite();
        waterMark[1].rightJustify();
        waterMark[1].bottomJustify();
        waterMark[1].setLineHeight(size);
        waterMark[1].setLocation(
                bottomRight.x-2.0/getWidth(),
                bottomRight.y-2.0/getHeight());
        waterMark[1].setColor(new Color(0.0f, 0.0f, 0.0f, 0.25f));
    }
    
    
    /** redraws the area which contains the Sprites */
    public void paintImmediately()
    {
        paintImmediately(0, 0, getSize().width,
                getSize().height);
    }
	
	/** 
	 * returns width of the canvas on which
     * drawing can occur.  The canvas size
     * is clipped to maintain the initial
     * aspect ratio. 
	 * @return width in pixels
	 */
    public int getWidth()
    {
        if(((double)getSize().width)/getSize().height>aspect)
        {
            return (int)(aspect*getSize().height);
        }
        return getSize().width;
    }
    
    /**
     * gets the maximum x value which
     * can be displayed on the canvas.
     * When the canvas is square, this
     * returns 1.  When the canvas is
     * more long than it is wide, the
     * return value will be the width/height,
     * and when the width is less than
     * the height, the return value is 1.
     * @return the maximum displayable x value
     */
    public double getMaxX()
    {
    	if(aspect>1)
    		return aspect;
    	return 1;
    }
    
    /**
     * gets the maximum y value which can
     * be displayed on the canvas.  If the
     * canvas is square, the return value
     * is 1.  If the canvas is taller than
     * it is wide, the return value is
     * height/width.  If the canvas is wider
     * than it is tall, the return value is 1.
     * @return the maximum displayable y value
     */
    public double getMaxY()
    {
    	if(aspect<1)
    		return 1/aspect;
    	return 1;
    }
    
    /** 
     * returns height of the canvas on which
     * drawing can occur.  The canvas size
     * is clipped to maintain the initial
     * aspect ratio.  
     * @return height in pixels
     */
    public int getHeight()
    {
        if(((double)getSize().width)/getSize().height>aspect)
            return getSize().height;
        return (int)(getSize().width/aspect);
    }
    
    /**
     * adds a Sprite to the canvas. The last added 
     * Sprite appears on top.
     * 
     * @param sprite
     *            the Sprite to be added
     */
    public void addSprite(Sprite ... sprite)
    {
    	removeSprite(sprite);
    	double layer=0;
        if(sprites.size()>0)
        	layer=sprites.lastKey()+1;
        addSprite(layer, sprite);
    }
    
    /**
     * reports to the error console when a null sprite is added
     * to the canvas
     * @param fileName the file where the error occurred
     * @param lineNumber the line where the error occurred
     * @param methodName the name of the method where the error occurred
     * @param parameterNumber the name of the parameter that was null
     */
    private void reportNullSprite(String fileName, int lineNumber,
    		String methodName, int parameterNumber)
    {
    	String line=getLine(fileName, lineNumber);
    	String fix="";
    	String diagnosis=
    		"You are trying to add a null Sprite to the canvas on line "+
    		lineNumber+" of the file<br>"+
    		indent(fixedWidth(fileName))+
			"This line is<br>"+fixedWidth(fixHTML(line))+"<br>";
			String parameters="";
			line=line.substring(line.indexOf("addSprite")+9);
			line=line.substring(line.indexOf('(')+1);
			String remaining=line;
			int parenthesisLeft=1;
			int i=0;
			while(parenthesisLeft>0)
			{
				if(remaining.charAt(i)=='(')
					parenthesisLeft++;
				else if(remaining.charAt(i)==')')
					parenthesisLeft--;
				i++;
			}
			parameters+=remaining.substring(0, i-1);
			int parenthesis=0;
			int pNumber=0;
			String nullParameter=null;
			int start=0;
			for(i=0; i<parameters.length(); i++)
			{
				if(parameters.charAt(i)=='(')
					parenthesis++;
				else if(parameters.charAt(i)==')')
					parenthesis--;
				if(parenthesis==0 && 
						(i==parameters.length()-1 || parameters.charAt(i)==','))
				{
					if(pNumber==parameterNumber)
					{
						if(i==parameters.length()-1) i++;
						nullParameter=parameters.substring(start, i);
						break;
					}
					pNumber++;
					start=i+1;
				}
			}
			if(nullParameter.indexOf('(')<0)
			{
				diagnosis+="The variable "+fixedWidth(nullParameter)+
						" is null. "+
						"The most likely cause is that "+fixedWidth(nullParameter)+
						" has not been initialized.";
				fix+=
						"In the file "+fixedWidth(fileName)+
						" look for a line that starts with<br>"+indent(fixedWidth(nullParameter+" = "))+
						"If you cannot find this line, then this is your problem. " +
						"You need a line that starts with<br>"+
						indent(fixedWidth(nullParameter+" = "))+
						"Typically, this initialization statement " +
						"should be in the method "
						+fixedWidth(methodName)+", "+
						"in the "+fixedWidth("makeSprites")+" method, or in a " +
						"method called from "+
						fixedWidth("makeSprites")+", "+
						fixedWidth("startGame")+", "+"or "+
						fixedWidth("startLevel")+".  " +
						"If you have a "+fixedWidth("makeSprites")+" method "+
						"be sure to call it from the "+
						fixedWidth("startGame")+" or " +
						fixedWidth("startLevel")+" method.";
			}
			else
			{
				diagnosis+=
					"The method "+
					fixedWidth(fixHTML(nullParameter.trim()))+" is returning a null value.";
				fix+=
						"Check the method " +
						fixedWidth(fixHTML(nullParameter.trim()))+
						" to make sure a valid sprite is " +
						"being returned in all cases since null sprites " +
						"cannot be added to the canvas.";
			}
			addError(diagnosis, fix, new NullPointerException());
    }
    
    /**
     * adds a Sprite to the canvas. The sprite is
     * added at the given layer.  The highest layer
     * is on top.
     * 
     * @param layer the order in which to draw
     * the added sprite.  The highest layer is on top
     * @param sprite
     *            the Sprites to be added
     */
    public void addSprite(double layer, Sprite ... sprite)
    {
    	LinkedHashSet<Sprite> element;
    	if(sprites.containsKey(layer))
    		element=sprites.get(layer);
    	else
    		element=new LinkedHashSet<Sprite>();
    	int count=0;
    	for(Sprite s: sprite)
    	{
    		if(s==null)
    		{
    	        reportNullSprite(
    	        		getErrorFile(), 
    	        		getErrorLineNumber(),
    	        		getErrorMethod(), count);
    			throw new NullPointerException("Cannot add null Sprite to AnimationCanvas");
    		}
    		else
    		{
    			element.add(s);
    	    	reverseSprites.put(s, layer);
    		}
    		count++;
    	}
    	sprites.put(layer, element);
    }
    
    /**
     * gets the layer on which a sprite is drawn.
     * The highest layer is on top.
     * @param sprite the sprite which to find the layer of.
     * @return
     */
    public double getLayer(Sprite sprite)
    {
    	if(reverseSprites.containsKey(sprite))
    		return reverseSprites.get(sprite);
    	return Double.NaN;
    }
    
    /**
     * gets the Sprites in the given layer
     * @param layer the position in which
     * to add the sprite.  The highest layer is on top. 
     * @return the sprites in the layer,
     * which could possibly be a zero length array
     * if there is no such layer
     */
    public Sprite[] getLayer(double layer)
    {
    	if(!sprites.containsKey(layer))
    		return new Sprite[0];
    	return sprites.get(layer).toArray(new Sprite[0]);
    }
    
    /**
     * gives each sprite a different layer
     * where the layers start at 0 and go
     * up by one for each consecutive sprite
     */
    public void flattenLayers()
    {
    	Sprite[] all=getAllSprites();
    	sprites.clear();
    	for(int i=0; i<all.length; i++)
    	{
    		LinkedHashSet<Sprite> element=
    			new LinkedHashSet<Sprite>();
    		element.add(all[i]);
    		sprites.put((double)i, element);
    	}
    }

    /**
     * removes the Sprite from the canvas.
     * Removing a non-existent sprite has
     * no effect and does not cause an exception.
     * @param sprite
     *            the Sprite to be removed
     */
    public void removeSprite(Sprite ... sprite)
    {
    	if(sprite==null)
    		return;
    	for(Sprite s: sprite)
    	{
    		if(s==null || !reverseSprites.containsKey(s))
    			continue;
    		double key=reverseSprites.get(s);
    		LinkedHashSet<Sprite> set=sprites.get(key);
    		set.remove(s);
    		if(set.size()==0)
    			sprites.remove(key);
    		reverseSprites.remove(s);
    	}
    }

    /** clears the canvas of all Sprites */
    public void removeAllSprites()
    {
        sprites.clear();
        reverseSprites.clear();
    }

    /**
     * determines if the Sprite exists on the 
     * AnimationCanvas.  This method does not
     * indicate whether the Sprite in question
     * is visible or enabled, just whether it
     * is currently on the AnimationCanvas.
     * @param sprite
     *            the Sprite to check
     * @return true if th sprite does exist in 
     * the collection, false otherwise
     */
    public boolean containsSprite(Sprite sprite)
    {
        return reverseSprites.containsKey(sprite);
    }

    /**
     * gets a copy of the Sprite collection
     * 
     * @return the copy of the Sprites collection in array form
     */
    public Sprite[] getAllSprites()
    {
    	Sprite[] all=new Sprite[reverseSprites.size()];
    	int index=0;
    	for(LinkedHashSet<Sprite> group: sprites.values())
    	{
    		for(Sprite s: group)
    		{
    			all[index]=s;
    			index++;
    		}
    	}
        return all;
    }

    /**
     * updates the cached array and removes all destoyed Sprites
     */
    public void updateSprites(double timeInterval)
    {
    	LinkedList<Double> keysToRemove=
    		new LinkedList<Double>();
        LinkedList<Sprite> toRemove = new LinkedList<Sprite>();
        double key;
    	for(LinkedHashSet<Sprite> group: sprites.values())
    	{
    		toRemove.clear();
    		key=Double.NaN;
    		for(Sprite sprite: group)
    		{
    			if (sprite.isDestroyed())
    			{
    				toRemove.add(sprite);
    				key=reverseSprites.get(sprite);
    				reverseSprites.remove(sprite);
    			}
    		}
    		group.removeAll(toRemove);
    		if(group.size()==0)
    			keysToRemove.add(key);
        }
    	for(Double keys: keysToRemove)
    		sprites.remove(keys);
        HashSet<Tracker> toUpdate=new HashSet<Tracker>();
        for(LinkedHashSet<Sprite> group: sprites.values())
    	{
    		for(Sprite sprite: group)
    		{
    			if(sprite.isEnabled() && sprite.getTracker()!=null)
    			{
    				if(sprite.getTracker() instanceof CompositeTracker)
    					toUpdate.addAll((getAllTrackers((CompositeTracker)sprite.getTracker())));
    				else
    					toUpdate.add(sprite.getTracker());
    			}
    		}
        }
        for(Tracker tracker: toUpdate)
            tracker.advanceTime(timeInterval);
        for(LinkedHashSet<Sprite> group: sprites.values())
    	{
    		for(Sprite sprite: group)
    		{
    			sprite.update();
    		}
    	}
    }
    
    /**recursively gets all the base trackers 
     * within the composite
     * @param composite the container for other trackers
     * @return all the non-composite trackers contained
     * in the composite tracker
     */
    private HashSet<Tracker> getAllTrackers(CompositeTracker composite)
    {
        HashSet<Tracker> all=new HashSet<Tracker>();
        for(Tracker tracker: composite.getAllTrackers())
        {
            if(tracker instanceof CompositeTracker)
                all.addAll(getAllTrackers((CompositeTracker)tracker));
            else
                all.add(tracker);
        }
        return all;
    }

    /**
     * paints all of the Sprites
     * 
     * @param brush
     *            the Graphics of the component
     */
    protected void paintComponent(Graphics brush)
    {
        Graphics2D copy = (Graphics2D) brush;
        double scalingFactor;
        Point2D.Double clip;
        //set the clip to the maximum size that fits within
        //the aspect ratio
        if(((double)getSize().width)/getSize().height>aspect)
        {
            clip=new Point2D.Double(
                    aspect*getSize().height, 
                    getSize().height);
        }
        else
        {
            clip=new Point2D.Double(
                    getSize().width, 
                    getSize().width/aspect);
        }
        scalingFactor=Math.min(clip.x, clip.y);
        copy.transform(AffineTransform.getScaleInstance(
                    scalingFactor, scalingFactor));
        copy.clip(new Rectangle(0, 0,
                (int)clip.x, (int)clip.y));
        clearBackground(copy);
        paintSprites(copy);
    }

    /**
     * paints a rectangle in the default background color
     * 
     * @param brush
     *            the Graphics of the component
     */
    private void clearBackground(Graphics2D brush)
    {
        RenderingHints hints = new RenderingHints(null);
        hints.put(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_SPEED);
        hints.put(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        brush.addRenderingHints(hints);
        brush.setBackground(getBackground());
        brush.clearRect(0, 0, 1000, 1000);
    }

    /**
     * paints all of the Sprites
     * 
     * @param brush
     *            the Graphics of the component
     */
    private void paintSprites(Graphics2D brush)
    {
        for(LinkedHashSet<Sprite> group: sprites.values())
    	{
    		for(Sprite sprite: group)
    		{
    			sprite.paintInternal(brush);
    		}
    	}
        waterMark[0].paintInternal(brush);
        waterMark[1].paintInternal(brush);
    }

    /**
     * adds sprite to the bottom of the canvas.
     * This method can be useful for adding and/or
     * changing the background.
     * @param sprite the sprite to add at the bottom
     */
	public void addBottom(Sprite ... sprite)
    {
        if(sprite==null) 
            throw new NullPointerException("Cannot add null Sprite to AnimationCanvas");
        double key=0;
        if(sprites.size()>0)
        	key=sprites.firstKey()-1;
        addSprite(key, sprite);
    }	
    
    /**gets the aspect ratio of width to height
     * @return the aspect ratio width/height
     */
    public double getAspect()
    {
        return aspect;
    }
}
