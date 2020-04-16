package uk.al_richard.experimental.angles;

import coreConcepts.Metric;
import dataPoints.cartesian.CartesianPoint;
import dataPoints.cartesian.Euclidean;
import testloads.TestContext;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;

import static uk.al_richard.experimental.angles.CircleGeometry.calculateMargin;
import static uk.al_richard.experimental.angles.CircleGeometry.calculateSafeRadius;
import static uk.al_richard.experimental.angles.Util.idim;
import static uk.al_richard.experimental.angles.Util.square;

/**
 *
 * Experiment to see if you can use local IDIM to work out what the angles are for an unseen query.
 * I used 200 reference points (noOfRefPoints) and measured local IDM using them and from this looked up a table of precalculated angles.
 * The calculated angles are compared with those looked up in the table.
 *
 * The table is created from the diagonal points (which we wouldn’t have in a real dataset)
 * and calculates angles to points within some radius (using the volume points)
 * (but which we could do in a real dataset).
 * The table maps from local idim (using the points within the radius) to the angle and std dev.
 *
 */
public class LIDIMtoAngleMap {

    public final static boolean printing = false;

    private final static int dim = 20;
    private final static int angle_calculation_repetitions = 1000000;
    private final static double query_radius = 0.25;
    private final static Random rand  = new Random(8796253 ); // was used for  manual sample generation

    private static DecimalFormat df2 = new DecimalFormat("#.##");
    private static DecimalFormat df4 = new DecimalFormat("#.####");


    double[] centre;
    double[] origin;
    CartesianPoint centre_cartesian;
    CartesianPoint origin_cartesian;
    Metric<CartesianPoint> metric;
    List<CartesianPoint> samples;
    List<CartesianPoint> pivots;
    TreeMap<Double, Angles> map;

    public LIDIMtoAngleMap( List<CartesianPoint> samples, List<CartesianPoint> pivots ) throws Exception {
        this.pivots = pivots;
        this.samples = samples;

        this.origin = new double[this.dim];
        this.centre = makePoint( 0.5 );
        this.centre_cartesian = new CartesianPoint(centre);
        this.origin_cartesian = new CartesianPoint(origin);
        this.metric = new Euclidean<>();

        map = createMap();
    }

    /**
     * Creates a map from LIDIM to angles by moving up the diagonal from [0,..0] to [1,..1]
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
     * Tests some points (100) drawn from the space and prints out the errors from ground truth compared with stored values in the map
     * @throws Exception
     */
    public void printTestPoints() throws Exception {

        System.out.println( "Query radius = " + query_radius + " dim = " + dim );

        for( int i = 1; i < 100; i++ ) {
            CartesianPoint p = samples.get(i);

            List<Double> list = getAngles( 0.25, p.getPoint() );

            int num_angles = list.size();

            // Calculate the local idim based on reference points.
            List<Double> dists = getDists(pivots, p.getPoint());
            double lidim = idim(dists);

            if( num_angles > 0 ) { // can only put entry in table if we have calculated some angles.

                double mean_rad = Util.mean(list);
                double std_dev = Util.stddev(list, mean_rad);

                Angles stored_angles = findClosest(lidim, map);

                double margin = calculateMargin( mean_rad, std_dev, query_radius, 3.0 );

                double mean_degrees = Math.toDegrees(mean_rad);
                double std_dev_degrees = Math.toDegrees(std_dev);

                System.out.println( i + ": real angle = " + df4.format(mean_degrees) + " real std dev = " +  df4.format(std_dev_degrees) +
                        " stored angle = " + df4.format( Math.toDegrees(stored_angles.angle)) + " stored std dev = " + df4.format( Math.toDegrees(stored_angles.std_dev)) +
                        " angle error = " + showError(mean_rad,stored_angles.angle) + " std dev error = " + showError(std_dev,stored_angles.std_dev) );
            }
        }
    }

