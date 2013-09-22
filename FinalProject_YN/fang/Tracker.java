package fang;

import java.awt.geom.Point2D;

/**
 * provides a mechanism for altering the size, 
 * location, orientation and other
 * aspects of a Sprite between each frame.
 * 
 * @author Jam Jenkins
 */
public interface Tracker
{
    /**
     * determines the amount to move.
     * This method must return the same
     * value every time until the next
     * time the advanceTime method is called. 
     * 
     * @return the absolute location of the Sprite
     */
    Point2D.Double getTranslation();

    /**
     * determines the relative scaling factor. 
     * This method will take the current
     * size of the Sprite and scale it by the 
     * factor returned.
     * This method must return the same
     * value every time until the next
     * time the advanceTime method is called. 
     * 
     * @return the multiplicative scaling factor
     */
    double getScaleFactor();

    /**
     * determines the relative rotation. This 
     * method will take the current
     * orientation and rotate it by the value returned.
     * This method must return the same
     * value every time until the next
     * time the advanceTime method is called. 
     * 
     * @return the amount to rotate in radians
     */
    double getRotationAddition();

    /**
     * called every time time is advanced. 
     * The amount of time which has passed
     * since the last call.  This method
     * must also accept negative time intervals
     * for reversing time.  The magnitude of
     * the negative time interval will not
     * exceed the magnitude of the last
     * positive time interval.  Also, the
     * keyboard and mouse inputs will remain
     * constant when the interval is negative.
     * @param timePassed the time in seconds since
     * the last frame
     * 
     */
    void advanceTime(double timePassed);
}
