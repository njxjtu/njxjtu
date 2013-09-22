/*
 * Created on Feb 4, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package fang;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.TexturePaint;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.PixelGrabber;
import java.awt.image.VolatileImage;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;

import java.awt.geom.GeneralPath;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;
import javax.swing.ImageIcon;

import java.util.LinkedList; 

/**This class takes an image and 
 * transforms it into a Sprite.
 * This class contains some very complex
 * operations.  First, because loading and
 * scaling images can be a time consuming
 * operation, all images and scaled images
 * are cached so that the operation needs
 * to only occur once.  However, the possibility
 * exists for the cache to take up all of
 * the available memory.  Therefore, there
 * is also the option for clearing the cache.
 * This is typically done between game levels.
 * All of the other sprites provided in the
 * game engine are dynamically scalled using
 * a transformation on the graphics object,
 * however this transformation is removed
 * before drawing the sprite.  This class also
 * contains more advanced image manipulations
 * such as support for animated gifs, using
 * images to fill shapes with or without
 * repeating the image, flipping the image
 * horizontally or vertically, and others.
 * @author Jam Jenkins 
 */
public class ImageSprite extends Sprite
{        
    /**used for manually setting the animation
     * index in an animation sequence*/
    private int animationIndex=-1;
    
    /**the array of images used for the currently
     * scaled animated gif*/
    private Image[] animatedBuffer;
    /**the original images from an animated gif*/
    private Image[] baseBuffer;
    /**the total duration of the animated gif*/
    private int delay;
    /**the per segment durations of the animated gif*/
    private int[] delaySegments;
    
    /**the shapes of each image*/
    private Shape[] animatedShapes;
    
    private Shape originalShape;
    
    /** the image in its original size */
    private Image image;

    /** the image in its scaled size */
    private Image buffered;

    /** the size of the currently scaled image */
    private double bufferedScale;

    /** the dimensions fo the transformed image */
    private Dimension bufferSize;
    
    /**the shearing values, zero by default*/
    private Point2D.Double shear=new Point2D.Double();

    /**the flipping values which are basically
     * the amount to scale when centered at the
     * origin*/
    private Point2D.Double flip=new Point2D.Double(1, 1);
    
    /**the fill pattern*/
    private TexturePaint fill=null;
    
    /**whether flip has changed since the last
     * drawing of the ImageSprite*/
    private boolean flipchange=false;
    
    /**whether the fill is seamless or not*/
    private boolean seamless=false;
    
    /**the time the animation should start from*/
    private long animationStart;
    
    /**whether the animate gif should repeat the animation*/
    private boolean looping=true;
    
    /**whether the aspect ratio of the image is kept or not*/
    private boolean keepAspect;

    /**
     * images are big and expensive, cache them if you can. This maps the name
     * of an image file to the Image to avoid making multiple copies.
     */
    static HashMap<URL, Image> imageCache = new HashMap<URL, Image>();

    /**
     * this is used to enable removal from the cache given the Image
     */
    static HashMap<Image, URL> reverseMap = new HashMap<Image, URL>();

    /**map to scaled image sequences when using animated gifs*/
    static HashMap<Image, Image[]> animatedMap = new HashMap<Image, Image[]>();
    
    /**map to original base image sequence when using animated gifs*/
    static HashMap<Image, Image[]> baseMap = new HashMap<Image, Image[]>();
    
    /**
     * every time an image is resized, the image is stored so that future uses
     * of images of this size need not be resized again.
     */
    static HashMap<String, Image> resizedCache = new HashMap<String, Image>();

    /**maps the image to the delay for animated gifs*/
    static HashMap<Image, int[]> delayMap = new HashMap<Image, int[]>();
    
    /**the image that comes up when a bad file
     * name is given*/
    private static final URL ERROR=
        ImageSprite.class.getResource("resources/badURL.gif");
    
    
    private double inverse;
    
    private Point2D.Double fillLocation=new Point2D.Double();
    
    private URL fileURL;
    
    /**sets whether to use clipping or not when drawing the image*/
    private boolean useClip=false;
        
