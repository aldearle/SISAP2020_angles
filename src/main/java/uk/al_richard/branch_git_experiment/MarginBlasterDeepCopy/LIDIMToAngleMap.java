package uk.al_richard.branch_git_experiment.MarginBlasterDeepCopy;


import eu.similarity.msc.core_concepts.Metric;
import eu.similarity.msc.data.cartesian.CartesianPoint;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;

import static uk.al_richard.branch_git_experiment.MarginBlasterDeepCopy.CircleGeometry.calculateMargin;
import static uk.al_richard.branch_git_experiment.MarginBlasterDeepCopy.CircleGeometry.calculateSafeRadius;
import static uk.al_richard.branch_git_experiment.MarginBlasterDeepCopy.Util.LIDimLevinaBickel;


/**
 *
 * Experiment to see if you can use local IDIM to work out what the angles are for an unseen query.
 * I used 200 reference points (noOfRefPoints) and measured local IDM using them and from this looked up a table of precalculated angles.
 * The calculated angles are compared with those looked up in the table.
 *
 * The table is created from the diagonal points (which we wouldn’t have in a real dataset)
 * and calculates angles to points within some radius (using the volume points)
 * (but which we could do in a real dataset).
 * The table maps from local IDIM Levina Bickel (using the points within the radius) to the angle and std dev.
 *
 * @author al@st-andrews.ac.uk
 *
 */
public class LIDIMtoAngleMap extends CommonBase {

    public final static boolean printing = false;

    private final Metric<CartesianPoint> metric;
    private final List<CartesianPoint> samples;
    private final List<CartesianPoint> pivots;
    private final double query_radius;
    private final int dim;

    private final static int angle_calculation_repetitions = 1000000;
    private final static Random rand  = new Random(8796253 ); // used for manual sample generation

    private static DecimalFormat df2 = new DecimalFormat("#.##");
    private static DecimalFormat df4 = new DecimalFormat("#.####");

    double[] centre;
    double[] origin;
    CartesianPoint centre_cartesian;
    CartesianPoint origin_cartesian;
    TreeMap<Double, Angles> map;

    public LIDIMtoAngleMap( String dataset_name, int number_samples, int noOfRefPoints ) throws Exception {

        super( dataset_name, number_samples, noOfRefPoints, 0 );

        this.samples = super.getData();
        this.pivots = super.getRos();
        this.metric = super.getMetric();
        this.dim = super.getDim();
        query_radius = super.getThreshold();

        this.origin = new double[dim];
        this.centre = makePoint( 0.5 );
        this.centre_cartesian = new CartesianPoint(centre);
        this.origin_cartesian = new CartesianPoint(origin);

        map = createMap();
    }


    /**
     * Creates a angle_map from LIDIM to angles by moving up the diagonal from [0,..0] to [1,..1]
     * @return a Map from LIDIM (calculated using pivots) to the angle and std dev.
     */
    public TreeMap<Double,Angles> createMap() throws Exception {

        TreeMap<Double,Angles> map = new TreeMap<>();

        for( int diagonal_distance = 1; ( diagonal_distance / 100 ) < Math.sqrt( dim ); diagonal_distance++ ) {
            populateMap( map, round( ((double) diagonal_distance ) / 100,2 ), query_radius );
        }

        return map;
    }

    /**
     *
     * @param p - a point (from a query) for which to calculate a safe smaller radius
     * @dists - an array of distances to reference points
     * @param threshold - the threshold of the query
     * @param factor -  how many std devs to add to the approximated safe minimum angle
     * @return an adjusted query radius.
     * @throws Exception if method cannot calculate local iDIMChavez
     */
    public double adjustedQueryRadius( CartesianPoint p, List<Double> dists, double threshold, double factor ) throws Exception {

        // Calculate the local iDIMChavez based on reference points.
        double lidim = LIDimLevinaBickel(dists);
        Angles stored_angles = findClosest(lidim, map);

        return calculateSafeRadius( stored_angles.angle, stored_angles.std_dev, threshold, factor );
    }

