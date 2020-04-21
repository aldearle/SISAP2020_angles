package uk.al_richard.experimental.angles;

import coreConcepts.Metric;
import dataPoints.cartesian.CartesianPoint;
import testloads.TestContext;
import uk.al_richard.experimental.angles.contexts.DeCafContext;
import uk.al_richard.experimental.angles.contexts.SiftContext;

import java.text.DecimalFormat;
import java.util.List;

public abstract class CommonBase {

    protected static final String EUC = "Euc";
    protected static final String EUC10 = "Euc10";
    protected static final String EUC20 = "Euc20";
    protected static final String EUC30 = "Euc30";
    protected static final String SIFT = "SIFT";
    protected static final String DECAF = "DECAF";

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

    protected DecimalFormat df = new DecimalFormat("#.##");

    public CommonBase(String dataset_name, int num_data_points, int num_ros, int num_queries) throws Exception {
        this.num_data_points = num_data_points;
        this.num_ros = num_ros;
        this.num_queries = num_queries;
        this.dataset_name = dataset_name;
        if( dataset_name.startsWith(EUC) ) {
            initEuc( dataset_name, num_data_points, num_ros, num_queries );
        } else if( dataset_name.equals(SIFT) ) {
            initSift( num_data_points, num_ros, num_queries );
        } else if( dataset_name.equals(DECAF) ) {
            initDecaf( num_data_points, num_ros, num_queries );
        } else {
            throw new Exception( "Dataset not recognised (" + dataset_name + ")" );
        }

    }

    private void initEuc(String dataset_name, int num_data_points, int num_ros, int num_queries) throws Exception {
        TestContext.Context context;
        if( dataset_name.equals(EUC10)) {
            context = TestContext.Context.euc10;
            dim = 10;
        } else if( dataset_name.equals(EUC20)) {
            context = TestContext.Context.euc20;
            dim = 20;
        } else if( dataset_name.equals(EUC30)) {
            context = TestContext.Context.euc30;
            dim =30;
        } else {
            throw new Exception( "Dataset not recognised (" + dataset_name + ")" );
        }

        TestContext tc = new TestContext(context);
        threshold = tc.getThreshold();
        tc.setSizes(num_queries, num_ros);
        metric = tc.metric();
        data = tc.getData().subList(0,num_data_points);
        queries = tc.getQueries();
        ros = tc.getRefPoints();
    }

    private void initSift(int num_data_points, int num_ros, int num_queries) throws Exception {
        SiftContext tc = new SiftContext();

        tc.setSizes(num_queries, num_ros);
        metric = tc.metric();
        data = tc.getData().subList(0,num_data_points);
        dim = 128;
    }

    private void initDecaf(int num_data_points, int num_ros, int num_queries) throws Exception {
        DeCafContext dc = new DeCafContext();

        dc.setSizes(num_data_points, num_ros, num_queries);
        metric = dc.metric();
        data = dc.getData();
        queries = dc.getQueries();
        ros = dc.getRefPoints();
        dim = 4096;
    }

    protected double getThreshold() throws Exception {
        if( threshold == -1 ) {
            throw new Exception( "threshold not intialised" );
        }
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
