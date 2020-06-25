package uk.al_richard.experimental.angles;

import coreConcepts.Metric;
import dataPoints.cartesian.CartesianPoint;
import util.OrderedList;

import java.util.*;

import static uk.al_richard.experimental.angles.Util.square;

public class EpsilonSort2 extends CommonBase {

    private final double thresh;

    private final Metric<CartesianPoint> metric;

    private final List<CartesianPoint> data_points;
    private final List<CartesianPoint> queries;
    private final List<CartesianPoint> ref_points;
    private final int data_size;

    private final Map<Integer,OrderedList<Integer,Double>> ro_map;
    private final int dim;

    private final LIDIMtoAngleMap ldim_to_angle_map;

    public EpsilonSort2(String dataset_name, int number_data_points, int noOfRefPoints, int num_queries ) throws Exception {

        super( dataset_name, number_data_points, noOfRefPoints, num_queries  );

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

        List<HashSet<Integer>> excludes = new ArrayList<>();

        for( int i = 0; i < ro_map.size(); i++ ) {

            Angles theta = ldim_to_angle_map.getEstimatedAngle( query_point );

            CartesianPoint ref_object = ref_points.get(i);

            excludes.add( queryROExclude( query_point, ref_object, ro_map.get(i), theta) );

        }
        Set<Integer> results = filter(excludes);
        check( query_point,results );
    }

    private Set<Integer> filter(List<HashSet<Integer>> excludes) {
        Set<Integer> results = new HashSet<>();
        int[] counts = new int[data_size];
        for( HashSet<Integer> exclude_ro : excludes ) {
            for( int i : exclude_ro ) {
                counts[i]++;
            }
        }
        for( int i = 0; i < counts.length; i++ ) {
            if( counts[i] != num_ros ) {   // require 100% exclusion
                results.add(i);
            }
        }
        return results;
    }

    private void check(CartesianPoint query_point, Set<Integer> results ) {

        int true_positive = 0;
        int false_positive = 0;
        int false_negative = 0;
        int true_negative = 0;

        int true_results = 0;

        for( int i = 0; i < data_points.size(); i++ ) {

            double d_q_s = metric.distance( data_points.get(i), query_point);

            if( d_q_s <= thresh ) {
                true_results++;
                if ( results.contains(i) ) {
                    true_positive++;
                } else {
                    false_negative++;
                }
            } else { // d_q_s > thresh
                if ( results.contains(i) ) {
                    false_positive++;
                } else {
                    true_negative++;
                }
            }
        }

        System.out.println( "TR" + "\t" + "TP" + "\t" + "TN" + "\t\t" + "FP" + "\t" + "FN" );
        System.out.println( true_results + "\t" + true_positive + "\t" + true_negative + "\t\t" + false_positive + "\t" + false_negative );
        System.out.println( "---");
    }

    private HashSet<Integer> queryROExclude(CartesianPoint query_point, CartesianPoint ro_object, OrderedList<Integer, Double> data_dists, Angles theta) {

        double d_ro_q = metric.distance(ro_object, query_point);
        HashSet<Integer> ro_exclude = new HashSet<>();

        List<Double> dists = data_dists.getComparators();
        List<Integer> indices = data_dists.getList();

        for( int i = dists.size() - 1; i >= 0; i-- ) {

            Double d_ro_s = dists.get(i);

            boolean gt_thresh = estimateGTThreshold( d_ro_q, d_ro_s, theta.angle - (2.5 * theta.std_dev));
            if( gt_thresh ) {
                ro_exclude.add( indices.get(i) );
            }

            if( ! gt_thresh ) {
                return ro_exclude;
            }
        }
        return ro_exclude;
    }

    private boolean estimateGTThreshold(double d_ro_q, double d_ro_s, double theta) {
        double epsilon = predictEpsilon(d_ro_q, d_ro_s, theta);
        double d_q_s_estimate = Math.sqrt( square(d_ro_s) + square(d_ro_q) - ( 2 * d_ro_q * d_ro_s * Math.cos(epsilon) ));
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

        EpsilonSort2 pp = new EpsilonSort2( EUC20,100000, 100, 60  );
        pp.experiment();
    }

}
