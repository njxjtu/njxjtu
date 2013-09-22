package fang;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;

import javax.swing.ImageIcon;

public class FunPainter
{
    /**the image to draw in the background*/
    private static final Image image=
        new ImageIcon(FunPainter.class.getResource("resources/back.jpg")).getImage();

    /**the maximum radius when spiraling*/
    private static final double MAX_RADIUS=1000;
        
    /**the last draw time*/
    private static long lastTime=System.currentTimeMillis();
    
    /**the current spiral angle*/
    private static double angle=0;
    
    /**the current radius*/
    private static double radius=MAX_RADIUS;
    
    /**the speed to move*/
    private static double speed=100;
    
    /**how fast to go in and out per second*/
    private static int growth=1;
    
    /**sets the font size and color properly
     * @param size the size of the window or canvas
     * @param component the component on which to set the font
     */
    public static void setProperties(
            Dimension size,
            Component component)
    {
        component.setForeground(Color.YELLOW);
        Font font=new Font(null, Font.BOLD, Math.min(size.width, size.height)/16);
        component.setFont(font);
    }
    
    /**draws the spiraling background
     * @param brush the graphics to draw with
     * @param position the position of the component
     * @param size how big the component is
     * @param fade how much to fade out
     */
    public static void paint(
            Graphics brush, 
            Point position, 
            Dimension size,
            int fade)
    {
        long time=System.currentTimeMillis();
        long duration=time-lastTime;
        lastTime=time;
        angle+=growth*speed/radius*duration/1000.0;
        radius+=growth*10*duration/1000.0;
        if(radius>MAX_RADIUS && growth==1) growth=-1;
        if(radius<-MAX_RADIUS && growth==-1) growth=1;
        int width=image.getWidth(null);
        int height=image.getHeight(null);
        int columns=size.width/width+3;
        int rows=size.height/height+3;
        int offsetX=(int)((Math.sin(angle))*radius)%width;
        offsetX=(offsetX+width)%width;
        int offsetY=(int)((Math.cos(angle))*radius)%height;
        offsetY=(offsetY+height)%height;
        for(int x=0; x<columns; x++)
        {
            for(int y=0; y<rows; y++)
            {
                brush.drawImage(image, 
                        -(position.x%width)+x*width-offsetX, 
                        -(position.y%height)+y*height-offsetY, 
                        null);
            }
        }
        if(fade<0)
        {
            brush.setColor(new Color(1.0f, 1.0f, 1.0f, 0.2f));
            brush.fillRect(0, 0, width*columns, height*rows);
        }
        if(fade>0)
        {
            brush.setColor(new Color(0.0f, 0.0f, 0.0f, 0.4f));
            brush.fillRect(0, 0, width*columns, height*rows);            
        }
    }
}
