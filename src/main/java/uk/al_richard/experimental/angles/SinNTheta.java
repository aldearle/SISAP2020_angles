package uk.al_richard.experimental.angles;

import dataPoints.cartesian.CartesianPoint;

import java.util.ArrayList;
import java.util.List;

/**
 * Expand a ball around the centre and see how angles change
 */
public class SinNTheta extends CommonBase {

    public static final int TEN_THOUSAND = 10000;
    public static final int HUNDRED_THOUSAND = 100000;
    public static final int ONE_MILLION = 1000000;
    public static final int TEN_MILLION = 10000000;

    private final static int repetitions = HUNDRED_THOUSAND;
    private final List<CartesianPoint> data;
    private final double query_radius;

    public SinNTheta( String dataset_name, int number_samples, int noOfRefPoints ) throws Exception {
        super( dataset_name, number_samples, noOfRefPoints,0  );
        System.out.println( "Dataset : " + dataset_name );
        this.data = super.getData();
        this.query_radius = super.getThreshold();
    }


    private void explore_random_points() {

        List<Double> dists = new ArrayList<>();
        for( int i = 1; i < repetitions*3; i+=3 ) { // repetitions
            CartesianPoint p1 = data.get(i);
            CartesianPoint p2 = data.get(i+1);
            CartesianPoint p3 = data.get(i+2);

            double ang = calculateAngle( p1,p2,p3 );
            dists.add( ang );
        }
        System.out.println( "Mean = " + Math.toDegrees( Util.mean(dists ) ) );
    }

    private void explore_constrained_points() {

        List<Double> dists = new ArrayList<>();
        for( int i = 1; i < repetitions*2; i+=3 ) {
            CartesianPoint centre = data.get(i);
            double[] centre_dbls = ((CartesianPoint) centre).getPoint();
            CartesianPoint p1 = data.get(i + 1);

            CartesianPoint some_point_cartesian = new CartesianPoint( getRandomVolumePoint( centre_dbls, query_radius ) );
            if( insideSpace( some_point_cartesian ) ) {
                double theta = calculateAngle(p1, centre, some_point_cartesian);
                if( ! Double.isNaN(theta)) {
                    dists.add(theta);
                }
            }
        }
        double mean = Util.mean(dists);
        double std_dev = Util.stddev(dists,mean);
        System.out.println( "Mean = " + Math.toDegrees( mean ) + " std dev = " + Math.toDegrees( std_dev ) );
    }



    public static void main(String[] args) throws Exception {

        int number_samples = ONE_MILLION;
        int noOfRefPoints = 0;
        SinNTheta sae = new SinNTheta( "Euc13",number_samples,noOfRefPoints );
        sae.explore_constrained_points();
    }



}
