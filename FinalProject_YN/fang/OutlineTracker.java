package fang;

import java.awt.Shape;
import java.awt.geom.*;
import java.util.ArrayList;

import static java.awt.geom.FlatteningPathIterator.*;

import fang.*;
/**
 * This tracker enables a path to be created from
 * any shape or Sprite.
 * @author Jam Jenkins
 *
 */
public class OutlineTracker extends TrackerAdapter
{
	/**the distance around the outline*/
	private double perimeter;
	/**whether to keep moving around the
	 * outline once the shape has been traversed*/
	private boolean looping=false;
	/**the shape to use the outline of*/
	private Shape shape;
	/**how fast to move and which direction*/
	private double speed;
	/**where the tracker currently is in the outline
	 * of the shape*/
	private Point2D.Double currentPoint;
	/**how far to move the next time step*/
	private Point2D.Double delta;
	/**the points along the outline*/
	private double[][] allPoints;
	/**the type of each point, SEG_MOVETO or SEG_LINETO*/
	private int[] pointType;
	/**which is the current point in the array*/
	private int pointIndex;

	/**
	 * creates the tracker given a Sprite to move
	 * around and a speed to travel. If you want
	 * to make the sprite travel along the actual
	 * Sprite instead of just in the shape of the
	 * Sprite, you also need to set the original
	 * location of the Sprite moving to the current
	 * location of the OutlineTracker using the
	 * method getCurrentPoint().
	 * @param outline the sprite on which to traverse
	 * the outline
	 * @param speed the portion of the screen to
	 * move per second
	 */
	public OutlineTracker(Sprite outline, double speed)
	{
		this(outline.getShape(), speed);
	}

	/**
	 * creates the tracker given a Shape to move
	 * around and a speed to travel. If you want
	 * to make the sprite travel along the actual
	 * Sprite instead of just in the shape of the
	 * Sprite, you also need to set the original
	 * location of the Sprite moving to the current
	 * location of the OutlineTracker using the
	 * method getCurrentPoint().
	 * @param outline the shape on which to traverse
	 * the outline
	 * @param speed the portion of the screen to
	 * move per second
	 */
	public OutlineTracker(Shape outline, double speed)
	{
		this.shape=outline;
		currentPoint=new Point2D.Double();
		delta=new Point2D.Double();
		initializeReversablePath();
		setSpeed(speed);
	}

	/**
		 * creates the tracker given a path to move
	 * on and a speed to travel. If you want
	 * to make the sprite travel along the actual
	 * path instead of just in the shape of the
	 * path, you also need to set the original
	 * location of the Sprite moving to the current
	 * location of the OutlineTracker using the
	 * method getCurrentPoint().
	 * @param speed the portion of the screen to
	 * move per second
	 * @param vertices the location of the vertices
	 * along the path
	 */
	public OutlineTracker(double speed, double ... vertices)
	{
		currentPoint=new Point2D.Double();
		delta=new Point2D.Double();
		setShape(vertices);
		setSpeed(speed);
	}


	/**
	 * gets the current location along the outline.
	 * This method is useful to call if you want the
	 * Sprite that is moving to stay on the outline
	 * of the original Shape/Sprite.
	 * @return the location along the outline
	 */
	public Point2D.Double getCurrentPoint()
	{
		return new Point2D.Double(currentPoint.x, currentPoint.y);
	}