    public ImageSprite(String filename)
    {
        this(filename, true);
    }
    
    /**this method is deactivated and done manually
     * when setting the shape within this class*/
    public void normalize(){}
    
    /**Creates a Sprite from an image file. The image file 
     * location is relative to the class constructing the
     * ImageSprite.
     * Like all Sprites, this one
     * will start centered at (0, 0) and have unit 
     * width and height.
     * @param filename the relative name of the image file
     * @param keepAspect whether the image should be resized
     * to fit into a square or not.  True indicates the
     * original aspect of the image should be kept.  False
     * indicates the image should be resized to fit into a
     * square.
     */
    public ImageSprite(String filename, boolean keepAspect)
    {
        super();
        this.keepAspect=keepAspect;
        URL url=getResource(filename);
        if(url==null)
        	System.err.println(filename+"\nis not a valid image file.");
        initialize(url);
    }
    
    /**
     * used internally for making duplicates
     * @param url the image url
     * @param keepAspect true for keep the
     * original aspect, false for make square
     */
    private ImageSprite(URL url, boolean keepAspect)
    {
    	this.keepAspect=keepAspect;
    	initialize(url);
    }
    
    /**
     * gets a duplicate of this ImageSprite.
     * Getting the duplicate does not add the
     * duplicate to the canvas.
     * @return a sprite with the same properties
     * as the original sprite
     */
    public ImageSprite clone()
    {
    	ImageSprite duplicate=new ImageSprite(fileURL, keepAspect);
    	duplicate.setShape(getShape());
    	duplicate.setColor(getColor());
    	duplicate.setScale(getScale());
    	duplicate.setRotation(getRotation());
    	duplicate.setLocation(getLocation());
    	duplicate.setBlurLength(getBlurLength());
    	duplicate.setImageIndex(animationIndex);
    	duplicate.delay=delay;
    	duplicate.setShear(shear.x, shear.y);
    	if(flip.x<0)
    		duplicate.flipAlongVerticalAxis();
    	if(flip.y<0)
    		duplicate.flipAlongHorizontalAxis();
    	duplicate.fill=fill;
    	duplicate.seamless=seamless;
    	duplicate.animationStart=animationStart;
    	duplicate.looping=looping;
    	duplicate.inverse=inverse;
    	duplicate.fillLocation=new Point2D.Double(fillLocation.x, fillLocation.y);
    	return duplicate;
    }
    
    /**
     * gets the file as a URL
     * @param filename the relative filename
     * @return
     */
    public URL getResource(String filename)
    {
        StackTraceElement[] all=Thread.currentThread().getStackTrace();
        int i;
        for(i=0; i<all.length; i++)
            if(all[i].getClassName().equals(getClass().getCanonicalName()))
                break;
        i++;
        while(all[i].getClassName().equals(getClass().getCanonicalName()))
            i++;
        try
        {
            Class c=Class.forName(all[i].getClassName());
            URL url=c.getResource(filename);
            return url;
        } catch (ClassNotFoundException e)
        {
            e.printStackTrace();
            return null;
        }
    }
    /**
     * resets the instance variables to their original
     * value.  This is called before setting the image.
     */
    private void clean()
    {
        animationIndex=-1;
        animatedBuffer=null;
        animatedShapes=null;
        originalShape=null;
        baseBuffer=null;
        delay=0;
        delaySegments=null;
        image=null;
        buffered=null;
        bufferedScale=0.0;
        bufferSize=null;
        fill=null;
        animationStart=0;
        inverse=0;
        fillLocation=new Point2D.Double();
        fileURL=null;
        useClip=false;
    }

    /**resets the image to another image
     * @param filename the new image*/
    public void setImage(String filename)
    {
        URL url=getResource(filename);
        if(url==null)
        	System.err.println(filename+"\nis not a valid image file.");
        initialize(url);
    }

