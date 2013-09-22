package fang;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Transparency;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.VolatileImage;

/**
 * This class provides a structure for 
 * representing sprites which have size,
 * shape, orientation, and color. These 
 * sprites will appear when add to the
 * AnimationCanvas. Sprites also optionally 
 * have a tracker which will update the
 * Sprite's properties at each time interval.
 * 
 * @author Jam Jenkins
 */
public class Sprite
{
    /** the color to use when clearing the background */
    private static Color defaultColor= new Color(100, 100, 255);

    /** shape should initially be centered at (0, 0) */
    protected GeneralPath shape;

    /** bitmap of the shape*/
    protected VolatileImage image;
    
    /**device for creating VolatileImages*/
    protected GraphicsConfiguration config;
    
    /**whether to draw the shape directly or
     * to draw to an image first then draw
     * the image*/
    protected boolean optimizeForBitmap=false;

    /** applied to the shape prior to drawing it */
    protected AffineTransform transform;

    /** the fill color of the shape, black by default */
    protected Color color;

    /** determines the generic movement of the sprite */
    protected Tracker tracker;

    /** use bounding box for bounce geometry */
    private boolean useBoundingBox = false;

    /** whether the sprite should be displayed and tracked */
    private boolean enabled = true;

    /**
     * true indicates the Sprite will be removed 
     * from the AnimationCanvas
     */
    private boolean destroy = false;

    /**
     * whether the sprite should be displayed. 
     * Note: a Sprite's visibility has
     * no effect its tracking.
     */
    private boolean visible = true;

    /**how many sprites to draw in the direction of the
     * sprite's previous location.  By default this is zero.*/
    private int pathLength=0;
    
    /**the scale when this sprite was last drawn.  This value
     * is used in conjunction with the pathLength in order
     * to draw the sprite along it's previous path.*/
    private double oldScale;
    
    /**the location when this sprite was last drawn.  This value
     * is used in conjunction with the pathLength in order
     * to draw the sprite along it's previous path.*/    
    private Point2D.Double oldLocation;

    
    /**
     * initialize to an empty shape and the 
     * default values for scale, rotation,
     * an rotation. The Sprite must be centered 
     * around (0, 0) and width and height 1.
     */
    public Sprite()
    {
        transform = new AffineTransform();
        color = defaultColor;
        shape = new GeneralPath();
    }

    /**
     * initializes to a given shape and the 
     * default values for scale and rotation.
     * The Sprite must be centered around 
     * (0, 0) and width and height 1.
     * @param shape the original shape of
     * this sprite.  Note: the size and location
     * of the given shape are not stored within
     * the Sprite.  Instead they can be set once
     * the Sprite has been constructed.
     */
    public Sprite(Shape shape)
    {
        transform = new AffineTransform();
        color = defaultColor;
        this.shape = new GeneralPath();
        setShape(shape);
    }

    /** destroys the Sprite by the next
     * advanceFrame call */
    public void kill()
    {
        destroy = true;
        enabled = false;
        visible = false;
    }

    /**
     * determines if the sprite should be 
     * removed from the AnimationCanvas
     * @return true if Sprite should be removed, false otherwise
     */
    protected boolean isDestroyed()
    {
        return destroy;
    }

    /**
     * Sets the parameter which determines if 
     * the actual shape or its minimal
     * bounding box is used in tests for 
     * intersection. Using exact intersection 
     * is the default.
     * 
     * @param box
     *            true indicates a bounding box should be used, 
     *            false indicates
     *            the actual shape should be used
     */
    public void setUseBoundingBox(boolean box)
    {
        useBoundingBox = box;
    }

    /**determines whether the bounding box is used
     * for intersections of the exact shape is used
     * @return true if a bounding box is being 
     *          used in tests for intersection,
     *         false if the actual shape is being used
     */
    public boolean getUseBoundingBox()
    {
        return useBoundingBox;
    }

    /**
     * sets shape to shape s.  Calling this
     * method has no effect on the scale,
     * location, or orientation of the sprite
     * @param s the new shape of the sprite
     */
    public void setShape(Shape s)
    {
        shape.reset();
        shape.append(s, true);
        normalize();
    }

