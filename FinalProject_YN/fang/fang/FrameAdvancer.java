package fang;

import java.awt.Dimension;
import java.util.LinkedList;
import java.util.TreeMap;

/**
 * used to advance the frame in an animation 
 * using the AnimationCanvas. There are two 
 * basic parametes to the FrameAdvancer: the 
 * model frame rate and the screen frame rate. 
 * The model frame rate describes the number 
 * of frames per second which must be computed 
 * to have a consistent model. This in effect
 * determines the maximum time for computations 
 * between displays. The screen frame rate is 
 * how often the screen is refreshed with the 
 * current Sprites. The screen rate is never 
 * higher than the model rate.
 * 
 * @author Jam Jenkins
 */

public abstract class FrameAdvancer 
    extends GameWindow
    implements AlarmScheduler
{
    /** keeps track of the screen refresh rate */
    private LinkedList<Double> refreshTimes;

    /** the canvas which displays the Sprites */
    protected AnimationCanvas canvas;

    /** the current time, used in updateInterval method */
    private double currentTime;
    
    /** offset used for restarting the game*/
    private double timeOffset=0;

    /** time since the last call to updateInterval */
    public double timeInterval;

    /** the highest allowable time between calls to update */
    private double maxModelTimeInterval = 1 / 10.0;

    /** all pending alarms */
    private TreeMap<Double, LinkedList<Alarm>> alarms = 
        new TreeMap<Double, LinkedList<Alarm>>();

    /** initializes the canvas to empty and no alarms set */
    public FrameAdvancer()
    {
        super();
        canvas = new AnimationCanvas();
        canvas.removeAllSprites();
        alarms.clear();
        refreshTimes = new LinkedList<Double>();
        for (int i = 0; i < 100; i++)
            refreshTimes.add(0.0);        
    }

    /** initializes the canvas to size and no alarms set */
	public FrameAdvancer(Dimension size)
    {
        super();
        canvas = new AnimationCanvas(size);
        canvas.removeAllSprites();
        alarms.clear();
        refreshTimes = new LinkedList<Double>();
        for (int i = 0; i < 100; i++)
            refreshTimes.add(0.0);
    }
    
    /**
     * set canvas to a new canvas
     * @param canvas
     */
	public void setCanvas(AnimationCanvas canvas)
    {
        this.canvas = canvas;
    }

    /**
     * returns the canvas
     * @return canvas
     */
	public AnimationCanvas getCanvas()
    {
        return canvas;
    }

    /** called repeatedly between every model update */
    public void advanceFrame(double timePassed)
    {

    }

    /**
     * returns the current time in seconds
     * since the beginning of this game.  When the
     * game starts over, so does the time.
     * @return the current time in seconds
     */
    public double getTime()
    {
        return currentTime-timeOffset;
    }

    /**
     * returns the screen refresh rate 
     * @return ScreenRefreshRate
     */
	public double getScreenRefreshRate()
    {
        return (refreshTimes.size()-1)/(refreshTimes.getLast() - refreshTimes.getFirst());
    }

    /**
     * sets the minimum number of frames which 
     * must be computed per second. This
     * is separate from the screen frame rate 
     * in that more frames can be computed 
     * than displayed, but it can be affected 
     * by (and affect) the screen rate. Too 
     * high of a model frame rate can cause 
     * computation to slow down the display 
     * rate, and too low of a model frame rate 
     * can cause important events to be missed 
     * (typically the intersection of Sprites).
     * Too high a model frame rate can slow 
     * down the advancing of time to slower
     * than physical time. Frame rates higher 
     * than 200 Hz are not allowed.
     * 
     * @param framesPerSecond
     *            the number of frames computed per second
     */
    public void setMinimumModelFrameRate(int framesPerSecond)
    {
        if (framesPerSecond > 0)
        {
            maxModelTimeInterval = Math.max(1.0 / 200, 1.0 / framesPerSecond);
        }
    }

    /**
     * sets and alarm to go off relative to 
     * the current time. For example, this
     * method can be used to set off an alarm 
     * in 5 seconds from the current time.
     * 
     * @param alarm
     *            the class to call the alarm method on
     * @param relative
     *            the time from now in seconds to call the alarm method
     */
    public void scheduleRelative(Alarm alarm, double relative)
    {
        scheduleAbsolute(alarm, relative+currentTime-timeOffset);
//        double absolute=currentTime + relative;
//        LinkedList<Alarm> existing = 
//            alarms.get(absolute);
//        if(existing==null)
//            existing=new LinkedList<Alarm>();
//        existing.add(alarm);
//        alarms.put(absolute, existing);        
    }

    /**
     * sets and alarm to go off at a time 
     * relative to the beginning of time
     * (zero). For example, this method can 
     * be used to set off an alarm 30
     * seconds from the beginning time.
     * 
     * @param alarm
     *            the class to call the alarm method on
     * @param absolute
     *            the time in seconds to call the alarm method
     */
    public void scheduleAbsolute(Alarm alarm, double absolute)
    {
        LinkedList<Alarm> existing = 
            alarms.get(absolute+timeOffset);
        if(existing==null)
            existing=new LinkedList<Alarm>();
        existing.add(alarm);
        alarms.put(absolute+timeOffset, existing);
    }

    /**
     * removes all pending alarms on this object. 
     * If there are no alarms with
     * this object as the target, the method call is ignored.
     * 
     * @param alarm
     *            the object that is the target of a pending alarm
     */
    public void cancelAlarm(Alarm alarm)
    {
        for (LinkedList<Alarm> list: alarms.values())
        {
            if (list.contains(alarm))
            {
                list.remove(alarm);
            }
        }
    }

    /**
     * removes all pending alarms. 
     * If there are no pending alarms, 
     * the method call is ignored.
     */
    public void cancelAllAlarms()
    {
        alarms.clear();
    }

    /**gets the list of alarms scheduled to go
     * off in the future.
     * @return the array of alarms in the order
     * which they would go off
     */
    public Alarm[] getAlarms()
    {
    	LinkedList<Alarm> all=new LinkedList<Alarm>();
    	for(LinkedList<Alarm> list: alarms.values())
    		all.addAll(list);
        return all.toArray(new Alarm[0]);
    }
    
    /**
     * updates the model between displays. 
     * This method can split the time passed
     * if more time has passed since the 
     * last display than the maximum time
     * interval allowable for the model frame rate.
     * @param time the current absolute time
     */
    public void updateModel(double time)
    {
        double timeInterval = time - currentTime;
        while (timeInterval > 0)
        {
            double timeToNextAlarm=Double.MAX_VALUE;
            if(!alarms.isEmpty())
            {
                timeToNextAlarm=alarms.firstKey()-currentTime;
            }
            double advanced = Math.min(timeInterval, maxModelTimeInterval);
            advanced=Math.min(advanced, timeToNextAlarm);
            if(alarms.size()>0 &&
                    currentTime-alarms.firstKey()==advanced)
            {
                LinkedList<Alarm> alarmsToSetOff=
                    alarms.remove(alarms.firstKey());
                for(Alarm alarm: alarmsToSetOff)
                {
                    alarm.alarm();
                }
            }
            canvas.updateSprites(advanced);
            currentTime += advanced;
            timeInterval -= advanced;
            advanceFrame(advanced);
            postAdvanceFrame(advanced);
        }
    }
    
    /**sets the cursor for the game engine
     * @param filename the image to display
     * as the cursor */
    public void setCursor(String filename)
    {
        canvas.setCursor(getClass().getResource(filename));
    }
    
    /**removes the cursor from the game canvas*/
    public void removeCursor()
    {
        canvas.removeCursor();
    }
    
    /**adds the default cursor back to the screen*/
    public void restoreCursor()
    {
    	canvas.restoreCursor();
    }

    /**called after advanceFrame
     * @param timePassed the duration since the last frame
     */
    public abstract void postAdvanceFrame(double timePassed);
    
    /**
     * updates the AnimationCanvas, should only be called from the AWTEvent
     * Thread.
     */
    public void refreshScreen()
    {
        refreshTimes.removeFirst();
        refreshTimes.addLast(currentTime);
        canvas.paintImmediately();
        controlPanel.repaint();
    }
    
    /**
     * makes the current time zero
     */
    public void resetTime()
    {
        timeOffset=currentTime;
    }
}
