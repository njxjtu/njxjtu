package fang;

/**
 * Used to enable timed events.
 * Classes which implement Alarm have
 * the alarm method called for them at
 * a particular time as specified by
 * the scheduleRelative and scheduleAbsolute
 * methods in the AnimationCanvas.
 * @author Jam Jenkins
 * 
 */
public interface Alarm
{
	/** indicates the action to take
     * after the specified period of time 
     */
	public void alarm();
}
