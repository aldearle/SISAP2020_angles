package uk.al_richard.experimental.angles;

import coreConcepts.Metric;
import dataPoints.cartesian.CartesianPoint;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Expand a ball around the centre and see how angles change
 */
public class ExpandingBallAngleExplorer extends CommonBase {

    private static final int ORIGIN = 0;
    private static final int CENTRE = 0xC;
    private static final int RANDOM = -1;

    private final Random rand;
    private final Metric<CartesianPoint> metric;

    private boolean debug = false;

    double[] centre;
    double[] origin;
    CartesianPoint centre_cartesian;
    CartesianPoint origin_cartesian;
    CartesianPoint global_reference_point;

    private final static int angle_calculation_repetitions = 1000000;


    public ExpandingBallAngleExplorer(String dataset_name, int number_samples, int noOfRefPoints, int WHICH_REF) throws Exception {
        super( dataset_name, number_samples, noOfRefPoints,0  );

        this.rand = new Random();

        this.origin = new double[getDim()];
        this.centre = makePoint( 0.5 );
        this.centre_cartesian = new CartesianPoint(centre);
        this.origin_cartesian = new CartesianPoint(origin);
        this.metric = super.getMetric();

        if( WHICH_REF == ORIGIN ) {
            global_reference_point = origin_cartesian;
            System.out.println( "using origin" );
        } else if( WHICH_REF == CENTRE ) {
            global_reference_point = centre_cartesian;
            System.out.println("using centre");
        } else if (WHICH_REF == RANDOM) {
            double radius = ( Math.sqrt( getDim() ) / 2 ) + 1;
            double[] random_point = getRandomVolumePoint( centre_cartesian.getPoint(), radius );
            global_reference_point = new CartesianPoint( random_point ); // some random point outside sphere
            System.out.println("random: " + pointToString( random_point ) + "distance from centre: " + radius );
        } else {
            throw new RuntimeException("unknown reference point");
        }
    }

    /**
     *
     * @return a point within radius of the midpoint specified
     */
    double[] getRandomVolumePoint( double[] midpoint, double radius ) {
        double[] res = new double[getDim()];
        double[] temp = new double[getDim() + 2];
        double acc = 0;
        for (int i = 0; i < getDim() + 2; i++) {
            double d = this.rand.nextGaussian();
            acc += d * d;
            temp[i] = d;
        }
        double magnitude = Math.sqrt(acc);   // the magnitude of the vector
        for (int i = 0; i < getDim(); i++) {
            res[i] = ( temp[i] / magnitude * radius ) + midpoint[i];
        }
        return res;
    }


    @Test
    public void testRandomVolumePoints() {

        for( int i = 0; i < 10000; i++ ) {
            for( double j = 0.1; j <= 0.9; j+=0.01 ) {
                double[] centre = makePoint(j);
                CartesianPoint centre_cartesian = new CartesianPoint(centre);
                double[] random_point = getRandomVolumePoint(centre, 0.25);
                CartesianPoint random_cartesian = new CartesianPoint(random_point);
                assert metric.distance(centre_cartesian, random_cartesian) < 0.25;
            }
        }
    }


    /*
     * Expand a ball from a point at 1 millionth of space up and see how angles change
     * Expand ball until the radius of the ball touches the apex of unit cube
     */
    public void expand() {

        System.out.println("Expanding sphere Checking " + dataset_name + " " );
        System.out.println("D\tangle(deg)\tstdev\tin range");
        for( double query_radius = getThreshold(); query_radius < Math.sqrt( getDim() ) / 2 ; query_radius += 0.01 ) {
            calculateAngles( query_radius );
        }
    }

    private void calculateAngles(double query_radius)  {
        List<Double> list = new ArrayList<>();

        CartesianPoint ball_centre = centre_cartesian;
        double[] ball_centre_dbls = ball_centre.getPoint();

        for( int j = 0; j < angle_calculation_repetitions; j++ ) {
            CartesianPoint some_point_cartesian = new CartesianPoint( getRandomVolumePoint( ball_centre_dbls, query_radius ) );
            if( insideSpace( some_point_cartesian ) ) {
                double theta = calculateAngle(global_reference_point, ball_centre, some_point_cartesian);
                list.add(theta);
            }
        }
        int count = list.size();
        if( count > 0 ) {
            double mean = (double) Util.mean(list);
            double std_dev = Util.stddev(list, mean);
            System.out.println("Radius = " + df.format(query_radius) + " mean angle (degrees) = " + df.format(Math.toDegrees(mean)) + " std_dev = " + df.format(Math.toDegrees(std_dev)) + " n = " + count );
            //System.out.println(df.format(query_radius) + "\t" + df.format(Math.toDegrees(mean)) + "\t" + df.format(Math.toDegrees(std_dev)) + "\t" + count );

        } else {
            System.out.println( "Distance = " + df.format(query_radius) + " No points in 0-1 range" );
        }
    }


    public static void main(String[] args) throws Exception {

        int number_samples = 0;
        int noOfRefPoints = 0;
        ExpandingBallAngleExplorer sae = new ExpandingBallAngleExplorer( EUC20,number_samples,noOfRefPoints, RANDOM );
        sae.expand();
    }


    public static void main1(String[] args) throws Exception {
        int number_samples = 0;
        int noOfRefPoints = 0;

        for( String dataset_name : eucs ) {

            ExpandingBallAngleExplorer sae = new ExpandingBallAngleExplorer( dataset_name,number_samples,noOfRefPoints, ORIGIN );
            sae.expand();

        }
    }




}
