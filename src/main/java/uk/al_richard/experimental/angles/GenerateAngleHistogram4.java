package uk.al_richard.experimental.angles;

import coreConcepts.Metric;
import dataPoints.cartesian.CartesianPoint;

import static uk.al_richard.experimental.angles.Util.square;

/**
 * This is the fourth version of GenerateAngleHistogram
 * @author al@st-andrews.ac.uk
 *
 * It takes a viewpoint radomly chose from Euc space,
 * a random point in the Euc space and
 * a third point within a sphere that is constrained to be in the cube.
 */
public class GenerateAngleHistogram4 extends CommonBase {

    private final int count;
    private final double thresh;

    public GenerateAngleHistogram4(String dataset_name, int count ) throws Exception {
        super( dataset_name, count * 3, 0, 0 );
        this.count = count;
        this.thresh = super.getThreshold();
    }

    /**
     * print out all of the angles from count points drawn from the dataset from the viewpoint
     *
     **/
    private void generateAngles( boolean constrained ) {
        System.out.println( dataset_name + " " + constrained );

        CartesianPoint[] eucs_array = new CartesianPoint[0];
        eucs_array = getData().toArray( eucs_array ); // count * count + 2 * count
        int len = eucs_array.length;

        Metric<CartesianPoint> metric = getMetric();

        for( int v = 0; v < count; v++ ) {

            CartesianPoint viewpoint = eucs_array[v];

            for (int i = count ; i < count * 2; i++) {

                CartesianPoint query = eucs_array[i];
                double d_view_q = getMetric().distance(viewpoint, query);

                double max_radius = 1 - getMaxCoordinate( query ); // biggest radius we can tolerate and not go outside the cube.
                double min_radius = getMinCoordinate( query );
                double radius = Math.max( max_radius, min_radius );

                for (int j = (count * 2); j < count * 3; j++) {

                    CartesianPoint some_point;
                    if (constrained) {
                        some_point = new CartesianPoint(getRandomVolumePoint(query.getPoint(), radius));
                    } else {
                        some_point = eucs_array[j];
                    }

                    double d_query_somepoint = metric.distance(query, some_point);
                    double d_view_somepoint = metric.distance(viewpoint, some_point);

                    double theta = Math.acos((square(d_query_somepoint) + square(d_view_q) - square(d_view_somepoint)) / (2 * d_query_somepoint * d_view_q));

                    System.out.println(theta);
                }
            }
        }
    }

    /**
     * @param point
     * @return the smallest coordinate in the point.
     */
    private double getMinCoordinate(CartesianPoint point) {
        double[] doubles = point.getPoint();
        double min = Double.MAX_VALUE;
        for( double next_double : doubles ) {
            if( next_double < min ) {
                min = next_double;
            }
        }
        return min;
    }

    /**
     * @param point
     * @return the largest coordinate in the point.
     */
    private double getMaxCoordinate(CartesianPoint point) {
        double[] doubles = point.getPoint();
        double max = Double.MIN_VALUE;
        for( double next_double : doubles ) {
            if( next_double > max ) {
                max = next_double;
            }
        }
        return max;
    }

    public static void main( String[] args ) throws Exception {

        GenerateAngleHistogram4 ea = new GenerateAngleHistogram4( EUC30,50  );
        ea.generateAngles( false );
    }

    public static void main1( String[] args ) throws Exception {

        for( String dataset_name : eucs ) {
            GenerateAngleHistogram4 ea = new GenerateAngleHistogram4(dataset_name, 100);
            ea.generateAngles( false );
            ea.generateAngles( true );

        }
    }


}
