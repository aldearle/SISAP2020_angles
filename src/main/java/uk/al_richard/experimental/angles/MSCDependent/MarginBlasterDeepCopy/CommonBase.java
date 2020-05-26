package uk.al_richard.experimental.angles.MSCDependent.MarginBlasterDeepCopy;


import eu.similarity.msc.core_concepts.Metric;
import eu.similarity.msc.data.cartesian.CartesianPoint;
import testloads.GetSiftData;

import java.text.DecimalFormat;
import java.util.*;

public abstract class CommonBase {

    protected static final String DECAF = "DECAF";
    protected static final String MIRFLKR = "MIRFLKR";

    protected static final String DECAF_BASE_DIR = "/Volumes/Data/profiset";
    protected static final String MIRLKR_BASE_DIR = "/Volumes/Data/mf_fc6";

    protected final int num_data_points;
    protected final int num_ros;
    protected final int num_queries;
    protected final String dataset_name;

    private Metric<CartesianPoint> metric;
    private List<CartesianPoint> data;
    private List<CartesianPoint> queries;
    private List<CartesianPoint> ros;
    private double threshold = -1;

    protected Map<Integer, int[]> nn_map;
    protected Map<Integer, float[]> data_map;
    protected Map<Integer, float[]> query_map;
    private Map<Integer, float[]> reference_object_map;
    private Map<Integer, Double> threshold_map;

    private int dim;

    protected Random rand = new Random(8796253);

    protected DecimalFormat df = new DecimalFormat("#.##");


    public CommonBase(String dataset_name, int num_data_points, int num_ros, int num_queries) throws Exception {

        this.num_data_points = num_data_points;
        this.num_ros = num_ros;
        this.num_queries = num_queries;
        this.dataset_name = dataset_name;
        this.metric = new Euclidean<>();

        if (dataset_name.equals(MIRFLKR)) {
            initMirFlkr(num_data_points, num_ros, 0);
//        } else if( dataset_name.equals(DECAF) ) {
//            initDecaf( num_data_points, num_ros, 0 );
        } else {
            throw new Exception("Dataset not recognised (" + dataset_name + ")");
        }
    }


    private void initMirFlkr(int num_data_points, int num_ros, int num_queries) throws Exception {
        GetSiftData mfd = new GetSiftData(MIRLKR_BASE_DIR);

        nn_map = mfd.getNNIds();
        data_map = mfd.getData();
        query_map = mfd.getQueries();

        setSizes(num_queries, num_ros);

        data = initData();
        queries = initQueries();
        ros = initRefPoints();

        dim = 128; // What is DIM?
    }


//    private void initDecaf(int num_data_points, int num_ros, int num_queries) throws Exception {
//        GetDeCafData dcd = new GetDeCafData(DECAF_BASE_DIR);
//
//        nn_map = dcd.getNNIds();
//        data_map = dcd.getData();
//        query_map = dcd.getQueries();
//
//        setSizes( num_queries, num_ros );
//
//        initData();
//        initQueries();
//        initRefPoints();
//
//        dim = 4096;
//    }

    public void setSizes(int num_queries, int num_ref_points) throws Exception {
        System.out.println("");
        if (num_queries > query_map.size()) {
            throw new Exception("Too many queries requested");
        }
        if (num_ref_points > data_map.size()) {
            throw new Exception("Too many ref points requested");
        }
        trimQueriesANDThresholds(num_queries);
        reference_object_map = createRosFromDataMap(num_ref_points);
        System.out.println("");
    }

    private void trimQueriesANDThresholds(int num_queries) {
        int count = 0;
        Map<Integer, float[]> new_query_map = new HashMap<>();
        Map<Integer, Double> new_theshold_map = new HashMap<>();
        // Is there a better way to do this avoiding map update problems
        // first add entries to reference_object_map
        for (Integer index : query_map.keySet()) {
            if (count < num_queries) {
                new_query_map.put(index, query_map.get(index));
                new_theshold_map.put(index, threshold_map.get(index));
            }
            count++;
        }
        query_map = new_query_map;
        threshold_map = new_theshold_map;
    }

    private Map<Integer, float[]> createRosFromDataMap(int num_ref_points) {
        Map<Integer, float[]> new_ro_map = new HashMap<>();
        int count = 0;
        // Is there a better way to do this avoiding map update problems
        // first add entries to reference_object_map
        for (Integer index : data_map.keySet()) {
            if (count < num_ref_points) {
                new_ro_map.put(index, data_map.get(index));
            }
            count++;
        }

        return new_ro_map;
    }

    private List<CartesianPoint> initData() {
        List<CartesianPoint> list = new ArrayList<CartesianPoint>();

        initialiseList(list, data_map);
        return list;
    }

    private List<CartesianPoint> initRefPoints() {

        List<CartesianPoint> list = new ArrayList<CartesianPoint>();

        initialiseList(list, reference_object_map);

        return list;
    }


    private List<CartesianPoint> initQueries() {
        List<CartesianPoint> list = new ArrayList<CartesianPoint>();

        initialiseList(list, query_map);

        return list;
    }

    private void initialiseList(List<CartesianPoint> list, Map<Integer, float[]> map) {

        for (Integer index : map.keySet()) {
            float[] floats = map.get(index);
            list.add(new CartesianPoint(convertFloatsToDoubles(floats)));
        }
    }

    public static double[] convertFloatsToDoubles(float[] input) {
        double[] output = new double[input.length];
        for (int i = 0; i < input.length; i++)
        {
            output[i] = input[i];
        }
        return output;
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
    protected double[] getRandomVolumePoint(double[] midpoint, double radius) {
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

    public Map<Integer, float[]> getDataMap() {
        return data_map;
    }

    public Map<Integer, float[]> getQueryMap() {
        return query_map;
    }

}

