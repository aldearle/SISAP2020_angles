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

    private static double theta_euc_20 = 1.3172;            // TODO HARD CODED theta_low for EUC20
    private static double std_dev_angle_euc_20 = 0.2141519; // TODO HARD CODED std dev for EUC20
    private final double theta;

    private HashSet<Integer> exclude; // HACK putting this here!

    public EpsilonSort(String dataset_name, int number_data_points, int noOfRefPoints, int num_queries ) throws Exception {

        super( dataset_name, number_data_points, noOfRefPoints,num_queries  );

        thresh = super.getThreshold();

        System.out.println( "thresh = " + thresh );

        this.theta = theta_euc_20 + (2 * std_dev_angle_euc_20);  // Making this bigger gets more exclusions (but more errors)

        this.data_points = getData();
        data_size = data_points.size();
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
        return dists;
    }

    private void experiment() {
        for( int i = 0; i < queries.size(); i++ ) {
            CartesianPoint query_point = queries.get(i);
            query( query_point );
        }

    }

    private void query(CartesianPoint query_point) {

        exclude = new HashSet<>();
        for( int i = 0; i < ro_map.size(); i++ ) {
            CartesianPoint ro_object = ref_points.get(i);
            queryRO(query_point, ro_map.get(i), ro_object);
        }
        check(query_point);
    }

    private void check(CartesianPoint query_point) {
        checkInclusions(query_point);
        checkExclusions(query_point);
    }

    private void checkInclusions(CartesianPoint query_point) {
        int true_positive = 0;
        int inclusion_count = 0;
        int solution_count = 0;
        for( int i = 0; i < data_points.size(); i++ ) {
            double d_q_s = metric.distance( data_points.get(i), query_point);

            if( d_q_s < thresh ) {
                solution_count++;
            }

            if( ! exclude.contains(i) ) { // these ones are candidate solutions
                inclusion_count++;
                if( d_q_s < thresh ) {
                    true_positive++;
                }
            }
        }
        System.out.println( "Included points size = " + inclusion_count + " correct = " + true_positive + " / " + solution_count );
    }

    private void checkExclusions(CartesianPoint query_point) {
        int true_positive = 0;
        for( int i : exclude) {
            double d_q_s = metric.distance( data_points.get(i), query_point);
            if( d_q_s > thresh ) {
                true_positive++;
            }
        }
        System.out.println( "Eliminated points size = " + exclude.size() + " correct = " + true_positive );
    }

    private void queryRO(CartesianPoint query_point, OrderedList<Integer, Double> data_dists, CartesianPoint ro_object) {

        double d_ro_q = metric.distance(ro_object, query_point);

        List<Double> dists = data_dists.getComparators();
        for( int i = dists.size() - 1; i >= 0; i-- ) {

            Double d_ro_s = dists.get(i);

            boolean gt_thresh = estimateGTThreshold( d_ro_q, d_ro_s, theta);
            if( gt_thresh ) {
                exclude.add( i );
            }

            if( ! gt_thresh ) {
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

        EpsilonSort pp = new EpsilonSort( EUC20,10000, 200, 20  );
        pp.experiment();
    }

}
