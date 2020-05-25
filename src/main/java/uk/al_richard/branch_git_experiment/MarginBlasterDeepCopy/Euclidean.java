package uk.al_richard.branch_git_experiment.MarginBlasterDeepCopy;


import eu.similarity.msc.core_concepts.Metric;
import eu.similarity.msc.data.cartesian.CartesianPoint;

public class Euclidean<T extends CartesianPoint> implements Metric<T> {
    public Euclidean() {
    }

    public double distance(T x, T y) {
        double[] ys = y.getPoint();
        double acc = 0.0D;
        int ptr = 0;
        double[] dbls = x.getPoint();
        int len = dbls.length;

        for(int i = 0; i < len; ++i) {
            double xVal = dbls[i];
            double diff = xVal - ys[ptr++];
            acc += diff * diff;
        }

        return Math.sqrt(acc);
    }

    public String getMetricName() {
        return "euc";
    }
}

