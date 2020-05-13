package uk.al_richard.experimental.angles;

import coreConcepts.Metric;
import dataPoints.cartesian.CartesianPoint;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SweepyIDIMExplorer extends CommonBase {

    private boolean debug = false;

    private final Metric<CartesianPoint> metric;
    private final List<CartesianPoint> samples;
    private final List<CartesianPoint> pivots;
    private final int dim;

    double[] centre;
    double[] origin;
    CartesianPoint centre_cartesian;
    CartesianPoint origin_cartesian;

    public SweepyIDIMExplorer(String dataset_name, int number_samples, int noOfRefPoints) throws Exception {
        super( dataset_name, number_samples, noOfRefPoints,0  );

        this.rand = new Random();

        pivots = super.getRos();
        samples = super.getData();
        metric = super.getMetric();
        dim = super.getDim();

        this.origin = new double[dim];
        this.centre = makePoint( 0.5 );
        this.centre_cartesian = new CartesianPoint(centre);
        this.origin_cartesian = new CartesianPoint(origin);
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


    @Test
    public void testDiagnonalPoints() {

        for( double i = 0.1; i < 1; i += 0.1 ) {
            double[] point = getDiagonalPoint( i );
            assert(  metric.distance(new CartesianPoint(point), origin_cartesian) < ( i + 0.005 ) ); // close to equal
        }
    }

    private final static int idim_calculation_repetitions = 1000000;


    /**
     * Move a point from 0.4 away from the origin up to 0.7 away up a diagonal
     * Measure the angles of points within 0.5 of the query point - TODO make this a param
     */
    public void sweep( double query_radius ) {

        System.out.println("Checking " + dataset_name + " " + idim_calculation_repetitions + " random points, query radius = " + query_radius + " pivots = " + pivots.size() );

        System.out.println( "diagonal_distance" + "\t" + "lidim" + "\t" + "count" + "\t" + "piv iDIM" + "\t" + "count"  );
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
        double lidim = Util.LIDimLevinaBickel(dists);
        System.out.print( df.format( diagonal_distance ) + "\t" + lidim + "\t" + count );
    }


    private void calculatePivotBasedLocalIDIM(List<CartesianPoint> pivots, double diagonal_distance) throws Exception {
        List<Double> dists = getPivotDists(pivots, diagonal_distance);

        int count = dists.size();
        double lidim = Util.LIDimLevinaBickel(dists);
        System.out.println( "\t" + lidim + "\t" + count );
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

    public static void main(String[] args) throws Exception {
        int number_samples = 999500; // 1M less 500
        int noOfRefPoints = 500;

        SweepyIDIMExplorer sie = new SweepyIDIMExplorer(EUC20,number_samples,noOfRefPoints );
        sie.sweep( 1.8 );
    }



    public static void main1(String[] args) throws Exception {
        int number_samples =  999800; // 1M less 200
        int noOfRefPoints = 200;

        for( String dataset_name : eucs ) {

            SweepyIDIMExplorer sie = new SweepyIDIMExplorer(dataset_name,number_samples,noOfRefPoints );
            double query_radius;
            if( dataset_name.equals( EUC10 ) ) {
                query_radius = 1;
            } else  if( dataset_name.equals( EUC20 ) ) {
                query_radius = 1.8;
            } else  if( dataset_name.equals( EUC30 ) ) {
                query_radius = 2.3;
            } else {
                System.out.println( "Unrecognised dataset name: " + dataset_name );
                return;
            }
            sie.sweep( query_radius );

        }
    }




}
