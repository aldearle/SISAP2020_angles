package uk.al_richard.experimental.angles;

import coreConcepts.Metric;
import dataPoints.cartesian.CartesianPoint;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static uk.al_richard.experimental.angles.Util.square;

public class SweepyAngleExplorer extends CommonBase {

    private boolean debug = false;


    Random rand  = new Random(8796253 );
    double[] centre;
    double[] origin;
    CartesianPoint centre_cartesian;
    CartesianPoint origin_cartesian;
    List<CartesianPoint> samples;
    List<CartesianPoint> pivots;

    public SweepyAngleExplorer(String dataset_name, int number_samples, int noOfRefPoints) throws Exception {
        super( dataset_name, number_samples, noOfRefPoints,0  );

        this.rand = new Random();

        pivots = super.getRos();
        samples = super.getData();

        this.origin = new double[getDim()];
        this.centre = makePoint( 0.5 );
        this.centre_cartesian = new CartesianPoint(centre);
        this.origin_cartesian = new CartesianPoint(origin);
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
                assert (getMetric().distance(centre_cartesian, random_cartesian) < 0.25);
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
            assert(  getMetric().distance(new CartesianPoint(point), origin_cartesian) < ( i + 0.005 ) ); // close to equal
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
     * Move a point from 0.4 away from the origin up to 0.7 away up a diagonal
     * Measure the angles of points within 0.5 of the query point - TODO make this a param
     */
    public void sweep( double query_radius ) {

        System.out.println("Checking " + angle_calculation_repetitions + " random points, query radius = " + query_radius );
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
                double theta = calculateAngle(origin_cartesian, diagonal_point_cartesian, some_point_cartesian);
                list.add(theta);
            }
        }
        int count = list.size();
        if( count > 0 ) {
            double mean = (double) Util.mean(list);
            double std_dev = Util.stddev(list, mean);
            System.out.println("Distance = " + df.format(diagonal_distance) + " mean angle (degrees) = " + df.format(Math.toDegrees(mean)) + " std_dev = " + df.format(Math.toDegrees(std_dev)) + " n = " + count );
        } else {
            System.out.println( "Distance = " + df.format(diagonal_distance) + " No points in 0-1 range" );
        }
    }

    private void calculatePivotAngles(double diagonal_distance, double query_radius)  {
        List<Double> list = new ArrayList<>();

        double[] diagonal_point = getDiagonalPoint( diagonal_distance );
        CartesianPoint diagonal_point_cartesian = new CartesianPoint(diagonal_point);

        for( int j = 0; j < pivots.size(); j++ ) {
                double theta = calculateAngle(origin_cartesian, diagonal_point_cartesian, pivots.get(j));
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
        int number_samples = 1000000;
        int noOfRefPoints = 200;
        SweepyAngleExplorer sae = new SweepyAngleExplorer( EUC20,number_samples,noOfRefPoints );

        sae.sweep( 0.25 );
    }





}
