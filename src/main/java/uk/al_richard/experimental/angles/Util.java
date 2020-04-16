package uk.al_richard.experimental.angles;

import java.util.List;

public class Util {

    public static double square(double x) { return x * x; }

    public static double mean(List<Double> list) {
        if( list.size() == 0 ) {
            throw new RuntimeException( "Empty list" );
        }
        double result = 0;
        for( double d : list ) {
            result = result + d;
        }
        return ((double) result) / list.size();
    }

    public static double stddev(List<Double> list, double mean) {
        if( list.size() == 0 ) {
            throw new RuntimeException( "Empty list" );
        }
        double sd = 0;
        for( double d : list ) {
            sd += Math.pow(d - mean, 2);
        }
        return Math.sqrt( sd / list.size() );
    }

    public static double idim(double mean, double std_dev) {
        return square(mean) / ( 2 * square(std_dev) );
    }

    public static double idim(List<Double> dists) throws Exception {
        if( dists.size() > 0 ) {
            double mean = (double) Util.mean(dists);
            double std_dev = Util.stddev(dists, mean);
            return idim(mean, std_dev);
        } else {
            throw new Exception("zero length list");
        }
    }


}
