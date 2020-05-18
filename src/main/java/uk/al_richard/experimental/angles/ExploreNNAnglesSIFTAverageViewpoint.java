package uk.al_richard.experimental.angles;

import coreConcepts.Metric;
import dataPoints.cartesian.CartesianPoint;

import java.util.ArrayList;
import java.util.List;

import static uk.al_richard.experimental.angles.Util.square;

public class ExploreNNAnglesSIFTAverageViewpoint extends CommonBase {

    private final boolean show_all;
    private List<Double> d_pivot_q_list = new ArrayList<>();
    private List<Double> d_pivot_point_list = new ArrayList<>();
    private List<Double> d_q_point_list = new ArrayList<>();
    private List<Double> theta_list = new ArrayList<>();


    /**
     *
     * Exhaustively search a space and measures angles between pivot-query-point for all NNs
     * @param dataset_name - the dataset to be explored
     * @param data_size - the number of points over which to perform exhaustive search
     * @param show_all - show the intermediate distances and angles
     * @throws Exception - if something goes wrong.
     */
    public ExploreNNAnglesSIFTAverageViewpoint(String dataset_name, int data_size, int query_size, boolean show_all ) throws Exception {

        super( dataset_name,data_size,0,query_size );
        this.show_all = show_all;
    }

    private float[] getData( int i ) {
        return getDataMap().get(i);
    }

    private float[] getQuery( int i ) {
        return getQueryMap().get(i);
    }

    private void explore() {

        CartesianPoint viewpoint = averagePoint();


        for( int index : nn_map.keySet() ) {

            CartesianPoint query = new CartesianPoint( getQuery( index ) );

            int[] nn_ids = nn_map.get(index);

            if (show_all) {
                System.out.println();
                System.out.println("d_pivot_q" + "\t" + "dd_pivot_point" + "\t" + "d_q_point" + "\t" + "theta");
            }

            for( int nn_index = 0; nn_index < nn_ids.length; nn_index++) {

                int next_nn_data_index = nn_ids[nn_index];

                if( index != next_nn_data_index ) {
                    CartesianPoint pivot = new CartesianPoint(getData(next_nn_data_index));

                    double d_pivot_q = getMetric().distance(pivot, query);

                    calculateAngle(viewpoint, pivot, query, d_pivot_q);
                }
            }
        }
        printDists();
        System.out.println( "finished" );
    }

    private CartesianPoint averagePoint() {
        double[] dbls = makePoint(0);
        List<CartesianPoint> queries = getQueries();
        for( CartesianPoint q : queries ) {
            double[] p = q.getPoint();
            for( int i = 0; i < p.length; i ++ ) {
                dbls[i] = p[i];
            }
        }
        for( int i = 0; i < dbls.length; i ++ ) {
            dbls[i] = dbls[i] / queries.size();
        }
        return new CartesianPoint( dbls );
    }

    private void calculateAngle( CartesianPoint some_point, CartesianPoint pivot, CartesianPoint query, double d_pivot_q ) {

        Metric<CartesianPoint> metric = getMetric();

        double d_q_point = metric.distance( query,some_point );
        double d_pivot_point = metric.distance( pivot,some_point );

        double theta = Math.acos( ( square(d_q_point) + square(d_pivot_q) - square(d_pivot_point) ) / (2 * d_q_point * d_pivot_q ) );

        collect( d_pivot_q, d_pivot_point, d_q_point, theta );

    }

    private void collect(double d_pivot_q, double d_pivot_point, double d_q_point, double theta) {

        if( Double.isNaN(d_pivot_q) || Double.isNaN(d_pivot_point) || Double.isNaN(d_q_point) || Double.isNaN(theta) ) {
            System.out.println( "Got nan in collect: " + d_pivot_q + " , " +  d_pivot_point + " , " + d_q_point + " , " + theta );
            return;
        }
        d_pivot_q_list.add( d_pivot_q );
        d_pivot_point_list.add( d_pivot_point );
        d_q_point_list.add( d_q_point );
        theta_list.add( theta );

        if( show_all ) {
            System.out.println(df.format(d_pivot_q) + "\t" + df.format(d_pivot_point) + "\t" + df.format(d_q_point) + "\t" + df.format(Math.toDegrees(theta)));
        }

    }

    private void printDists() {

        System.out.println( "Summary for NNs:" );
        averages("d_pivot_q",d_pivot_q_list );
        averages("d_pivot_point",d_pivot_point_list);
        averages("d_q_point",d_q_point_list);
        averages("theta",theta_list) ;

    }

    private void averages(String label, List<Double> list) {
        double mean;
        double std_dev;
        try {
             mean = Util.mean(list);
             std_dev = Util.stddev(list, mean);
        } catch( RuntimeException e ) {
            System.out.println(label + ": no results" );
            return;
        }
        if( label.equals("theta")) {
            System.out.println(label + " mean = " + df.format(Math.toDegrees(mean)) + " std_dev = " + df.format(Math.toDegrees(std_dev)) + " degrees" );
        } else {
            System.out.println(label + " mean = " + df.format(mean) + " std_dev = " + df.format(std_dev));
        }
    }



    public static void main( String[] args ) throws Exception {

        ExploreNNAnglesSIFTAverageViewpoint ea = new ExploreNNAnglesSIFTAverageViewpoint( SIFT, 1000, 1000, true );
        ea.explore();
    }
}
