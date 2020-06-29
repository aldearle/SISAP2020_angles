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
import eu.similarity.msc.data.MetricSpaceResource;

public class MeasureAngleDistributions {

	public static void main(String[] args) throws ClassNotFoundException, IOException {

		System.out.println("testing sift");
//		final SiftMetricSpace sift = new SiftMetricSpace("/Volumes/Data/SIFT_mu/");
		final DecafMetricSpace space = new DecafMetricSpace("/Volumes/Data/profiset/");
//		final MfAlexMetricSpace sift = new MfAlexMetricSpace("/Volumes/Data/mf_fc6/");

		measure(space);

		// get angles from refPoint to other refPoints (pseudo random points) and to
		// query nn
//		querySolutionVsRandom(sift, data, metric, refPoints, query, query2, queryNns, refPoint, pqDist);

	}

	private static <K, T> void measure(MetricSpaceResource<K, T> space) throws ClassNotFoundException, IOException {

		Map<K, T> data = space.getData();
		List<IdDatumPair<K, T>> l = DataListView.convert(data);
		final Map<K, T> queries2 = space.getQueries();
		List<IdDatumPair<K, T>> queries = DataListView.convert(queries2);
		Metric<IdDatumPair<K, T>> metric = DataListView.convert(space.getMetric());

		Map<K, List<K>> nnids = space.getNNIds();
		List<IdDatumPair<K, T>> refPoints = getRandom(l, 100);

		IdDatumPair<K, T> query = queries.get(10);
		T query2 = queries2.get(query.id);
		List<K> queryNns = nnids.get(query.id);
		System.out.println(queryNns.size() + " query solutions");

		for (IdDatumPair<K, T> pI : refPoints) {
			double[] angs = new double[refPoints.size() - 1];
			double acc = 0;
			double pqDist = metric.distance(query, pI);
			int ptr = 0;
			for (IdDatumPair<K, T> pJ : refPoints) {
				if (pJ != pI) {
					double pipj = metric.distance(pI, pJ);
					double qpj = metric.distance(query, pJ);
					double angle = AngleInfo.getAngle(pqDist, pipj, qpj);
					angs[ptr++] = angle;
					acc += angle;
				}
			}
			AngleInfo ai1 = new AngleInfo(acc / angs.length, angs);

			double[] nnAngs = new double[queryNns.size()];
			double nnAcc = 0;
			int nnPtr = 0;
			for (K nnJ : queryNns) {
				final T nnJval = data.get(nnJ);
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
	}

	@SuppressWarnings("unused")
	private static <K, T> void querySolutionVsRandom(final MetricSpaceResource<K, T> sift, Map<K, T> data,
			Metric<IdDatumPair<K, T>> metric, List<IdDatumPair<K, T>> refPoints, IdDatumPair<K, T> query, T query2,
			List<Integer> queryNns, IdDatumPair<K, T> refPoint, double pqDist) {
		int ptr = 0;
		for (IdDatumPair<K, T> o : refPoints.subList(0, queryNns.size())) {
			if (o != refPoint) {
				double pTopDist = metric.distance(refPoint, o);
				double d1 = metric.distance(query, o);

				final T nextNN = data.get(queryNns.get(ptr++));
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
