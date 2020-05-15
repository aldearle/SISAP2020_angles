package uk.al_richard.experimental.angles;

import coreConcepts.Metric;
import dataPoints.cartesian.CartesianPoint;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;

import static uk.al_richard.experimental.angles.CircleGeometry.calculateMargin;

/**
 *
 * Experiment to see if you can use local IDIM to work out what the angles are for an unseen query.
 * I used 200 reference points (noOfRefPoints) and measured local IDM using them and from this looked up a table of precalculated angles.
 * The calculated angles are compared with those looked up in the table.
 * The table is created from the pivots                                                          <<<<<<<<<<<<<<<<
 * and calculates angles to points within some radius using whole data set for now.              <<<<<<<<<<<<<<<<
 * The table maps from local iDIMChavez (using the points within the radius) to the angle and std dev.
 *
 */
public class LIDIMtoAngleMapNonEucSIFT extends CommonBase {

    public final static boolean printing = true;

    private final Metric<CartesianPoint> metric;
    private final List<CartesianPoint> samples;
    private final List<CartesianPoint> pivots;
    private final List<CartesianPoint> references;

    private final double query_radius;
    private final int dim;

    private final static int angle_calculation_repetitions = 10000; // the maximum number of angles to use when getting angles
    private static int num_test_points = 1000;  // number of points used for testing.


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
        List<CartesianPoint> all_ros = super.getRos();
        // split pivots into two sets - references to build map and pivots.
        int half = all_ros.size() / 2;
        this.references = all_ros.subList(0,half);
        pivots = all_ros.subList(half,all_ros.size());

        this.metric = super.getMetric();
        this.dim = super.getDim();
        query_radius = super.getThreshold();

        this.origin = new double[dim];
        this.centre = makePoint( 0.5 );
        this.centre_cartesian = new CartesianPoint(centre);
        this.origin_cartesian = new CartesianPoint(origin);

