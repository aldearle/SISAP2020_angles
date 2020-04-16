package uk.al_richard.experimental.angles;

import dataPoints.cartesian.CartesianPoint;
import util.OrderedList;

import java.util.List;

public class NNs {

    private final int number_required;
    private OrderedList<CartesianPoint, Double> nns;
    private double furthest = Double.MAX_VALUE;

    public NNs(int number_required) {

        this.number_required = number_required;
        nns = new OrderedList<>(number_required);
    }

    public void add(CartesianPoint point, double distance) {
        nns.add(point, distance);
    }

    public List<CartesianPoint> getNNs() {
        return nns.getList();
    }

    public boolean contains( CartesianPoint point ) {
        return nns.getList().contains(point);
    }
}


// Richard's code:
//    private static void getNNs(Map<Integer, float[]> data) throws Exception {
//        Metric<CartesianPoint> jsd = ExperimentsForIS.getJsDistance();
////		Metric<CartesianPoint> euc = new Euclidean();
//        ExperimentalContext_IS_paper ec = new ExperimentalContext_IS_paper(ExperimentsForIS.DataSets.gist, jsd, 0);
//        List<CartesianPoint> queries = ec.getQueries();
//
//        PrintWriter pw = new PrintWriter(groudTruthFileName);
//        System.out.println(queries.size());
//        for (int qid = 0; qid < queries.size(); qid++) {
//            if (qid > 40) {
//                long t0 = System.currentTimeMillis();
//                CartesianPoint q = queries.get(qid);
//                OrderedList<Integer, Double> ol = new OrderedList<>(100);
//                for (int i : data.keySet()) {
//                    float[] d = data.get(i);
//                    CartesianPoint dp = new CartesianPoint(d);
//                    double dist = jsd.distance(q, dp);
//                    ol.add(i, dist);
//                }
//                long t1 = System.currentTimeMillis();
//                List<Integer> nnids = ol.getList();
//                List<Double> dists = ol.getComparators();
//                int ptr = 0;
//                for (int nnid : nnids) {
//                    pw.print(nnid + "\t" + dists.get(ptr++) + "\t");
//                }
//                pw.println();
//                pw.flush();
//                System.out.println("exhaustive query took " + (t1 - t0) + " msec");
//            }
//        }
//
//        pw.close();
//    }
//    public Map<Integer, int[]> getNNIds() throws IOException {
//        Map<Integer, int[]> res = new TreeMap<>();
//        LineNumberReader fr = new LineNumberReader(new FileReader(this.gtFilePath));
//
//        boolean finished = false;
//        while (!finished) {
//            try {
//                Scanner s = new Scanner(fr.readLine());
//                s.useDelimiter("\\t");
//                int qid = getQid(s);
//                int[] nnids = getNNIdsFromNextLine(s, qid);
//                res.put(qid, nnids);
//            } catch (Exception e) {
//                finished = true;
//            }
//        }
//
//        fr.close();
//        return res;
//    }
//
//    private static int[] getNNIdsFromNextLine(Scanner s, int qid) {
//        int[] res = new int[100];
//        res[0] = qid;
//        for (int i = 1; i < 100; i++) {
//            res[i] = s.nextInt();
//            @SuppressWarnings("unused")
//            double f = s.nextDouble();
//        }
//        return res;
//    }
//
//    private static int getQid(Scanner s) {
//        int id = s.nextInt();
//        @SuppressWarnings("unused")
//        double f = s.nextDouble();
//        return id;
//    }
//    **
//            * returns a list of 100 nearest neighbour ids for each query id
//	 */
//    @Override
//    @SuppressWarnings("boxing")
//    public Map<Integer, int[]> getNNIds() throws IOException {
//        Map<Integer, int[]> res = new TreeMap<>();
//        LineNumberReader fr = new LineNumberReader(new FileReader(this.gtFilePath));
//
//        boolean finished = false;
//        while (!finished) {
//            try {
//                Scanner s = new Scanner(fr.readLine());
//                s.useDelimiter("\\t");
//                int qid = getQid(s);
//                int[] nnids = getNNIdsFromNextLine(s, qid);
//                res.put(qid, nnids);
//            } catch (Exception e) {
//                finished = true;
//            }
//        }
//
//        fr.close();
//        return res;
//    }
//
//    private static int[] getNNIdsFromNextLine(Scanner s, int qid) {
//        int[] res = new int[100];
//        res[0] = qid;
//        for (int i = 1; i < 100; i++) {
//            res[i] = s.nextInt();
//            @SuppressWarnings("unused")
//            double f = s.nextDouble();
//        }
//        return res;
//    }
//
//    private static int getQid(Scanner s) {
//        int id = s.nextInt();
//        @SuppressWarnings("unused")
//        double f = s.nextDouble();
//        return id;
//    }