    private void initialize(URL filename)
    {
    	clean();
    	if(!initializeImage(filename))
        {
            filename=ERROR;
            initializeImage(filename);
        }
        fileURL=filename;
        image = imageCache.get(filename);
        buffered=image;
        int width = image.getWidth(null);
        int height = image.getHeight(null);
        double max = Math.max(width, height);
        inverse=10;
        /*if (width > 0 && height > 0 && keepAspect)
            setShape(new Rectangle2D.Double(-width / max / 2,
                    -height / max / 2, width / max, height / max));
        else
            setShape(new Rectangle2D.Double(-0.5, -0.5, 1, 1));*/
        if (keepAspect)
            bufferSize = new Dimension((int) max, (int) max);
        else
            bufferSize = new Dimension(width, height);
        bufferedScale = 0;
        initializeAnimatedBuffer();
        loadAnimatedShapes();
        originalShape=getShape(image);
        super.setShape(originalShape);
    }
    
    /**sets the shape of the image to display.
     * This shape is intersected with the opaque
     * part of the image.
     */
    public void setShape(Shape shape)
    {
    	useClip=true;
    	Area area=new Area(shape);
        Rectangle2D bounds = shape.getBounds2D();
        double max = Math.max(bounds.getWidth(), bounds.getHeight());
        // resizes to be 1x1
        area.transform(AffineTransform.getScaleInstance(1 / max, 1 / max));
        bounds = area.getBounds2D();
        // centers around (0, 0)
        area.transform(AffineTransform.getTranslateInstance(-bounds.getWidth()
                / 2 - bounds.getX(), -bounds.getHeight() / 2 - bounds.getY()));
        originalShape=new Area(area);
        for(int i=0; animatedShapes!=null && i<animatedShapes.length; i++)
        {
        	animatedShapes[i]=new Area(originalShape);
        	((Area)animatedShapes[i]).intersect(new Area(getShape(baseBuffer[i])));
        }
        ((Area)originalShape).intersect(new Area(getShape(image)));
    	super.setShape(originalShape);
    }
    
    /**
     * flips the image along the vertical axis 
     * before drawing it
     */
    public void flipAlongVerticalAxis()
    {
        if(flip.x>0)
            flip.x=-1;
        else
            flip.x=1;
        flipchange=true;
    }

    /**
     * flips the image along the horizontal 
     * axis before drawing it
     */
    public void flipAlongHorizontalAxis()
    {
        if(flip.y>0)
            flip.y*=-1;
        else
            flip.y=1;
        flipchange=true;
    }

    /**
     * sets the shearing which has the effect
     * of stretching the image.  The shearing is
     * done prior to the scaling and rotating.
     * @param x the upper right shearing value
     * @param y the lower left shearing value
     */
    public void setShear(double x, double y)
    {
        shear.x=x;
        shear.y=y;
    }

    /**
     * gets the image from cache if possible, from file otherwise
     * 
     * @param filename
     *            the image file
     */
    private boolean initializeImage(URL filename)
    {
        if (imageCache.containsKey(filename))
        {
            image = imageCache.get(filename);
        } else
        {
            try
            {
                ImageIcon icon = new ImageIcon(filename);
                image = icon.getImage();
            }
            catch(Exception e)
            {
                System.err.println("Null URL given to ImageSprite.\n"+  
                        "Check for spelling and case sensitivity\n"+
                        "in the URL used at the line number indicated\n"+
                        "below at the first line that starts with\n"+
                        "the name of a class that you wrote.");
                e.printStackTrace();
                return false;
            }
            imageCache.put(filename, image);
            reverseMap.put(image, filename);
        }
        return true;
    }
    
