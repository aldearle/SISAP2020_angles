package uk.al_richard.experimental.angles;

import coreConcepts.CountedMetric;
import coreConcepts.Metric;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.DataDistance;
import uk.al_richard.metricbitblaster.util.OpenBitSet;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

public class MetricMarginBlaster<T> {



    private final RefPointSet<T> rps;
    private List<T> dat;
    private OpenBitSet[] datarep;
    private BiFunction<T,T,Double> distfn;
    private CountedMetric<T> cm;

    private final int nChoose2;
    private int noOfBitSets = 0;
    private final int number_sheets;
    private final int number_balls;
    private List<ExclusionZoneModded<T>> ezs;
    private int number_of_items;    // Keep a note of how many items are in the data set
    private boolean fourPoint = false;
    private boolean balanced = false;

    private static final double RADIUS_INCREMENT = 0.3;                 // TODO revisit these
    private static double MEAN_DIST = 1.81;                             // TODO revisit these
    private static double[] ball_radii = new double[] {
            MEAN_DIST - 2 * RADIUS_INCREMENT, MEAN_DIST - RADIUS_INCREMENT,
            MEAN_DIST, MEAN_DIST - RADIUS_INCREMENT,
            MEAN_DIST - 2 * RADIUS_INCREMENT };

    private int normal_results = 0;
    private int normal_include = 0;
    private int normal_exclude = 0;
    private int normal_distance_totals = 0;
    private int normal_pre_filter = 0;
    private int normal_post_filter = 0;

    private int margin_results = 0;
    private int margin_include = 0;
    private int margin_exclude = 0;
    private int margin_distance_totals = 0;
    private int margin_pre_filter = 0;
    private int margin_post_filter = 0;

    private static int count_post_filter = 0;
    private static int count_pre_filter = 0;

    public MetricMarginBlaster(BiFunction<T, T, Double> distfn, List<T> refs, List<T> dat, boolean balls, boolean sheets, boolean fourPoint) {

        this.distfn = distfn;
        this.dat = dat;
        this.fourPoint = fourPoint;
        this.balanced = false;

        Metric<T> m = new Metric<T>() {

            @Override
            public double distance(T o1, T o2) {
                return distfn.apply(o1,o2);
            }

            @Override
            public String getMetricName() {
                return "supplied";
            }
        };

        cm = new CountedMetric( m );


        rps = new RefPointSet<T>(refs, cm);
        ezs = new ArrayList<>();

        int noOfRefPoints = refs.size();
        nChoose2 = ((noOfRefPoints - 1) * noOfRefPoints) / 2;

        number_balls = noOfRefPoints * ball_radii.length;
        number_sheets = nChoose2;

        if( sheets ) {
            noOfBitSets = noOfBitSets + number_sheets;
        }
        if( balls ) {
            noOfBitSets = noOfBitSets + number_balls;
        }

        if( sheets ) {
            addSheetExclusions(dat, refs, rps, ezs);
        }
        if( balls ) {
            addBallExclusions(dat, refs, rps, ezs);
        }

        datarep = new OpenBitSet[noOfBitSets];
        buildBitSetData(dat, datarep, rps, ezs);
    }


    /**
     * Find the nodes within range r of query.
     *
     * @param q - some data for which to find the neighbours within distance r
     * @param threshold     the distance from query over which to search
     * @return all those nodes within r of @param T.
     */
    public List<DataDistance<T>> rangeSearch(final T q, final double threshold) {

            cm.reset();

            List<DataDistance<T>> res = new ArrayList<>();

            double[] dists = rps.extDists(q);

            List<Integer> mustBeIn = new ArrayList<>();
            List<Integer> cantBeIn = new ArrayList<>();

            for (int i = 0; i < ezs.size(); i++) {
                ExclusionZoneModded<T> ez = ezs.get(i);
                if (ez.mustBeIn( dists, threshold)) {
                    mustBeIn.add(i);
                } else if (ez.mustBeOut( dists, threshold)) {
                    cantBeIn.add(i);
                }
            }

            normal_include +=  mustBeIn.size();
            normal_exclude +=  cantBeIn.size();

            doExclusions(dat, threshold, datarep, cm, q, res, dat.size(), mustBeIn, cantBeIn);

            int distance_count = cm.reset();

            int num_results = res.size();

            normal_results += num_results;
            normal_distance_totals += distance_count;

            normal_pre_filter += count_pre_filter;
            normal_post_filter += count_post_filter;

            return res;
    }