        map = createMap();
        System.out.println( "Map created size: " + map.size() );
    }

    /**
     * Creates a map from LIDIM to angles by moving up the diagonal from [0,..0] to [1,..1]
     * @return a Map from LIDIM (calculated using pivots) to the angle and std dev.
     */
    public TreeMap<Double,Angles> createMap() throws Exception {

        TreeMap<Double,List<Angles>> map_list = new TreeMap<>();

        if( printing ) {
            System.out.println("createMap() QR: " + query_radius + " pivots:" + pivots.size());
            System.out.println("d(ro,q)" + "\t" + "d(ro,soln)" + "\t" + "d(q,soln)" + "\t" + "theta");
        }

        for( CartesianPoint viewpoint : references ) {
            for( CartesianPoint query : references ) {
                if (viewpoint != query) {       // pntr equality is fine!
                    populateMap(map_list, viewpoint, query, query_radius);
                    if( map_list.size() == 500 ) {
                        System.out.println( "Map populated - size (500) achieved (breaking)" );
                        return condense( map_list );
                    }
                }
            }
        }

        return condense( map_list );
    }

    /**
     * Averages the lists in the map and returns a map from lidim to angles.
     * @param map_list - a map from lidim to lists of angles
     * @return  a map from lidim to angles
     */
    private TreeMap<Double, Angles> condense(TreeMap<Double, List<Angles>> map_list) {
        TreeMap<Double,Angles> new_map = new TreeMap<>();

        for( double key : map_list.keySet() ) {
            List<Angles> list = map_list.get(key);
            new_map.put( key, average(list) );
        }
        return new_map;
    }

    private Angles average(List<Angles> list) {
        double angle_sum = 0;
        double std_dev_sum = 0;
        int angles_measured_sum = 0;

        for( Angles next : list ) {
            int weight = next.angles_measured;
            angle_sum = angle_sum + ( next.angle * weight );
            std_dev_sum = std_dev_sum + ( next.std_dev * weight );
            angles_measured_sum = angles_measured_sum + weight;
        }
        return new Angles( angle_sum / angles_measured_sum, std_dev_sum / angles_measured_sum, angles_measured_sum );
    }


    /**
     * Tests some points (1000) drawn from the space and prints out the errors from ground truth compared with stored values in the map
     * @throws Exception
     */
    public void printTestPoints() throws Exception {

        System.out.println( "Query radius = " + query_radius + " dim = " + dim );

        for( int i = 1; i < num_test_points ; i++ ) {
            CartesianPoint p = samples.get(i);
            CartesianPoint q = samples.get(i+num_test_points);

            List<Double> real_angles = getAngles( query_radius, p, q );

            int num_angles = real_angles.size();

            // Calculate the local IDIM based on reference points.
            List<Double> dists = getDists(pivots, p.getPoint());  // dists of p to pivots (this is the real angles in sphere in other version)
            double lidim = Util.LIDimLevinaBickel(dists);

            if( num_angles > 0 ) { // can only put entry in table if we have calculated some angles.

                double mean_rad = Util.mean(real_angles);
                double std_dev_rad = Util.stddev(real_angles, mean_rad);

                Angles stored_angles = findClosest(lidim, map);

                double margin = calculateMargin( mean_rad, std_dev_rad, query_radius, 3.0 );

                System.out.println( i + ": real angle = " + df4.format(Math.toDegrees(mean_rad)) + " real std dev = " +  df4.format(Math.toDegrees(std_dev_rad)) +
                        " stored angle = " + df4.format( Math.toDegrees(stored_angles.angle)) + " stored std dev = " + df4.format( Math.toDegrees(stored_angles.std_dev)) +
                        " angle error = " + showError(mean_rad,stored_angles.angle) + " std dev error = " + showError(std_dev_rad,stored_angles.std_dev) );
            }
        }
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
    private void populateMap(TreeMap<Double, List<Angles>> map, CartesianPoint viewpoint, CartesianPoint query, double query_radius ) throws Exception {

        List<Double> list = getAngles( query_radius, viewpoint, query );  // TODO fold into below.

        int num_angles = list.size();

        // Calculate the local iDIM based on reference points.

        List<Double> dists = getDists( pivots, query.getPoint() );   // TODO look at folding this into above. CHANGED <<<<<<<
        double lidim = round( Util.LIDimLevinaBickel(dists), 4 );

        if( num_angles > 0 ) { // can only put entry in table if we have calculated some angles.

            double mean = (double) Util.mean(list);
            double std_dev = Util.stddev(list, mean);

            List<Angles> l = map.get(lidim);
            if( l == null ) {
                l = new ArrayList<>();
            }
            l.add( new Angles(mean, std_dev, num_angles) );
            map.put(lidim,l );

            if( printing ) {
                System.out.println("Adding map entry: lidim: " + lidim + " mean ang: " + Math.toDegrees(mean) + " std_dev: " +  Math.toDegrees(std_dev) + " entries: " + map.size() );
            }
        }
    }

    /**
     * @param query_radius
     * @param viewpoint
     * @param query
     * @return the angles in RADIANS from the origin to a point and then to random points within the query_radius of the point
     */
    private List<Double> getAngles( double query_radius, CartesianPoint viewpoint, CartesianPoint query ) {
        // Calculate all the angles in the space
        List<Double> list = new ArrayList<>();

        for( CartesianPoint sample_cartesian : samples ) {
            if( sample_cartesian != query ) {               // Protect against using same point for both
                if( insideRadius( query, sample_cartesian ) ) {
                    double theta = calculateAngle(viewpoint, query, sample_cartesian);
                    list.add(theta);
                }
            }
            if( list.size() >= angle_calculation_repetitions ) {
                return list;
            }
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
     * @return the error between measured and stored in degrees
     */
    private String showError(double measured, double stored) {
        return df4.format( Math.toDegrees( Math.abs(measured - stored) ) );
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
        int noOfRefPoints = 2000; // two sets of noOfRefPoints / 2 each.
        int number_samples = 1000000 - noOfRefPoints;

        LIDIMtoAngleMapNonEucSIFT lam = new LIDIMtoAngleMapNonEucSIFT(SIFT, number_samples, noOfRefPoints);
        lam.testMap();
    }




}
