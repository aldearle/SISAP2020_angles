package uk.al_richard.experimental.angles;

import coreConcepts.Metric;
import dataPoints.cartesian.CartesianPoint;
import sisap_2017_experiments.NdimSimplex;
import testloads.TestContext;
import uk.al_richard.experimental.angles.contexts.SiftContext;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Generate a Matlab program to visualise distances in a Metric space
 * This generates the 3D version of the 2Dpq diagrams
 *
 * XXXXX
 */
public class Matlab3DDistancesGenerator {

    static final TestContext.Context context = TestContext.Context.euc20;

    private Metric<CartesianPoint> metric;
    private List<CartesianPoint> ros;
    private CartesianPoint ro1;
    private CartesianPoint ro2;
    private List<CartesianPoint> queries;
    private List<CartesianPoint> data;
    private final CartesianPoint query;
    private double thresh;
    private final double inter_ro_distance;
    private final double d_ro1_query;
    private final double d_ro2_query;

    private final CartesianPoint ro1_3D;
    private final CartesianPoint ro2_3D;
    private final CartesianPoint query_3D;

    private NdimSimplex<CartesianPoint> transformSimplex3D; // a Simplex used to create transformed 3D points based on distances
    private String dataset_name;

    /**
     * @param num_data_points - the number of points to sample from
     * @throws Exception - if something goes wrong.
     */
    public Matlab3DDistancesGenerator( int num_data_points, String dataset_name ) throws Exception {

        this.dataset_name = dataset_name;

        if( dataset_name.equals( "Euc20" ) ) {
            initEuc20( num_data_points );
        } else if( dataset_name.equals( "Sift" ) ) {
            initSift(num_data_points);
        }
        query = queries.get(0);
        inter_ro_distance = metric.distance(ro1,ro2);
        d_ro1_query = metric.distance(ro1,query);
        d_ro2_query = metric.distance(ro2,query);

        CartesianPoint ro1_2D = new CartesianPoint( new double[]{ 0,0 } );
        CartesianPoint ro2_2D = new CartesianPoint( new double[]{ inter_ro_distance,0 } );

        NdimSimplex<CartesianPoint> nds = new NdimSimplex(metric,ro1_2D,ro2_2D);

        double[] query_2D_points = nds.getApex(new double[]{d_ro1_query, d_ro2_query});

        // ro1 at origin, to2 at inter_ro_distance,0,0 and query in same plane
        ro1_3D = new CartesianPoint( new double[]{ 0,0,0 } );
        ro2_3D = new CartesianPoint( new double[]{ inter_ro_distance,0,0 } );
        query_3D = new CartesianPoint( new double[]{ query_2D_points[0], query_2D_points[1], 0 } );

        transformSimplex3D = new NdimSimplex(metric,ro1_3D, ro2_3D, query_3D);
    }


    private void initEuc20(int num_data_points) throws Exception {
        TestContext tc = new TestContext(context);

        tc.setSizes(1, 2);
        ros = tc.getRefPoints();
        ro1 = ros.get(0);
        ro2 = ros.get(1);
        metric = tc.metric();
        thresh = tc.getThreshold();
        data = tc.getData().subList(0,num_data_points);
        queries = tc.getQueries();
    }

    private void initSift(int num_data_points) throws Exception {
        SiftContext tc = new SiftContext();

        tc.setSizes(1, 2);
        ros = tc.getRefPoints();
        ro1 = ros.get(0);
        ro2 = ros.get(1);
        metric = tc.metric();
        queries = tc.getQueries();          // only one query
        thresh = tc.getThresholds()[0];     // only one threshold
        data = tc.getData().subList(0,num_data_points);
    }


    /**
     * Actually performs the generation of the matlab visualisation of the space
     */
    private void generate() {

        List<CartesianPoint> in = new ArrayList<>();
        List<CartesianPoint> out = new ArrayList<>();
        System.out.println( "% Generated by: " + this.getClass().toString() + " on: " + Calendar.getInstance().getTime() );
        System.out.println( "% Dataset = " + dataset_name );

        sort( in,out );
        out = filter( out,2500 ); // too many outs get rid of most
        declareLists( in, out );
        declare( "out_size", out.size() );
        declare( "in_size", in.size() );
        declare( "thresh", thresh );

        declare( "d_ro1_query", d_ro1_query );
        declare( "d_ro2_query", d_ro2_query );
        declare( "inter_ro_distance", inter_ro_distance );

        System.out.println();
        // extractPoints( "in","points_in" );
        // extractPoints( "out","points_out" );
        declareXYZs( "in","in", 1, in.size()  );
        int out_start = 3 + in.size() + 1;
        declareXYZs( "out","out", 1, out.size() );
        declarePoint( "q","define query point", query_3D );
        declarePoint( "ro1","define ro1 point", ro1_3D );
        declarePoint( "ro2","define ro2 point", ro2_3D );
        ballDeclarations();
        sheetDeclarations();
        printFigureCode();
    }

