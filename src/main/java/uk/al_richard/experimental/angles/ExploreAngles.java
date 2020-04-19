package uk.al_richard.experimental.angles;

import coreConcepts.Metric;
import dataPoints.cartesian.CartesianPoint;

import java.util.ArrayList;
import java.util.List;

import static uk.al_richard.experimental.angles.Util.*;

public class ExploreAngles extends CommonBase {

    public ExploreAngles( String dataset_name, int count ) throws Exception {
        super( dataset_name, count, 0, 0 );
    }

    /**
     * Calculate all of the angles from count points drawn from the dataset.
     * @param print_intermediaries - if true prints the individual distances
     *
     * Calculate:
     *         the pivot number,
     *         distance from pivot to query,
     *         distance from pivot to a point,
     *         distance from a query to a point and
     *         the angle from pivot-query-point.
     **/
    private void explore( boolean print_intermediaries ) {
        System.out.println( "Dataset: " + getDataSetName() );
        CartesianPoint[] eucs_array = new CartesianPoint[0];
        eucs_array = getData().toArray( eucs_array );  // an array of Cartesians drawn from the euc 20 space
        int len = eucs_array.length;
        if( print_intermediaries ) {
            System.out.println("Piv" + "\t" + "d PQ" + "\t" + "D Ppt" + "\t" + "D Qpt" + "\t" + "Angle Degrees");
        }

        List<Double> angles = new ArrayList<>();

        for( int i = 0; i < len; i++ ) {
            for( int j = 0; j < len; j++ ) {
                if( i != j ) {

                    double d_pivot_q = getMetric().distance( eucs_array[i],eucs_array[j] );
                    for( int k = 0; k < len; k++ ) {
                        if (k != i && k != j) {
                            angles.add( calculateAngle(i,j,k,d_pivot_q,eucs_array, print_intermediaries) );
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

    private double calculateAngle(int i, int j, int k, double d_pivot_q, CartesianPoint[] eucs_array, boolean print) {
        CartesianPoint pivot = eucs_array[i];
        CartesianPoint query = eucs_array[j];
        CartesianPoint some_point = eucs_array[k];

        Metric<CartesianPoint> metric = getMetric();

        double dqpi = metric.distance( query,some_point );
        double p1pi = metric.distance( pivot,some_point );

        double theta = Math.acos( ( square(dqpi) + square(d_pivot_q) - square(p1pi) ) / (2 * dqpi * d_pivot_q ) );

        if( print ) {
            System.out.println(i + "\t" + df.format(d_pivot_q) + "\t" + df.format(p1pi) + "\t" + df.format(dqpi) + "\t" + df.format(Math.toDegrees(theta)));
        }

        return theta;
    }

    public static void main( String[] args ) throws Exception {

        ExploreAngles ea = new ExploreAngles( EUC30,500  );
        ea.explore( false );
    }


}
