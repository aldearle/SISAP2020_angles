package uk.al_richard.experimental.angles.contexts;

import coreConcepts.Metric;
import dataPoints.cartesian.CartesianPoint;
import dataPoints.cartesian.Euclidean;
import javafx.util.Pair;

import java.io.*;
import java.util.*;

/**
 * This class is something that acts like a context from the Metric Space Framework.
 * It was adapted from code in an earlier project - fc6_stuff written by Richard in Brno?
 */
public class DeCafContext {

    public static final String DECAF_DEFAULT_BASE_DIR = "/Users/al/repos/github/fc6_stuff/resources/";

    private String decaf_base_dir = DECAF_DEFAULT_BASE_DIR;

    private List<CartesianPoint> data_list = null;
    private List<CartesianPoint> query_list = null;
    private List<CartesianPoint> reference_object_list = null;

    private Metric<CartesianPoint> metric;
    private int num_queries = 0;
    private int num_ref_points = 0;

    public DeCafContext() {
        this(DECAF_DEFAULT_BASE_DIR);
    }

    public DeCafContext( String decaf_base_dir ) {

        this.decaf_base_dir = decaf_base_dir;
        metric = new Euclidean<>();
    }

    //------- Interface methods

    public void setSizes(int num_queries, int num_ref_points) throws Exception {
        this.num_queries = num_queries;
        this.num_ref_points = num_ref_points;
    }

    //------- Interface getters

    public Metric<CartesianPoint> metric() {
        return metric;
    }

    public List<CartesianPoint> getData() throws IOException, ClassNotFoundException {
        if( data_list == null ) {
            loadProfisetData();
        }
        return data_list;
    }

    public List<CartesianPoint> getRefPoints() throws IOException, ClassNotFoundException {
        if( reference_object_list == null ) {
            loadProfisetData();
        }
        return reference_object_list;
    }

    public List<CartesianPoint> getQueries() throws IOException, ClassNotFoundException {
        if( query_list == null ) {
            loadProfisetQueries();
        }
        return query_list;
    }


    public double getThreshold() {
        // there is no one threshold?
        throw new RuntimeException( "getThreshold unimplemented" );
    }


    public double[] getThresholds() {
        // code for this in the old fc6_stuff project if needed
        throw new RuntimeException( "getThreshold unimplemented" );
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

        for (int batch = 0; batch < 1000; batch++) {
            FileInputStream fis = new FileInputStream(decaf_base_dir + "data_relu_obj/" + batch + ".obj");
            ObjectInputStream ois = new ObjectInputStream(fis);

            copyToDataList( (List<Pair<Integer, float[]>>) ois.readObject() );

            ois.close();
        }
        createRosFromData();
    }

    private void copyToDataList(List<Pair<Integer,float[]>> data) {
        for( Pair<Integer,float[]> next : data ) {
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
