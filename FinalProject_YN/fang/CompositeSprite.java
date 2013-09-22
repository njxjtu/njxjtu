package fang;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.*;
import java.awt.geom.Rectangle2D;
import java.util.*;

/**
 * This class extend the base Sprite class in
 * order to allow multiple colors and shapes
 * in a single sprite.
 * Note: this is a new class which has
 * not been fully tested.  Testing is
 * currently underway.  Please email any
 * bug reports to bugs@fangengine.org.
 * @author Jam Jenkins
 *
 */
public class CompositeSprite 
	extends Sprite
{
	/**the colors for each shape*/
	private LinkedHashMap<Shape, Color> colorMap=
		new LinkedHashMap<Shape, Color>();
	
	/**the names of the shapes*/
	private HashMap<String, Shape> shapeMap=
		new HashMap<String, Shape>();
	
	/**whether the shape is displayed or not*/
	private HashMap<Shape, Boolean> visibility=
		new HashMap<Shape, Boolean>();
	
	/**the sum of the shapes*/
	private Area totalShape=new Area();
	
	/**the transform used to normalize the sprite*/
	private AffineTransform scaleToFit=
		new AffineTransform();
	
	/**updates the scaleToFit variable*/
	private void updateScaling()
	{
		Rectangle2D bounds=totalShape.getBounds2D();
		double scaling=1.0/Math.max(bounds.getWidth(), bounds.getHeight());
		scaleToFit=new AffineTransform();
		scaleToFit.scale(scaling, scaling);		
		bounds=totalShape.getBounds2D();
		scaleToFit.translate(
				-bounds.getCenterX(),
				-bounds.getCenterY());
	}
	
	/**determines if this sprite intersects with
	 * the other sprite
	 * @param sprite the other sprite
	 * @return true if this sprite intersects with
	 * the other sprite, false otherwise
	 */
	public boolean intersects(Sprite sprite)
	{
		return intersects(getShape(), sprite.getShape());
	}
	
	/**
	 * gets all of the shapes intersecting with another
	 * sprite.  Calling this method may be slow if there
	 * are many shapes in the sprite.
	 * @param sprite the sprite which may or may not
	 * be intersecting
	 * @return the array of sprites which intersect
	 */
	public Shape[] getShapesIntersecting(Sprite sprite)
	{
		LinkedList<Shape> intersecting=new LinkedList<Shape>();
		for(String name: shapeMap.keySet())
			if(intersects(sprite, name))
				intersecting.add(shapeMap.get(name));
		return intersecting.toArray(new Shape[0]);		
	}
	
	/**
	 * gets the names of the intersecting shapes
	 * @param sprite the other sprite to check for
	 * intersection with
	 * @return the array of names of shapes
	 * which are intersecting with the other sprite
	 */
	public String[] getNamesOfIntersecting(Sprite sprite)
	{
		LinkedList<String> intersecting=new LinkedList<String>();
		for(String name: shapeMap.keySet())
			if(intersects(sprite, name))
				intersecting.add(name);
		return intersecting.toArray(new String[0]);
	}
		
	/**
	 * determines if a particular shape intersects with
	 * another sprite
	 * @param sprite the other sprite
	 * @param name the name of the shape to check
	 * @return true if the shape indicated intersects,
	 * false otherwise
	 */
	public boolean intersects(Sprite sprite, String name)
	{
		if(!shapeMap.containsKey(name))
			return false;
		return intersects(sprite, shapeMap.get(name));
	}
	
	/**
	 * determines if a particular shape intersects with
	 * another sprite
	 * @param sprite the other sprite
	 * @param shape the shape to check
	 * @return true if the shape indicated intersects,
	 * false otherwise
	 */
	public boolean intersects(Sprite sprite, Shape shape)
	{
		Area area=new Area(shape);
		area.transform(scaleToFit);
		area.transform(transform);
		area.intersect(new Area(sprite.getShape()));
		return !area.isEmpty();
	}
	
	/**
	 * gives a shape a name.  This method is added for
	 * convenience in later identifying the component
	 * shapes which make up this MultiShapeSprite
	 * @param name the identifier for the shape
	 * @param shape the shape to identify
	 */
	public void setShapeName(String name, Shape shape)
	{
		shapeMap.put(name, shape);
	}
	
	/**
	 * gets the particular shape in the same location,
	 * orientation, and scale in which it was originally
	 * added
	 * @param name the identifier for the shape to retrieve
	 * @return the shape corresponding with this name
	 */
	public Shape getShape(String name)
	{
		return shapeMap.get(name);
	}
	
	/**
	 * gets the shape of this sprite in its current
	 * orientation, position and size
	 * @return the current shape
	 */
	public Shape getShape()
	{
		Area area=new Area(totalShape);
		area.transform(scaleToFit);
		area.transform(transform);
		return area;
	}
	
	/**
	 * gets the color assigned to this shape.
	 * Returns null if the shape is not part
	 * of this MultiShapeSprite.
	 * @param shape the shape to check
	 * @return the color of the shape
	 */
	public Color getColor(Shape shape)
	{
		if(colorMap.containsKey(shape))
			return colorMap.get(shape);
		return null;
	}
	
	/**
	 * gets the color assigned to the shape
	 * with this name.
	 * Returns null if no shape is associated
	 * with the given name.
	 * @param name the name of the shape to check
	 * @return the color of the shape
	 */
	public Color getColor(String name)
	{
		if(shapeMap.containsKey(name))
			return getColor(shapeMap.get(name));
		return null;
	}
	
	/**
	 * sets the color of a shape given its name
	 * @param name the name of the shape
	 * @param color the color for the shape
	 */
	public void setColor(String name, Color color)
	{
		if(shapeMap.containsKey(name))
			setColor(shapeMap.get(name), color);
	}
	
	/**
	 * sets the color of a shape 
	 * @param shape the shape to color
	 * @param color the color for the shape
	 */
	public void setColor(Shape shape, Color color)
	{
		if(colorMap.containsKey(shape))
			colorMap.put(shape, color);
	}
	
	/**
	 * adds the shape with a given color to
	 * this MultiShapeSprite.  When the shape
	 * is added, it may increase the bounds of
	 * the total shape.  If allowRescale is true,
	 * then this sprite will rescale to keep the
	 * display of the sprite consistently sized
	 * by changing the scale if necessary.  If
	 * allowRescale is false, the appearance of
	 * the existing shapes may shrink when a shape
	 * is added which increases the bounds of the
	 * current sprite in order to keep the scale
	 * consistent.
	 * @param shape the shape to add
	 * @param color the color of the shape
	 * @param allowRescale true allows the scale to
	 * change in order to maintain a consistent
	 * appearance, false indicates to maintain a
	 * consistent scale and possible shrink the
	 * appearance
	 */
	public void addShape(Shape shape, Color color, 
			boolean allowRescale)
	{
		Rectangle2D originalBounds=totalShape.getBounds2D();
		
		visibility.put(shape, true);
		colorMap.put(shape, color);
		totalShape.add(new Area(shape));
		setShape(totalShape);
		updateScaling();
		
		if(allowRescale && colorMap.size()>1)
			fixChange(originalBounds);

	}

	/**
	 * scales and translates the sprite in order to
	 * maintain a consistent placement and size appearance
	 * when shapes are added or removed. 
	 * @param originalBounds
	 */
	private void fixChange(Rectangle2D originalBounds)
	{
		Rectangle2D bounds=totalShape.getBounds2D();
		
		double dx=bounds.getCenterX()-originalBounds.getCenterX();
		double dy=bounds.getCenterY()-originalBounds.getCenterY();
		
		double scalingFactor=
			Math.max(originalBounds.getWidth(), originalBounds.getHeight())/
			Math.max(bounds.getWidth(), bounds.getHeight());
		
		scale(1.0/scalingFactor);
		Point2D.Double trans=new Point2D.Double(
				dx*getScale()*scalingFactor/Math.max(originalBounds.getWidth(), originalBounds.getHeight()), 
				dy*getScale()*scalingFactor/Math.max(originalBounds.getWidth(), originalBounds.getHeight()));
		double magnitude=trans.distance(0, 0);
		double angle=Math.atan2(trans.y, trans.x);
		angle+=getRotation();
		translate(
				magnitude*Math.cos(angle),
				magnitude*Math.sin(angle));		
	}
	
	/**
	 * adds the shape with a given color to
	 * this MultiShapeSprite.  When the shape
	 * is added, the size of the original
	 * sprite will appear unchanged even though
	 * the scale of this sprite may need to be
	 * changed in order to keep the display
	 * consistently sized.
	 * @param shape the shape to add
	 * @param color the color of the shape
	 */
	public void addShape(Shape shape, Color color)
	{
		addShape(shape, color, true);
	}
	
	/**
	 * removes the shape from the MultiShapeSprite,
	 * possibly rescaling in order for the sprite
	 * to maintain a consistent appearance.
	 * @param name the name of the shape to remove
	 */
	public void removeShape(String name)
	{
		removeShape(name, true);
	}
	
	/**
	 * removes the shape from the MultiShapeSprite,
	 * possibly rescaling in order for the sprite
	 * to maintain a consistent appearance.
	 * @param shape the shape to remove
	 */
	public void removeShape(Shape shape)
	{
		removeShape(shape, true);
	}
	
	/**
	 * removes the shape from the MultiShapeSprite,
	 * possibly rescaling in order for the sprite
	 * to maintain a consistent appearance.
	 * @param name the name of the shape to remove
	 * @param allowRescale true indicates that the
	 * sprite may be resized in order to provide for
	 * a consistent appearance, false indicates the
	 * scale should remain consistent and the appearance
	 * of the remaining shapes may appear larger or
	 * smaller.
	 */
	public void removeShape(String name, boolean allowRescale)
	{
		if(shapeMap.containsKey(name))
		{
			removeShape(shapeMap.get(name));
			shapeMap.remove(name);
		}
	}
	
	/**
	 * determines if the named shape is being displayed
	 * @param name the name of the shape to check
	 * @return true if the named shape is displayed when
	 * this sprite is drawn, false otherwise
	 */
	public boolean isVisible(String name)
	{
		if(shapeMap.containsKey(name))
			return isVisible(shapeMap.get(name));
		return false;
	}
	
	/**
	 * determines if the shape is being displayed
	 * @param shape the shape to check
	 * @return true if the shape is displayed when
	 * this sprite is drawn, false otherwise
	 */
	public boolean isVisible(Shape shape)
	{
		if(visibility.containsKey(shape))
			return visibility.get(shape);
		return false;
	}
	
	/**
	 * sets the named shape's visibility
	 * @param name the name of the shape to alter
	 * @param vis true indicates the shape should
	 * be drawn, false for invisibility
	 */
	public void setVisible(String name, boolean vis)
	{
		if(shapeMap.containsKey(name))
			setVisible(shapeMap.get(name), vis);
	}
	
	/**
	 * sets the shape's visibility
	 * @param shape the shape to alter
	 * @param vis true indicates the shape should
	 * be drawn, false for invisibility
	 */
	public void setVisible(Shape shape, boolean vis)
	{
		if(!visibility.containsKey(shape))
			return;
		visibility.put(shape, vis);
	}
	
	
	/**
	 * removes the shape from the MultiShapeSprite,
	 * possibly rescaling in order for the sprite
	 * to maintain a consistent appearance.
	 * @param shape the the shape to remove
	 * @param allowRescale true indicates that the
	 * sprite may be resized in order to provide for
	 * a consistent appearance, false indicates the
	 * scale should remain consistent and the appearance
	 * of the remaining shapes may appear larger or
	 * smaller.
	 */
	public void removeShape(Shape shape, boolean allowRescale)
	{
		Rectangle2D originalBounds=totalShape.getBounds2D();
		
		visibility.remove(shape);
		colorMap.remove(shape);
		totalShape=new Area();
		for(Shape s: colorMap.keySet())
			totalShape.add(new Area(s));
		setShape(totalShape);
		updateScaling();	
		
		if(allowRescale && colorMap.size()>0)
			fixChange(originalBounds);
	}
		
	/**
	 * draws all of the visible shapes in their given colors
	 * @param brush the graphics used to draw with
	 */
	public void paint(Graphics2D brush)
	{
        AffineTransform original=brush.getTransform();
        brush.transform(transform);
        brush.transform(scaleToFit);
        for(Map.Entry<Shape, Color> entry: colorMap.entrySet())
        {
        	if(visibility.get(entry.getKey()))
        	{
        		brush.setColor(entry.getValue());
        		brush.fill(entry.getKey());
        	}
        }
        brush.setTransform(original);
	}
	
	public boolean hasShape(String name)
	{
		return shapeMap.containsKey(name);
	}
	
	public boolean hasShape(Shape shape)
	{
		return colorMap.containsKey(shape);
	}

	/**
	 * moves the indicated shape to the
	 * bottom of the stack of shapes
	 * @param name the name of the shape to move
	 */
	public void moveToBottom(String name)
	{
		if(!shapeMap.containsKey(name))
			return;
		moveToBottom(shapeMap.get(name)); 
	}
	
	/**
	 * moves the indicated shape to the
	 * bottom of the stack of shapes
	 * @param shape the shape to move
	 */
	public void moveToBottom(Shape shape)
	{
		if(!colorMap.containsKey(shape))
			return;
		LinkedHashMap<Shape, Color> replacement=
			new LinkedHashMap<Shape, Color>();
		replacement.put(shape, colorMap.get(shape));
		colorMap.remove(shape);
		replacement.putAll(colorMap);
		colorMap=replacement;
	}
	
	/**
	 * moves the indicated shape to the
	 * top of the stack of shapes
	 * @param name the name of the shape to move
	 */
	public void moveToTop(String name)
	{
		if(!shapeMap.containsKey(name))
			return;
		moveToTop(shapeMap.get(name)); 		
	}
	
	/**
	 * moves the indicated shape to the
	 * top of the stack of shapes
	 * @param shape the shape to move
	 */
	public void moveToTop(Shape shape)
	{
		if(!colorMap.containsKey(shape))
			return;
		Color color=colorMap.get(shape);
		colorMap.remove(shape);
		colorMap.put(shape, color);
	}
}