    /**this method gets the scaled sequence of
     * images needed for drawing animated gifs.
     * If this method is called and this is not
     * using an animated gif, then animated is
     * set to false.
     */
    private void initializeAnimatedBuffer()
    {
        /**get from cache if possible*/
        if(animatedMap.containsKey(buffered))
        {
            animatedBuffer=animatedMap.get(buffered);
            if(baseMap.containsKey(image))
                baseBuffer=baseMap.get(image);
            else
            {
                baseBuffer=animatedBuffer;
                baseMap.put(image, baseBuffer);
            }
            delaySegments=delayMap.get(image);
            delay=0;
            for(int d: delaySegments)
                delay+=d;
        }
        else
        {
            /**use baseBuffer is already loaded*/
            if (baseBuffer != null)
            {
                delaySegments=delayMap.get(image);
                delay=0;
                for(int d: delaySegments)
                    delay+=d;
                animatedBuffer = new Image[baseBuffer.length];
                for (int i = 0; i < baseBuffer.length; i++)
                {
                    Point size=new Point(buffered.getWidth(null), 
                            buffered.getHeight(null));
                    animatedBuffer[i] = baseBuffer[i].getScaledInstance(
                            size.x, size.y,
                            Image.SCALE_FAST);
                    if(flip.x<0 || flip.y<0)
                    {
                        BufferedImage buff=new BufferedImage(
                                size.x, size.y,
                                BufferedImage.TYPE_INT_ARGB);
                        Graphics2D g=(Graphics2D)buff.getGraphics();
                        g.translate(size.x/2.0, size.y/2.0);
                        g.scale(flip.x, flip.y);
                        g.translate(-size.x/2.0, -size.y/2.0);
                        g.drawImage(animatedBuffer[i], 0, 0, null);
                        animatedBuffer[i] = buff;
                    }
                }
                animatedMap.put(buffered, animatedBuffer);
                return;
            }
            if(baseMap.containsKey(image))
                baseBuffer=baseMap.get(image);
            else/**load from source*/
                loadImagesFromURL();
        }
    }
    
    private void loadAnimatedShapes()
    {
    	if(baseBuffer==null) return;
        animatedShapes=new Shape[baseBuffer.length];
    	for(int i=0; i<animatedShapes.length; i++)
            animatedShapes[i] = getShape(baseBuffer[i]);
    }
    
    /**loads the list of images used in this
     * animated sequence.  If there is only
     * one image, then this is not animated.
     */
    private void loadImagesFromURL()
    {
        try
        {
            URL url = reverseMap.get(image);
            String file = url.getFile();
            String extension = file.substring(file.lastIndexOf('.') + 1);
            Iterator<ImageReader> readers = ImageIO
                    .getImageReadersBySuffix(extension);
            ImageReader reader = readers.next();
            InputStream urlIn=url.openStream();
            ImageInputStream iis = ImageIO.createImageInputStream(urlIn);
            reader.setInput(iis);
            int numImages = reader.getNumImages(true);
            if (numImages <= 1)
            {
            	urlIn.close();
                return;
            }
            animatedBuffer = new Image[numImages];
            baseBuffer = new Image[numImages];
            delaySegments = new int[numImages];
            delay = 0;
            for (int i = 0; i < numImages; ++i)
            {
                baseBuffer[i] = reader.read(i);
                animatedBuffer[i] = baseBuffer[i];
                IIOMetadata meta = reader.getImageMetadata(i);
                if (meta instanceof com.sun.imageio.plugins.gif.GIFImageMetadata)
                {
                    com.sun.imageio.plugins.gif.GIFImageMetadata gifM = (com.sun.imageio.plugins.gif.GIFImageMetadata) meta;
                    delaySegments[i] = gifM.delayTime * 10;
                } 
                else
                    delaySegments[i] = 100;
                delay += delaySegments[i];
            }
            delayMap.put(image, delaySegments);
            animatedMap.put(buffered, animatedBuffer);
            urlIn.close();
        } catch (Exception e)
        {
            e.printStackTrace();
        }

    }
    
    private double getActualScale()
    {
        return getScale()*inverse;
    }
        
