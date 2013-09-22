package fang;

import java.awt.geom.*;

/**
 * This class represents a rectangular sprite.
 * @author Jam Jenkins
 */
public class oLSprite extends Sprite 
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
	public oLSprite()
	{
		GeneralPath path = new GeneralPath();
		path.moveTo((float) 0.0, (float) 0.0);
		path.lineTo((float) 0.0, (float) 2.0);
		path.lineTo((float) 4.0, (float) 2.0);
		path.lineTo((float) 4.0, (float) 4.0);
		path.lineTo((float) 6.0, (float) 4.0);
		path.lineTo((float) 6.0, (float) 0.0);
		path.closePath();
		setShape(path);
	}
	
}
