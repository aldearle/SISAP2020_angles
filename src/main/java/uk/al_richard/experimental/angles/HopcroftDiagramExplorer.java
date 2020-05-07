package uk.al_richard.experimental.angles;

import coreConcepts.Metric;
import dataPoints.cartesian.CartesianPoint;

import java.util.Random;

/**
 * To test assertion in Computer Science Theory for the Information Age
 * https://www.cs.cmu.edu/~venkatg/teaching/CStheory-infoage/
 * See diagram in pics/Hopcroft.png from page 28 Fig 2.12.
 *
 * Expand a ball around the centre and see how angles change
 */
public class HopcroftDiagramExplorer extends CommonBase {

    private final Random rand;
    private final Metric<CartesianPoint> metric;

    private boolean debug = false;

    double[] centre;
    CartesianPoint centre_cartesian;
    double root_d;
    double root_2d;


    private final static int repetitions = 5000;

    /**
     * No param constructor for JUnit
     */
    public HopcroftDiagramExplorer() throws Exception {
        super(EUC20,0,0,0 );
        this.rand = new Random();

        this.centre = makePoint( 0.5 );
        this.centre_cartesian = new CartesianPoint(centre);
        this.metric = super.getMetric();
        this.root_d = Math.sqrt( super.getDim() );
        this.root_2d = Math.sqrt( 2 * super.getDim() );
    }

    public HopcroftDiagramExplorer(String dataset_name, int number_samples, int noOfRefPoints) throws Exception {
        super( dataset_name, number_samples, noOfRefPoints,0  );

        this.rand = new Random();

        this.centre = makePoint( 0.5 );
        this.centre_cartesian = new CartesianPoint(centre);
        this.metric = super.getMetric();
        this.root_d = Math.sqrt( super.getDim() );
        this.root_2d = Math.sqrt( 2 * super.getDim() );
    }

    /**
     *
     * @return a point on radius of the ball of specified radius with the specified midpoint
     */
    double[] getRandomSurfacePoint( double[] midpoint, double radius ) {
        double[] res = new double[getDim()];
        double acc = 0;
        for (int i = 0; i < getDim(); i++) {
            double d = this.rand.nextGaussian();
            acc += d * d;
            res[i] = d;
        }
        double magnitude = Math.sqrt(acc);   // the magnitude of the vector
        for (int i = 0; i < getDim(); i++) {
            res[i] = ( radius * res[i] / magnitude ) + midpoint[i];
        }
        return res;
    }

//    @Test
//    public void checkSurfacePoint() {
//        double[] surface_point = getRandomSurfacePoint( centre, 5.0 );
//        CartesianPoint p = new CartesianPoint( surface_point );
//        double d = metric.distance(centre_cartesian, p);
//        System.out.println( "Distance = " + d );
//        assert( d < 5.0001 && d > 4.9999 );
//    }


    /**
     *
     */
    public void checkPoints() {

        System.out.println("Expore Hopcroft " + dataset_name + " " );
        double total = 0.0;
        int count = 1000;
        for( int i = 0; i < count; i++ ) {
            CartesianPoint p1 = new CartesianPoint( getRandomSurfacePoint(centre,root_d) );
         //   System.out.println( "P1 = " + pointToString(p1.getPoint()));
            CartesianPoint p2 = new CartesianPoint( getRandomSurfacePoint(centre,root_d) );
          //  double d_c_p1 = metric.distance( centre_cartesian,p1 );
         //   System.out.println( "dist centre p1 = " + d_c_p1 + " close to? " + root_d );
         //   System.out.println( "P2 = " + pointToString(p2.getPoint()));
            double d = metric.distance(p1, p2);
            total = total + d;
            System.out.println( "dist = " + d + " close to? " + root_2d );
        }
        System.out.println( "Average dist = " + total / count + " close to? " + root_2d );
    }


    public static void main(String[] args) throws Exception {

        int number_samples = 0;
        int noOfRefPoints = 0;
        HopcroftDiagramExplorer sae = new HopcroftDiagramExplorer( EUC20,number_samples,noOfRefPoints );
        sae.checkPoints();
    }


    public static void main1(String[] args) throws Exception {
        int number_samples = 0;
        int noOfRefPoints = 0;

        for( String dataset_name : eucs ) {

            HopcroftDiagramExplorer sae = new HopcroftDiagramExplorer( dataset_name,number_samples,noOfRefPoints );
            sae.checkPoints();

        }
    }




}
