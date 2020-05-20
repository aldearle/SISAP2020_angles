package uk.al_richard.experimental.angles;

import coreConcepts.Metric;
import dataPoints.cartesian.CartesianPoint;

import static uk.al_richard.experimental.angles.Util.square;

public class GenerateAngleHistogram extends CommonBase {

    private final CartesianPoint viewpoint;
    private final int count;
    private final double thresh;

    public GenerateAngleHistogram(String dataset_name, int count ) throws Exception {
        super( dataset_name, (count * count) + count, 0, 0 );
        this.count = count;
        this.thresh = super.getThreshold();
        this.viewpoint = new CartesianPoint(makePoint( 0.5 ));
    }

    /**
     * print out all of the angles from count points drawn from the dataset from the viewpoint
     *
     **/
    private void generateAngles( boolean constrained ) {
        System.out.println( dataset_name + " " + constrained );

        CartesianPoint[] eucs_array = new CartesianPoint[0];
        eucs_array = getData().toArray( eucs_array ); // count * count + count
        int len = eucs_array.length;

        Metric<CartesianPoint> metric = getMetric();

        for (int i = 0; i < count; i++) {

            double d_viewpoint_q = getMetric().distance(viewpoint, eucs_array[i]);
            for (int j = count + (count * i); j < ( 2 * count ) + (count * i); j++ ) {

                CartesianPoint query = eucs_array[i];
                CartesianPoint some_point;
                if( constrained ) {
                    some_point = new CartesianPoint(getRandomVolumePoint(query.getPoint(), thresh));
                    while( ! insideSpace( query ) ) {
                        some_point = new CartesianPoint(getRandomVolumePoint(query.getPoint(), thresh));
                    }
                } else {
                    some_point = eucs_array[j];
                }

                double dqpi = metric.distance( query,some_point );
                double p1pi = metric.distance( viewpoint,some_point );

                double theta = Math.acos( ( square(dqpi) + square(d_viewpoint_q) - square(p1pi) ) / (2 * dqpi * d_viewpoint_q ) );

                System.out.println(theta);
            }
        }
    }

    public static void main( String[] args ) throws Exception {

        GenerateAngleHistogram ea = new GenerateAngleHistogram( EUC20,100  );
        ea.generateAngles( true );
    }

    public static void main1( String[] args ) throws Exception {

        for( String dataset_name : eucs ) {
            GenerateAngleHistogram ea = new GenerateAngleHistogram(dataset_name, 100);
            ea.generateAngles( false );
            ea.generateAngles( true );

        }
    }


}