    /**
     * Declare a Matlab variable of the form identifier = rhs;
     * @param identifier - the Matlab identifier
     * @param rhs - the Matlab initialising expression
     */
    private void declare(String identifier, String rhs) {
        System.out.println( identifier + " = " + rhs + ";" );
    }

    /**
     * Declare a Matlab variable of the form identifier = rhs;
     * @param identifier - the Matlab identifier
     * @param rhs - the Matlab initialising expression
     */
    private void declare(String identifier, int rhs) {
        declare( identifier, Integer.toString(rhs));
    }

    /**
     * Declare a Matlab variable of the form identifier = rhs;
     * @param identifier - the Matlab identifier
     * @param rhs - the Matlab initialising expression
     */
    private void declare(String identifier, double rhs) {
        declare( identifier, Double.toString(rhs));
    }

    /**
     * Declares a single 3D point in Matlab format with the identifiers id_x, id_y and id_z if
     * id is the identfier supplied.
     * @param identifier - the identifier used to identify the points in the Matlab program
     * @param comment - a comment in the Matlab program associated with the points
     * @param threeD_point - the Java value from which the coordinates are extracted
     */
    private void declarePoint(String identifier, String comment, CartesianPoint threeD_point ) {
        System.out.println();
        System.out.println( "%" + comment );

        declare( identifier + "_x", threeD_point.getPoint()[0] );
        declare( identifier + "_y", threeD_point.getPoint()[1] );
        declare( identifier + "_z", threeD_point.getPoint()[2] );
        System.out.println();
    }

    /**
     * Declares a matrix of 3D points by transformation from the ND space
     * @param identifier - the identifier used to identify the points in the Matlab program
     * @param comment - a comment in the Matlab program associated with the points
     * @param points - the points to be transformed and declared
     */
    private void declarePointsMatrix(String identifier, String comment, List<CartesianPoint> points ) {
        System.out.println();
        System.out.println("%" + comment);
        System.out.print( identifier + " = [" );
        printPoints( points );
        System.out.println( " ];" );
        System.out.println();
    }

    /**
     * Declares two lists of 3D points corresponding to the pre-sorted in and out lists
     * @param in  - the in points (within threshold of query
     * @param out - the out points (outwith threshold of query
     */
    private void declareLists(List<CartesianPoint> in, List<CartesianPoint> out ) {
        declarePointsMatrix( "in","points within threshold", pointsTo3D( in ) );
        declarePointsMatrix( "out","points outwith threshold", pointsTo3D( out ) );
    }

    /**
     * Declares the x,y and z coordinates as prefix_x,prefix_y and prefix_z individually by extracting from a Matlab identifier
     * @param identifier_prefix - the prefix to use for the Matlab identifiers declared
     * @param from_identifier - the Matlab identifier for the points Matrix from which we are extracting
     * @param start - start index from the Matlab points Matrix
     * @param end  - start index from the Matlab points Matrix
     */
    private void declareXYZs(String identifier_prefix, String from_identifier, int start, int end ) {
        for( int i = 0; i < 3; i++ ) {
            declare( identifier_prefix + coordName(i), from_identifier + "(" + start + ":" + end + "," + ( i + 1 ) + ")" );
        }
    }

    /**
     * Makes the necessary Matlab query ball declarations
     */
    private void ballDeclarations() {
        declare( "[x y z]", "sphere" );
        declare( "q_ball","[q_x q_y q_z thresh]" );
    }

    /**
     * Makes the necessary Matlab query ball declarations
     */
    private void sheetDeclarations() {
        declare( "[xx, yy, zz]", "meshgrid(-2:2,-2:2,-2:2)" );
        declare( "diff_d_squared", "abs(((ro1_x - xx).^2 + (ro1_y - yy).^2 + (ro1_z - zz).^2 - (ro2_x - xx).^2 - (ro2_y - yy).^2 - (ro2_z - zz).^2 ))" );
        declare( "four_point_boundary", "2 * thresh * inter_ro_distance" );

    }

