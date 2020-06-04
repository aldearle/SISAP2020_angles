package uk.al_richard.experimental.angles;

import coreConcepts.Metric;
import dataPoints.cartesian.CartesianPoint;
import util.OrderedList;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static uk.al_richard.experimental.angles.Util.square;

public class EpsilonSort extends CommonBase {

    private final double thresh;

    private final Metric<CartesianPoint> metric;

    private final List<CartesianPoint> data_points;
    private final List<CartesianPoint> queries;
    private final List<CartesianPoint> ref_points;
    private final int data_size;

    private final Map<Integer,OrderedList<Integer,Double>> ro_map;
    private final int dim;

//    private static double theta_euc_20 = 1.3172;            // TODO HARD CODED theta_low for EUC20
//    private static double std_dev_angle_euc_20 = 0.2141519; // TODO HARD CODED std dev for EUC20
//    private final double theta;
    private final LIDIMtoAngleMap ldim_to_angle_map;

    public EpsilonSort(String dataset_name, int number_data_points, int noOfRefPoints, int num_queries ) throws Exception {

        super( dataset_name, number_data_points, noOfRefPoints,num_queries  );

        this.thresh = super.getThreshold();

        System.out.println( "thresh = " + thresh );

//        this.theta = theta_euc_20 + (4 * std_dev_angle_euc_20);  // Making this bigger gets more exclusions (but more errors)

        ldim_to_angle_map = new LIDIMtoAngleMap(dataset_name, number_data_points, noOfRefPoints );

        this.data_points = getData();
        this.data_size = data_points.size();
        this.ref_points = getRos();
        this.queries = getQueries();
        this.metric = getMetric();
        this.dim = getDim();
        ro_map = new HashMap<>();
        initDists();

    }

    /************* private methods *************/

    private void initDists() {

        for (int i = 0; i < ref_points.size(); i++) {
            CartesianPoint pivot = ref_points.get(i);

            OrderedList<Integer, Double> dists = getDists(pivot);
            ro_map.put( i,dists );
        }
    }

    /**
     * @param pivot
     * @return the distances from the pivots to all the points.
     */
    private OrderedList<Integer, Double> getDists(CartesianPoint pivot) {

        OrderedList<Integer,Double> dists = new OrderedList<>(data_size); // list of data indexes and distances

        for(int i = 0; i < data_size ; i++ ) {

            double d = metric.distance( pivot,data_points.get(i) );
            dists.add(i,d);
        }

//        comps = dists.getComparators();
//        indices  = dists.getList();
//        for(int i = 0; i < 10 ; i++ ) {
//
//        }
        return dists;
    }

    private void experiment() throws Exception {
        for( int i = 0; i < queries.size(); i++ ) {
            CartesianPoint query_point = queries.get(i);
            query( query_point );
        }

    }

    private void query(CartesianPoint query_point) throws Exception {

        HashSet<Integer> exclude = new HashSet<>();
        HashSet<Integer> include = new HashSet<>();
        for( int i = 0; i < ro_map.size(); i++ ) {

            Angles angles = ldim_to_angle_map.getEstimatedAngle( query_point );

            HashSet<Integer> ro_exclude = new HashSet<>();
            HashSet<Integer> ro_include = new HashSet<>();

            CartesianPoint ref_object = ref_points.get(i);

            System.out.println( "RO " + i + " d: " + metric.distance(query_point,ref_object) + " angle: " + angles.angle + " std.dev: " + angles.std_dev );
            queryROExclude(query_point, angles, ro_map.get(i), ref_object, ro_exclude, exclude );
            queryROInclude(query_point, angles, ro_map.get(i), ref_object, ro_include, include );
            check(query_point,include,exclude);

        }
        System.out.println( "*** " );
        check(query_point,include,exclude);
    }

