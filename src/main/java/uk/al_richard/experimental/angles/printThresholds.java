package uk.al_richard.experimental.angles;

import dataPoints.cartesian.CartesianPoint;

import java.util.List;

public class printThresholds extends CommonBase {

    private final List<CartesianPoint> data;

    /**
     *
     * Exhaustively search a space and measures angles between pivot-query-point for all points within query threshold.
     * @param dataset_name - the dataset to be explored
     * @param count - the number of points over which to perform exhaustive search
     * @throws Exception - if something goes wrong.
     */
    public printThresholds(String dataset_name, int count ) throws Exception {

        super( dataset_name,count,0,0 );

        data = super.getData();

    }


    private void print() {
        System.out.println( dataset_name + " threshold = " + getThreshold() );
    }


    public static void main( String[] args ) throws Exception {

        for( String dataset_name : eucs ) {
            printThresholds ea = new printThresholds( dataset_name, 0 );
            ea.print();
        }
    }
}
