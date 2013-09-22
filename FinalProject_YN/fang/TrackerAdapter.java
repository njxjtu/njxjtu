package fang;

import java.awt.geom.*;

import fang.Tracker;


/**provides a default implementation of the
 * Tracker interface methods except for
 * advanceTime
 * @author Jam Jenkins
 */
public abstract class TrackerAdapter implements Tracker
{

    /**
     * returns (0, 0)
     * 
     * @see fang.Tracker#getTranslation()
     */
    public Point2D.Double getTranslation()
    {
        return new Point2D.Double();
    }

    /**
     * returns 1
     * 
     * @see fang.Tracker#getScaleFactor()
     */
    public double getScaleFactor()
    {
        return 1;
    }

    /**
     * returns 0.  This method calls getRotationAdditionDegrees
     * and converts the result from degrees to radians.
     * If you want a non-zero value returned, you can override
     * this method.  Override this method, the 
     * getRotationAdditionDegrees method, or the 
     * getRotationAdditionRevolutions method, but not 
     * a combination of the three.
     * @return the radians to add
     * @see fang.Tracker#getRotationAddition()
     */
    public double getRotationAddition()
    {
        return Math.toRadians(getRotationAdditionDegrees());
    }

    /**
     * returns 0.  This method may be overridden to return
     * a non-zero number of degrees to add.  
     * Override this method, the 
     * getRotationAddition method, or the 
     * getRotationAdditionRevolutions method, but not 
     * a combination of the three.
     * @return the degrees to add
     */
    public double getRotationAdditionDegrees()
    {
    	return getRotationAdditionRevolutions()*Math.PI*2;
    }

    /**
     * returns 0.  This method may be overridden to return
     * a non-zero number of revolutions to add.  
     * Override this method, the 
     * getRotationAdditionDegrees method, or the 
     * getRotationAddition method, but not 
     * a combination of the three.
     * @return the degrees to add
     */
    public double getRotationAdditionRevolutions()
    {
    	return 0;
    }
    
    /**
     * this method is called between each frame.
     * The time parameter is the amount of time
     * which has pased since the last frame, which
     * is typically about 1/26 of a second.
     * 
     * @param time
     *            the amount of time passed since
     *            the last call to this method
     */
    public abstract void advanceTime(double time);
}