    /**
     *
     * @param p - a point (from a query) for which to calculate a safe smaller radius
     * @dists - an array of distances to reference points
     * @param threshold - the threshold of the query
     * @param factor -  how many std devs to add to the approximated safe minimum angle
     * @return an adjusted query radius.
     * @throws Exception if method cannot calculate local idim
     */
    public double adjustedQueryRadius( CartesianPoint p, List<Double> dists, double threshold, double factor ) throws Exception {

        // Calculate the local idim based on reference points.
        // dists = getDists(pivots, p.getPoint());
        double lidim = idim(dists);
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
     * @throws Exception if method cannot calculate local idim
     */
    public double margin( CartesianPoint p, List<Double> dists, double threshold, double factor ) throws Exception {

        // Calculate the local idim based on reference points.
        // dists = getDists(pivots, p.getPoint());
        double lidim = idim(dists);
        Angles stored_angles = findClosest(lidim, map);

        return calculateMargin( stored_angles.angle, stored_angles.std_dev, threshold, factor );
    }



    /**
     * @return a random point within radius of the midpoint specified
     */
    private double[] getRandomVolumePoint( double[] midpoint, double radius ) {
        double[] res = new double[this.dim];
        double[] temp = new double[this.dim + 2];
        double acc = 0;
        for (int i = 0; i < this.dim + 2; i++) {
            double d = this.rand.nextGaussian();
            acc += d * d;
            temp[i] = d;
        }
        double magnitude = Math.sqrt(acc);   // the magnitude of the vector
        for (int i = 0; i < this.dim; i++) {
            res[i] = ( temp[i] / magnitude * radius ) + midpoint[i];
        }
        return res;
    }

    /**
     * @param distance_from_o - this distance from the origin
     * @return a point on the diagonal that distance from the origin
     */
    private double[] getDiagonalPoint(double distance_from_o ) {

        double coordinate = Math.sqrt( Math.pow(distance_from_o,2) / dim );
        return makePoint( coordinate );
    }

    /**
     * @param coordinate a vlue used to initialise the coordinates
     * @return a point in dim space with all the coordinates equal to coordinate
     */
    private double[] makePoint( double coordinate ) {
        double[] point = new double[this.dim];
        for (int i = 0; i < dim; i++) {
            point[i] = coordinate;
        }
        return point;
    }

    /**
     *
     * @param pivot
     * @param query
     * @param some_point
     * @return the internal angle (pivot,query,some_point) in RADIANS
     */
    private double calculateAngle( CartesianPoint pivot, CartesianPoint query, CartesianPoint some_point ) {

        double dpq =  metric.distance( pivot,query );
        double dqpi = metric.distance( query,some_point );
        double p1pi = metric.distance( pivot,some_point );

        double theta = Math.acos( ( square(dqpi) + square(dpq) - square(p1pi) ) / (2 * dqpi * dpq ) );

        if(printing) {
            System.out.println(df2.format(dpq) + "\t" + df2.format(p1pi) + "\t" + df2.format(dqpi) + "\t" + df2.format(Math.toDegrees(theta)));
        }

        return theta;
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
     * Populates a map mapping from LIDIm to Angles uses points within query_radius to calculate the angles.
     * @param map - the map to be populated
     * @param diagonal_distance - the distance up the diagonal
     * @param query_radius - the query radius used to calculate angles
     * @throws Exception if there are no points found within the query_radius
     */
    private void populateMap(TreeMap<Double, Angles> map, double diagonal_distance, double query_radius ) throws Exception {

        double[] diagonal_point = getDiagonalPoint( diagonal_distance );

        List<Double> list = getAngles( query_radius, diagonal_point );

        int num_angles = list.size();

        // Calculate the local idim based on reference points.

        List<Double> dists = getDists(pivots, diagonal_point);
        double lidim = round( idim(dists), 2 );

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
     * @param some_point_cartesian
     * @return true of the point is within [0,..0] and [1,..1]
     */
    private boolean insideSpace(CartesianPoint some_point_cartesian) {
        double[] point = some_point_cartesian.getPoint();
        for( int i = 0; i < point.length; i++ ) {
            if( point[i] < 0.0 || point[i] > 1.0 ) { // inclusive?
                return false;
            }
        }
        return true;
    }

    /**
     *
     * @param measured
     * @param stored
     * @return the error between measured and stored
     */
    private String showError(double measured, double stored) {
        double diff = measured - stored;
        return df4.format( Math.abs(diff)/stored );
    }

    /**
     *
     * @param key
     * @param map
     * @return the closest entry to key in the map
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
     * Displays the map
     */
    private void print() {
        for( Double idim : map.keySet() ) {
            Angles ang = map.get(idim);
            System.out.println( "IDIM:" + idim + " mean = " + df2.format(ang.angle) + " std_dev = " + df2.format(ang.std_dev) + " degrees n = " + ang.angles_measured );
        }
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

    private static void testMap() throws Exception {

        int number_samples = 1000000;
        int noOfRefPoints = 200;
        TestContext.Context context = TestContext.Context.euc20;
        TestContext tc = new TestContext(context,number_samples );
        tc.setSizes(0, noOfRefPoints);

        LIDIMtoAngleMap sae = new LIDIMtoAngleMap(tc.getData(),tc.getRefPoints());

        System.out.println( "Printing map..." );
        sae.print();
        System.out.println( "Testing map..." );
        sae.printTestPoints();
    }

    //********************************* Main *********************************

    public static void main(String[] args) throws Exception {
        testMap();
    }
}