	/**
	 * makes a path around the Shape/Sprite such that
	 * the points can be traversed both forward and
	 * backward.  This is done in two primary ways.
	 * First, every SEG_CLOSE is replaced by a SEG_LINETO
	 * since closing would be different when traversing
	 * in the opposite direction.  Secondly, each SEG_MOVETO
	 * is replaced by a SEG_MOVETO and SEG_LINETO so that
	 * lines to close shapes actually draw the final closing
	 * line and not just move toward it.
	 */
	private void initializeReversablePath()
	{
		pointIndex=0;
		perimeter=0;
		Point2D.Double startPoint=new Point2D.Double();
		double[] curve=new double[6];
		PathIterator path;
		path=shape.getPathIterator(new AffineTransform(), 0.001);
		path.currentSegment(curve);
		currentPoint.x=curve[0];
		currentPoint.y=curve[1];
		startPoint.x=curve[0];
		startPoint.y=curve[1];

		ArrayList<Integer> pointsType=new ArrayList<Integer>();
		ArrayList<double[]> points=new ArrayList<double[]>();
		pointsType.add(SEG_MOVETO);
		points.add(new double[]{curve[0], curve[1]});
		pointsType.add(SEG_LINETO);
		points.add(new double[]{curve[0], curve[1]});
		path.next();
		Point2D.Double last=new Point2D.Double(curve[0], curve[1]);
		boolean fixReverse=false;
		while(!path.isDone())
		{
			int curveType=path.currentSegment(curve);
			if(curveType==SEG_CLOSE)
			{
				curve[0]=startPoint.x;
				curve[1]=startPoint.y;
				if(!fixReverse)
				{
					curveType=SEG_LINETO;
					fixReverse=true;
				}
				else
				{
					fixReverse=false;
					curveType=SEG_MOVETO;
				}
			}
			else if(curveType==SEG_MOVETO)
			{
				startPoint.x=curve[0];
				startPoint.y=curve[1];
				if(!fixReverse)
				{
					fixReverse=true;
				}
				else
				{
					fixReverse=false;
					curveType=SEG_LINETO;
				}
			}
			if(curveType==SEG_LINETO)
			{
				perimeter+=last.distance(curve[0], curve[1]);
			}
			last.x=curve[0];
			last.y=curve[1];
			pointsType.add(curveType);
			points.add(new double[]{curve[0], curve[1]});
			if(!fixReverse)
				path.next();
		}
		this.allPoints=new double[pointsType.size()][2];
		this.pointType=new int[pointsType.size()];
		for(int i=0; i<this.pointType.length; i++)
		{
			this.pointType[i]=pointsType.get(i);
			this.allPoints[i]=points.get(i);
		}
	}

	/**
	 * sets whether the tracker should stop moving
	 * once it goes all the way around the shape or
	 * if it should move continuously
	 * @param looping true indicates move continuously
	 * and false means stop when the shape have bee
	 * traversed.  Calling skipDistance can cause less
	 * than the entire shape to be traversed if looping
	 * is false.
	 */
	public void setLooping(boolean looping)
	{
		this.looping=looping;
	}

	/**
	 * tells whether the tracker should stop moving
	 * once it goes all the way around the shape or
	 * if it should move continuously
	 * @return true if the tracker moves continously
	 * or false if it stops once the shape has been
	 * traversed
	 */
	public boolean getLooping()
	{
		return looping;
	}

	/**
	 * gets the distance around the entire outline
	 * of the shape
	 * @return the perimeter of the shape
	 */
	public double getPathDistance()
	{
		return perimeter;
	}

	/**
	 * skips past a part of the outline.  If the
	 * amount skipped past moves past the end of the
	 * outline and looping is false, the tracker will
	 * continue moving around the shape again until it
	 * reaches the end.  Calling this method does not
	 * move the tracker, it just changes where it starts
	 * and alters the currrentPoint.
	 * @param distance the amount to skip.  This can
	 * be used in conjunction with getPathDistance.
	 */
	public void skipDistance(double distance)
	{
		double originalX=delta.x;
		double originalY=delta.y;
		advanceDistance(distance);
		delta.x=originalX;
		delta.y=originalY;
	}

	/**
	 * moves along the outline a given distance.
	 * Unlike shipDistance, this has the effect of
	 * moving the tracker and it changes currentPoint.
	 * @param distance the amount to move around
	 * the outline
	 */
	public void advanceDistance(double distance)
	{
		while(distance>0)
		{
			distance-=moveAlongCurrentLine(distance);
			if(distance>0)
			{
				if(!advancePointIndex())
					distance=0;
			}
		}
	}

