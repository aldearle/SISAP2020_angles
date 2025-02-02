package uk.al_richard.experimental.angles;


import coreConcepts.Metric;
import dataPoints.cartesian.CartesianPoint;
import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;
import org.apache.commons.math3.fitting.PolynomialCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoints;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;

import static uk.al_richard.experimental.angles.CircleGeometry.calculateMargin;
import static uk.al_richard.experimental.angles.CircleGeometry.calculateSafeRadius;
import static uk.al_richard.experimental.angles.Util.LIDimLevinaBickel;

/**
 *
 * Experiment to see if you can use local IDIM to work out what the angles are for an unseen query.
 * I used 200 reference points (noOfRefPoints) and measured local IDM using them and from this looked up a table of precalculated angles.
 * The calculated angles are compared with those looked up in the table.
 *
 * The table is created from the diagonal points (which we wouldn’t have in a real dataset)
 * and calculates angles to points within some radius (using the volume points)
 * (but which we could do in a real dataset).
 * The table maps from local iDIM (using the points within the radius) to the angle and std dev.
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

    private TreeMap<Double, Angles> map;
    private PolynomialFunction fit_func;
    private double[] coefficients;

    public LIDIMtoAngleMap( String dataset_name, int number_samples, int noOfRefPoints ) throws Exception {

        super( dataset_name, number_samples, noOfRefPoints,0  );

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
        fit(map);
    }

    /**
     * Creates a angle_map from LIDIM to angles by moving up the diagonal from [0,..0] to [1,..1]
     * @return a Map from LIDIM (calculated using pivots) to the angle and std dev.
     */
    public TreeMap<Double,Angles> createMap() throws Exception {

        map = new TreeMap<Double,Angles>();

        for( int diagonal_distance = 1; ( diagonal_distance / 100 ) < Math.sqrt( dim ); diagonal_distance++ ) {
            populateMap( map, round( ((double) diagonal_distance ) / 100,2 ), query_radius );
        }

        return map;
    }

    /**
     * Tests some points (1000) drawn from the space and prints out the errors from ground truth compared with stored values in the angle_map
     * @throws Exception
     */
    public void printTestPoints() throws Exception {

        System.out.println( "Query radius = " + query_radius + " dim = " + dim );

        for( int i = 1; i < 100; i++ ) {
            CartesianPoint p = samples.get(i);

            List<Double> real_angles = getAngles( 0.25, p.getPoint() ); // the real angles in the volume around p radius specified.

            int num_angles = real_angles.size();

            // Calculate the local iDIMChavez based on reference points.
            List<Double> dists = getDists(pivots, p.getPoint());
            double lidim = LIDimLevinaBickel(dists);
            System.out.println( i + " Pivot based IDIM = " + lidim );

            if( num_angles > 0 ) { // can only put entry in table if we have calculated some angles.

                double mean_rad = Util.mean(real_angles);
                double std_dev = Util.stddev(real_angles, mean_rad);

                Angles fitted_angles = getFitted(lidim);
                Angles mapped_angles = findClosest(lidim,map);

                double margin = calculateMargin( mean_rad, std_dev, query_radius, 3.0 );

                double mean_degrees = Math.toDegrees(mean_rad);
                double std_dev_degrees = Math.toDegrees(std_dev);

                System.out.println( i + ": real angle = " + df4.format(mean_degrees) +
                        " fitted angle = " + df4.format( Math.toDegrees(fitted_angles.angle ) ) +
                        " mapped angle = " + df4.format( Math.toDegrees( mapped_angles.angle ) ) );


//                System.out.println( i + ": real angle = " + df4.format(mean_degrees) + " real std dev = " +  df4.format(std_dev_degrees) +
//                        " stored angle = " + df4.format( Math.toDegrees(fitted_angles.angle)) + " stored std dev = " + df4.format( Math.toDegrees(fitted_angles.std_dev)) +
//                        " angle error = " + showError(mean_rad,fitted_angles.angle) + " std dev error = " + showError(std_dev,fitted_angles.std_dev) );
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
     * @param p - a point for which we are getting an estimated angle
     * @return the angle and std dev estimated for the point p.
     * @throws Exception if something breaks
     */
    public Angles getEstimatedAngle( CartesianPoint p ) throws Exception {
        List<Double> dists = getDists( pivots, p.getPoint() );
        double lidim = LIDimLevinaBickel(dists);
        return findClosest(lidim, map);
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
     *
     * @param map               - the angle_map to be populated
     * @param diagonal_distance - the distance up the diagonal
     * @param query_radius      - the query radius used to calculate angles
     * @throws Exception if there are no points found within the query_radius
     */
    private void populateMap(TreeMap<Double, Angles> map, double diagonal_distance, double query_radius ) throws Exception {

        double[] diagonal_point = getDiagonalPoint( diagonal_distance );

        List<Double> list = getAngles( query_radius, diagonal_point );  // <<<<<<<<<<<<<<<< PROBLEM for general non Euclidian spaces

        int num_angles = list.size();

        // Calculate the local iDIM based on reference points.

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
     * Displays the angle_map
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

        // System.out.println( "Printing angle_map (" + dataset_name + ") ..." );
        // print();
        System.out.println( "Testing angle_map (" + dataset_name + ") ..." );
        printTestPoints();
    }

    /**
     * Fit a polynomial to the observed angles
     * Creates the polynomial coefficients - y = nx2 + mx + c in coefficients [n=2,m=1,c=0]
     * @param map - a map from Lidim to angle (in radians)
     */
    private void fit(TreeMap<Double, Angles> map) {
        final WeightedObservedPoints obs = new WeightedObservedPoints();

        for( double key : map .keySet()) {

            double x = key;
            Angles angles = map.get(key);
            double y = angles.angle;
            //double y1 = y + angles.std_dev;
            //double y2 = y - angles.std_dev;

            obs.add(x, y);
            //obs.add(x, y1);
            //obs.add(x, y2);
        }

        PolynomialCurveFitter fitter = PolynomialCurveFitter.create(1);
        // fitter creates an array of the form: { 12.9, -3.4, 2.1 } which represents the polynomial 12.9 - 3.4 x + 2.1 x^2
        coefficients = fitter.fit(obs.toList());
        fit_func = new PolynomialFunction(coefficients);

        StringBuilder res = new StringBuilder();

         for (int i = coefficients.length - 1; i >= 0; i--) {
            res.append(coefficients[i]).append(":");
        }
        System.out.println(res.substring(0, res.length() - 1));
    }

    /**
     * @param lidim
     * @return the calculated angle based on idim and the calculated coefficients of fit.
     */
    private Angles getFitted(double lidim) {

        double result = fit_func.value(lidim);
        // this does this (for arbitrary coeff): result = coefficients[0] + ( lidim * coefficients[1] ) + ( lidim * lidim * coefficients[2] );

        return new Angles( result,0, 0 ); // don't know std dev or number - probably should return a double but like this for compatibility.
    }


    //********************************* Main *********************************

    public static void main(String[] args) throws Exception {
        int number_samples = 5000; // 998000; // 1M less 200
        int noOfRefPoints = 10; // 200;
        LIDIMtoAngleMap lam = new LIDIMtoAngleMap(EUC20, number_samples, noOfRefPoints);
        lam.testMap();
    }

    public static void main1(String[] args) throws Exception {
        int number_samples =  10000; // 999800; // 1M less 200
        int noOfRefPoints = 400;

        for( String dataset_name : eucs ) {

            LIDIMtoAngleMap lam = new LIDIMtoAngleMap(dataset_name, number_samples, noOfRefPoints);
            lam.testMap();

        }
    }
}
