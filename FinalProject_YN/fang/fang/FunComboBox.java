package fang;

import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JComboBox;

/**This class represents a text field which
 * can animate the background.
 * @author Jam Jenkins
 */
@SuppressWarnings("serial")
public class FunComboBox 
    extends JComboBox
{
    /**
     * constructs a text field with a given starting
     * size and no text
     * @param size the starting size
     */
    public FunComboBox(Dimension size)
    {
        this(new String[]{}, size);
    }
    
    /**
     * constructs a text field with a given starting
     * size and text
     * @param text the original characters in the text field
     * @param size the starting size
     */
    public FunComboBox(String[] items, Dimension size)
    {
        super(items);
        setOpaque(false);
        FunPainter.setProperties(size, this);
    }
        
    /**
     * draws the animated background and then
     * the foreground using the JTextField
     * @param g the graphics used to draw
     */
    public void paintComponent(Graphics g)
    {
        if(this.isShowing())
            FunPainter.paint(
            		g, getLocationOnScreen(),
                getSize(), -1);
        setOpaque(false);
        super.paintComponent(g);
        setOpaque(true);
    }
}
