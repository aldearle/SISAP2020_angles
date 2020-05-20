package uk.al_richard.experimental.angles.contextsMSC;


import eu.similarity.msc.data.cartesian.CartesianPoint;
import eu.similarity.msc.core_concepts.Metric;

import java.util.List;
import java.util.Map;

public interface IContextMSC {

    public Metric<CartesianPoint> metric();

    public List<CartesianPoint> getData() throws Exception;

    public List<CartesianPoint> getQueries() throws Exception;

    public List<CartesianPoint> getRefPoints() throws Exception;

    //** Maps **//

    public Map<Integer, Integer[]> getNNMap();

    public Map<Integer, float[]> getDataMap();

    Map<Integer, float[]> getQueryMap();

    public double getThreshold(); // probably not here.

}
