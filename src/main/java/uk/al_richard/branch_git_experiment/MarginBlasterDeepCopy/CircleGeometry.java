package uk.al_richard.branch_git_experiment.MarginBlasterDeepCopy;

import java.text.DecimalFormat;

/**
 * Explanation of the geometry is in explanation.md and the pictures Picture1.png and Picture2.png
 */
public class CircleGeometry {

    private static DecimalFormat df4 = new DecimalFormat("#.####");

    /**
     * @param mu - the mean angle in RADIANS
     * @param std_dev - the standard deviation from the mean in RADIANS
     * @param query_radius - the radius of the query
     * @param multiplication_factor - the factor to be applied to the std_dev (n times std_dev)
     * @return the distance margin based on the mean angle the std_dev and the multiplication_factor
     * The margin calculated is maximum based on the multiplication_factor X the std_dev from the mean.
     */
    public static double calculateMargin( double mu, double std_dev, double query_radius, double multiplication_factor ) {

        double angle = mu - ( std_dev * multiplication_factor );
        double chord_length = chordLength( angle, query_radius );
        double margin = calculateMargin(query_radius, chord_length );

        if( LIDIMtoAngleMap.printing ) {
            System.out.println("\tAngle = " + df4.format(Math.toDegrees(angle)) + " chord length = " + df4.format(chord_length) + " margin = " + margin);
        }

        return margin;
    }

    /**
     * @param mu - the mean angle in RADIANS
     * @param std_dev - the standard deviation from the mean in RADIANS
     * @param query_radius - the radius of the query
     * @param multiplication_factor - the factor to be applied to the std_dev (n times std_dev)
     * @return the new radius based on the mean angle the std_dev and the multiplication_factor
     * The new radius calculated is maximum based on the multiplication_factor X the std_dev from the mean.
     */
    public static double calculateSafeRadius( double mu, double std_dev, double query_radius, double multiplication_factor ) {

        double angle = mu - ( std_dev * multiplication_factor );
        double chord_length = chordLength( angle, query_radius );
        double dist_to_chord = calculateRadius( query_radius,chord_length );

        return dist_to_chord;
    }


    /**
     *
     * @param angle - the caculated angle in RADIANS - the mean - n times the multiplication_factor
     * @param query_radius - the radius of the query
     * @return the chord length L - see Picture2.png
     */
    private static double chordLength(double angle, double query_radius  ) {

        return Math.sqrt( ( 2 * uk.al_richard.experimental.angles.Util.square(query_radius) ) - ( 2 * query_radius * Math.cos( 2 * angle ) ) );
    }

    /**
     *
     * @param query_radius - radius of the query
     * @param chord_length - the length of the chord
     * @return the margin m as shown in Picture2.png
     */
    public static double calculateMargin(double query_radius, double chord_length) {

        double dist_to_chord = calculateRadius( query_radius,chord_length );
        return query_radius - dist_to_chord;
    }

    /**
     *
     * @param query_radius - radius of the query
     * @param chord_length - the length of the chord
     * @return the new radius d as shown in Picture2.png
     */
    private static double calculateRadius(double query_radius, double chord_length) {

        return Math.sqrt( uk.al_richard.experimental.angles.Util.square(query_radius) - Util.square(chord_length / 2) ); // pythag.
    }

}