    /**
     * get the image from cache if possible, 
     * otherwise, resize and cache the
     * scaled image
     */
    private void updateBufferedImage()
    {
        if (buffered == null || 
                getActualScale() != bufferedScale ||
                flipchange)
        {
            flipchange=false;
            String key = flip.toString()+(int) getActualScale() + fileURL.getFile();
            if (resizedCache.containsKey(key))
            {
                buffered = resizedCache.get(key);
            } else
            {
                Point size=new Point(Math.max(1, (int)(image.getWidth(null)
                        * getActualScale() / bufferSize.width)),
                        Math.max(1, (int)(image.getHeight(null)
                                        * getActualScale() / bufferSize.height)));
                buffered = image.getScaledInstance(
                        size.x, size.y,
                        Image.SCALE_FAST);
                if(flip.x<0 || flip.y<0)
                {
                    BufferedImage buff=new BufferedImage(
                            size.x, size.y,
                            BufferedImage.TYPE_INT_ARGB);
                    Graphics2D g=(Graphics2D)buff.getGraphics();
                    g.translate(size.x/2.0, size.y/2.0);
                    g.scale(flip.x, flip.y);
                    g.translate(-size.x/2.0, -size.y/2.0);
                    while(animatedBuffer==null && !g.drawImage(buffered, 0, 0, null))
                    {
                        synchronized(this)
                        {
                            try
                            {
                                wait(1);
                            }
                            catch(InterruptedException e)
                            {
                                e.printStackTrace();
                            }
                        }
                    }
                    buffered=buff;
                }
                /**make sure buffered is completely loaded*/
                while(buffered.getHeight(null)<size.y ||
                        buffered.getWidth(null)<size.x)
                {
                    synchronized(this)
                    {
                        try
                        {
                            wait(1);
                        }
                        catch(InterruptedException e)
                        {
                            e.printStackTrace();
                        }
                    }
                }
                resizedCache.put(key, buffered);
            }
            bufferedScale = getActualScale();
            if(animatedBuffer!=null)
                initializeAnimatedBuffer();
        }
    }  
    
    /** draws the image */
    public void paint(Graphics2D brush)
    {
        AffineTransform original=brush.getTransform();
        Shape clip=brush.getClip();
        if(inverse!=original.getScaleX())
        {
            if(fill!=null)
            {
                double oldFill=getFill();
                inverse=original.getScaleX();
                if(!seamless)
                    setFill(oldFill);
                else
                    setSeamlessFill(oldFill);
            }
            else
            {
                inverse=original.getScaleX();
                bufferedScale=-1;
            }
        }
        brush.setTransform(new AffineTransform());
        if(useClip)
        {
        	Area area=new Area(getShape());
        	area.transform(AffineTransform.getScaleInstance(inverse, inverse));
        	brush.setClip(area);
        }
        updateBufferedImage();
        Dimension bounds = new Dimension(
                buffered.getWidth(null), 
                buffered.getHeight(null));
        Point2D.Double location = getLocation();
        brush.translate(+location.x*inverse, +location.y*inverse);
        brush.rotate(getRotation());
        brush.shear(shear.x, shear.y);
        brush.translate(-location.x*inverse, -location.y*inverse);
        drawReliably(brush, buffered, 
                (int) Math.round(inverse*location.x - bounds.width / 2.0),
                (int) Math.round(inverse*location.y - bounds.height / 2.0));
        
        brush.setTransform(original);
        brush.setClip(clip);
    }
    
    /**
     * sets the index of the image which
     * should be displayed.  Setting this
     * value to a negative number means
     * the index should follow the animation
     * sequence.  The index must be less than
     * the number of frames or an exception
     * will be thrown.
     * @param index the image to display
     */
    public void setImageIndex(int index)
    {
        animationIndex=index;
        if(animatedShapes!=null && index<animatedShapes.length)
        	super.setShape(animatedShapes[index]);
    }
    
    /**determines if the animated gif loops
     * back to the beginning once it reaches
     * the end
     * @return true if this is an animated gif
     * and loops, false otherwise.  The return
     * value is always false for non-animated
     * gifs.
     */
    public boolean isLooping()
    {
        return looping && this.baseBuffer!=null;
    }
    
    /**
     * sets whether this animated image
     * should loop or not.  Calling this
     * method on non-animated images has
     * no effect and causes no error.
     * @param looping true indicates the
     * animation should begin again after
     * reaching the end, false means it
     * should stay at the last frame once
     * the animation ends.
     */
    public void setLooping(boolean looping)
    {
        this.looping=looping;
    }
    
    
    
