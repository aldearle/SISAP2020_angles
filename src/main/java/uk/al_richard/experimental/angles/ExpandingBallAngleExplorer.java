package uk.al_richard.experimental.angles;

import coreConcepts.Metric;
import dataPoints.cartesian.CartesianPoint;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static uk.al_richard.experimental.angles.Util.square;

/**
 * Expand a ball around the centre and see how angles change
 */
public class ExpandingBallAngleExplorer extends CommonBase {

    private static final int ORIGIN = 0;
    private static final int CENTRE = 0xC;

    private boolean debug = false;

    private final Metric<CartesianPoint> metric;
    private final List<CartesianPoint> samples;
    private final List<CartesianPoint> pivots;
    private final int dim;

    Random rand  = new Random(8796253 );
    double[] centre;
    double[] origin;
    CartesianPoint centre_cartesian;
    CartesianPoint origin_cartesian;
    CartesianPoint global_reference_point;



    public ExpandingBallAngleExplorer(String dataset_name, int number_samples, int noOfRefPoints, int WHICH_REF) throws Exception {
        super( dataset_name, number_samples, noOfRefPoints,0  );

        this.rand = new Random();

        pivots = super.getRos();
        samples = super.getData();
        metric = super.getMetric();
        dim = super.getDim();

        this.origin = new double[getDim()];
        this.centre = makePoint( 0.5 );
        this.centre_cartesian = new CartesianPoint(centre);
        this.origin_cartesian = new CartesianPoint(origin);

        if( WHICH_REF == ORIGIN ) {
            global_reference_point = origin_cartesian;
            System.out.println( "using origin" );
        } else if( WHICH_REF == CENTRE ) {
            global_reference_point = centre_cartesian;
            System.out.println( "using centre" );
        } else {
            throw new RuntimeException( "unknown reference point");
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

    /**
     *
     * @param distance_from_o - this distance from the origin
     * @return a point on the diagonal that distance from the origin
     */
    private double[] getDiagonalPoint(double distance_from_o ) {

        double coordinate = Math.sqrt( Math.pow(distance_from_o,2) / getDim() );
        return makePoint( coordinate );
    }

    @Test
    public void testDiagnonalPoints() {

        for( double i = 0.1; i < 1; i += 0.1 ) {
            double[] point = getDiagonalPoint( i );
            assert(  metric.distance(new CartesianPoint(point), global_reference_point) < ( i + 0.005 ) ); // close to equal
        }
    }

    private double[] makePoint( double coordinate ) {
        double[] point = new double[getDim()];
        for (int i = 0; i < getDim(); i++) {
            point[i] = coordinate;
        }
        return point;
    }

    /**
     *
     * @param pivot
     * @param query
     * @param some_point
     * @return the angle in RADIANS.
     */
    private double calculateAngle( CartesianPoint pivot, CartesianPoint query, CartesianPoint some_point ) {

        Metric<CartesianPoint> metric = getMetric();

        double dpq =  metric.distance( pivot,query );
        double dqpi = metric.distance( query,some_point );
        double p1pi = metric.distance( pivot,some_point );

        double theta = Math.acos( ( square(dqpi) + square(dpq) - square(p1pi) ) / (2 * dqpi * dpq ) );

        if( debug ) {
            System.out.println(df.format(dpq) + "\t" + df.format(p1pi) + "\t" + df.format(dqpi) + "\t" + df.format(Math.toDegrees(theta)));
        }

        return theta;
    }

    private final static int angle_calculation_repetitions = 1000000;


    /**
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

    private void calculatePivotAngles(double diagonal_distance, double query_radius)  {
        List<Double> list = new ArrayList<>();

        double[] diagonal_point = getDiagonalPoint( diagonal_distance );
        CartesianPoint diagonal_point_cartesian = new CartesianPoint(diagonal_point);

        for( int j = 0; j < pivots.size(); j++ ) {
                double theta = calculateAngle(global_reference_point, diagonal_point_cartesian, pivots.get(j));
                list.add(theta);
        }
        int count = list.size();
        double mean = (double) Util.mean(list);
        double std_dev = Util.stddev(list, mean);
        System.out.println("Distance = " + df.format(diagonal_distance) + " mean angle (degrees) = " + df.format(Math.toDegrees(mean)) + " std_dev = " + df.format(Math.toDegrees(std_dev)) + " n = " + count );
    }

    private boolean insideSpace(CartesianPoint some_point_cartesian) {
        double[] point = some_point_cartesian.getPoint();
        for( int i = 0; i < point.length; i++ ) {
            if( point[i] < 0.0 || point[i] > 1.0 ) { // inclusive?
                return false;
            }
        }
        return true;
    }


    public static void main(String[] args) throws Exception {

        int number_samples =  1000000; // 1M less 200
        int noOfRefPoints = 0;
        ExpandingBallAngleExplorer sae = new ExpandingBallAngleExplorer( EUC20,number_samples,noOfRefPoints, ORIGIN );
        sae.expand();
    }


    public static void main1(String[] args) throws Exception {
        int number_samples =  1000000; // 1M less 200
        int noOfRefPoints = 0;

        for( String dataset_name : eucs ) {

            ExpandingBallAngleExplorer sae = new ExpandingBallAngleExplorer( dataset_name,number_samples,noOfRefPoints, ORIGIN );
            sae.expand();

        }
    }




}
