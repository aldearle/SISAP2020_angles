package uk.al_richard.experimental.angles.contexts;

import dataPoints.cartesian.CartesianPoint;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CalculateSIFTThreshold extends SiftContext {

    public CalculateSIFTThreshold() throws IOException, ClassNotFoundException {
    }

    public static double findMax( double[] arrai ) {
        double max = Double.MIN_VALUE;
        for( int i = 1; i < arrai.length; i++ ) {
            if( arrai[i] > max ) {
                max = arrai[i];
            }
        }
        return max;
    }

    public static double findMin( double[] arrai ) {
        double min = Double.MAX_VALUE;
        for( int i = 1; i < arrai.length; i++ ) {
            if( arrai[i] < min ) {
                min = arrai[i];
            }
        }
        return min;
    }

    private boolean insideRadius( CartesianPoint query_point, CartesianPoint sample_point, double query_radius ) {
        return metric.distance( query_point, sample_point ) < query_radius;
    }


    private int countInside(List<CartesianPoint> data, int query_index, double query_radius) {
        CartesianPoint query = data.get(query_index);
        int count = 0;
        for( int i = 0; i < data.size(); i++ ) {
            if( i != query_index ) {
                if (insideRadius(query, data.get(i), query_radius)) {
                    count++;
                }
            }
        }
        return count;
    }

    private double refine(List<CartesianPoint> data, double query_radius, int query_index, double increment ) {

        int count = 0;
        query_radius = query_radius + increment; // Java loops!

        do {
            query_radius = query_radius - increment;
            count = countInside( data, query_index, query_radius );
            System.out.println( "count: " + count );
        } while( count > 1 );  // looking for 1 in a million.

        return query_radius;
    }

    private double mean(List<Double> values) {
        double count = 0;
        for( double d : values ) {
            count = count + d;
        }
        return count / values.size();
    }

    private void findThresh() {

        double[] threshs = getThresholds();
        double min = findMax( threshs );
        List<CartesianPoint> data = getData();
        List<Double> radii = new ArrayList<>();

        for( int query_index = 0; query_index < 100; query_index ++ ) {
            double query_radius = refine(data, min, query_index, 1.0);
            radii.add(query_radius);
        }
        System.out.println( "Mean 1:10^6 threshold = " +  mean( radii ) );
        // result is: Mean 1:10^6 threshold = 168.71506000000028
    }


    public static void main(String[] args) throws IOException, ClassNotFoundException {
        CalculateSIFTThreshold cst = new CalculateSIFTThreshold();
        cst.findThresh();
    }


}