    /**gets the index of the sequence of
     * images that should be displayed at
     * this time
     * @return the index to display
     */
    public int getImageIndex()
    {
        if(animationIndex>=0)
            return animationIndex;
        long time=System.currentTimeMillis()-animationStart;
        if(!looping && time>delay)
            return delaySegments.length-1;
        time%=delay;
        for(int i=0; i<delaySegments.length; i++)
        {
            if(delaySegments[i]>time)
            {
            	super.setShape(animatedShapes[i]);
                return i;
            }
            time-=delaySegments[i];
        }
        int index=delaySegments.length-1;
        if(animatedShapes!=null && index<animatedShapes.length)
        	super.setShape(animatedShapes[index]);
        return index;
    }

    /**sets now as the start time for the
     * animation
     */
    public void startAnimationNow()
    {
        animationStart=System.currentTimeMillis();
    }
    
    private void fillToShape(boolean seamless)
    {
        double oldRotation=getRotation();
        setRotation(0);
        Rectangle2D shapeBounds=getBounds2D();
        double imageWidth=image.getWidth(null);
        double imageHeight=image.getHeight(null);
        double shapeAspect=shapeBounds.getWidth()/shapeBounds.getHeight();
        double imageAspect=imageWidth/imageHeight;
        if(shapeAspect>1)
        {
            shapeAspect=1.0/shapeAspect;
            if(imageAspect<1)
                shapeAspect/=imageAspect;
        }
        else
        {
            if(imageAspect>1)
                shapeAspect*=imageAspect;
        }
        if(seamless)
            setSeamlessFill(2*shapeAspect);
        else
            setFill(shapeAspect);
        setFillPosition(new Point2D.Double(
                shapeBounds.getMinX(),
                shapeBounds.getMinY()));
        setRotation(oldRotation);        
    }
    
    public void fillToShapeSeamless()
    {
        fillToShape(true);
    }
    
    public void fillToShape()
    {
        fillToShape(false);
    }
    