    /** resizes to be 1 by 1 and centers around origin (0, 0) */
    protected void normalize()
    {
        Rectangle2D bounds = shape.getBounds2D();
        double max = Math.max(bounds.getWidth(), bounds.getHeight());
        // resizes to be 1x1
        shape.transform(AffineTransform.getScaleInstance(1 / max, 1 / max));
        bounds = shape.getBounds2D();
        // centers around (0, 0)
        shape.transform(AffineTransform.getTranslateInstance(-bounds.getWidth()
                / 2 - bounds.getX(), -bounds.getHeight() / 2 - bounds.getY()));
    }

    /**
     * determines the fill color of the shape. 
     * All shapes are filled by default
     * and the default color of the fill is blue.
     * 
     * @param c
     *            the fill color
     */
    public void setColor(Color c)
    {
        color = c;
        if(optimizeForBitmap)
            restoreImage();
    }

    /**
     * attaches the Tracker to this Sprite. 
     * The advanceTime method will be
     * called between each Frame, and the 
     * Tracker will alter the Sprite's
     * location, orientation, and size.
     * 
     * @param t
     *            the Tracker which will act upon this Sprite
     */
    public void setTracker(Tracker t)
    {
        tracker = t;
    }

    /**
     * returns the fill color of the Sprite
     * 
     * @return the fill color
     */
    public Color getColor()
    {
        return color;
    }

    /**
     * gets the Tracker which has been attached to the Sprite
     * 
     * @return the Tracker acting upon the Sprite, or null if no Tracker has
     *         been set
     */
    public Tracker getTracker()
    {
        return tracker;
    }
    
    /** gets the square root of the
     * determinant of the transformation
     * matrix
     * @return the scaling factor
     */
    private double internalGetScale()
    {
        double determinant = transform.getScaleX() * transform.getScaleY()
        - transform.getShearX() * transform.getShearY();
        return Math.sqrt(determinant);
    }

    /**
     * sets the absolute scale without regard 
     * to its current value, must be nonzero.
     * If zero is sent in as the parameter,
     * a very small number will be used instead.
     * @param scale
     *            the new scale
     */
    public void setScale(double scale)
    {
        if (scale == 0)
            scale=0.00001;
        double determinant = transform.getScaleX() * transform.getScaleY()
        - transform.getShearX() * transform.getShearY();
        scale = scale / Math.sqrt(determinant);
        if(scale==1)
            return;
        transform.setTransform(scale * transform.getScaleX(), scale
                * transform.getShearY(), scale * transform.getShearX(), scale
                * transform.getScaleY(), transform.getTranslateX(), transform
                .getTranslateY());
        if(optimizeForBitmap)
            restoreImage();
    }
    