    /**
     * Find the nodes within range r of query.
     *
     * @param q - some data for which to find the neighbours within distance r
     * @param threshold     the distance from query over which to search
     * @return all those nodes within r of @param T.
     */
    public List<DataDistance<T>> rangeSearchMargin(final T q, final double margin, double threshold) {

        cm.reset();

        List<DataDistance<T>> res = new ArrayList<>();

        double[] dists = rps.extDists(q);

        List<Integer> mustBeIn = new ArrayList<>();
        List<Integer> cantBeIn = new ArrayList<>();

        for (int i = 0; i < ezs.size(); i++) {
            ExclusionZoneModded<T> ez = ezs.get(i);
            if (ez.mustBeInMargin( dists, threshold, margin)) {
                mustBeIn.add(i);
            } else if (ez.mustBeOutMargin( dists, threshold, margin)) {
                cantBeIn.add(i);
            }
        }

        margin_include +=  mustBeIn.size();
        margin_exclude += cantBeIn.size();

        doExclusions(dat, threshold, datarep, cm, q, res, dat.size(), mustBeIn, cantBeIn);

        int distance_count = cm.reset();

        int num_results = res.size();

        margin_results += num_results;
        margin_distance_totals += distance_count;

        margin_pre_filter += count_pre_filter;
        margin_post_filter += count_post_filter;

        return res;
    }

    public int size() {
        return dat.size();
    }

    //--------- Private ---------

    private void addBallExclusions(List<T> dat,
                                   List<T> refs, RefPointSet<T> rps,
                                   List<ExclusionZoneModded<T>> ezs) {
        for (int i = 0; i < refs.size(); i++) {
            List<BallExclusionModded<T>> balls = new ArrayList<>();
            for (double radius : ball_radii) {
                BallExclusionModded<T> be = new BallExclusionModded<>(rps, i,
                        radius);
                balls.add(be);
            }
            if (balanced) {
                balls.get(0).setWitnesses(dat.subList(0, 1000));
                double midRadius = balls.get(0).getRadius();
                double thisRadius = midRadius
                        - ((balls.size() / 2) * RADIUS_INCREMENT);
                for (int ball = 0; ball < balls.size(); ball++) {
                    balls.get(ball).setRadius(thisRadius);
                    thisRadius += RADIUS_INCREMENT;
                }
            }
            ezs.addAll(balls);
        }
    }

    private void addSheetExclusions(List<T> dat,
                                          List<T> refs, RefPointSet<T> rps,
                                          List<ExclusionZoneModded<T>> ezs) {
        for (int i = 0; i < refs.size() - 1; i++) {
            for (int j = i + 1; j < refs.size(); j++) {
                SheetExclusionModded<T> se;
                if( fourPoint ) {
                    se = new SheetExclusion4pModded<>(rps, i, j);
                } else {
                    se = new SheetExclusion3pModded<>(rps, i, j);
                }
                if (balanced) {
                    se.setWitnesses(dat.subList(0, 1000));
                }
                ezs.add(se);
            }
        }
    }

    private static <T> void buildBitSetData(List<T> data, OpenBitSet[] datarep,
                                            RefPointSet<T> rps, List<ExclusionZoneModded<T>> ezs) {
        int dataSize = data.size();
        for (int i = 0; i < datarep.length; i++) {
            datarep[i] = new OpenBitSet(dataSize);
        }
        for (int n = 0; n < dataSize; n++) {
            T p = data.get(n);
            double[] dists = rps.extDists(p);
            for (int x = 0; x < datarep.length; x++) {
                boolean isIn = ezs.get(x).isIn(dists);
                if (isIn) {
                    datarep[x].set(n);
                }
            }
        }
    }

