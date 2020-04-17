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

    private List<Pair<Integer, float[]>> data_list;
    private List<Pair<Integer, float[]>> query_list;
    private List<Pair<Integer, float[]>> reference_object_list;

    private Metric<CartesianPoint> metric;

    public DeCafContext() throws IOException, ClassNotFoundException {
        this(DECAF_DEFAULT_BASE_DIR);
    }

    public DeCafContext( String decaf_base_dir ) throws IOException, ClassNotFoundException {

        this.decaf_base_dir = decaf_base_dir;
        metric = new Euclidean<>();
        data_list = getProfisetData();
        query_list = getProfisetQueries();
        reference_object_list = new ArrayList(); // should not be used (and isnt unless setSizes() is not called).
    }

    // Some of the methods were from earlier class - seems a waste to delete them as we may need later.
    /**
     * @param nn
     * @return the ids and nnth nearest neighbour distance of all 1000 ground truth
     *         queries
     * @throws IOException
     */
    @SuppressWarnings("boxing")
    private Map<Integer, Float> getNthNearestNeigbourDists(int nn) throws IOException {
        Map<Integer, Float> queries = new TreeMap<>();
        String filename = decaf_base_dir + "groundtruth-profineural-1M-q1000.txt";
        FileReader fr = new FileReader(filename);
        LineNumberReader lnr = new LineNumberReader(fr);

        for (int i = 0; i < 1000; i++) {
            String idLine = lnr.readLine();
            String[] idLineSplit = idLine.split("=");

            String nnLine = lnr.readLine();
            String[] spl = nnLine.split(",");
            String nnBit = spl[nn - 1]; // should look like: " 49.658: 0000927805"
            String[] flBit = nnBit.split(":");

            float f = Float.parseFloat(flBit[0]);

            /*
             * add the query id and the distance to the nnth nearest-neighbour
             */
            queries.put(Integer.parseInt(idLineSplit[1]), f);
        }

        lnr.close();
        return queries;
    }

    /**
     * @param queryId
     * @return all the nearest neighbour ids and distances for a given query
     * @throws IOException
     */
    @SuppressWarnings("boxing")
    private Map<Integer, Float> getNearestNeighbours(int queryId) throws IOException {
        Map<Integer, Float> queries = new TreeMap<>();
        String filename = decaf_base_dir + "groundtruth-profineural-1M-q1000.txt";
        FileReader fr = new FileReader(filename);
        LineNumberReader lnr = new LineNumberReader(fr);

        for (int i = 0; i < 1000; i++) {
            String idLine = lnr.readLine();
            String[] idLineSplit = idLine.split("=");
            final int qId = Integer.parseInt(idLineSplit[1]);

            String nnLine = lnr.readLine();

            if (qId == queryId) {
                String[] spl = nnLine.split(",");

                for (String thing : spl) {
                    String[] flBit = thing.split(": ");// should look like: " 49.658: 0000927805"

                    float f = Float.parseFloat(flBit[0]);
                    int id = Integer.parseInt(flBit[1]);

                    queries.put(id, f);
                }
            }
        }

        lnr.close();
        return queries;
    }

    private List<Pair<Integer, float[]>> getProfisetQueries()
            throws IOException, FileNotFoundException, ClassNotFoundException {
        FileInputStream fis = new FileInputStream(decaf_base_dir + "queries_relu/queryData.obj");
        ObjectInputStream ois = new ObjectInputStream(fis);

        @SuppressWarnings("unchecked")
        List<Pair<Integer, float[]>> res = (List<Pair<Integer, float[]>>) ois.readObject();
        ois.close();
        return res;
    }

    private List<Pair<Integer, float[]>> getProfisetData()
            throws FileNotFoundException, IOException, ClassNotFoundException {
        List<Pair<Integer, float[]>> allData = new ArrayList<>();

        for (int batch = 0; batch < 1000; batch++) {
            FileInputStream fis = new FileInputStream(decaf_base_dir + "data_relu_obj/" + batch + ".obj");
            ObjectInputStream ois = new ObjectInputStream(fis);
            @SuppressWarnings("unchecked")
            List<Pair<Integer, float[]>> next = (List<Pair<Integer, float[]>>) ois.readObject();
            allData.addAll(next);

            ois.close();
            if (batch % 100 == 0) {
                System.out.print(".");
            }
        }
        return allData;
    }

    private Map<Integer, float[]> getProfisetDataById()
            throws FileNotFoundException, ClassNotFoundException, IOException {
        List<Pair<Integer, float[]>> dat = getProfisetData();
        Map<Integer, float[]> res = new HashMap<>();
        for (Pair<Integer, float[]> item : dat) {
            res.put(item.getKey(), item.getValue());
        }
        return res;
    }

    private void trimQueries(int num_queries) {
        query_list.subList(0,num_queries -1).clear();
    }

    private List<Pair<Integer,float[]>> createRosFromData(int num_ref_points) {
        List<Pair<Integer,float[]>> new_ro_list = new ArrayList<>();
        int count = 0;

        for( Pair<Integer,float[]> pair : data_list ) {
            if( count < num_ref_points ) {
                new_ro_list.add(pair);
                count++;
            }
        }
        data_list.removeAll(new_ro_list);
        return new_ro_list;
    }

    //------- Interface methods

    public void setSizes(int num_queries, int num_ref_points) throws Exception {
        if( num_queries > query_list.size() ) {
            throw new Exception( "Too many queries requested" );
        }
        if( num_ref_points > data_list.size() ) {
            throw new Exception( "Too many ref points requested" );
        }
        trimQueries( num_queries );
        reference_object_list = createRosFromData( num_ref_points );
    }

    //------- Interface getters

    public Metric<CartesianPoint> metric() {
        return metric;
    }

    public List<CartesianPoint> getData() {
        return getCartesianPoints( data_list );
    }

    public List<CartesianPoint> getRefPoints() {
        return getCartesianPoints( reference_object_list );
    }

    public List<CartesianPoint> getQueries() {
        return getCartesianPoints( query_list );
    }

    private List<CartesianPoint> getCartesianPoints( List<Pair<Integer, float[]>> source_list ) {
        List<CartesianPoint> result = new ArrayList<>();
        for( Pair<Integer,float[]> pair : source_list ) {
            result.add( new CartesianPoint( pair.getValue() ) );
        }
        return result;
    }


    public double getThreshold() {
        // there is no one threshold?
        throw new RuntimeException( "getThreshold unimplemented" );
    }


    public double[] getThresholds() {
        // code for this in the old fc6_stuff project if needed
        throw new RuntimeException( "getThreshold unimplemented" );
    }

}
