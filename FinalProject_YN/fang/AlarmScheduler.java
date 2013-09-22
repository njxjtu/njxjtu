package fang;

/**
 * This interface contains all of the
 * methods for scheduling and cancelling
 * alarms.  The FrameAdvancer implements
 * this interface, and by extension, so
 * too does the GameLoop and GameLevel.
 * @author Jam Jenkins
 */
public interface AlarmScheduler
{
    /**
     * sets and alarm to go off relative to the current time. For example, this
     * method can be used to set off an alarm in 5 seconds from the current
     * time.
     * 
     * @param alarm
     *            the class to call the alarm method on
     * @param relative
     *            the time from now in seconds to call the alarm method
     */
    public void scheduleRelative(Alarm alarm, double relative);

    /**
     * sets and alarm to go off at a time relative to the beginning of time
     * (zero). For example, this method can be used to set off an alarm 30
     * seconds from the beginning time.
     * 
     * @param alarm
     *            the class to call the alarm method on
     * @param absolute
     *            the time in seconds to call the alarm method
     */
    public void scheduleAbsolute(Alarm alarm, double absolute);
    
    /**
     * removes all pending alarms on this object. If there are no alarms with
     * this object as the target, the method call is ignored.
     * 
     * @param alarm
     *            the object that is the target of a pending alarm
     */
    public void cancelAlarm(Alarm alarm);

    /**
     * removes all pending alarms. If there are no pending alarms, the method
     * call is ignored.
     * 
     */
    public void cancelAllAlarms();

}
