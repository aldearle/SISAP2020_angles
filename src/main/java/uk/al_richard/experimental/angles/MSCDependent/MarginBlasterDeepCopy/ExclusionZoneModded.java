package uk.al_richard.experimental.angles.MSCDependent.MarginBlasterDeepCopy;

/**
 * @author richard
 *
 * @param <T> the type of values being modelled
 * 
 *        an ExclusionZone implements a binary partition of a metric space; not
 *        necessarily a contiguous region, based on a set of reference points
 * 
 *        this is in the context where many zones are created from a fixed set
 *        of reference object distances, hence the array of distances passed in
 *        to each function; the individual zone probably relies on only one or
 *        two of these distances
 * 
 *        mustBeIn and mustBeOut are clumsy names implying that the object from
 *        which the distances have been calculated is greater than the threshold
 *        t from the boundary; thus for example mustBeIn(ds,0) is the same as
 *        isIn(ds)
 */
public abstract class ExclusionZoneModded<T> {

	abstract public boolean isIn(double[] dists);

	abstract public boolean mustBeIn(double[] dists, double t);

	abstract public boolean mustBeOut(double[] dists, double t);

	public abstract boolean mustBeInMargin(double[] dists, double t, double m);

	public abstract boolean mustBeOutMargin(double[] dists, double t, double m);
}
