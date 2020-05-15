package uk.al_richard.experimental.angles.contexts;

import coreConcepts.Metric;
import dataPoints.cartesian.CartesianPoint;

import java.util.List;
import java.util.Map;

public interface IContext {

    public Metric<CartesianPoint> metric();

    public List<CartesianPoint> getData() throws Exception;

    public List<CartesianPoint> getQueries() throws Exception;

    public List<CartesianPoint> getRefPoints() throws Exception;

    //** Maps **//

    public Map<Integer, int[]> getNNMap();

    public Map<Integer, float[]> getDataMap();

    Map<Integer, float[]> getQueryMap();

    public double getThreshold(); // probably not here.

}
