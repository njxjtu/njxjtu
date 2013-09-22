package fang;

import java.awt.geom.Point2D.Double;
import java.util.HashSet;

import fang.Tracker;


/**This class combines several trackers
 * into a single tracker by composing
 * the scaling, rotating, and translating.
 * Since these operations are commutative,
 * they compose seamlessly.
 * @author Jam Jenkins
 */
public final class CompositeTracker implements Tracker
{
    /**all trackers*/
    private HashSet<Tracker> all=
        new HashSet<Tracker>();
    
    /**
     * makes the CompositeTracker and
     * adds all of the arguments to the
     * list of trackers
     * @param tracker the trackers to compose
     */
    public CompositeTracker(Tracker ... tracker)
    {
        for(Tracker t: tracker)
            all.add(t);
    }
    
    /**gets all of the Trackers in this collection
     * of Trackers
     * @return all the trackers in this group of trackers
     */
    public Tracker[] getAllTrackers()
    {
        return all.toArray(new Tracker[0]);
    }
    
    /**
     * adds one tracker to compose.  The
     * tracker being added must create a
     * cycle of Trackers.  For example, adding
     * this tracker to itself would cause
     * infinite recursion.  Be sure to avoid
     * more complex cyclic structures as well.
     * CompositeTracker
     * @param tracker the tracker to compose
     */
    public void addTracker(Tracker tracker)
    {
        all.add(tracker);
    }

    /**
     * removes the tracker from the list
     * of trackers composed.  Trying to
     * remove non-exitent trackers is ignored.
     * @param tracker the tracker to remove
     */
    public void removeTracker(Tracker tracker)
    {
        all.remove(tracker);
    }
    
    /**
     * composes the trackers' translations
     * @return the sum of the translations
     */
    public Double getTranslation()
    {
        Double translate=new Double();
        translate.x=0;
        translate.y=0;
        for(Tracker tracker: all)
        {
            Double current=tracker.getTranslation();
            translate.x+=current.x;
            translate.y+=current.y;
        }
        return translate;
    }

    /**composes the trackers' scaling factors
     * @return the product of all scale factors
     */
    public double getScaleFactor()
    {
        double factor=1;
        for(Tracker tracker: all)
        {
            factor*=tracker.getScaleFactor();
        }
        return factor;
    }

    /**composes the trackers' rotation additions
     * @return the sum of all rotation additions
     */
    public double getRotationAddition()
    {
        double addition=0;
        for(Tracker tracker: all)
        {
            addition+=tracker.getRotationAddition();
        }
        return addition;
    }
    
    /**does nothing.  The AnimationCanvas
     * takes care of advancing the time of
     * all the trackers contained in this tracker.
     * @param timePassed the duration of time
     * since the last call to advanceTime
     */
    public void advanceTime(double timePassed)
    {
    }
}