	/**
	 * moves up to a given distance along the current
	 * line.  If the line length is less than the distance
	 * to travel, then the whole line is traversed.  If
	 * the line length is more than the distance to travel
	 * then the line is only partially traversed.
	 * @param distance the maximum amount to travel along
	 * the current line
	 * @return the actual amount traveled along the current
	 * line
	 */
	private double moveAlongCurrentLine(double distance)
	{
		double originalDistance=distance;
		if(pointType[pointIndex]==SEG_MOVETO)
		{
			delta.x+=allPoints[pointIndex][0]-currentPoint.x;
			delta.y+=allPoints[pointIndex][1]-currentPoint.y;
			currentPoint.x=allPoints[pointIndex][0];
			currentPoint.y=allPoints[pointIndex][1];
		}
		else
		{
			double lineDistance=
			    currentPoint.distance(
			        allPoints[pointIndex][0],
			        allPoints[pointIndex][1]);
			if(lineDistance<distance)
			{
				delta.x+=allPoints[pointIndex][0]-currentPoint.x;
				delta.y+=allPoints[pointIndex][1]-currentPoint.y;
				currentPoint.x=allPoints[pointIndex][0];
				currentPoint.y=allPoints[pointIndex][1];
				distance-=lineDistance;
			}
			else
			{
				double part=distance/lineDistance;
				delta.x+=part*(allPoints[pointIndex][0]-currentPoint.x);
				delta.y+=part*(allPoints[pointIndex][1]-currentPoint.y);
				currentPoint.x+=part*(allPoints[pointIndex][0]-currentPoint.x);
				currentPoint.y+=part*(allPoints[pointIndex][1]-currentPoint.y);
				distance=0;
			}
		}
		return originalDistance-distance;
	}

	/**
	 * moves the current pointIndex forward or
	 * backward depending on the speed and whether it
	 * is looping or not.
	 * @return true if there is a next point, false
	 * otherwise
	 */
	private boolean advancePointIndex()
	{
		boolean didAdvance=true;
		if(speed>0)
		{
			if(pointIndex<allPoints.length-1)
				pointIndex++;
			else if(looping)
				pointIndex=0;
			else
				didAdvance=false;
		}
		else
		{
			if(pointIndex>0)
				pointIndex--;
			else if(looping)
				pointIndex=allPoints.length-1;
			else
				didAdvance=false;
		}
		return didAdvance;
	}

	/**
	 * sets the shape to traverse
	 * @param sprite the sprite to trace the outline of
	 */
	public void setShape(Sprite sprite)
	{
		setShape(sprite.getShape());
	}

	/**
	 * sets the shape to traverse
	 * @param shape the shape to trace the outline of
	 */
	public void setShape(Shape shape)
	{
		this.shape=shape;
		initializeReversablePath();
	}

	public void setShape(double ... vertices)
	{
		GeneralPath path=new GeneralPath();
		path.moveTo((float)vertices[0], (float)vertices[1]);
		for(int i=2; i<vertices.length-1; i+=2)
			path.lineTo((float)vertices[i], (float)vertices[i+1]);
		shape=path;
		initializeReversablePath();
	}

	/**
	 * sets the speed of the tracker
	 * @param speed the speed of the tracker in
	 * screens per second
	 */
	public void setSpeed(double speed)
	{
		if(speed<0 && pointIndex==0)
			pointIndex=allPoints.length-1;
		if(speed>0 && pointIndex==allPoints.length-1)
			pointIndex=0;
		this.speed=speed;
	}

	/**
	 * sets the speed of the tracker
	 * @return the speed of the tracker in
	 * screens per second
	 */
	public double getSpeed()
	{
		return speed;
	}

	@Override
	public Point2D.Double getTranslation()
	{
		return delta;
	}

	@Override
	public void advanceTime(double time)
	{
		delta.x=0;
		delta.y=0;
		double distanceLeft=time*Math.abs(speed);
		advanceDistance(distanceLeft);
	}
}

