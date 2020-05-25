package uk.al_richard.experimental.angles.MSCDependent.MarginBlasterDeepCopy;

import eu.similarity.msc.core_concepts.Metric;
import eu.similarity.msc.data.cartesian.CartesianPoint;

import java.text.DecimalFormat;
import java.util.List;

public class TestMargins extends CommonBase {

    public static final String DECAF = "DECAF";
    public static final String MIRFLKR = "MIRFLKR";

    private static DecimalFormat df4 = new DecimalFormat("#.####");


    public TestMargins(String dataset_name, int num_data_points, int num_ros, int num_queries) throws Exception {

        super( dataset_name, num_data_points, num_ros, 0 );

    }

    public void doComp(String label, boolean balls, boolean sheets, boolean four_point, double factor) throws Exception {

        System.out.println( label + " Testing Margins(" + balls + "," + sheets + "," + factor + ") data size " + num_data_points + " data entries " + num_ros + " pivots " + num_queries + " queries " );

        List<CartesianPoint> dat = getData();
        List<CartesianPoint> queries = getQueries();
        Metric<CartesianPoint> metric = getMetric();
        double thresh = getThreshold();

        List<CartesianPoint> pivots = dat.subList(0, num_ros);
        List<CartesianPoint> data = dat.subList(num_ros,dat.size());

        System.out.println( "initialising LIDIM Map");

        LIDIMtoAngleMap sae = new LIDIMtoAngleMap(dataset_name, num_data_points, num_ros );

        System.out.println( "initialising BB");

        MetricMarginBlaster<CartesianPoint> bitblaster = new MetricMarginBlaster<>(metric::distance, pivots, data, balls, sheets,four_point );

        System.out.println( "Running queries");

        int i = 1;

        int list1_count = 0;
        int list2_count = 0;

        for( CartesianPoint q : queries ) {

            List<Double> dists = sae.getDists(pivots, q.getPoint());
            double d = sae.adjustedQueryRadius(q, dists, thresh, factor);
            double m = sae.margin(q,dists,thresh,factor);

            List<DataDistance<CartesianPoint>> list1 = bitblaster.rangeSearch(q, thresh);
            System.out.print("q" + i + ": " + list1.size());

            List<DataDistance<CartesianPoint>> list2 = bitblaster.rangeSearchMargin(q, m, thresh);
            System.out.print(" q adjusted: " + ": " + list2.size());

            i++;

            list1_count += list1.size();
            list2_count += list2.size();

            if( list1.size() == list2.size() ) {
                System.out.println( " Match" );
            } else {
                System.out.println( " Error: " + df4.format( (double) list2.size() / (double) list1.size() ) );
            }

        }
        bitblaster.printDiagnostics();
        System.out.println( "Overall: " + df4.format( (double) list2_count / (double) list1_count ) );

    }

    public static void main( String[] args ) throws Exception {
        boolean balls = true;
        boolean sheets = true;
        boolean four_point = true;
        double factor = 3.0;


        TestMargins tms = new TestMargins(DECAF, 1000000, 200, 100);
        tms.doComp( "EUC20 B+S+4P+f3.0 ", balls, sheets, four_point, factor );
    }
}
