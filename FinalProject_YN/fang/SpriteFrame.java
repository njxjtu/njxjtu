package fang;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.util.LinkedList;

/**
 * This class represents a way to group
 * sprites together such that they can
 * be scaled, translated, and rotated
 * together.  Because using a tracker
 * to control this motion is desired,
 * this class extends Sprite, yet it is
 * never directly displayed on the
 * screen.  Instead, all of the component
 * sprites are drawn if they are on the
 * canvas and visible.  Adding this Sprite
 * to the canvas is only necessary if a
 * Tracker is being added to it.  This
 * SpriteFrame behaves well with sprites
 * which do not change their own location.
 * The SpriteFrame is only designed to be 
 * used with sprites which are not translated.
 * If a sprite within the SpriteFrame is
 * translated, the behavior is unspecified.
 * In general, it is best to remove a sprite
 * from the SpriteFrame before translating it.
 * Rotating and scaling sprites contained
 * within the SpriteFrame is acceptable.
 * Note: this is a new class which has
 * not been fully tested.  Testing is
 * currently underway.  Please email any
 * bug reports to bugs@fangengine.org.
 * @author Jam Jenkins
 */
public class SpriteFrame extends Sprite
{
	/**the sprites in this frame*/
	private LinkedList<Sprite> sprites=
		new LinkedList<Sprite>();
	
	/**the center of the frame*/
	private Point2D.Double center=
		new Point2D.Double();
	
	/**how much the frame has been rotated*/
	private double rotation=0;
	
	/**used to send all setLocations to translate*/
	private Point2D.Double location=
		new Point2D.Double();
	
	/**current scale*/
	private double scaling=1;
	
	/**sets the rotation with respect to
	 * the original rotation of this frame.
	 * This has the effect of rotating each
	 * sprite the same amount.  Also, for each
	 * sprite not located at the center, this
	 * will also have the effect of translating
	 * the sprite.
	 * @param radians the absolute amount to rotate
	 */
	public void setRotation(double radians)
	{
		rotate(radians-rotation);
	}
	
	/**sets the rotation with respect to
	 * the current rotation of this frame.
	 * Also, for each
	 * sprite not located at the center, this
	 * will also have the effect of translating
	 * the sprite.
	 * @param radians the relative amount to rotate
	 */	
	public void rotate(double radians)
	{
		rotation+=radians;
		rotation=rotation%(2*Math.PI);
		for(Sprite s: sprites)
		{
			Point2D.Double location=s.getLocation();
			s.rotate(radians);
			double magnitude=location.distance(center);
			if(magnitude==0) continue;
			double startAngle=Math.atan2(
					location.y-center.y,
					location.x-center.x);
			double endAngle=startAngle+radians;
			s.setLocation(
					magnitude*Math.cos(endAngle)+center.x,
					magnitude*Math.sin(endAngle)+center.y);
		}
	}
	
	/**
	 * sets the scale with respect to the
	 * original scale of this frame.
	 * Also, for each
	 * sprite not located at the center, this
	 * will also have the effect of translating
	 * the sprite.
	 * @param scale the absolute scale for this frame
	 */
	public void setScale(double scale)
	{
		scale(scale/scaling);
	}
	
	/**
	 * gets the amount this frame is rotated with
	 * respect to its original orientation
	 * @return the absolute amount rotated in radians
	 */
	public double getRotation()
	{
		return rotation;
	}
	
	/**
	 * scales with respect to the current frame.
	 * Also, for each
	 * sprite not located at the center, this
	 * will also have the effect of translating
	 * the sprite.
	 * @param scaling the relative scaling factor
	 */
	public void scale(double scaling)
	{
		this.scaling*=scaling;
		for(Sprite s: sprites)
		{
			Point2D.Double location=s.getLocation();
			s.scale(scaling);
			double magnitude=location.distance(center);
			if(magnitude==0) continue;
			double angle=Math.atan2(
					location.y-center.y,
					location.x-center.x);
			magnitude*=scaling;
			s.setLocation(
					magnitude*Math.cos(angle)+center.x,
					magnitude*Math.sin(angle)+center.y);
		}
	}
	
	/**
	 * translates all of the sprites in this
	 * frame as well as the center of the frame.
	 * @param x the horizontal amount to move
	 * @param y the vertical amount to move
	 */
	public void translate(double x, double y)
	{
		center.x+=x;
		center.y+=y;
		location.x+=x;
		location.y+=y;
		for(Sprite s: sprites)
			s.translate(x, y);
	}
	
