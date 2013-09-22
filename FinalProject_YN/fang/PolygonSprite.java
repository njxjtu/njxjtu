package fang;

import java.awt.geom.GeneralPath;

/**
 * This class makes a polygon shaped sprite.
 * @author Jam Jenkins
 *
 */
public class PolygonSprite extends Sprite
{
	/**
	 * make a sprite with a shape determined
	 * by the given vertexes.  The number of
	 * arguments sent to this method must be
	 * even because each pair of values represent
	 * the x and y for the next vertex.  At least
	 * three points must be supplied.  To make a
	 * square, you could send in the following
	 * points: 0, 0, 1, 0, 1, 1, 1, 0
	 * which represent the four corners of a
	 * square: (0, 0) (1, 0) (1, 1) and (1, 0).
	 * The order in which the points are given
	 * is the order in which the lines will be
	 * drawn.
	 * @param points the x,y pairs for the 
	 * vertexes of the polygon.  The order of
	 * the points matters and determines the
	 * order of the lines drawn to connect the
	 * edges of the polygon.
	 */
	public PolygonSprite(double ... points)
	{
		GeneralPath path=new GeneralPath();
		path.moveTo((float)points[0], (float)points[1]);
		for(int i=2; i<points.length; i+=2)
			path.lineTo((float)points[i], (float)points[i+1]);
		path.closePath();
		setShape(path);
	}
	
	/**
	 * makes a regular polygon with a given
	 * number of sides.
	 * @param sides number of sides of the polygon
	 */
	public PolygonSprite(int sides)
	{
		GeneralPath path=new GeneralPath();
		int i=0;
		double angle=-Math.PI/2+i*2*Math.PI/sides;
		path.moveTo(
				(float)Math.cos(angle), 
				(float)Math.sin(angle));
		for(i=1; i<sides; i++)
		{
			angle=-Math.PI/2+i*2*Math.PI/sides;
			path.lineTo(
					(float)Math.cos(angle), 
					(float)Math.sin(angle));			
		}
		path.closePath();
		setShape(path);		
	}
}
