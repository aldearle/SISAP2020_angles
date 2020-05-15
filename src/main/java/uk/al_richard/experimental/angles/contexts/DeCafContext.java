package uk.al_richard.experimental.angles.contexts;

import coreConcepts.Metric;
import dataPoints.cartesian.CartesianPoint;
import dataPoints.cartesian.Euclidean;
import javafx.util.Pair;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This class is something that acts like a context from the Metric Space Framework.
 * It was adapted from code in an earlier project - fc6_stuff written by Richard in Brno?
 */
public class DeCafContext implements IContext {

    public static final String DECAF_DEFAULT_BASE_DIR = "/Users/al/repos/github/fc6_stuff/resources/";

    private String decaf_base_dir = DECAF_DEFAULT_BASE_DIR;

    private List<CartesianPoint> data_list = null;
    private List<CartesianPoint> query_list = null;
    private List<CartesianPoint> reference_object_list = null;

    protected Metric<CartesianPoint> metric;
    private int num_queries = 0;
    private int num_ref_points = 0;
    private int num_data_points = 0;

    public DeCafContext() {
        this(DECAF_DEFAULT_BASE_DIR);
    }

    public DeCafContext( String decaf_base_dir ) {

        this.decaf_base_dir = decaf_base_dir;
        metric = new Euclidean<>();
    }

    //------- Interface methods

    public void setSizes(int num_data_points, int num_ref_points, int num_queries) throws Exception {
        this.num_data_points = num_data_points;
        this.num_queries = num_queries;
        this.num_ref_points = num_ref_points;
    }

    //------- Interface getters

    public Metric<CartesianPoint> metric() {
        return metric;
    }

    public List<CartesianPoint> getData() throws Exception {
        if( data_list == null ) {
            loadProfisetData();
        }
        return data_list;
    }

    public List<CartesianPoint> getRefPoints() throws Exception {
        if( reference_object_list == null ) {
            loadProfisetData();
        }
        return reference_object_list;
    }

    @Override
    public Map<Integer, int[]> getNNMap() {
        return null;
    }

    @Override
    public Map<Integer, float[]> getDataMap() {
        return null;
    }

    @Override
    public Map<Integer, float[]> getQueryMap() {
        return null;
    }

    public List<CartesianPoint> getQueries() throws Exception {
        if( query_list == null ) {
            loadProfisetQueries();
        }
        return query_list;
    }


    public double getThreshold() {
        return 55;  // DECAF 1% is somewhere near here.
    }


    public double[] getThresholds() {
        return null;
    }

    //------- Private methods

    private void loadProfisetQueries()
            throws IOException, ClassNotFoundException {
        FileInputStream fis = new FileInputStream(decaf_base_dir + "queries_relu/queryData.obj");
        query_list = new ArrayList<>();
        ObjectInputStream ois = new ObjectInputStream(fis);

        copyToQueryList( (List<Pair<Integer, float[]>>) ois.readObject() );

        ois.close();
    }

    private void copyToQueryList(List<Pair<Integer,float[]>> queries) {
        if( num_queries > queries.size() ) {
            num_queries = queries.size();
        }
        for( Pair<Integer,float[]> next : queries ) {
            if( query_list.size() == num_queries ) {
                return;
            }
            query_list.add( new CartesianPoint( next.getValue() ) );
        }
    }


    private void loadProfisetData()
            throws IOException, ClassNotFoundException {

        data_list = new ArrayList<>();
        int size_needed = num_data_points + num_ref_points;

        for (int batch = 0; batch < 1000; batch++) {
            FileInputStream fis = new FileInputStream(decaf_base_dir + "data_relu_obj/" + batch + ".obj");
            ObjectInputStream ois = new ObjectInputStream(fis);

            copyToDataList( (List<Pair<Integer, float[]>>) ois.readObject(),size_needed );

            ois.close();
            if( data_list.size() == size_needed ) {
                break;
            }
        }
        createRosFromData();
    }

    private void copyToDataList(List<Pair<Integer,float[]>> data, int size_needed) {

        for( Pair<Integer,float[]> next : data ) {
            if( data_list.size() == size_needed ) {
                return;
            }
            data_list.add( new CartesianPoint( next.getValue() ) );
        }
    }


    private void createRosFromData() {
        reference_object_list = new ArrayList<>();
        int count = 0;

        for( CartesianPoint p : data_list ) {
            if( count < num_ref_points ) {
                reference_object_list.add(p);
                count++;
            }
        }
        data_list.removeAll(reference_object_list);
    }

}