	/**does nothing*/
	public void paint(Graphics2D brush){}
	
	/**does nothing
	 * @param shape not used
	 */
	public void setShape(Shape shape)
	{
		System.err.println("Do not set the shape of a SpriteFrame!");
	}
	
	/**
	 * gets the current location
	 * @return the current location
	 */
	public Point2D.Double getLocation()
	{
		return new Point2D.Double(location.x, location.y);
	}
	
	/**
	 * calls translate with the difference of
	 * the parameters and the current location
	 * @param x the horizontal position
	 * @param y the vertical position
	 */
	public void setLocation(double x, double y)
	{
		translate(x-location.x, y-location.y);		
	}
	
	/**
	 * calls translate with the difference of
	 * the parameters and the current location
	 * @param loc the position to move to
	 */
	public void setLocation(Point2D.Double loc)
	{
		translate(loc.x-location.x, loc.y-location.y);
	}
	
	/**
	 * given a point relative to the center of
	 * the original frame, this method calculates
	 * where that point would currently be in
	 * the canvas coordinate system
	 * @param insideFrame the position relative
	 * to the original frame
	 * @return the corresponding position in the
	 * canvas coordinate system
	 */
	public Point2D.Double getRealLocation(Point2D.Double insideFrame)
	{
		Point2D.Double location=new Point2D.Double(
				insideFrame.x, insideFrame.y);
		double startAngle=Math.atan2(
				location.y-center.y,
				location.x-center.x);
		double endAngle=startAngle+rotation;
		double magnitude=scaling*location.distance(center);
		location.x=magnitude*Math.cos(endAngle)+center.x;
		location.y=magnitude*Math.sin(endAngle)+center.y;
		return location;		
	}
	
	/**
	 * given a point in the canvas coordinate
	 * system, this method calculates
	 * where that point would currently be 
	 * relative to the original frame
	 * @param outsideFrame the position in the
	 * canvas coordinate system
	 * @return the position relative
	 * to the original frame
	 */
	public Point2D.Double getFrameLocation(Point2D.Double outsideFrame)
	{
		Point2D.Double location=new Point2D.Double(
				outsideFrame.x, outsideFrame.y);
		double startAngle=Math.atan2(
				location.y-center.y,
				location.x-center.x);
		double endAngle=startAngle-rotation;
		double magnitude=1.0/scaling*location.distance(center);
		location.x=magnitude*Math.cos(endAngle)+center.x;
		location.y=magnitude*Math.sin(endAngle)+center.y;
		return location;
	}
		
	/**
	 * sets the center around which to rotate
	 * and scale.  When scaling, there is always
	 * translation away from the center.  When
	 * rotating, there is always movement around
	 * the center.
	 * @param x the horizontal center
	 * @param y the vertical center
	 */
	public void setCenter(double x, double y)
	{
		setCenter(new Point2D.Double(x, y));
	}
	
	/**
	 * gets the center around which to rotate
	 * and scale.  When scaling, there is always
	 * translation away from the center.  When
	 * rotating, there is always movement around
	 * the center.
	 * @return the center
	 */
	public Point2D.Double getCenter()
	{
		return new Point2D.Double(center.x, center.y);
	}
	
	/**
	 * gets all the sprites in this SpriteFrame
	 * @return the array of sprites
	 */
	public Sprite[] getAllSprites()
	{
		return sprites.toArray(new Sprite[0]);
	}
	
	/**
	 * determines if the sprite given as a parameter
	 * is part of this frame.
	 * @param sprite the sprite to check for membership
	 * @return true if the sprite is part of the frame,
	 * false otherwise
	 */
	public boolean contains(Sprite sprite)
	{
		return sprites.contains(sprite);
	}
		
	/**
	 * sets the center around which to rotate
	 * and scale.  When scaling, there is always
	 * translation away from the center.  When
	 * rotating, there is always movement around
	 * the center.
	 * @param center the position around which
	 * to rotate and scale
	 */	
	public void setCenter(Point2D.Double center)
	{
        this.center.x=center.x;
        this.center.y=center.y;
	}
	
	/**
	 * adds a sprite to this frame
	 * @param s the sprite to add
	 */
	public void addSprite(Sprite s)
	{
		sprites.add(s);
	}

	/**
	 * removes a sprite from this frame.
	 * If the sprite is not part of this
	 * frame, the call is ignored and no
	 * exception is thrown.
	 * @param s the sprite to remove
	 */
	public void removeSprite(Sprite s)
	{
		sprites.remove(s);
	}
}
