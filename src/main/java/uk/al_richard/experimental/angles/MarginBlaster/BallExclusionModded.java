package uk.al_richard.experimental.angles.MarginBlaster;

import searchStructures.ObjectWithDistance;
import searchStructures.Quicksort;

import java.util.List;
import java.util.Objects;

/**
 * @author newrichard
 * 
 * 
 * @param <T>
 *            the type of the values
 */
public class BallExclusionModded<T> extends ExclusionZoneModded<T> {

	private final RefPointSet<T> pointSet;
	private int ref;
	private double radius;
    private double min_witness = Double.MAX_VALUE; // easily checked to be wrong if unset
    private double max_witness = Double.MAX_VALUE; // easily checked to be wrong if unset
    private double lower_median_quartile = Double.MAX_VALUE; // easily checked to be wrong if unset
    private double upper_median_quartile = Double.MAX_VALUE; // easily checked to be wrong if unset


    public BallExclusionModded(RefPointSet<T> pointSet, int ref, double radius) {
		this.pointSet = pointSet;
		this.setRef(ref);
		this.radius = radius;
	}

    public void setWitnesses(List<T> witnesses) {
        ObjectWithDistance<Integer>[] owds = new ObjectWithDistance[witnesses.size()];
        for (int i = 0; i < witnesses.size(); i++) {
            double d1 = getPointSet().extDist(witnesses.get(i), this.getRef());
            owds[i] = new ObjectWithDistance<>(0, d1);
        }
        Quicksort.placeMedian(owds);
        this.radius = owds[owds.length / 2].getDistance();
        //
        // LinearInterpolator li = new LinearInterpolator();
        // PolynomialSplineFunction sp = li.interpolate(null, null);
        // PolynomialFunction[] x = sp.getPolynomials();
    }

	public void setWitnessesQuartiles(List<T> witnesses) {
		ObjectWithDistance<Integer>[] owds = new ObjectWithDistance[witnesses.size()];
		for (int i = 0; i < witnesses.size(); i++) {
			double d1 = getPointSet().extDist(witnesses.get(i), this.getRef());
			owds[i] = new ObjectWithDistance<>(i, d1);   //i was zero
		}
		Quicksort.sort(owds); // placeMedian

        int quartile_position = owds.length / 4;
        int median_position = owds.length / 2;

		this.min_witness = owds[0].getDistance();
		this.max_witness = owds[owds.length-1].getDistance();
		this.radius = owds[median_position].getDistance();

		this.lower_median_quartile = owds[quartile_position].getDistance();
		this.upper_median_quartile = owds[median_position + quartile_position].getDistance();
	}

    public double setWitnessesPercentage(List<T> witnesses, int percentage_position) {
        ObjectWithDistance<Integer>[] owds = new ObjectWithDistance[witnesses.size()];
        for (int i = 0; i < witnesses.size(); i++) {
            double d1 = getPointSet().extDist(witnesses.get(i), this.getRef());
            owds[i] = new ObjectWithDistance<>(i, d1);   //i was zero
        }
        Quicksort.sort(owds); // placeMedian

        this.radius = owds[ ( percentage_position * witnesses.size() ) / 100 ].getDistance();
        return this.radius;
    }

	/**
	 * @return the radius
	 */
	public double getRadius() {
		return radius;
	}

	/**
	 * @param radius the radius to set
	 */
	public void setRadius(double radius) {
		this.radius = radius;
	}

	/**
	 * @return true if the point is to the left of a defined partition
	 * 
	 *         this is a bit weird because of the single-distance calculation
	 *         optimisation...
	 */
	@Override
	public boolean isIn(double[] dists) {
		return dists[this.getRef()] < this.radius;
	}

	@Override
	public boolean mustBeIn(double[] dists, double t) {
		return dists[this.getRef()] < this.radius - t;
	}

	@Override
	public boolean mustBeOut(double[] dists, double t) {
		return dists[this.getRef()] >= this.radius + t;
	}

	@Override
	public boolean mustBeInMargin(double[] dists, double t, double m)  {
		return dists[this.getRef()] < this.radius + m - t;
	}

	@Override
	public boolean mustBeOutMargin(double[] dists, double t, double m) {
			return dists[this.getRef()] >= this.radius + t - m;
	}

	public Integer getIndex() {
		return getRef();
	}

	public RefPointSet<T> getPointSet() {
		return pointSet;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof BallExclusionModded)) return false;
		BallExclusionModded<?> that = (BallExclusionModded<?>) o;
		return getRef() == that.getRef();
	}

	@Override
	public int hashCode() {

		return Objects.hash(getRef());
	}

	public int getRef() {
		return ref;
	}

	public void setRef(int ref) {
		this.ref = ref;
	}
}
