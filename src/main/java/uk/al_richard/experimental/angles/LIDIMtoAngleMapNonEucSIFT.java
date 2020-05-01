package uk.al_richard.experimental.angles;

import coreConcepts.Metric;
import dataPoints.cartesian.CartesianPoint;

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
 * The table is created from the pivots                                                          <<<<<<<<<<<<<<<<
 * and calculates angles to points within some radius using whole data set for now.              <<<<<<<<<<<<<<<<
 * The table maps from local idim (using the points within the radius) to the angle and std dev.
 *
 */
public class LIDIMtoAngleMapNonEucSIFT extends CommonBase {

    public final static boolean printing = true;

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

    public LIDIMtoAngleMapNonEucSIFT(String dataset_name, int number_samples, int noOfRefPoints) throws Exception {

        super( dataset_name, number_samples, noOfRefPoints,0  );

        this.samples = super.getData();
        this.pivots = super.getRos();
        this.metric = super.getMetric();
        this.dim = super.getDim();
        query_radius = 100; // super.getThreshold();  248 is real 1%

        this.origin = new double[dim];
        this.centre = makePoint( 0.5 );
        this.centre_cartesian = new CartesianPoint(centre);
        this.origin_cartesian = new CartesianPoint(origin);

        map = createMap();
    }

    /**
     * Creates a map from LIDIM to angles by moving up the diagonal from [0,..0] to [1,..1]
     * @return a Map from LIDIM (calculated using pivots) to the angle and std dev.
     */
    public TreeMap<Double,Angles> createMap() throws Exception {

        TreeMap<Double,Angles> map = new TreeMap<>();

        if( printing ) {
            System.out.println("createMap() QR: " + query_radius + " pivots:" + pivots.size());
            System.out.println("d(ro,q)" + "\t" + "d(ro,soln)" + "\t" + "d(q,soln)" + "\t" + "theta");

        }

        for( CartesianPoint viewpoint : pivots ) {
            for( CartesianPoint query : pivots ) {
                if (viewpoint != query) {       // pntr equality is fine!
                    populateMap(map, viewpoint, query, query_radius);
                }
            }
        }

        return map;
    }

    /**
     * Tests some points (1000) drawn from the space and prints out the errors from ground truth compared with stored values in the map
     * @throws Exception
     */
    public void printTestPoints() throws Exception {

        System.out.println( "Query radius = " + query_radius + " dim = " + dim );

        for( int i = 1; i < 100; i++ ) {
            CartesianPoint p = samples.get(i);
            CartesianPoint q = samples.get(i+100);


            List<Double> list = getAngles( query_radius, p, q );

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
     * @param ro
     * @param query
     * @param some_point
     * @return the internal angle (ro,query,some_point) in RADIANS
     */
    private double calculateAngle( CartesianPoint ro, CartesianPoint query, CartesianPoint some_point ) {

        double d_ro_q =  metric.distance( ro,query );
        double d_q_soln = metric.distance( query,some_point );
        double d_ro_soln = metric.distance( ro,some_point );
        double theta = Math.acos( ( square(d_q_soln) + square(d_ro_q) - square(d_ro_soln) ) / (2 * d_q_soln * d_ro_q ) );

        if(printing) {
            System.out.println(df2.format(d_ro_q) + "\t" + df2.format(d_ro_soln) + "\t" + df2.format(d_q_soln) + "\t" + df2.format(Math.toDegrees(theta)));
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
     * @param viewpoint
     * @param query_radius - the query radius used to calculate angles
     * @throws Exception if there are no points found within the query_radius
     */
    private void populateMap(TreeMap<Double, Angles> map, CartesianPoint viewpoint, CartesianPoint query, double query_radius ) throws Exception {

        List<Double> list = getAngles( query_radius, viewpoint, query );  // TODO fold into below.

        int num_angles = list.size();

        // Calculate the local idim based on reference points.

        List<Double> dists = getDists(pivots, query.getPoint() );   // TODO look at folding this into above. CHANGED <<<<<<<
        double lidim = round( idim(dists), 2 );

        if( num_angles > 0 ) { // can only put entry in table if we have calculated some angles.

            double mean = (double) Util.mean(list);
            double std_dev = Util.stddev(list, mean);

            map.put( lidim, new Angles( mean, std_dev, num_angles ) );
        }
    }

    /**
     * @param query_radius
     * @param viewpoint
     * @param query
     * @return the angles in RADIANS from the origin to a point and then to random points within the query_radius of the point
     */
    private List<Double> getAngles( double query_radius, CartesianPoint viewpoint, CartesianPoint query ) {
        // Calculate all the angles to angle_calculation_repetitions points in the space within query_radius
        List<Double> list = new ArrayList<>();

        for( CartesianPoint sample_cartesian : samples ) {
                if( insideRadius(query,sample_cartesian) ) {
                    double theta = calculateAngle(viewpoint, query, sample_cartesian);
                    list.add(theta);
                }
        }
        if( list.size() >= angle_calculation_repetitions ) { // short circuit if we have enough already
            return list;
        }
        return list;
    }

    private boolean insideRadius(CartesianPoint query_point, CartesianPoint sample_cartesian) {
        return metric.distance( query_point, sample_cartesian ) < query_radius;
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
            System.out.println( "IDIM:" + idim + " mean = " + df2.format(Math.toDegrees(ang.angle)) + " std_dev = " + df2.format(Math.toDegrees(ang.std_dev)) + " degrees n = " + ang.angles_measured );
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

    private void testMap() throws Exception {

        System.out.println( "Printing map (" + dataset_name + ") ..." );
        print();
        System.out.println( "Testing map (" + dataset_name + ") ..." );
        printTestPoints();
    }

    //********************************* Main *********************************

    public static void main(String[] args) throws Exception {
        int number_samples = 999800;       // let's cut this down for brevity -  was 1M less 200
        int noOfRefPoints = 200;
        LIDIMtoAngleMapNonEucSIFT lam = new LIDIMtoAngleMapNonEucSIFT(SIFT, number_samples, noOfRefPoints);
        lam.testMap();
    }




}
