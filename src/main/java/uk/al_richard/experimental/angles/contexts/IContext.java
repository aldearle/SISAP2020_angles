package uk.al_richard.experimental.angles.contexts;

import coreConcepts.Metric;
import dataPoints.cartesian.CartesianPoint;

import java.util.List;

public interface IContext {

    public Metric<CartesianPoint> metric();

    public List<CartesianPoint> getData() throws Exception;

    public List<CartesianPoint> getQueries() throws Exception;

    public List<CartesianPoint> getRefPoints() throws Exception;

    public double getThreshold();

}
