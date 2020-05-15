package uk.al_richard.experimental.angles.contexts;

import coreConcepts.Metric;
import dataPoints.cartesian.CartesianPoint;
import dataPoints.cartesian.Euclidean;
import testloads.TestLoad;

import java.util.List;
import java.util.Map;

public class EucN implements IContext {

    private final int dim;
    private final Euclidean metric;
    private final int dataSize;
    private double threshold;

    private List<CartesianPoint> queries;
    private List<CartesianPoint> refPoints;

    private final TestLoad tl;

    public EucN(int dim, int datasize, int num_ros, int num_queries ) {
        this.dim = dim;
        metric = new Euclidean();
        dataSize = datasize;
        tl = new TestLoad(dim, datasize, true, true);
        setSizes( num_ros, num_queries );
        setThreshold(getThreshold(dim, 1));
    }

    /**
     * caculates the threshold per million for some dimension
     */
    public double getThreshold( int dim, int perMil ) {
        return euclideanRadius(dim, perMil * 0.000001);
    }

    private static double euclideanRadius(int dim, double vol) {
        if (dim % 2 == 0) {
            return getEvenRadius(dim, vol);
        } else {
            return getOddRadius(dim, vol);
        }
    }

    private static double getOddRadius(int dim, double vol) {
        int k = dim / 2;
        final long dFac = dFactorial(dim);
        double kFacV = dFac * vol;
        double bot = Math.pow(2, k + 1) * Math.pow(Math.PI, k);
        double frac = kFacV / bot;
//        System.out.println(k + "\t" + dim + "\t" + vol + "\t" + dFac + "\t" + bot);
        final double doubleDims = (double) 1 / dim;
//        System.out.println(doubleDims);
        return Math.pow(frac, doubleDims);
    }

    private static double getEvenRadius(int dim, double vol) {
        int k = dim / 2;
        double kFacV = factorial(k) * vol;
        double top = Math.pow(kFacV, (double) 1 / (2 * k));
        double bot = Math.sqrt(Math.PI);
        return top / bot;
    }

    private static long factorial(long k) {
        if (k == 1) {
            return 1;
        } else {
            return k * factorial(k - 1);
        }
    }

    private static long dFactorial(long k) {
        if (k == 1) {
            return 1;
        } else {
            return k * dFactorial(k - 2);
        }
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

    @Override
    public double getThreshold() {
        return threshold;
    }
}
