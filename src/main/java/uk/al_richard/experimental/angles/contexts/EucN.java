package uk.al_richard.experimental.angles.contexts;

import coreConcepts.Metric;
import dataPoints.cartesian.CartesianPoint;
import dataPoints.cartesian.Euclidean;
import testloads.CartesianThresholds;
import testloads.TestLoad;

import java.util.List;

public class EucN implements IContext {

    private final int dim;
    private final Euclidean metric;
    private final int dataSize;
    private double threshold;

    private List<CartesianPoint> queries;
    private List<CartesianPoint> refPoints;

    private final TestLoad tl;


    public EucN(int dim, int datasize, int num_ros, int num_queries ) {
        System.out.println( "Data set - Euc " + dim );
        this.dim = dim;
        metric = new Euclidean();
        dataSize = datasize;
        tl = new TestLoad(dim, datasize, true, true);
        setSizes( num_ros, num_queries );
        setThreshold(CartesianThresholds.getThreshold("euc", dim, 1));
    }

    public void setSizes( int num_ros, int num_queries ) {
        this.queries = tl.getQueries(num_queries);
        this.refPoints = tl.getQueries(num_ros);
    }

    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }

    @Override
    public Metric<CartesianPoint> metric() {
        return metric;
    }

    @Override
    public List<CartesianPoint> getData() {
        return this.tl.getDataCopy();
    }

    @Override
    public List<CartesianPoint> getQueries() throws Exception {
        return queries;
    }

    @Override
    public List<CartesianPoint> getRefPoints() throws Exception {
        return refPoints;
    }

    @Override
    public double getThreshold() {
        return threshold;
    }
}
