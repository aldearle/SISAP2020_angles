package uk.al_richard.experimental.angles.contextsMSC;


import eu.similarity.msc.core_concepts.Metric;
import eu.similarity.msc.data.SiftMetricSpace;
import eu.similarity.msc.data.cartesian.CartesianPoint;
import eu.similarity.msc.metrics.cartesian.Euclidean;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SiftContextMSC implements IContextMSC {

    public static final String SIFT_DEFAULT_BASE_DIR = "/Volumes/Data/SIFT_mu/";

    private final Map<Integer,float[]> data_map;
    private final Map<Integer, Integer[]> nn_map;
    private Map<Integer, float[]> query_map;
    private Map<Integer, float[]> reference_object_map;
    private Map<Integer, double[]> threshold_map;

    private List<CartesianPoint> cached_data = null;
    private List<CartesianPoint> cached_queries = null;
    private List<CartesianPoint> cached_ros = null;
    private double[] cached_thresholds = null;

    protected Metric<CartesianPoint> metric;

    public SiftContextMSC() throws IOException, ClassNotFoundException {
        this( SIFT_DEFAULT_BASE_DIR );
    }

    public SiftContextMSC(String sift_base_dir ) throws IOException, ClassNotFoundException {

        SiftMetricSpace sms = new SiftMetricSpace(sift_base_dir);
        metric = new Euclidean<>();
        data_map = sms.getData();
        query_map = sms.getQueries();
        threshold_map = sms.getThresholds();
        reference_object_map = new HashMap<>(); // should not be used (and isnt unless setSizes() is not called).
        nn_map = sms.getNNIds();
    }

    public void setSizes(int num_queries, int num_ref_points) throws Exception {
        System.out.println("");
        if( num_queries > query_map.size() ) {
            throw new Exception( "Too many queries requested" );
        }
        if( num_ref_points > data_map.size() ) {
            throw new Exception( "Too many ref points requested" );
        }
        trimQueriesANDThresholds( num_queries );
        reference_object_map = createRosFromDataMap( num_ref_points );
        System.out.println("");
    }

    private void trimQueriesANDThresholds(int num_queries) {
        int count = 0;
        Map<Integer,float[]> new_query_map = new HashMap<>();
        Map<Integer,double[]> new_theshold_map = new HashMap<>();
        // Is there a better way to do this avoiding map update problems
        // first add entries to reference_object_map
        for( Integer index : query_map.keySet() ) {
            if( count < num_queries ) {
                new_query_map.put( index, query_map.get(index) );
                new_theshold_map.put( index, threshold_map.get(index));
            }
            count++;
        }
        query_map = new_query_map;
        threshold_map = new_theshold_map;
    }

    private Map<Integer,float[]> createRosFromDataMap(int num_ref_points) {
        Map<Integer,float[]> new_ro_map = new HashMap<>();
        int count = 0;
        // Is there a better way to do this avoiding map update problems
        // first add entries to reference_object_map
        for( Integer index : data_map.keySet() ) {
            if( count < num_ref_points ) {
                new_ro_map.put( index, data_map.get(index) );}
                count++;
        }
        // then remove from data_map - don't remove for SIFT
//        count = 0;
//        for( Integer index : new_ro_map.keySet() ) {
//            if( count < num_ref_points ) {
//                data_map.remove(index);
//                count++;
//            }
//        }
        return new_ro_map;
    }


    public List<CartesianPoint> getData() {
        if( cached_data == null ) {
            cached_data = new ArrayList<>();
            initialiseList( cached_data,data_map );
        }
        return cached_data;
    }

    public List<CartesianPoint> getRefPoints() {
        if( cached_ros == null ) {
            cached_ros = new ArrayList<>();
            initialiseList(cached_ros, reference_object_map);
        }
        return cached_ros;
    }

    @Override
    public Map<Integer, Integer[]> getNNMap() {
        return nn_map;
    }

    @Override
    public Map<Integer, float[]> getDataMap() {
        return data_map;
    }

    @Override
    public Map<Integer, float[]> getQueryMap() {
        return query_map;
    }

    public List<CartesianPoint> getQueries() {
        if( cached_queries == null ) {
            cached_queries = new ArrayList<>();
            initialiseList(cached_queries, query_map);
        }
        return cached_queries;
    }


    private void initialiseList( List<CartesianPoint> list, Map<Integer, float[]> map ) {

        for( Integer index : map.keySet() ) {
            float[] floats = map.get(index);
            list.add( new CartesianPoint( convertFloatsToDoubles(floats) ) );
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

    public Metric<CartesianPoint> metric() {
        return metric;
    }

    public double getThreshold() {
        return 168.7150 ; // 1 in a million.
    }

    public Map<Integer, double[]> getThresholdMap() { return threshold_map; }

    public static void main(String[] args) throws Exception {
        SiftContextMSC sc = new SiftContextMSC();
        sc.setSizes( 3,5 );

    }

}