    /**
     *
     * @param p - a point (from a query) for which to calculate a safe smaller radius
     * @dists - an array of distances to reference points
     * @param threshold - the threshold of the query
     * @param factor -  how many std devs to add to the approximated safe minimum angle
     * @return the safety margin
     * @throws Exception if method cannot calculate local iDIMChavez
     */
    public double margin( CartesianPoint p, List<Double> dists, double threshold, double factor ) throws Exception {

        // Calculate the local iDIMChavez based on reference points.
        double lidim = LIDimLevinaBickel(dists);
        Angles stored_angles = findClosest(lidim, map);

        return calculateMargin( stored_angles.angle, stored_angles.std_dev, threshold, factor );
    }


    /**
     *
     * @param pivots
     * @param point
     * @return the distances from the point to the pivots
     */
    public List<Double> getDists(List<CartesianPoint> pivots, double[] point) {
        List<Double> dists = new ArrayList<>();

        CartesianPoint diagonal_point_cartesian = new CartesianPoint(point);

        for(int i = 0; i < pivots.size(); i++ ) {
            double d = metric.distance( pivots.get(i),diagonal_point_cartesian );
            dists.add(d);
        }
        return dists;
    }

    /**
     * Populates a angle_map mapping from LIDIm to Angles uses points within query_radius to calculate the angles.
     * @param map - the angle_map to be populated
     * @param diagonal_distance - the distance up the diagonal
     * @param query_radius - the query radius used to calculate angles
     * @throws Exception if there are no points found within the query_radius
     */
    private void populateMap(TreeMap<Double, Angles> map, double diagonal_distance, double query_radius ) throws Exception {

        double[] diagonal_point = getDiagonalPoint( diagonal_distance );

        List<Double> list = getAngles( query_radius, diagonal_point );  // <<<<<<<<<<<<<<<< PROBLEM ....

        int num_angles = list.size();

        // Calculate the local iDIMChavez based on reference points.

        List<Double> dists = getDists(pivots, diagonal_point);
        // double chav_idim = round( Util.iDIMChavez(dists), 2 );
        double lidim = round( LIDimLevinaBickel(dists), 2 );

        if( num_angles > 0 ) { // can only put entry in table if we have calculated some angles.

            double mean = (double) Util.mean(list);
            double std_dev = Util.stddev(list, mean);

            map.put( lidim, new Angles( mean, std_dev, num_angles ) );
        }
    }

    /**

     * @param query_radius
     * @param point
     * @return the angles in RADIANS from the origin to a point and then to random points within the query_radius of the point
     */
    private List<Double> getAngles( double query_radius, double[] point ) {
        // Calculate all the angles to angle_calculation_repetitions points in the space within query_radius
        List<Double> list = new ArrayList<>();
        CartesianPoint diagonal_point_cartesian = new CartesianPoint(point);
        for( int j = 0; j < angle_calculation_repetitions; j++ ) {
            CartesianPoint some_point_cartesian = new CartesianPoint( getRandomVolumePoint( point, query_radius ) );
            if( insideSpace( some_point_cartesian ) ) {
                double theta = calculateAngle(origin_cartesian, diagonal_point_cartesian, some_point_cartesian);
                list.add(theta);
            }
        }
        return list;
    }

    /**
     *
     * @param key
     * @param map
     * @return the closest entry to key in the angle_map
     */
    private Angles findClosest( double key, TreeMap<Double,Angles> map ) {
        Map.Entry<Double,Angles> low = map.floorEntry(key);
        Map.Entry<Double,Angles> high = map.ceilingEntry(key);
        Angles res = null;
        if (low != null && high != null) {
            res = Math.abs(key-low.getKey()) < Math.abs( key-high.getKey() )
                    ?   low.getValue()
                    :   high.getValue();
        } else if (low != null || high != null) {
            res = low != null ? low.getValue() : high.getValue();
        }
        return res;

    }

    /**
     * Round a value to n decimal places
     * @param value
     * @param places
     * @return
     */
    private static double round(double value, int places) {
        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

}