    @SuppressWarnings("boxing")
    private static OpenBitSet getAndOpenBitSets(OpenBitSet[] datarep, final int dataSize,
                                                List<Integer> mustBeIn) {
        OpenBitSet ands = null;
        if (mustBeIn.size() != 0) {
            ands = datarep[mustBeIn.get(0)].get(0, dataSize);
            for (int i = 1; i < mustBeIn.size(); i++) {
                ands.and(datarep[mustBeIn.get(i)]);
            }
        }
        return ands;
    }

    @SuppressWarnings("boxing")
    private static OpenBitSet getOrOpenBitSets(OpenBitSet[] datarep, final int dataSize,
                                               List<Integer> cantBeIn) {
        OpenBitSet nots = null;
        if (cantBeIn.size() != 0) {
            nots = datarep[cantBeIn.get(0)].get(0, dataSize);
            for (int i = 1; i < cantBeIn.size(); i++) {
                final OpenBitSet nextNot = datarep[cantBeIn.get(i)];
                nots.or(nextNot);
            }
        }
        return nots;
    }

    private static <T> void doExclusions(List<T> dat, double t,
                                           OpenBitSet[] datarep, CountedMetric<T> cm, T q, List<DataDistance<T>> res,
                                           final int dataSize, List<Integer> mustBeIn, List<Integer> cantBeIn) {
        if (mustBeIn.size() != 0) {
            OpenBitSet ands = getAndOpenBitSets(datarep, dataSize, mustBeIn);
            if (cantBeIn.size() != 0) {
                /*
                 * hopefully the normal situation or we're in trouble!
                 */
                OpenBitSet nots = getOrOpenBitSets(datarep, dataSize, cantBeIn);
                nots.flip(0, dataSize);
                ands.and(nots);
                filterContenders(dat, t, cm, q, res, dataSize, ands); // <<<<<< use check for checking!
            } else {
                // there are no cantBeIn partitions
                filterContenders(dat, t, cm, q, res, dataSize, ands); // <<<<<< use check for checking!
            }
        } else {
            // there are no mustBeIn partitions
            if (cantBeIn.size() != 0) {
                OpenBitSet nots = getOrOpenBitSets(datarep, dataSize, cantBeIn);
                nots.flip(0, dataSize);
                filterContenders(dat, t, cm, q, res, dataSize, nots);  // <<<<<< use check for checking!
            } else {
                // there are no exclusions at all...
                System.out.println( "No exclusions!");
                for (T d : dat) {
                    double dist = cm.distance(q, d);
                    if ( dist < t) {                                 // <<<<<< use check for checking!
                        res.add(new DataDistance<T>(d,(float)dist));
                    }
                }
            }
        }
    }

    static <T> void filterContenders(List<T> dat, double t,
                                     CountedMetric<T> cm, T q, List<DataDistance<T>> res, final int dataSize,
                                     OpenBitSet results) {

        count_pre_filter = 0;
        count_post_filter = 0;

        for (int i = results.nextSetBit(0); i != -1 && i < dataSize; i = results.nextSetBit(i + 1)) {
            if (results.get(i)) {
                count_pre_filter++;
                double dist = cm.distance(q, dat.get(i));
                if ( dist <= t) {
                    res.add(new DataDistance<T>(dat.get(i),(float) dist));   // TODO change this test in all?
                    count_post_filter++;
                }
            }
        }
    }

    public void printDiagnostics() {
        System.out.println( "Normal exclusions, inclusions = \t" + normal_include + "\texclusions = " + normal_exclude + "\tpre filter = " + normal_pre_filter +  "\tpost filter = " + normal_post_filter + "\tpost results = " + normal_results + "\tdistance calcs = " + normal_distance_totals );
        System.out.println( "Margin exclusions, inclusions = \t" + margin_include + "\texclusions = " + margin_exclude + "\tpre filter = " + margin_pre_filter +  "\tpost filter = " + margin_post_filter + "\tpost results = " + margin_results + "\tdistance calcs = " + margin_distance_totals );
    }
}
