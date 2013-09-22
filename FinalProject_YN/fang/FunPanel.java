package fang;

import java.awt.Graphics;
import java.awt.LayoutManager;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JPanel;

/**This class represents a frame which
 * can animate the background.
 * @author Jam Jenkins
 */
@SuppressWarnings("serial")
public class FunPanel extends JPanel
{
    /**timer used for updating the spiraling
     * background
     */
    private Timer timer;
    
    /**the timers set to go off*/
    private static final LinkedList<Timer> allTimers=
        new LinkedList<Timer>();
    
    /**makes a default FunPanel*/
    public FunPanel()
    {
        super();
        setOpaque(true);
        timer=new Timer();
        allTimers.add(timer);
        timer.scheduleAtFixedRate(new PaintMe(), 0, 100);
    }

    /**cancels all animation in the background*/
    public static void stopUpdating()
    {
        for(Timer time: allTimers)
            time.cancel();
    }
    
    /**
     * creates a FunPanel with a given LayourManager
     * @param layout the LayoutManager for this FunPanel
     */
    public FunPanel(LayoutManager layout)
    {
        super(layout);
        setOpaque(true);
        timer=new Timer();
        allTimers.add(timer);
        timer.scheduleAtFixedRate(new PaintMe(), 0, 40);
    }
        
    /**
     * draws all of the components within the container
     * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
     */
    public void paintComponent(Graphics g)
    {
        if(isShowing())
            FunPainter.paint(g, 
                    getLocationOnScreen(),
                    getSize(), 0);
        setOpaque(false);
        super.paintComponent(g);
        setOpaque(true);
    }    
    
    /**This class repaints the FunPanel.
     * @author Jam Jenkins
     */
    class PaintMe extends TimerTask
    {
        /**invalidates and repaints the FunPanel*/
        public void run()
        {
            invalidate();
            repaint();
        }
    }
}
