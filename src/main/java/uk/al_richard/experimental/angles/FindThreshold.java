package uk.al_richard.experimental.angles;

import dataPoints.cartesian.CartesianPoint;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class FindThreshold extends CommonBase {

    private final List<CartesianPoint> data;

    /**
     *
     * Exhaustively search a space and measures angles between pivot-query-point for all points within query threshold.
     * @param dataset_name - the dataset to be explored
     * @param count - the number of points over which to perform exhaustive search
     * @throws Exception - if something goes wrong.
     */
    public FindThreshold(String dataset_name, int count ) throws Exception {

        super( dataset_name,count,0,0 );

        data = super.getData();

    }


    private void explore() {
        System.out.print( dataset_name + ": " );

        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
        List<Double> all_dists = new ArrayList<Double>();

        for( CartesianPoint p1 : data ) {
            for( CartesianPoint p2 : data ) {
                if( ! p1.equals(p2) ) {
                    double dist = getMetric().distance( p1,p2 );
                    if( dist < min ) {
                        min = dist;
                    }
                    if( dist > max ) {
                        max = dist;
                    }
                    all_dists.add( dist );
                }
            }
        }
        printResults( min,max, all_dists );
    }

    private void printResults(double min, double max, List<Double> all_dists) {
        System.out.println( "Min = " + min );
        System.out.println( "Max = " + max );
        double mean = Util.mean(all_dists);
        double std_dev = Util.stddev( all_dists, mean );
        System.out.println( "Mean = " + mean );
        System.out.println( "Std Dev = " + std_dev );
        print1percent( all_dists );
    }

    private void print1percent(List<Double> all_dists) {
        int len = all_dists.size();

        all_dists.sort( Comparator.naturalOrder() ) ;
        System.out.println( "1% distance = " + df.format( all_dists.get( len / 100 ) ) );
        System.out.println( "10% distance = " + df.format( all_dists.get( len / 10 ) ) );
        System.out.println( "50% distance = " + df.format( all_dists.get( len / 2 ) ) );


    }


    public static void main( String[] args ) throws Exception {

        FindThreshold ea = new FindThreshold( DECAF, 300 );
        ea.explore();
    }

    public static void main1( String[] args ) throws Exception {

        for( String dataset_name : datasets ) {
            FindThreshold ea = new FindThreshold( dataset_name, 500 );
            ea.explore();
        }
    }
}
