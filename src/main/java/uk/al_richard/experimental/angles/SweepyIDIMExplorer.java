package uk.al_richard.experimental.angles;

import coreConcepts.Metric;
import dataPoints.cartesian.CartesianPoint;
import dataPoints.cartesian.Euclidean;
import org.junit.Test;
import testloads.TestContext;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static uk.al_richard.experimental.angles.Util.idim;

public class SweepyIDIMExplorer {

    private static int dim = 20;

    private boolean debug = false;
    private DecimalFormat df = new DecimalFormat("#.##");

    Random rand  = new Random(8796253 );
    double[] centre;
    double[] origin;
    CartesianPoint centre_cartesian;
    CartesianPoint origin_cartesian;
    Metric<CartesianPoint> metric;
    List<CartesianPoint> samples;
    List<CartesianPoint> pivots;

    public SweepyIDIMExplorer() throws Exception {

        int number_samples = 1000000;
        int noOfRefPoints = 200;

        // samples = generateSamples(number_samples);

        TestContext.Context context = TestContext.Context.euc20;

        TestContext tc = new TestContext(context,number_samples );

        tc.setSizes(0, noOfRefPoints);
        pivots = tc.getRefPoints();
        samples = tc.getData();

        this.origin = new double[this.dim];
        this.centre = makePoint( 0.5 );
        this.centre_cartesian = new CartesianPoint(centre);
        this.origin_cartesian = new CartesianPoint(origin);
        this.metric = new Euclidean<>();
    }

    // generate sample points in the 0,1 space
    private List<CartesianPoint> generateSamples(int count ) {

        List<CartesianPoint> samples = new ArrayList<>();

        for(int i = 0; i < count; i++ ) {
            double[] point = new double[dim];
            for( int coord = 0; coord < dim; coord++ ) {
                point[coord] = rand.nextDouble();
            }
            samples.add( new CartesianPoint( point ) );
        }
        return samples;
    }

    /**
     *
     * @return a point within radius of the midpoint specified
     */
    double[] getRandomVolumePoint( double[] midpoint, double radius ) {
        double[] res = new double[this.dim];
        double[] temp = new double[this.dim + 2];
        double acc = 0;
        for (int i = 0; i < this.dim + 2; i++) {
            double d = rand.nextGaussian();
            acc += d * d;
            temp[i] = d;
        }
        double magnitude = Math.sqrt(acc);   // the magnitude of the vector
        for (int i = 0; i < this.dim; i++) {
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
                assert (metric.distance(centre_cartesian, random_cartesian) < 0.25);
            }
        }
    }

    /**
     *
     * @param distance_from_o - this distance from the origin
     * @return a point on the diagonal that distance from the origin
     */
    private double[] getDiagonalPoint(double distance_from_o ) {

        double coordinate = Math.sqrt( Math.pow(distance_from_o,2) / dim );
        return makePoint( coordinate );
    }

    @Test
    public void testDiagnonalPoints() {

        for( double i = 0.1; i < 1; i += 0.1 ) {
            double[] point = getDiagonalPoint( i );
            assert(  metric.distance(new CartesianPoint(point), origin_cartesian) < ( i + 0.005 ) ); // close to equal
        }
    }

    private double[] makePoint( double coordinate ) {
        double[] point = new double[this.dim];
        for (int i = 0; i < dim; i++) {
            point[i] = coordinate;
        }
        return point;
    }

    private static double square( double x ) { return x * x; }

    private final static int idim_calculation_repetitions = 1000000;


    /**
     * Move a point from 0.4 away from the origin up to 0.7 away up a diagonal
     * Measure the angles of points within 0.5 of the query point - TODO make this a param
     */
    public void sweep( double query_radius ) {

        System.out.println("Checking " + idim_calculation_repetitions + " random points, query radius = " + query_radius );

        for( double diagonal_distance = 0.01; diagonal_distance < Math.sqrt( dim ) ; diagonal_distance += 0.01 ) {
            try {
                calculateLocalIDIM(samples, diagonal_distance, query_radius);
                calculatePivotBasedLocalIDIM(pivots, diagonal_distance);
            } catch( Exception e ) {
                System.out.println( "Distance = " + df.format( diagonal_distance ) + " no points in range" );
            }
        }
    }

    private void calculateLocalIDIM( List<CartesianPoint> samples, double diagonal_distance, double query_radius) throws Exception {
        List<Double> dists = getDists(samples, diagonal_distance,query_radius);

        int count = dists.size();
        double lidim = idim(dists);
        System.out.print( "Distance = " + df.format( diagonal_distance ) + " LIDIM = " + lidim + " n = " + count );
    }


    private void calculatePivotBasedLocalIDIM(List<CartesianPoint> pivots, double diagonal_distance) throws Exception {
        List<Double> dists = getPivotDists(pivots, diagonal_distance);

        int count = dists.size();
        double lidim = idim(dists);
        System.out.println( " Pivot LIDIM = " + lidim + " n = " + count );
    }


    private List<Double> getDists(List<CartesianPoint> samples, double diagonal_distance, double query_radius ) {
        List<Double> dists = new ArrayList<>();

        double[] diagonal_point = getDiagonalPoint( diagonal_distance );
        CartesianPoint diagonal_point_cartesian = new CartesianPoint(diagonal_point);

        for(int i = 0; i < samples.size(); i++ ) {
            double d = metric.distance( samples.get(i),diagonal_point_cartesian );
            if( d < query_radius ) {
                dists.add(d);
            }
        }
        return dists;
    }

    private List<Double> getPivotDists(List<CartesianPoint> pivots, double diagonal_distance) {
        List<Double> dists = new ArrayList<>();

        double[] diagonal_point = getDiagonalPoint( diagonal_distance );
        CartesianPoint diagonal_point_cartesian = new CartesianPoint(diagonal_point);

        for(int i = 0; i < pivots.size(); i++ ) {
            double d = metric.distance( pivots.get(i),diagonal_point_cartesian );
            dists.add(d);
        }
        return dists;
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
        SweepyIDIMExplorer sie = new SweepyIDIMExplorer();
        sie.sweep( 1.5 );
    }





}
