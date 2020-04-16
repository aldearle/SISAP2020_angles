package uk.al_richard.experimental.angles;

import coreConcepts.Metric;
import dataPoints.cartesian.CartesianPoint;
import testloads.TestContext;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import static uk.al_richard.experimental.angles.Util.mean;
import static uk.al_richard.experimental.angles.Util.square;
import static uk.al_richard.experimental.angles.Util.stddev;

public class ExploreAngles {

    static final TestContext.Context context = TestContext.Context.euc20;
    private Metric<CartesianPoint> metric;
    private List<CartesianPoint> eucs;

    private DecimalFormat df = new DecimalFormat("#.##");

    public ExploreAngles( int count ) throws Exception {

        TestContext tc = new TestContext(context);
        metric = tc.metric();
        tc.setSizes(0, 0);
        eucs = tc.getData().subList(0,count);
    }


    /**
     * Measure and report all of the angles from count points drawn from eucs.
     * Reports the pivot number,
     *         distance from pivot to query,
     *         distance from pivot to a point,
     *         distance from a query to a point and
     *         the angle from pivot-query-point.
     */
    private void explore() {
        CartesianPoint[] eucs_array = new CartesianPoint[0];
        eucs_array = eucs.toArray( eucs_array );  // an array of Cartesians drawn from the euc 20 space
        int len = eucs_array.length;
        System.out.println("Piv" + "\t" +"d PQ" + "\t" + "DP pt" + "\t" + "D Q pt" + "\t" + "Angle Degrees");

        List<Double> angles = new ArrayList<>();

        for( int i = 0; i < len; i++ ) {
            for( int j = 0; j < len; j++ ) {
                if( i != j ) {

                    double d_pivot_q = metric.distance( eucs_array[i],eucs_array[j] );
                    for( int k = 0; k < len; k++ ) {
                        if (k != i && k != j) {
                            angles.add( calculateAngle(i,j,k,d_pivot_q,eucs_array) );
                        }
                    }
                }
            }
        }
        summarizeAngles( angles );
        System.out.println( "finished" );
    }

    private void summarizeAngles(List<Double> angles) {
        double mu = mean(angles);
        double std_dev = stddev(angles,mu);

        System.out.println( "Mean = " + df.format( Math.toDegrees( mu )  ) + " Std_dev = " + df.format( Math.toDegrees( std_dev ) ) );

    }

    private double calculateAngle(int i, int j, int k, double d_pivot_q, CartesianPoint[] eucs_array) {
        CartesianPoint pivot = eucs_array[i];
        CartesianPoint query = eucs_array[j];
        CartesianPoint some_point = eucs_array[k];

        double dqpi = metric.distance( query,some_point );
        double p1pi = metric.distance( pivot,some_point );

        double theta = Math.acos( ( square(dqpi) + square(d_pivot_q) - square(p1pi) ) / (2 * dqpi * d_pivot_q ) );

        System.out.println(i + "\t" + df.format(d_pivot_q) + "\t" + df.format(p1pi) + "\t" + df.format(dqpi) + "\t" + df.format(Math.toDegrees(theta)));

        return theta;
    }

    public static void main( String[] args ) throws Exception {

        ExploreAngles ea = new ExploreAngles( 20 ); // 1000
        ea.explore();
    }


}
