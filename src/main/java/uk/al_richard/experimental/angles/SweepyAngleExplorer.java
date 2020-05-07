package uk.al_richard.experimental.angles;

import coreConcepts.Metric;
import dataPoints.cartesian.CartesianPoint;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SweepyAngleExplorer extends CommonBase {

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



    public SweepyAngleExplorer(String dataset_name, int number_samples, int noOfRefPoints, int WHICH_REF) throws Exception {
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

    @Test
    public void testDiagnonalPoints() {

        for( double i = 0.1; i < 1; i += 0.1 ) {
            double[] point = getDiagonalPoint( i );
            assert(  metric.distance(new CartesianPoint(point), global_reference_point) < ( i + 0.005 ) ); // close to equal
        }
    }

    private final static int angle_calculation_repetitions = 1000000;


    /**
     * Move a point from 0.4 away from the origin up to 0.7 away up a diagonal
     * Measure the angles of points within 0.5 of the query point - TODO make this a param
     */
    public void sweep( double query_radius ) {

        System.out.println("Checking " + dataset_name + " " + angle_calculation_repetitions + " random points, query radius = " + query_radius );
        System.out.println("D\tangle(deg)\tstdev\tin range");
        for( double diagonal_distance = 0.01; diagonal_distance < Math.sqrt( getDim() ) ; diagonal_distance += 0.01 ) {
            calculateAngles( diagonal_distance, query_radius );
        }
    }

    private void calculateAngles(double diagonal_distance, double query_radius)  {
        List<Double> list = new ArrayList<>();

        double[] diagonal_point = getDiagonalPoint( diagonal_distance );
        CartesianPoint diagonal_point_cartesian = new CartesianPoint(diagonal_point);

        for( int j = 0; j < angle_calculation_repetitions; j++ ) {
            CartesianPoint some_point_cartesian = new CartesianPoint( getRandomVolumePoint( diagonal_point, query_radius ) );
            if( insideSpace( some_point_cartesian ) ) {
                double theta = calculateAngle(global_reference_point, diagonal_point_cartesian, some_point_cartesian);
                list.add(theta);
            }
        }
        int count = list.size();
        if( count > 0 ) {
            double mean = (double) Util.mean(list);
            double std_dev = Util.stddev(list, mean);
            // System.out.println("Distance = " + df.format(diagonal_distance) + " mean angle (degrees) = " + df.format(Math.toDegrees(mean)) + " std_dev = " + df.format(Math.toDegrees(std_dev)) + " n = " + count );
            System.out.println(df.format(diagonal_distance) + "\t" + df.format(Math.toDegrees(mean)) + "\t" + df.format(Math.toDegrees(std_dev)) + "\t" + count );

        } else {
            System.out.println( "Distance = " + df.format(diagonal_distance) + " No points in 0-1 range" );
        }
    }


    public static void main(String[] args) throws Exception {

        int number_samples =  999800; // 1M less 200
        int noOfRefPoints = 200;
        SweepyAngleExplorer sae = new SweepyAngleExplorer( EUC20,number_samples,noOfRefPoints, CENTRE );
        sae.sweep( sae.getThreshold() );
    }


    public static void main1(String[] args) throws Exception {
        int number_samples =  999800; // 1M less 200
        int noOfRefPoints = 200;

        for( String dataset_name : eucs ) {

            SweepyAngleExplorer sae = new SweepyAngleExplorer( dataset_name,number_samples,noOfRefPoints, CENTRE );
            sae.sweep( sae.getThreshold() );

        }
    }




}
