package uk.al_richard.experimental.angles;

import coreConcepts.Metric;
import dataPoints.cartesian.CartesianPoint;
import testloads.TestContext;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.DataDistance;

import java.text.DecimalFormat;
import java.util.List;

public class TestMargins {

    public boolean balls = true;
    public boolean sheets = true;
    public double factor = 3.0;

    public TestMargins() {
    }

    private static DecimalFormat df4 = new DecimalFormat("#.####");

    public void doComp(String label, boolean fourPoint, int data_size, int num_pivots, int num_queries, TestContext.Context context) throws Exception {

        System.out.println( label + " Testing Margins(" + balls + "," + sheets + "," + factor + ") data size " + data_size + " data entries " + num_pivots + " pivots " + num_queries + " queries " );

        TestContext tc = new TestContext(context, data_size);
        tc.setSizes(num_queries, num_pivots);

        List<CartesianPoint> dat = tc.getData();
        List<CartesianPoint> queries = tc.getQueries();
        Metric<CartesianPoint> metric = tc.metric();
        double thresh = tc.getThreshold();

        List<CartesianPoint> pivots = dat.subList(0, num_pivots);
        List<CartesianPoint> data = dat.subList(num_pivots,dat.size());

        System.out.println( "initialising LIDIM Map");

        LIDIMtoAngleMap sae = new LIDIMtoAngleMap(data, pivots);

        System.out.println( "initialising BB");

        MetricMarginBlaster<CartesianPoint> bitblaster = new MetricMarginBlaster<>(metric::distance, pivots, data, balls, sheets,fourPoint );

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
        TestMargins tms = new TestMargins();
        tms.doComp("Balls and 4P Sheets", true, 1000000, 200, 100, TestContext.Context.euc20);
    }
}