    /**
     * Prints a subscript, x,y or z
     * @param i - the selector controlling which subscript is printed
     * @return the subscript
     */
    private String coordName(int i) {
        switch( i ) {
            case 0  : return "_x";
            case 1  : return "_y";
            case 2  : return "_z";
            default : return "ERROR_IN_GEN";
        }
    }

    /**
     * Converts a list of ND points to 3D using ro1,ro2 and query as the bottom of a 3D simplex of 4 points.
     * @param points
     * @return a list of 3D cartesian points
     */
    private List<CartesianPoint> pointsTo3D(List<CartesianPoint> points) {
        ArrayList<CartesianPoint> result = new ArrayList<>();  // 3D space
        for( CartesianPoint p : points ) { // in N-D space
            double[] distances_to_p = new double[]{ metric.distance( p,ro1 ), metric.distance( p,ro2 ), metric.distance( p,query ) };
            CartesianPoint transformed = new CartesianPoint( transformSimplex3D.getApex(distances_to_p) );
            result.add(transformed);
        }
        return result;
    }

    /**
     * print out the Matlab representation of points from a list
     * @param points - the points to be printed
     */
    private void printPoints(List<CartesianPoint> points) {
        for( CartesianPoint p : points ) {
            printPoint( p );
        }
    }

    /**
     * print out the Matlab representation of a single point
     * @param p - the point to be printed
     */
    private void printPoint(CartesianPoint p) {
        double[] point_values = p.getPoint();
        for( int i = 0; i < point_values.length ; i++ ) {
            System.out.print( " " + point_values[i] );
        }
        System.out.println( ";" );
    }

    /**
     * Filter out 1 in sample_rate of the points
     * @param points - the points to be filtered
     * @param sample_rate - how many points to include the number printed is 1/sample_rate
     * @return teh filtered list
     */
    private List<CartesianPoint> filter(List<CartesianPoint> points, int sample_rate) {

        List<CartesianPoint> result = new ArrayList<>();

        int count = 0;
        for( CartesianPoint p : points ) {

            if( count % sample_rate == 0 ) {
                result.add( p );
            }
            count++;
        }

        return result;
    }

    private void printFigureCode() {
        System.out.println("figure");
        System.out.println("hold on");
        System.out.println("scatter3( in_x, in_y, in_z, 'r', 'filled')");  // red in
        System.out.println("scatter3( out_x, out_y, out_z, 'g', 'filled')"); // green out
        System.out.println("scatter3( ro1_x, ro1_y, ro1_z, 'm', 'filled')" );
        System.out.println("scatter3( ro2_x, ro2_y, ro2_z, 'm', 'filled')" );
        System.out.println("scatter3( q_x, q_y, q_z, 'b', 'filled');" );
        System.out.println("ball=surf(x*q_ball(1,4)+q_ball(1,1),y*q_ball(1,4)+q_ball(1,2),z*q_ball(1,4)+q_ball(1,3));" );
        System.out.println("set(ball, 'FaceAlpha', 0.4);" );
        System.out.println("set(ball, 'FaceColor', [0 0.2 0.7] ); %RGB: pale blue/green" );
        declare( "sheet", "patch( isosurface(xx,yy,zz,diff_d_squared,four_point_boundary) )" );
        System.out.println("set( sheet,'FaceColor','yellow');" );
        System.out.println("set( sheet,'FaceAlpha', 0.4);" );
        System.out.println("set( sheet, 'EdgeColor', 'none' );" );
        System.out.println("hold off");
        System.out.println("view(10, 14)");
        System.out.println("title('3D Distances for " + dataset_name + "')");
        System.out.println("xlabel('X')");
        System.out.println("ylabel('Y')");
        System.out.println("zlabel('Z')");
    }

    /**
     * Partitions the points into those points that are solns to the query and those that are not
     * @param in - an empty list filled with those points that are within the threshold
     * @param out - an empty list filled with those points that are outwith the threshold
     */
    private void sort(List<CartesianPoint> in, List<CartesianPoint> out) {
        for( CartesianPoint p : data ) {
            if( metric.distance(query,p) < thresh ) {
                in.add(p);
            } else {
                out.add(p);
            }
        }
    }


    public static void main( String[] args ) throws Exception {
        Matlab3DDistancesGenerator m2pg = new Matlab3DDistancesGenerator( 500000,"Euc20" );
        m2pg.generate();
    }
}
