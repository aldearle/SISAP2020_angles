package uk.al_richard.experimental.angles.MSCDependent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import eu.similarity.msc.core_concepts.Metric;
import eu.similarity.msc.data.DataListView;
import eu.similarity.msc.data.DataListView.IdDatumPair;
import eu.similarity.msc.data.DecafMetricSpace;
import eu.similarity.msc.data.SiftMetricSpace;

public class MeasureAngleDistributions {

	@SuppressWarnings("boxing")
	public static void main(String[] args) throws ClassNotFoundException, IOException {

		System.out.println("testing sift");
//		final SiftMetricSpace sift = new SiftMetricSpace("/Volumes/Data/SIFT_mu/");
		final DecafMetricSpace space = new DecafMetricSpace("/Volumes/Data/profiset/");
//		final MfAlexMetricSpace sift = new MfAlexMetricSpace("/Volumes/Data/mf_fc6/");
		
		
		Map<Integer, float[]> data = space.getData();
		List<IdDatumPair<float[]>> l = DataListView.convert(data);
		final Map<Integer, float[]> queries2 = space.getQueries();
		List<IdDatumPair<float[]>> queries = DataListView.convert(queries2);
		Metric<IdDatumPair<float[]>> metric = DataListView.convert(space.getMetric());

		Map<Integer, Integer[]> nnids = space.getNNIds();
		List<IdDatumPair<float[]>> refPoints = getRandom(l, 100);

		IdDatumPair<float[]> query = queries.get(10);
		float[] query2 = queries2.get(query.id);
		Integer[] queryNns = nnids.get(query.id);
		System.out.println(queryNns.length + " query solutions");

		for (IdDatumPair<float[]> pI : refPoints) {
			double[] angs = new double[refPoints.size() - 1];
			double acc = 0;
			double pqDist = metric.distance(query, pI);
			int ptr = 0;
			for (IdDatumPair<float[]> pJ : refPoints) {
				if (pJ != pI) {
					double pipj = metric.distance(pI, pJ);
					double qpj = metric.distance(query, pJ);
					double angle = AngleInfo.getAngle(pqDist, pipj, qpj);
					angs[ptr++] = angle;
					acc += angle;
				}
			}
			AngleInfo ai1 = new AngleInfo(acc / angs.length, angs);

			double[] nnAngs = new double[queryNns.length];
			double nnAcc = 0;
			int nnPtr = 0;
			for (int nnJ : queryNns) {
				final float[] nnJval = data.get(nnJ);
				double pinnj = space.getMetric().distance(data.get(pI.id), nnJval);
				double qpj = space.getMetric().distance(query2, nnJval);
				double angle = AngleInfo.getAngle(pqDist, pinnj, qpj);
				nnAngs[nnPtr++] = angle;
				nnAcc += angle;

			}
			AngleInfo ai2 = new AngleInfo(nnAcc / nnAngs.length, nnAngs);

			System.out.println(
					ai1.getMean() + "\t" + ai1.getStd() + "\t" + ai2.getMean() + "\t" + ai2.getStd() + "\t" + pqDist);
		}

		// get angles from refPoint to other refPoints (pseudo random points) and to
		// query nn
//		querySolutionVsRandom(sift, data, metric, refPoints, query, query2, queryNns, refPoint, pqDist);

	}

	@SuppressWarnings({ "unused", "boxing" })
	private static void querySolutionVsRandom(final SiftMetricSpace sift, Map<Integer, float[]> data,
			Metric<IdDatumPair<float[]>> metric, List<IdDatumPair<float[]>> refPoints, IdDatumPair<float[]> query, float[] query2,
			Integer[] queryNns, IdDatumPair<float[]> refPoint, double pqDist) {
		int ptr = 0;
		for (IdDatumPair<float[]> o : refPoints.subList(0, queryNns.length)) {
			if (o != refPoint) {
				double pTopDist = metric.distance(refPoint, o);
				double d1 = metric.distance(query, o);

				final float[] nextNN = data.get(queryNns[ptr++]);
				double d2 = sift.getMetric().distance(query2, nextNN);
				double d3 = sift.getMetric().distance(data.get(refPoint.id), nextNN);

				// print angle pqr, where r is random
				System.out.print(AngleInfo.getAngle(pqDist, pTopDist, d1));
				// print angle pqs, where s is solution
				System.out.println("\t" + AngleInfo.getAngle(pqDist, d3, d2));
			}
		}
	}

	private static <T> List<T> getRandom(List<T> l, int noOfValues) {
		List<T> res = new ArrayList<>();
		Random r = new Random();
		while (res.size() < noOfValues) {
			final T next = l.get(r.nextInt(l.size()));
			if (!res.contains(next)) {
				res.add(next);
			}
		}
		return res;
	}

}
