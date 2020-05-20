package uk.al_richard.experimental.angles;

import coreConcepts.Metric;
import dataPoints.cartesian.CartesianPoint;

import static uk.al_richard.experimental.angles.Util.square;

public class GenerateAngleHistogram3 extends CommonBase {

    private final int count;
    private final double thresh;

    public GenerateAngleHistogram3(String dataset_name, int count ) throws Exception {
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

                for (int j = (count * 2); j < count * 3; j++) {

                    CartesianPoint some_point;
                    if (constrained) {
                        some_point = new CartesianPoint(getRandomVolumePoint(query.getPoint(), thresh));
                        while (!insideSpace(query)) {
                            some_point = new CartesianPoint(getRandomVolumePoint(query.getPoint(), thresh));
                        }
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

    public static void main( String[] args ) throws Exception {

        GenerateAngleHistogram3 ea = new GenerateAngleHistogram3( EUC10,50  );
        ea.generateAngles( true );
    }

    public static void main1( String[] args ) throws Exception {

        for( String dataset_name : eucs ) {
            GenerateAngleHistogram3 ea = new GenerateAngleHistogram3(dataset_name, 100);
            ea.generateAngles( false );
            ea.generateAngles( true );

        }
    }


}
