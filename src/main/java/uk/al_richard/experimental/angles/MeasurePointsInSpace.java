package uk.al_richard.experimental.angles;

import dataPoints.cartesian.CartesianPoint;

import java.text.DecimalFormat;
import java.util.List;

public class MeasurePointsInSpace extends CommonBase {

    private final int count;
    private final double thresh;
    private final List<CartesianPoint> data;

    private static DecimalFormat df2 = new DecimalFormat("#.##");

    public MeasurePointsInSpace(String dataset_name, int count ) throws Exception {
        super( dataset_name, count, 0, 0 );
        this.count = count;
        this.thresh = super.getThreshold();
        this.data = getData();
    }

    /**
     * print out the proportion of points that are within unit cube.
     *
     **/
    private void exploreInclusion() {
        System.out.println( dataset_name );

        int inside = 0;
        int outside = 0;

        int comparisons = count * count;

        for (int i = 0; i < count; i++) {

            CartesianPoint query = data.get(i);

            for (int j = 0; j < count; j++) {
                CartesianPoint some_point = new CartesianPoint(getRandomVolumePoint(query.getPoint(), thresh));

                if (insideSpace(some_point)) {
                    inside++;
                } else {
                    outside++;
                }
            }
        }
        System.out.println( "Inside = " + df2.format( inside * 100.0 / comparisons ) + "% Outside = " + df2.format( outside * 100.0 / comparisons ) + "%" );
    }

    public static void main1( String[] args ) throws Exception {

        MeasurePointsInSpace ea = new MeasurePointsInSpace( EUC10,1000  );
        ea.exploreInclusion();
    }

    public static void main( String[] args ) throws Exception {

        for( String dataset_name : eucs ) {
            MeasurePointsInSpace ea = new MeasurePointsInSpace(dataset_name, 1000 );
            ea.exploreInclusion();

        }
    }


}
