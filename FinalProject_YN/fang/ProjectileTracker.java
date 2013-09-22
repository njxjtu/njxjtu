package fang;

import java.awt.geom.Point2D;

import fang.*;

/**
 * This class simulates 2D projectile motion, with or without gravity.
 * 
 * @author Jam Jenkins
 */
public class ProjectileTracker implements Tracker
{
    Point2D.Double translation;
    
    /** the instantaneous velocity in screens/second */
    Point2D.Double velocity;

    /** the angular velocity in radians/second */
    double angularVelocity;

    /** how much to rotate in radians */
    double rotate;

    /**
     * Creates a new instance of ProjectileTracker
     * 
     * @param p
     *            the starting position in screens
     * @param v
     *            the starting velocity in screens/second
     */
    public ProjectileTracker(Point2D.Double v)
    {
        translation=new Point2D.Double();
        velocity = new Point2D.Double(v.x, v.y);
    }

    /**
     * Creates a new instance of ProjectileTracker
     * 
     * @param x
     *            the horizontal velocity in screens/second
     * @param y
     *            the vertical velocity in screens/second
     */
    public ProjectileTracker(double x, double y)
    {
    	translation=new Point2D.Double();
    	velocity = new Point2D.Double(x, y);
    }
    
    /**
     * updates the position and/or velocity after a given time has passed
     * 
     * @param time
     *            the time passed in seconds
     */
    public void advanceTime(double time)
    {
        translation.x = time * velocity.x;
        translation.y = time * velocity.y;
        rotate = angularVelocity * time;
    }

    /**
     * set how much to rotate per time
     * 
     * @param radPerSecond
     *            the magnitude and direction of rotation around the object's
     *            center, in radians/second
     */
    public void setAngularVelocity(double radPerSecond)
    {
        angularVelocity = radPerSecond;
    }

    /**
     * gets the rotational velocity
     * 
     * @return the rotational velocity in radians/second
     */
    public double getAngularVelocity()
    {
        return angularVelocity;
    }

    /**
     * sets the direction of the velocity in radians while keeping the
     * maginitude of the velocity constant
     * 
     * @param angle
     *            the direction in radians
     */
    public void setVelocityDirection(double angle)
    {
        double magnitude = Math.sqrt(velocity.x * velocity.x + velocity.y
                * velocity.y);
        velocity.x = magnitude * Math.cos(angle);
        velocity.y = magnitude * Math.sin(angle);
    }

    /**
     * reflect as if off a wall with the given normal. This method keeps the
     * magnitude of the velocity unchanged and only changes the direction.
     * The 
     * @param normal
     *            the perpendicular to the flat surface in radians
     */
    public void bounce(double normal)
    {
        if (Double.isNaN(normal) ||
                Math.cos(normal)*velocity.x+Math.sin(normal)*velocity.y>=0)
            return;
        double velocityAngle = Math.atan2(velocity.y, velocity.x);
        setVelocityDirection(Math.PI + normal - velocityAngle
                + normal);
    }

    /**
     * determines the location of the Sprite
     * 
     * @return the location of the Sprite
     * @see fang.Tracker#getLocation()
     */
    public java.awt.geom.Point2D.Double getTranslation()
    {
        return translation;
    }

    /**
     * returns 1
     * 
     * @return one
     * @see fang.Tracker#getScaleFactor()
     */
    public double getScaleFactor()
    {
        return 1.0;
    }

    /**
     * returns the amount to rotate this time interval
     * 
     * @return the amount to rotate in radians
     * @see fang.Tracker#getRotationAddition()
     */
    public double getRotationAddition()
    {
        return rotate;
    }

    /**
     * determines the velocity in pixels/second
     * 
     * @return the velocity in pixels/second
     */
    public Point2D.Double getVelocity()
    {
        return new Point2D.Double(velocity.x, velocity.y);
    }

    /**
     * sets the velocity in pixels/second
     * 
     * @param v
     *            the (x, y) velocity in pixels/second
     */
    public void setVelocity(Point2D.Double v)
    {
        velocity.x = v.x;
        velocity.y = v.y;
    }
}