    /**
     * instead of drawing the image normal
     * size, this method tiles the image
     * at the given scale to fill the shape
     * of this ImageSprite.  The shape of
     * ImageSprites by default is rectangular.
     * A scale of less than 1 reverts back
     * to drawing the image at normal size
     * instead of tiling.
     * @param scale the scale to tile the
     * image at, less than 1 means no tiling
     */
    public void setFill(double scale)
    {
        //what does the scale mean?
        if(scale==0)
        {
            fill=null;
            return;
        }
        seamless=false;
        double oldScale=getScale();
        //System.out.println("oldScale is "+oldScale);
        scale(scale);
        updateBufferedImage();
        Rectangle2D.Double box=new Rectangle2D.Double(
                0, 0,
                buffered.getWidth(null),
                buffered.getHeight(null));
        //System.out.println("box is "+box);
        //System.out.println("inverse is "+inverse);
        BufferedImage buff=new BufferedImage(
                (int)(box.getWidth()),
                (int)(box.getHeight()),
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D brush=(Graphics2D)buff.getGraphics();
        fill=null;
        if(animatedBuffer!=null)
            drawReliably(brush, animatedBuffer[getImageIndex()], 
                    0, 0);
        else
            drawReliably(brush, buffered, 0, 0);
        setScale(oldScale);
        updateBufferedImage();
        fill=new TexturePaint(buff, box);
        setFillPosition(new Point2D.Double());
    }
    
    /**gets the position at which to start tiling.
     * (0, 0) is the default position.
     * @return the position for tiling.
     */
    public Point2D.Double getFillPosition()
    {
        if(fill==null)
            return null;
        return fillLocation;
    }
    
    /**sets the position at which to start filling.
     * @param upperLeft the upper left of where to
     * fill.  (0, 0) is the default.
     */
    public void setFillPosition(Point2D.Double upperLeft)
    {
        if(fill==null)
            return;
        Rectangle2D anchor=fill.getAnchorRect();
        anchor=new Rectangle2D.Double(
                Math.round((getBounds2D().getMinX()+upperLeft.x)*inverse), 
                Math.round((getBounds2D().getMinY()+upperLeft.y)*inverse),
                anchor.getWidth(), 
                anchor.getHeight());
        //System.out.println("Anchor is "+anchor);
        fill=new TexturePaint(fill.getImage(), anchor);
    }
    
    /**
     * instead of drawing the image normal
     * size, this method tiles the image
     * at the given scale to fill the shape
     * of this ImageSprite.  Each tile in this
     * fill represents the mirror image of the
     * adjacent tiles so that the overall
     * effect is seamless.  The shape of
     * ImageSprites by default is rectangular.
     * A scale of less than 1 reverts back
     * to drawing the image at normal size
     * instead of tiling.
     * @param scale the scale to tile the
     * image at, less than 1 means no tiling
     */
    public void setSeamlessFill(double scale)
    {
        if(scale==0)
        {
            fill=null;
            return;
        }
        seamless=true;
        double oldScale=getScale();
        scale(scale*0.5);
        updateBufferedImage();
        Rectangle2D.Double box=new Rectangle2D.Double(
                0, 0,
                buffered.getWidth(null)*2,
                buffered.getHeight(null)*2);
        BufferedImage buff=new BufferedImage(
                (int)(box.getWidth()),
                (int)(box.getHeight()),
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D brush=(Graphics2D)buff.getGraphics();
        fill=null;
        if(animatedBuffer!=null)
        {
            drawReliably(brush, animatedBuffer[getImageIndex()], 
                    0, 0);
            flipAlongHorizontalAxis();
            updateBufferedImage();
            drawReliably(brush, animatedBuffer[getImageIndex()],
                    0, box.height/2.0);
            flipAlongVerticalAxis();
            updateBufferedImage();
            drawReliably(brush, animatedBuffer[getImageIndex()],
                    box.width/2.0, box.height/2.0);
            flipAlongHorizontalAxis();
            updateBufferedImage();
            drawReliably(brush, animatedBuffer[getImageIndex()],
                    box.width/2.0, 0); 
            flipAlongVerticalAxis();
        }
        else
        {
            drawReliably(brush, buffered, 
                    0, 0);
            flipAlongHorizontalAxis();
            updateBufferedImage();
            drawReliably(brush, buffered,
                    0, box.height/2.0);
            flipAlongVerticalAxis();
            updateBufferedImage();
            drawReliably(brush, buffered,
                    box.width/2.0, box.height/2.0);
            flipAlongHorizontalAxis();
            updateBufferedImage();
            drawReliably(brush, buffered,
                    box.width/2.0, 0);
            flipAlongVerticalAxis();
        }
        setScale(oldScale);
        updateBufferedImage();
        fill=new TexturePaint(buff, box);     
        setFillPosition(new Point2D.Double());
    }
    
    /**
     * gets the size of the tiling for
     * the image, or -1 if the image is
     * not tiled
     * @return the scale of the tiling,
     * or -1 if the image is not tiled
     */
    public double getFill()
    {
        if(fill==null)
            return -1;
        double factor=1;
        //if(seamless)
        //    factor=2;
        return factor*Math.min(
                fill.getImage().getWidth()/(double)buffered.getWidth(null),
                fill.getImage().getHeight()/(double)buffered.getHeight(null));
    }
    
    /**if an image is not ready to be drawn,
     * the drawImage method of graphics returns
     * immediately false.  Since the image must
     * be drawn reliably, this method tries every
     * 1 millisecond until the image is fully
     * loaded and ready to draw.  Animated GIFs
     * always return false, so they are just
     * drawn once.
     * @param brush the Graphics2D used to draw the image
     * @param image the image to be drawn
     * @param x the horizontal position to draw
     * @param y the vertical position to draw
     */
    private void drawReliably(Graphics2D brush, Image image,
            double x, double y)
    {
        Paint paint=brush.getPaint();
        if(animatedBuffer!=null)
        {
            if(fill!=null)
            {
                int max=Math.max(
                        fill.getImage().getWidth(), 
                        fill.getImage().getHeight());
                if(seamless)
                    setSeamlessFill(max/2);
                else
                    setFill(max);
                brush.setPaint(fill);
                Area area=new Area(getShape());
                area.transform(AffineTransform.getScaleInstance(inverse, inverse));
                brush.fill(area);
                brush.setPaint(paint);
            }
            else
            {
                brush.drawImage(animatedBuffer[getImageIndex()],
                        (int) x, (int) y, null);                
            }
            return;
        }
        if(fill!=null)
        {
            brush.setPaint(fill);
            Area area=new Area(getShape());
            area.transform(AffineTransform.getScaleInstance(inverse, inverse));
            brush.fill(area);
            return;
        }
        while(!brush.drawImage(buffered, 
                (int)Math.round(x), 
                (int)Math.round(y), null) &&
                !(image instanceof VolatileImage))
        {
            synchronized(this)
            {
                try
                {
                    wait(1);
                }
                catch(InterruptedException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }
    
    public static void clearCache()
    {
        imageCache.clear();
        reverseMap.clear();
        animatedMap.clear();
        baseMap.clear();
        resizedCache.clear();
        delayMap.clear();
    }
    
    /**
     * @author Jam Jenkins, Alex Kuhl
     * @param image
     * @return GeneralPath composed of the image with transparency removed
     */
	public static Shape getShape(Image image)
	{
		int h=image.getHeight(null);
		int w=image.getWidth(null);
		int[] pixels=new int[w*h];
		PixelGrabber grabber=new PixelGrabber(image, 0, 0, 
				w, h,
				pixels,
				0, w);
		try
		{
			grabber.grabPixels();
		} catch (InterruptedException e)
		{
			e.printStackTrace();
		}

		// stores the intersection points of the scanlines with edges
		// stored in a [y,x] manner since algorithm goes row by row
        LinkedList< LinkedList< Integer > > ll = new LinkedList< LinkedList< Integer > >( ) ;
        
        // simple scanline algorithm to find edges between transparent and non-transparent areas
		for (int row = 0; row < h; ++row ) 
		{
			// set up new list for current row
			ll.add( new LinkedList< Integer >( ) ) ;
			
			// initialize lastAlpha to first pixel in row
			int lastAlpha = 0 ;// pixels[ row * w ] >> 24 & 0xff ;
			
			// loop over columns
            for( int col = 0; col < w; ++col ) 
            {
            	int alpha = (pixels[row * w + col] >> 24) & 0xff;
;
            	// at an edge? 
            	if( ( alpha != 0 && lastAlpha == 0 ) ||
            		  ( alpha == 0 && lastAlpha != 0 ) )
            		ll.get( row ).addLast( col ) ;
            	
            	lastAlpha = alpha ;
            }

            // if there are an odd number of points for this scanline add 
            // the end of the image to close it off
            // this means the sprite overlaps the right side
            if( ll.get( row ).size( ) % 2 == 1 )
            	ll.get( row ).addLast( w - 1 ) ;
        }
		
		int x1, x2 ;
		LinkedList< Integer > temp ;
		Rectangle r = new Rectangle( 0, 0, 1, 1 ) ;
		GeneralPath total = new GeneralPath( ) ;
		for( int i = 0 ; i < ll.size() ; ++i )
		{
			temp = ll.get( i ) ;
			while( !temp.isEmpty() )
			{
				x1 = temp.removeFirst( ) ;
				x2 = temp.removeFirst( ) ;
	
				r.x = x1 ;
				r.y = i ;
				r.height = 1 ;
				r.width = x2 - x1 + 1 ;
				total.append( r, false ) ;
			}
		}		
		
		double scale=1.0/Math.max(w, h);
		total.transform(AffineTransform.getScaleInstance(scale, scale));
		total.transform(AffineTransform.getTranslateInstance(-w*scale/2.0, -h*scale/2.0));

		return total;
	}
}
