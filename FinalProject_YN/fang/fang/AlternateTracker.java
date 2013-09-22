package fang;

import java.awt.geom.Point2D;

/**
 * This class provides an alternative interface
 * to the Tracker interface.  Instead of providing
 * relative location, scaling, and rotation
 * information, this one allow setting the absolute
 * location, scaling, and rotation information.
 * The class takes care of taking the differences
 * and returning the relative values that the
 * Tracker interface expects.  To use this class,
 * simply provide an advanceTime method which calls
 * setScale, setLocation, or setRotation methods.
 * @author Jam Jenkins
 */
public abstract class AlternateTracker 
	extends TrackerAdapter
{
	/**the absolute rotation in radians*/
	private double rotation;
	/**the absolute scale*/
	private double scale;
	/**the absolute position*/
	private Point2D.Double location=
		new Point2D.Double();

	/**the relative rotation*/
	private double rotationAddition=0;
	/**the relative scaling factor*/
	private double scaleFactor=1;
	/**the relative position*/
	private Point2D.Double translation=
		new Point2D.Double();
	
	public AlternateTracker()
	{
		this(0, 1, new Point2D.Double());
	}
	
	/**
	 * sets up the current state of the tracker
	 * @param rotation the rotation in radians
	 * @param scale the current scale
	 * @param location the current position
	 */
	public AlternateTracker(double rotation,
			double scale, Point2D.Double location)
	{
		this.rotation=rotation;
		this.scale=scale;
		this.location.x=location.x;
		this.location.y=location.y;
	}
	
	/**
	 * gets the amount to scale from the current
	 * rotation in radians
	 * @return the amount to rotate in radians
	 */
	public final double getRotationAddition()
	{
		return rotationAddition;
	}
	
	/**
	 * gets the amount to scale from the current
	 * scale
	 * @return the scaling factor
	 */
	public final double getScaleFactor()
	{
		return scaleFactor;
	}
	
	/**
	 * gets the amount to move from the current
	 * location
	 * @return the amount to move in the horizontal
	 * and vertical direction
	 */
	public final Point2D.Double getTranslation()
	{
		return translation;
	}
	
	/**
	 * sets the next rotation.  Calling this
	 * method updates what is returned from the
	 * getRotationAddition method.
	 * @param r the next orientation in radians
	 */
	public final void setRotation(double r)
	{
		rotationAddition=r-rotation;
		rotation=r;
	}
	
	/**
	 * sets the next rotation.  Calling this
	 * method updates what is returned from the
	 * getRotationAddition method.
	 * @param d the next orientation in degrees
	 */
	public final void setRotationDegrees(double d)
	{
		setRotation(Math.toRadians(d));
	}

	/**
	 * sets the next rotation.  Calling this
	 * method updates what is returned from the
	 * getRotationAddition method.
	 * @param rev the next orientation in degrees
	 */
	public final void setRotationRevolutions(double rev)
	{
		setRotation(rev*Math.PI*2);
	}
	
	/**
	 * sets the next scale.  Calling this
	 * method updates what is returned from the
	 * getScaleFactor method.
	 * @param s the next scale
	 */
	public final void setScale(double s)
	{
		scaleFactor=s/scale;
		scale=s;
	}
	
	/**
	 * sets the next location.  Calling this
	 * method updates what is returned from the
	 * getTranslation method.
	 * @param l the next location
	 */
	public final void setLocation(Point2D.Double l)
	{
		translation.x=l.x-location.x;
		translation.y=l.y-location.y;
		location.x=l.x;
		location.y=l.y;
	}
	
	/**
	 * this method is called automatically every
	 * time the frame is advanced.  The methods
	 * setScale, setLocation, and setRotation should
	 * only be called from this method and nowhere
	 * else.
	 * @param time the amount of time which has
	 * passed since the last frame
	 */
    public abstract void advanceTime(double time);

}
