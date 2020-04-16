package uk.al_richard.experimental.angles;

import coreConcepts.Metric;
import dataPoints.cartesian.CartesianPoint;
import testloads.TestContext;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import static uk.al_richard.experimental.angles.Util.square;

public class ExploreAnglesThresholded {

    static final TestContext.Context context = TestContext.Context.euc30;
    private boolean show_all = false;
    private Metric<CartesianPoint> metric;
    private List<CartesianPoint> eucs;
    private double thresh;

    private List<Double> d_pivot_q_list = new ArrayList<>();
    private List<Double> d_pivot_point_list = new ArrayList<>();
    private List<Double> d_q_point_list = new ArrayList<>();
    private List<Double> theta_list = new ArrayList<>();

    private DecimalFormat df = new DecimalFormat("#.##");

    /**
     *
     * Exhaustively search a space and measures angles between pivot-query-point for all points within query threshold.
     * @param count - the number of points over which to perform exhaustive search
     * @param thresh - the query threshold - if -1 uses TestContext.getThreshold()
     * @param show_all - show the intermediate distances and angles
     * @throws Exception - if something goes wrong.
     */
    public ExploreAnglesThresholded( int count, double thresh, boolean show_all ) throws Exception {

        this.show_all = show_all;
        TestContext tc = new TestContext(context);
        metric = tc.metric();
        if( thresh == -1 ) {
            this.thresh = tc.getThreshold();
        } else {
            this.thresh = thresh;
        }
        tc.setSizes(0, 0);
        eucs = tc.getData().subList(0,count);
    }


    private void explore() {
        CartesianPoint[] eucs_array = new CartesianPoint[0];
        eucs_array = eucs.toArray( eucs_array );
        int len = eucs_array.length;
        if( show_all ) {
            System.out.println("d_pivot_q" + "\t" + "dd_pivot_point" + "\t" + "d_q_point" + "\t" + "theta");
        }

        for( int i = 0; i < len; i++ ) {
            for( int j = 0; j < len; j++ ) {
                if( i != j ) {
                    double d_pivot_q = metric.distance( eucs_array[i],eucs_array[j] );
                    for( int k = 0; k < len; k++ ) {
                        if (k != i && k != j) {
                            calculateAngle(i,j,k,d_pivot_q,eucs_array);
                        }
                    }
                }
            }
        }
        printDists();
        System.out.println( "finished" );
    }

    private void calculateAngle(int i, int j, int k, double d_pivot_q, CartesianPoint[] eucs_array) {
        CartesianPoint pivot = eucs_array[i];
        CartesianPoint query = eucs_array[j];
        CartesianPoint some_point = eucs_array[k];

        double d_q_point = metric.distance( query,some_point );
        double d_pivot_point = metric.distance( pivot,some_point );

        double theta = Math.acos( ( square(d_q_point) + square(d_pivot_q) - square(d_pivot_point) ) / (2 * d_q_point * d_pivot_q ) );

        if( d_q_point < thresh ) {
            collect( d_pivot_q, d_pivot_point, d_q_point, theta );
        }
    }

    private void collect(double d_pivot_q, double d_pivot_point, double d_q_point, double theta) {

        d_pivot_q_list.add( d_pivot_q );
        d_pivot_point_list.add( d_pivot_point );
        d_q_point_list.add( d_q_point );
        theta_list.add( theta );

        if( show_all ) {
            System.out.println(df.format(d_pivot_q) + "\t" + df.format(d_pivot_point) + "\t" + df.format(d_q_point) + "\t" + df.format(Math.toDegrees(theta)));
        }

    }

    private void printDists() {

        System.out.println( "Summary for threshold = " + df.format(thresh ) + " n = " + eucs.size() + " context = " + context.name() );
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

        ExploreAnglesThresholded ea = new ExploreAnglesThresholded( 1000,  0.8, false ); // 1000
        ea.explore();
    }
}
