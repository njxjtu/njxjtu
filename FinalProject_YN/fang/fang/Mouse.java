package fang;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.Externalizable;
import java.util.Observer;
import java.awt.geom.*;

/**
 * Stores the mouse positions and clicks 
 * for use in the gaming engine.
 * @author Jam Jenkins
 */
public class Mouse implements MouseListener, MouseMotionListener,
        Externalizable

{
    /**
     * used for serialization versioning
     */
    private static final long serialVersionUID = 1L;

    /** position of the mouse*/
	private Point2D.Double mousePosition;
    
    /** cached position of the mouse*/
    private Point2D.Double lastMousePosition;
        
    /** canvas for getting scaled coordinates*/
    private transient AnimationCanvas canvas;

	/** position of mouse click */
    private Point2D.Double mouseClick;

    /** position of left mouse click */
    private Point2D.Double leftClick;
    
    /** position of middle mouse click */
    private Point2D.Double middleClick;
    
    /** position of right mouse click */
    private Point2D.Double rightClick;

    /** whether mouse is down or not */
    private boolean mouseDown;

    /** observer to update on mouse changes*/
    private Observer observer;
    
    /** Creates a new instance of Mouse */
    public Mouse()
    {
        mouseDown = false;
        lastMousePosition=new Point2D.Double();
    }

    /**sets the canvas.  The size of the canvas
     * is used to scale the mouse position by the
     * inverse of the canvas size in order to make
     * the positions from (0, 0) to (1, 1).  If
     * the canvas is rectangular, it scales from
     * 0 to 1 on the short side and 0 to n on the
     * long side.
     * @param canvas the canvas used for scaling
     * the positions of the mouse
     */
    public void setCanvas(AnimationCanvas canvas)
    {
        this.canvas=canvas;
    }
    
    /**writes the position and clicking of the
     * mouse to the output stream.
     * @param out the output stream to write to
     */
    public void writeExternal(java.io.ObjectOutput out)
            throws java.io.IOException
    {
        out.writeBoolean(mouseDown);
        writePoint(out, mousePosition);
        writePoint(out, mouseClick);
        writePoint(out, leftClick);
        writePoint(out, middleClick);
        writePoint(out, rightClick);
    }

    /**writes a point to the output stream
     * @param out the output stream to write to
     * @param point the point to write
     * @throws java.io.IOException if an error
     * occurs with the networked connection
     */
    private void writePoint(java.io.ObjectOutput out, 
            Point2D.Double point)
            throws java.io.IOException
    {
        if (point == null)
        {
            out.writeBoolean(false);
        } else
        {
            out.writeBoolean(true);
            out.writeDouble(point.x);
            out.writeDouble(point.y);
        }
    }

    /**reads in the point from the input stream
     * @param in the input stream to read from
     * @param point the place to store the read point
     * @return the point read in
     * @throws java.io.IOException if there is an
     * error in the game's network connection
     */
    private Point2D.Double readPoint(java.io.ObjectInput in, 
            Point2D.Double point)
            throws java.io.IOException
    {
        boolean notNull = in.readBoolean();
        if (notNull)
        {
            if (point != null)
            {
                point.x = in.readDouble();
                point.y = in.readDouble();
                return point;
            }
            return new Point2D.Double(
                    in.readDouble(), 
                    in.readDouble());
        }
        return point;
    }

    /**reads in the mouse from the input stream.
     * @param in the input stream to read from
     */
    public void readExternal(java.io.ObjectInput in)
            throws java.io.IOException
    {
        mouseDown = in.readBoolean();
        mousePosition = readPoint(in, mousePosition);
        if(mousePosition!=null)
        {
            lastMousePosition.x=mousePosition.x;
            lastMousePosition.y=mousePosition.y;
            lastMousePosition.x=lastMousePosition.x;
            lastMousePosition.y=lastMousePosition.y;
        }
        mouseClick = readPoint(in, mouseClick);
        leftClick = readPoint(in, leftClick);
        middleClick = readPoint(in, middleClick);
        rightClick = readPoint(in, rightClick);
    }

    /** returns a string representation of the mouse position */
    public String toString()
    {
        return "Mouse at " + mousePosition;
    }

    /** clear all pending mouse events */
    public void clear()
    {
        mousePosition = null;
        mouseClick = null;
        leftClick = null;
        rightClick = null;
        middleClick = null;
        mouseDown = false;
    }

    /** clears all mouse clicks */
    public void clearClicks()
    {
        mouseClick = null;
        leftClick = null;
        rightClick = null;
        middleClick = null;        
    }
    
    /**
     * gets the last position of the mouse. 
     * Subsequent calls to getMousePosition
     * will return the same position until 
     * the mouse moves to a different
     * location.
     * 
     * @return the last position of the mouse
     */
    public Point2D.Double getLocation()
    {
        return new Point2D.Double(
                lastMousePosition.x,
                lastMousePosition.y);
    }
    
    /**converts the point on the screen to
     * the scaled point using the canvas size.
     * @param point the screen position
     * @return the relative position on the canvas
     */
    private Point2D.Double getPoint2D(Point point)
    {
        if(canvas==null)
            return new Point2D.Double();
        double min=Math.min(
                canvas.getWidth(),
                canvas.getHeight());
        Point2D.Double clipped=
            new Point2D.Double(
                point.x/min,
                point.y/min);
        if(canvas.getAspect()>1)
        {
            clipped.x=Math.min(canvas.getAspect(), 
                    clipped.x);
            clipped.y=Math.min(1, clipped.y);
        }
        else
        {
            clipped.x=Math.min(1, clipped.x);
            clipped.y=Math.min(1.0/canvas.getAspect(), 
                    clipped.y);            
        }
        clipped.x=Math.max(0, clipped.x);
        clipped.y=Math.max(0, clipped.y);
        return clipped;
    }

    /**
     * determines the last position of a 
     * click.
     * 
     * @return the last clicked position, 
     * null if not clicked since last frame advance
     */
    public Point2D.Double getClickLocation()
    {
        if(mouseClick==null)
            return null;
        return new Point2D.Double(
                mouseClick.x,
                mouseClick.y);
    }

    /**
     * determines the last position of a left 
     * click.
     * 
     * @return the last left clicked position, 
     * null if not clicked since last frame advance
     */
    public Point2D.Double getLeftClickLocation()
    {
        if(leftClick==null)
            return null;
        return new Point2D.Double(
                leftClick.x,
                leftClick.y);
    }

    /**
     * determines the last position of a 
     * middle click.
     * 
     * @return the last middle clicked position, 
     * null if not clicked since last frame advance
     */
    public Point2D.Double getMiddleClickLocation()
    {
        if(middleClick==null)
            return null;
        return new Point2D.Double(
                middleClick.x,
                middleClick.y);
    }

    /**
     * determines the last position of a right 
     * click.
     * 
     * @return the last right clicked position, 
     * null if not clicked since last frame advance
     */
    public Point2D.Double getRightClickLocation()
    {
        if(rightClick==null)
            return null;
        return new Point2D.Double(
                rightClick.x,
                rightClick.y);
    }

    /**determines if the mouse is current
     * pressed.  Clicking occurs when the
     * mouse is released.
     *  @return true if the mouse is currently 
     * down, false otherwise */
    public boolean pressed()
    {
        return mouseDown;
    }

    /**
     * stores the MouseEvent for later polling.
     * 
     * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
     */
    public void mouseClicked(java.awt.event.MouseEvent mouseEvent)
    {
    }

    /**
     * stores the MouseEvent for later polling.
     * 
     * @see java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent)
     */
    public void mouseDragged(java.awt.event.MouseEvent mouseEvent)
    {
        Point2D.Double point=getPoint2D(mouseEvent.getPoint());
        if(!point.equals(mousePosition))
        {
            mousePosition = point;
            observer.update(null, null);
        }
    }

    /**
     * stores the MouseEvent for later polling.
     * 
     * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
     */
    public void mouseEntered(java.awt.event.MouseEvent mouseEvent)
    {
    }

    /**
     * stores the MouseEvent for later polling.
     * 
     * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
     */
    public void mouseExited(java.awt.event.MouseEvent mouseEvent)
    {
    }

    /**
     * stores the MouseEvent for later polling.
     * 
     * @see java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
     */
    public void mouseMoved(java.awt.event.MouseEvent mouseEvent)
    {
        Point2D.Double point=getPoint2D(mouseEvent.getPoint());
        if(!point.equals(mousePosition))
        {
            mousePosition = point;
            observer.update(null, null);
        }
    }

    /**
     * stores the MouseEvent for later polling.
     * 
     * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
     */
    public void mousePressed(java.awt.event.MouseEvent mouseEvent)
    {
        mouseDown = true;
        Point point=mouseEvent.getPoint();
        mouseClick = getPoint2D(point);
        if (mouseEvent.getButton() == MouseEvent.BUTTON1)
        {
            leftClick = mouseClick;
        } else if (mouseEvent.getButton() == MouseEvent.BUTTON2)
        {
            middleClick = mouseClick;
        } else if (mouseEvent.getButton() == MouseEvent.BUTTON3)
        {
            rightClick = mouseClick;
        }
        observer.update(null, null);
    }

    /**
     * stores the MouseEvent for later polling.
     * 
     * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
     */
    public void mouseReleased(java.awt.event.MouseEvent mouseEvent)
    {
        mouseDown = false;
        observer.update(null, null);
    }
    
    /** sets the observer to update when the mouse changes*/
    public void setObserver(Observer observer)
    {
        this.observer=observer;
    }
}
