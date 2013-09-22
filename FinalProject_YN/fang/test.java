package fang;

import java.awt.Rectangle;
import java.awt.geom.*;

/**
 * This class represents a rectangular sprite.
 * @author Jam Jenkins
 */
public class test extends Sprite 
{
	/**
	 * makes a rectangular sprite.  The magnitude of
	 * the width and height only matter with respect
	 * to each other.  For example, a RectangleSprite
	 * constructed with a width
	 * 2 and height 1 is the same as making the
	 * width 20 and the height 10 because they both
	 * have the same height/width aspect ratio of 2.
	 * @param width the horizontal span
	 * @param height the vertical span
	 */
	public test()
	{  Area area = new Area();
	Rectangle box = new Rectangle(0, 0, 20, 60);
	area.add(new Area(box));
	Ellipse2D.Double circle = new Ellipse2D.Double(2, 2, 16, 16);
	area.subtract(new Area(circle));
	circle = new Ellipse2D.Double(2, 22, 16, 16);
	area.subtract(new Area(circle));
	circle = new Ellipse2D.Double(2, 42, 16, 16);
	area.subtract(new Area(circle));
		setShape(area);
	}
	
}
