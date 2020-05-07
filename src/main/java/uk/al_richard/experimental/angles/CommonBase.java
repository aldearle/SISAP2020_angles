package uk.al_richard.experimental.angles;

import coreConcepts.Metric;
import dataPoints.cartesian.CartesianPoint;
import org.junit.Test;
import testloads.TestContext;
import uk.al_richard.experimental.angles.contexts.DeCafContext;
import uk.al_richard.experimental.angles.contexts.EucN;
import uk.al_richard.experimental.angles.contexts.SiftContext;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Random;

public abstract class CommonBase {

    protected static final String EUC = "Euc";
    protected static final String EUC10 = "Euc10";
    protected static final String EUC20 = "Euc20";
    protected static final String EUC30 = "Euc30";
    protected static final String SIFT = "SIFT";
    protected static final String DECAF = "DECAF";

    public static final String[] eucs = new String[] { EUC10,EUC20,EUC30 };
    public static final String[] datasets = new String[] { EUC10,EUC20,EUC30,SIFT,DECAF };

    protected final int num_data_points;
    protected final int num_ros;
    protected final int num_queries;
    protected final String dataset_name;

    private Metric<CartesianPoint> metric;
    private List<CartesianPoint> data;
    private List<CartesianPoint> queries;
    private List<CartesianPoint> ros;
    private double threshold = -1;
    private int dim;

    protected Random rand  = new Random(8796253 );

    protected DecimalFormat df = new DecimalFormat("#.##");

    public CommonBase(String dataset_name, int num_data_points, int num_ros, int num_queries) throws Exception {
        this.num_data_points = num_data_points;
        this.num_ros = num_ros;
        this.num_queries = num_queries;
        this.dataset_name = dataset_name;
        if( dataset_name.equals(SIFT) ) {
            initSift( num_data_points, num_ros, num_queries );
        } else if( dataset_name.equals(DECAF) ) {
            initDecaf( num_data_points, num_ros, num_queries );
        } else if( dataset_name.startsWith(EUC) ) {
            initEuc( dataset_name, num_data_points, num_ros, num_queries );
        } else {
            throw new Exception( "Dataset not recognised (" + dataset_name + ")" );
        }

    }

    private void initEuc(String dataset_name, int num_data_points, int num_ros, int num_queries) throws Exception {
        TestContext.Context context;
        if( dataset_name.equals(EUC10)) {
            context = TestContext.Context.euc10;
            initContext( context, 10 );
        } else if( dataset_name.equals(EUC20)) {
            context = TestContext.Context.euc20;
            initContext( context, 20 );
        } else if( dataset_name.equals(EUC30)) {
            context = TestContext.Context.euc30;
            initContext( context, 30 );

        } else {
            String string_dim = dataset_name.substring( 3 );
            dim = Integer.parseInt( string_dim );
            initEucN( new EucN( dim, num_data_points, num_ros, num_queries  ) );
        }
    }

    private void initEucN(EucN euc_n) throws Exception {
        threshold = euc_n.getThreshold();
        metric = euc_n.metric();
        data = euc_n.getData().subList(0, num_data_points);
        queries = euc_n.getQueries();
        ros = euc_n.getRefPoints();
    }

    private void initContext(TestContext.Context context, int dim) throws Exception {

        TestContext tc = new TestContext(context);
        threshold = tc.getThreshold();
        tc.setSizes(num_queries, num_ros);
        metric = tc.metric();
        data = tc.getData().subList(0, num_data_points);
        queries = tc.getQueries();
        ros = tc.getRefPoints();
        this.dim = dim;
    }

    private void initSift(int num_data_points, int num_ros, int num_queries) throws Exception {
        SiftContext tc = new SiftContext();

        tc.setSizes(num_queries, num_ros);
        metric = tc.metric();
        data = tc.getData().subList(0,num_data_points);
        ros = tc.getRefPoints();
        threshold = tc.getThreshold();
        dim = 128;
    }

    private void initDecaf(int num_data_points, int num_ros, int num_queries) throws Exception {
        DeCafContext tc = new DeCafContext();

        tc.setSizes(num_data_points, num_ros, num_queries);
        metric = tc.metric();
        data = tc.getData();
        queries = tc.getQueries();
        ros = tc.getRefPoints();
        threshold = tc.getThreshold();
        dim = 4096;
    }

    ///--------------------------- utility methods ---------------------------

    public double[] makePoint(double coordinate) {
        double[] point = new double[this.dim];
        for (int i = 0; i < dim; i++) {
            point[i] = coordinate;
        }
        return point;
    }

    /**
     *
     * @return a point within radius of the midpoint specified
     */
    double[] getRandomVolumePoint( double[] midpoint, double radius ) {
        double[] res = new double[dim];
        double[] temp = new double[dim + 2];
        double acc = 0;
        for (int i = 0; i < dim + 2; i++) {
            double d = rand.nextGaussian();
            acc += d * d;
            temp[i] = d;
        }
        double magnitude = Math.sqrt(acc);   // the magnitude of the vector
        for (int i = 0; i < dim; i++) {
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
    public double[] getDiagonalPoint(double distance_from_o) {

        double coordinate = Math.sqrt( Math.pow(distance_from_o,2) / getDim() );
        return makePoint( coordinate );
    }

    /**
     *
     * @param pivot
     * @param query
     * @param some_point
     * @return the angle in RADIANS.
     */
    public double calculateAngle(CartesianPoint pivot, CartesianPoint query, CartesianPoint some_point) {

        Metric<CartesianPoint> metric = getMetric();

        double dpq =  metric.distance( pivot,query );
        double dqpi = metric.distance( query,some_point );
        double p1pi = metric.distance( pivot,some_point );

        double theta = Math.acos( ( square(dqpi) + square(dpq) - square(p1pi) ) / (2 * dqpi * dpq ) );

//        System.out.println(df.format(dpq) + "\t" + df.format(p1pi) + "\t" + df.format(dqpi) + "\t" + df.format(Math.toDegrees(theta)));

        return theta;
    }

    public boolean insideSpace(CartesianPoint some_point_cartesian) {
        double[] point = some_point_cartesian.getPoint();
        for( int i = 0; i < point.length; i++ ) {
            if( point[i] < 0.0 || point[i] > 1.0 ) { // inclusive?
                return false;
            }
        }
        return true;
    }

    public String pointToString(double[] point) {
        StringBuilder sb = new StringBuilder();
        sb.append( "[" );
        for( int i = 0; i < point.length; i++ ) {
            sb.append( df.format( point[i] ) );
            sb.append( "," );
        }
        sb.deleteCharAt( sb.lastIndexOf(",") );
        sb.append( "]" );
        return sb.toString();
    }

    private static double square( double x ) { return x * x; }

    ///--------------------------- Getters ---------------------------

    protected double getThreshold()  {
        return threshold;
    }

    public int getDim() {
        return dim;
    }

    public Metric<CartesianPoint> getMetric() {
        return metric;
    }

    public List<CartesianPoint> getData() {
        return data;
    }

    public List<CartesianPoint> getQueries() {
        return queries;
    }

    public List<CartesianPoint> getRos() {
        return ros;
    }

}
