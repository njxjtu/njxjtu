package fang;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import javax.swing.JButton;

/**This class represents a button which
 * can animate the background and also
 * resize the font with changes in the
 * button size.
 * @author Jam Jenkins
 */
@SuppressWarnings("serial")
public class FunButton 
    extends JButton
    implements ComponentListener
{    
    /**the previous width of the button*/
    private int oldWidth=-1;
    
    /**
     * constructs a button with a given size and no text
     * @param size
     */
    public FunButton(Dimension size)
    {
        this("", size);
    }
           
    /**
     * constructs a button with a given size and text
     * @param text the characters to appear on the button
     * @param size the starting size of the button
     */
    public FunButton(String text, Dimension size)
    {
        super(text);
        this.setBackground(Color.RED);
        this.setMargin(new Insets(1, 1, 1, 1));
        this.setOpaque(true);
        FunPainter.setProperties(size, this);
        this.addComponentListener(this);

    }
        
    /**
     * uses the FunPainter to draw the background
     * then uses the normal Button painting to
     * draw everything else
     * @param g the graphics used to draw
     */
    public void paintComponent(Graphics g)
    {
        if(oldWidth<0)
        {
            oldWidth=getSize().width;
        }
        if(isShowing())
            FunPainter.paint(g, getLocationOnScreen(),
                getSize(), 1);
        super.paintComponent(g);
        setOpaque(false);
    }

    /**
     * tracks changes in size in order to change
     * the font size
     * @param arg0 not used
     */
    public void componentResized(ComponentEvent arg0)
    {
        if(oldWidth<0) return;
        Font font=getFont();
        double scaleX=getSize().width/(double)oldWidth;
        oldWidth=getSize().width;
        font=font.deriveFont((float)(scaleX*font.getSize2D()));
        setFont(font);
    }

    /**
     * does nothing
     * @param arg0 ignored
     */
    public void componentMoved(ComponentEvent arg0)
    {        
    }

    /**
     * does nothing
     * @param arg0 ignored
     */
    public void componentShown(ComponentEvent arg0)
    {
    }

    /**
     * does nothing
     * @param arg0 ignored
     */
    public void componentHidden(ComponentEvent arg0)
    {
    }
}
