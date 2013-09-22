package fang;

import java.awt.Graphics;

import javax.swing.JFrame;

/**This class represents a frame which
 * can animate the background.
 * @author Jam Jenkins
 */
@SuppressWarnings("serial")
public class FunFrame 
    extends JFrame
{
    /**
     * constructs a default FunFrame
     */
    public FunFrame()
    {
        super();
    }
    
    /**
     * constructs a FunFrame with a given title
     * @param text the title of the FunFrame
     */
    public FunFrame(String text)
    {
        super(text);
    }

    /**
     * uses the FunPainter to draw the background
     * then uses the normal JFrame painting to
     * draw everything else
     * @param g the graphics used to draw
     */    
    public void update(Graphics g)
    {
        FunPainter.paint(g, getLocationOnScreen(),
                getSize(), 0);
        super.update(g);
    }
    
}