    /**called when the volatile image expires.
     * This method is only called when optimizing
     * the sprite for bitmap display.
     */
    private void restoreImage()
    {
        if(image==null)
            return;
        int status=image.validate(config);
        Rectangle2D bounds=getShape().getBounds2D();
        if(status==VolatileImage.IMAGE_INCOMPATIBLE ||
                bounds.getWidth()>image.getWidth() ||
                bounds.getHeight()>image.getHeight())
            image=createImage();
        Graphics2D brush=image.createGraphics();
        brush.setBackground(new Color(0, 0, 0, 0));
        brush.clearRect(0, 0, image.getWidth(), image.getHeight());
        brush.translate(
                -bounds.getMinX()+(image.getWidth()-bounds.getWidth())/2, 
                -bounds.getMinY()+(image.getHeight()-bounds.getHeight())/2);
        brush.setColor(color);
        RenderingHints hints = new RenderingHints(null);
        hints.put(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_SPEED);
        hints.put(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        brush.addRenderingHints(hints);
        brush.fill(getShape());
    }

    /**creates a volatile image appropriately sized
     * @return a volatile image which will be used
     * to display the sprite
     */
    private VolatileImage createImage()
    {
        Rectangle2D bounds=getShape().getBounds2D();
        return config.createCompatibleVolatileImage(
                (int)bounds.getWidth()+1, 
                (int)bounds.getHeight()+1, 
                Transparency.TRANSLUCENT);
    }
    
    /**
     * Updates the Sprite's location, size, and 
     * orientation are determined using the Tracker.
     * If overridden, this method must not add or
     * remove sprites from the canvas.  If adding
     * or removing sprites is desired, schedule this
     * action to occur zero seconds from now by
     * creating an alarm which performs the desired
     * adding or removing.
     */
    public void update()
    {
        if (tracker != null)
        {
            Point2D.Double translation = tracker.getTranslation();
            double scaleFactor = tracker.getScaleFactor();
            double rotationAddition = tracker.getRotationAddition();
            translate(translation);
            if (scaleFactor != 1)
                scale(scaleFactor);
            if (rotationAddition != 0)
                rotate(rotationAddition);
        }
    }

    /**
     * scale with regard to the current scaling. 
     * The resulting scale will be the
     * product of the current scale and the scale parameter.
     * 
     * @param scale
     *            the factor by which to change the current scale, must be
     *            nonzero
     */
    public void scale(double scale)
    {
        setScale(scale * internalGetScale());
    }

    /**
     * sets the orientation without regard to the current orientation
     * 
     * @param rotation
     *            the new orientation in radians counter-clockwise for the
     *            original orientation
     */
    public void setRotation(double rotation)
    {
        transform.rotate(rotation - getRotation());
        if(optimizeForBitmap)
            restoreImage();
    }

    /**
     * sets the orientation with regard to the current orientation
     * 
     * @param rotation
     *            the additional rotation in radians
     */
    public void rotate(double rotation)
    {
        setRotation(getRotation() + rotation);
    }

    /**
     * the scaling factor
     * 
     * @return the ratio of the current size to the orignal size of the shape
     */
    public double getScale()
    {
        return internalGetScale();
    }

    /**
     * uses the transform to get the location
     * @return the location as obtained from the transform
     */
    private Point2D.Double internalGetLocation()
    {
        return new Point2D.Double(
                transform.getTranslateX(), 
                transform.getTranslateY());
    }
    
    /**
     * the location of the shape on the canvas
     * 
     * @return the location of the shape
     */
    public Point2D.Double getLocation()
    {
        return internalGetLocation();
    }

    /**
     * sets the location without regard to its current location
     * 
     * @param location
     *            the new location
     */
    public void setLocation(Point2D.Double location)
    {
        setLocation(location.x, location.y);
    }
    
    /**
     * sets the location without regard to its current location
     * 
     * @param x the horizontal location
     * @param y the vertical location
     */
    public void setLocation(double x, double y)
    {
        transform.setTransform(transform.getScaleX(), transform.getShearY(),
                transform.getShearX(), transform.getScaleY(),
                x, y);
    }

    /**
     * moves the shape with regard to its current location
     * 
     * @param delta
     *            the amount and direction to move the shape from its current
     *            location
     */
    public void translate(Point2D.Double delta)
    {
        translate(delta.x, delta.y);
    }

    /**
     * moves the shape wiht regard to its current location
     * 
     * @param x the amount to move horizontally            location
     * @param y the amount to move vertically            location
     */
    public void translate(double x, double y)
    {
        setLocation(
                x+transform.getTranslateX(),
                y+transform.getTranslateY());
    }

    /**Simulates the surface normal used for
     * bouncing the moving object off of the
     * stationary object.  Normal is in the direction
     * from the surface of the stationary object to
     * the center of the moving shape's bounding box.
     * @param stationary the object not in motion
     * @param moving the object that will bounce off
     * of the stationary object
     * @return the radians of the normal vector
     */
    public static double getNormalVector(
            Shape stationary, Shape moving)
    {
        Area movingArea=new Area(moving);
        Area stationaryArea=new Area(stationary);
        Area intersecting=new Area(stationaryArea);
        intersecting.intersect(new Area(movingArea));
        if(intersecting.isEmpty())
        {   
            return Double.NaN;
        }
        Rectangle2D movingBounds=moving.getBounds2D();
        Rectangle2D overlapBounds=intersecting.getBounds2D();
        Point2D.Double normal=new Point2D.Double(
                movingBounds.getCenterX()-overlapBounds.getCenterX(),
                movingBounds.getCenterY()-overlapBounds.getCenterY());
        return Math.atan2(normal.y, normal.x);
    }
    
    /**
     * gets the current amount of rotation in radians
     * 
     * @return the positive rotation in radians less than 2*PI and zero or more
     */
    public double getRotation()
    {
        AffineTransform copy = new AffineTransform(transform);
        copy.translate(-copy.getTranslateX(), -copy.getTranslateY());
        double scale = internalGetScale();
        copy.scale(1 / scale, 1 / scale);
        double theta = Math.acos(copy.getScaleX());
        if (Math.asin(-copy.getShearX()) < 0)
            theta *= -1;
        return theta;
    }

    /**
     * returns whether the Sprite is currently displayable
     * 
     * @return true if Sprite can be displayed, false otherwise
     */
    public boolean isVisible()
    {
        return visible;
    }

    /**
     * sets if the Sprite will be displayed 
     * when added to an AnimationCanvas
     * 
     * @param vis
     *            true indicates to display, false to hide
     */
    public void setVisible(boolean vis)
    {
        visible = vis;
    }

    /**adds a sequence of translucent sprites
     * from the sprite's previous location to
     * the current location in increasing
     * opacity.  By default, only the sprite's
     * current location is drawn.  Setting the
     * number to draw as greater than one can
     * help show the path that fast moving
     * sprites take.
     * @param length how many sprites to draw
     * from the previous position
     */
    public void setBlurLength(int length)
    {
        pathLength=length;
    }
    
    /**gets the number of sprites which are being
     * drawn from the sprite's previous location.
     * By default, only the sprite's
     * current location is drawn.  Setting the
     * number to draw as greater than one can
     * help show the path that fast moving
     * sprites take.
     * @return the number of sprites to draw in
     * the direction of the previous location.
     */
    public int getBlurLength()
    {
        return pathLength;
    }
    
    /**
     * provides a mechanism for enabling and visibility.
     * This method also handles drawing sprites in the
     * direction of the previous location for fast moving
     * sprites with the blur length set to greater than
     * zero.
     * @param brush
     *            the Graphics2D to draw on
     */
    protected void paintInternal(Graphics2D brush)
    {
        if(oldLocation==null)
        {
            oldLocation=getLocation();
            oldScale=getScale();
        }
        double currentScale=getScale();
        Point2D.Double currentLocation=getLocation();
        if (visible && enabled)
        {
            if(pathLength>0 && 
                    (!oldLocation.equals(currentLocation) ||
                            oldScale!=currentScale))
            {
                Color original=getColor();
                int alpha=original.getAlpha();
                for(int i=0; i<pathLength; i++)
                {
                    double factor=(i+1.0)/(pathLength+2.0);
                    setColor(new Color(
                            original.getRed(),
                            original.getGreen(),
                            original.getBlue(),
                            (int)(alpha*factor)));
                    setScale(oldScale+factor*(currentScale-oldScale));
                    setLocation(
                            oldLocation.x+factor*(currentLocation.x-oldLocation.x),
                            oldLocation.y+factor*(currentLocation.y-oldLocation.y));
                    paint(brush);
                }
                setColor(original);
                setScale(currentScale);
                setLocation(currentLocation);
                paint(brush);
            }
            else
                paint(brush);                
        }
        oldScale=currentScale;
        oldLocation=currentLocation;
    }

    /**
     * draws the shape in the proper location, orientation, and size
     * If overridden, this method must not add or
     * remove sprites from the canvas.  If adding
     * or removing sprites is desired, schedule this
     * action to occur zero seconds from now by
     * creating an alarm which performs the desired
     * adding or removing.

     * @param brush
     *            the Graphics used to draw the shape
     */
    public void paint(Graphics2D brush)
    {
        if(optimizeForBitmap)
        {
            if(image==null)
            {
                config=brush.getDeviceConfiguration();
                image=createImage();
                restoreImage();
            }
            if(image.contentsLost())
                restoreImage();
            Point2D.Double location=internalGetLocation();
            brush.drawImage(image, 
                    (int)location.x-image.getWidth()/2, 
                    (int)location.y-image.getHeight()/2, 
                    null);
        }
        else
        {
            brush.setColor(color);
            AffineTransform original=brush.getTransform();
            brush.transform(transform);
            brush.fill(shape);
            brush.setTransform(original);
        }
    }

    /**
     * gets the shape in its current position, size, and orientation
     * 
     * @return the shape
     */
    public Shape getShape()
    {
        GeneralPath path=new GeneralPath(shape);
        path.transform(transform);
        return path;
    }

    /**
     * returns 2-D bounding box of sprites
     * @return BoundingBox
     */
    public Rectangle2D getBounds2D()
    {
        return getShape().getBounds2D();
    }
    
    /**determines if a point is within the sprite's shape
     * @param point the point to test
     * @return true if the point is in the shape,
     * false otherwise
     */
    public boolean intersects(Point2D.Double point)
    {
        Area area=new Area(getShape());
        return area.contains(point);
    }
    
    /**
     * determines if another Sprite intersects this Sprite.
     * Intersection is a geometric property of the sprites
     * and does not depend on whether the sprites are on
     * the canvas or visible.
     * 
     * @param sprite
     *            another Sprite
     * @return true if the other Sprite intersects this Sprite, false if there
     *         is no intersection
     */
    public boolean intersects(Sprite sprite)
    {
        Rectangle2D spriteBounds=sprite.getBounds2D();
        Rectangle2D bounds=getBounds2D();
        //check simplest case first:
        //if upright bounding boxes don't intersect, shapes can't
        if(!bounds.intersects(spriteBounds))
        {
            return false;
        }
        //now check to see if rotated bounding boxes intersect
        double originalRotation=getRotation();
        setRotation(0);
        Rectangle2D original=getShape().getBounds2D();
        setRotation(originalRotation);
        GeneralPath path=new GeneralPath(original);
        path.transform(AffineTransform.getRotateInstance(originalRotation,
                original.getCenterX(), original.getCenterY()));
        Area boundsRotated=new Area(path);
        originalRotation=sprite.getRotation();
        sprite.setRotation(0);
        original=sprite.getShape().getBounds2D();
        sprite.setRotation(originalRotation);
        path=new GeneralPath(original);
        path.transform(AffineTransform.getRotateInstance(originalRotation,
                original.getCenterX(), original.getCenterY()));
        Area spriteBoundsRotated=new Area(path);
        //if rotated bounding box does not intersect with
        //upright bounding box, then they do not intersect
        if(!boundsRotated.intersects(spriteBounds) ||
                !spriteBoundsRotated.intersects(bounds))
        {
            return false;
        }
        Area rotatedIntersection=new Area(boundsRotated);
        rotatedIntersection.intersect(spriteBoundsRotated);
        //if rotated bounding boxes do not intersect with
        //each other, then they do not intersect
        if(rotatedIntersection.isEmpty())
        {
            return false;
        }
        //if both are using bounding boxes, 
        //then they do intersect
        if(sprite.getUseBoundingBox() && useBoundingBox)
        {
            return true;
        }
        //since the boxes do intersect, must determine
        //if the shape and box intersect
        else if(useBoundingBox)
        {
            //they only intersect if the sprite shape 
            //intersects both the upright and the rotated
            //bounding boxes of this
            Area transformedArea=new Area(sprite.getShape());
            transformedArea.intersect(boundsRotated);
            return sprite.getShape().intersects(bounds) &&
                !transformedArea.isEmpty();
        }
        //since the boxes do intersect, must determine
        //if the shape and box intersect
        else if(sprite.useBoundingBox)
        {
            //they only intersect if this shape 
            //intersects both the upright and the rotated
            //bounding boxes of the sprite
            Area transformedArea=new Area(getShape());
            transformedArea.intersect(spriteBoundsRotated);
            return getShape().intersects(spriteBounds) &&
                !transformedArea.isEmpty();
        }
        //hardest case: boxes intersect and can't use bounding
        //boxes, must use intersection of actual shapes
        //this could take time if the shapes are complex
        else
        {
            Area one=new Area(getShape());
            Area two=new Area(sprite.getShape());
            one.intersect(two);
            return !one.isEmpty();            
        }
    }

    public static boolean intersects(Shape one, Shape two)
    {
    	Area total=new Area(new Area(one));
    	total.intersect(new Area(two));
    	return !total.isEmpty();
    }
    
    /**
     * determines the Sprite's ability to display and update
     * 
     * @return true if Sprite can display and update, false otherwise
     */
    public boolean isEnabled()
    {
        return enabled;
    }

    /**
     * sets the Sprite's ability to display and update
     * 
     * @param able
     *            true indicates the Sprite can display and update, false
     *            otherwise
     */
    public void setEnabled(boolean able)
    {
        enabled = able;
    }
    
    /**Note: this method is current disabled.  If the
     * method is desired, email the author of the FANG
     * Engine to request it be added back in.
     * sets whether the shape should be drawn directly
     * to the screen or if the shape should first be
     * drawn to an image and then the image is used to
     * draw the shape to the screen.  When the sprite
     * is optimized for bitmaps, potentially complex
     * shapes with curves and/or many vertexes can be
     * simplified into a 2D array of pixels using the
     * VolatileImage.  If the shape is not complex,
     * then it is probably just faster to draw the
     * shape directly to the screen.  By default,
     * all Sprites are not optimized for bitmap.  The
     * decision on whether to use bitmaps or not is
     * based upon the speed with or without using the
     * bitmaps since the rendering will look identical.
     * @param enableBitmap true indicates the shapes
     * should be flattened into pixels on a 2D image
     * and this image should be used for drawing to
     * screen.  False indicates the shape should be
     * drawn directly to screen without first caching
     * it into a VolatileImage. 
     */
    public void setOptimizedForBitmap(boolean enableBitmap)
    {
        //optimizeForBitmap=enableBitmap;
    }

    /**gets whether the shape should be drawn directly
     * to the screen or if the shape should first be
     * drawn to an image and then the image is used to
     * draw the shape to the screen.  When the sprite
     * is optimized for bitmaps, potentially complex
     * shapes with curves and/or many vertexes can be
     * simplified into a 2D array of pixels using the
     * VolatileImage.  If the shape is not complex,
     * then it is probably just faster to draw the
     * shape directly to the screen.  By default,
     * all Sprites are not optimized for bitmap.  The
     * decision on whether to use bitmaps or not is
     * based upon the speed with or without using the
     * bitmaps since the rendering will look identical.
     * @return true if the shapes are flattened into 
     * pixels on a 2D image and this image should be 
     * used for drawing to screen.  False indicates 
     * the shape is drawn directly to screen without 
     * first caching it into a VolatileImage. 
     */
    public boolean getOptimizedForBitmap()
    {
        return optimizeForBitmap;
    }
    
    /**sets the default color that all
     * sprites start with.  Calling this
     * method only affects Sprites
     * created after calling this
     * method.
     * @param defaultColor
     */
    public static void setDefaultColor(Color defaultColor)
    {
        Sprite.defaultColor=defaultColor;
    }
    
    /**
     * gets the default color which sprites
     * are made with
     * @return the default color of the sprites
     */
    public static Color getDefaultColor()
    {
        return defaultColor;
    }
    
    /**
     *  sets the orientation with regard to the 
     *  current orientation.  This method converts
     *  the degrees to radians and calls rotate.
     * @param degrees
     * 		the additional rotation in degrees
     */
    public void rotateDegrees(double degrees)
    {
    	rotate(Math.toRadians(degrees));
    }
    
    /**
     * sets the orientation without regard 
     * to the current orientation.
     * This method converts
     *  the degrees to radians and calls setRotation.
     * @param rotation
     *            the new orientation in degrees 
     *            from the original orientation
     */
    public void setRotationDegrees(double degrees)
    {
    	setRotation(Math.toRadians(degrees));
    }

    /**
     * gets the orientation of the sprite in
     * degrees.  This method calls getRotation
     * and converts the radians into degrees.
     * @return the rotation in degrees
     */
    public double getRotationDegrees()
    {
    	return Math.toDegrees(getRotation());
    }

    /**
     *  sets the orientation with regard to the 
     *  current orientation.  This method converts
     *  the revolutions to radians and calls rotate.
     *  One revolution is the same as 360 degrees or
     *  2*PI radians.
     * @param revolutions
     * 		the additional rotation in revolution
     */
    public void rotateRevolutions(double revolutions)
    {
    	rotate(revolutions*Math.PI*2);
    }
    
    /**
     * sets the orientation without regard 
     * to the current orientation.
     * This method converts
     *  the revolutions to radians and calls setRotation.
     *  One revolution is the same as 360 degrees or
     *  2*PI radians.
     * @param revolutions
     *            the new orientation in revolutions 
     *            from the original orientation
     */
    public void setRotationRevolutions(double revolutions)
    {
    	setRotation(revolutions*Math.PI*2);
    }

    /**
     * gets the orientation of the sprite in
     * revolutions.  This method calls getRotation
     * and converts the radians into revolutions.
     * @return the rotation in revolutions
     */
    public double getRotationRevolutions()
    {
    	return getRotation()/(Math.PI*2);
    }
/////////I added///make a move towards the center///



}
