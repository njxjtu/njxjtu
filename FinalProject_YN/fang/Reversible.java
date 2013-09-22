package fang;

/**This interface indicates
 * that a Tracker can advance the
 * time backwards as well as forwards.
 * What this means is that the parameter
 * passed to advanceTime can be either
 * negative or positive.
 * @author Jam Jenkins
 */
public interface Reversible
    extends Tracker
{
}
