package uk.al_richard.experimental.angles;

import coreConcepts.Metric;
import dataPoints.cartesian.CartesianPoint;
import util.OrderedList;

import java.util.HashMap;
import java.util.List;

public class EpsilonSort4 extends CommonBase {

    private final double thresh;

    private final Metric<CartesianPoint> metric;

    private final List<CartesianPoint> data_points;
    private final List<CartesianPoint> queries;
    private final List<CartesianPoint> ref_points;
    private final HashMap<Integer, Double> ro_map;
    private final int data_size;

    private final int dim;



    public EpsilonSort4(String dataset_name, int number_data_points, int noOfRefPoints, int num_queries ) throws Exception {

        super( dataset_name, number_data_points, noOfRefPoints, num_queries  );

        this.thresh = super.getThreshold();

        this.data_points = getData();
        this.data_size = data_points.size();
        this.ref_points = getRos();
        this.queries = getQueries();
        this.metric = getMetric();
        this.dim = getDim();
        this.ro_map = new HashMap<>();
        initDists();

    }

    /************* private methods *************/

    private void initDists() {

        for (int i = 0; i < ref_points.size(); i++) {
            CartesianPoint pivot = ref_points.get(i);

            OrderedList<Integer, Double> dists = getDists(pivot);
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
            simulateQuery( query_point );
        }

    }

    private void simulateQuery(CartesianPoint query_point) throws Exception {

        for( int i = 0; i < ro_map.size(); i++ ) {

//            Angles theta = ldim_to_angle_map.getEstimatedAngle( query_point );
//
//            CartesianPoint ref_object = ref_points.get(i);
//
//            checkAngles( query_point, ref_object, ro_map.get(i), theta);

        }
    }

    private void checkAngles(CartesianPoint query_point, CartesianPoint ro_object, OrderedList<Integer, Double> data_dists, Angles theta) {


        List<Double> dists = data_dists.getComparators();

        for( int i = dists.size() - 1; i >= 0; i-- ) {

            CartesianPoint s = data_points.get(i);

            double real_angle = calculateAngle(ro_object, query_point, s);

            System.out.println( real_angle + "\n" + theta.angle + "\n" + ( real_angle - theta.angle ) );

        }
    }

    /************* Main *************/


    public static void main( String[] args ) throws Exception {

        EpsilonSort4 pp = new EpsilonSort4( EUC20,100000, 100, 60  );
        pp.experiment();
    }

}