    private void check(CartesianPoint query_point, HashSet<Integer> include, HashSet<Integer> exclude) {

        int include_true_positive = 0;
        int include_false_positive = 0;
        int exclude_true_positive = 0;
        int exclude_false_positive = 0;
        int true_inclusions = 0;

        for( int i = 0; i < data_points.size(); i++ ) {

            double d_q_s = metric.distance( data_points.get(i), query_point);

            if( d_q_s < thresh ) {
                true_inclusions++;
            }
        }

        for( int i : exclude) {
            double d_q_s = metric.distance( data_points.get(i), query_point);
            if( d_q_s > thresh ) {
                exclude_true_positive++;
            }  else {
                exclude_false_positive++;
            }
        }

        for( int i : include) {
            double d_q_s = metric.distance( data_points.get(i), query_point);
            if( d_q_s < thresh ) {
                include_true_positive++;
            } else {
                include_false_positive++;
            }

        }

        System.out.println( "Exclusions TP = " + exclude_true_positive + " Exclusions FP = " + exclude_false_positive + " True exclusions = " + ( data_points.size() - true_inclusions ) );
        System.out.println( "Inclusions TP = " + include_true_positive + " Inclusions FP = " + include_false_positive + " True inclusions = " + true_inclusions);
        System.out.println( "---");
    }

    private void queryROExclude(CartesianPoint query_point, Angles angles, OrderedList<Integer, Double> data_dists, CartesianPoint ro_object, HashSet<Integer> ro_exclude, HashSet<Integer> exclude) {

        double d_ro_q = metric.distance(ro_object, query_point);

        List<Double> dists = data_dists.getComparators();
        List<Integer> indices = data_dists.getList();

        for( int i = dists.size() - 1; i >= 0; i-- ) {

            Double d_ro_s = dists.get(i);

            boolean gt_thresh = estimateGTThreshold( d_ro_q, d_ro_s, angles.angle  -  (2 * angles.std_dev));
            if( gt_thresh ) {
                exclude.add( indices.get(i) );
                ro_exclude.add( indices.get(i) );
            }

            if( ! gt_thresh ) {
                return;
            }
        }
    }

    private void queryROInclude(CartesianPoint query_point, Angles angles, OrderedList<Integer, Double> data_dists, CartesianPoint ro_object, HashSet<Integer> ro_include, HashSet<Integer> include) {

        double d_ro_q = metric.distance(ro_object, query_point);

        List<Double> dists = data_dists.getComparators();
        List<Integer> indices = data_dists.getList();

        for( int i = 0; i < dists.size(); i++) {

            Double d_ro_s = dists.get( i );

            boolean lt_thresh = estimateLTThreshold( d_ro_q, d_ro_s, angles.angle + (2 * angles.std_dev));
            if( lt_thresh ) {
                include.add( indices.get(i) );
                ro_include.add( indices.get(i) );
            }

            if( ! lt_thresh ) {
                return;
            }
        }
    }

    private boolean estimateLTThreshold(double d_ro_q, double d_ro_s, double theta) {
        double epsilon = predictEpsilon(d_ro_q, d_ro_s, theta);
        double d_q_s_estimate = Math.sqrt( square(d_ro_s) + square(d_ro_q) - ( 2.0d * d_ro_q * d_ro_s * Math.cos(epsilon) ));
        return d_q_s_estimate < thresh;
    }

    private boolean estimateGTThreshold(double d_ro_q, double d_ro_s, double theta) {
        double epsilon = predictEpsilon(d_ro_q, d_ro_s, theta);
        double d_q_s_estimate = Math.sqrt( square(d_ro_s) + square(d_ro_q) - ( 2.0d * d_ro_q * d_ro_s * Math.cos(epsilon) ));
        return d_q_s_estimate > thresh;
    }

    /**
     * predict the angle at the reference object to some point based on params
     * @param d_ro_q - the distance from the reference object to a query (known at query time).
     * @param d_ro_s - the distance from the reference object some point (known early at analysis time)
     * @param theta - the angle from the ro_q_s - predicted from model
     * @return the angle from s_ro_q
     */
    private double predictEpsilon(double d_ro_q, double d_ro_s, double theta) {
        double beta = Math.asin( Math.min( 1.0d, Math.sin(theta) * d_ro_q / d_ro_s ) );  // rounding can take over 1 yielding NaN
        return Math.PI - theta - beta;
    }

    /************* Main *************/


    public static void main( String[] args ) throws Exception {

        EpsilonSort pp = new EpsilonSort( EUC20,10000, 60, 20  );
        pp.experiment();
    }

}
