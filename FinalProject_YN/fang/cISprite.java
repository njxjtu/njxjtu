package fang;

import java.awt.geom.*;

public class cISprite extends Sprite 
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
	public cISprite(int width, int height)
	{
		setShape(new Rectangle2D.Double(0, 0, width, height));
	}
	
}
