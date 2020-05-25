package uk.al_richard.branch_git_experiment;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import eu.similarity.msc.core_concepts.Metric;
import eu.similarity.msc.data.DecafMetricSpace;
import eu.similarity.msc.data.MetricSpaceResource;
import eu.similarity.msc.data.MfAlexMetricSpace;
import eu.similarity.msc.data.SiftMetricSpace;
import uk.al_richard.branch_git_experiment.Laesa;

public class RunSearch {

	private static class IdDatumPair {
		public int id;
		public float[] datum;

		IdDatumPair(int id, float[] datum) {
			this.id = id;
			this.datum = datum;
		}
	}

	private static class CountedMetric<T> implements Metric<T> {
		private Metric<T> metric;
		private int count;

		CountedMetric(Metric<T> m) {
			this.metric = m;
			this.count = 0;
		}

		@Override
		public double distance(T x, T y) {
			this.count++;
			return this.metric.distance(x, y);
		}

		@Override
		public String getMetricName() {
			return this.metric.getMetricName();
		}

		public int reset() {
			int res = this.count;
			this.count = 0;
			return res;
		}
	}

	private static List<IdDatumPair> convert(Map<Integer, float[]> dat) {
		List<IdDatumPair> res = new ArrayList<>();
		for (int i : dat.keySet()) {
			IdDatumPair idp = new IdDatumPair(i, dat.get(i));
			res.add(idp);
		}
		return res;
	}

	private static CountedMetric<IdDatumPair> convertMetric(final Metric<float[]> m) {
		Metric<IdDatumPair> met = new Metric<IdDatumPair>() {

			@Override
			public double distance(IdDatumPair x, IdDatumPair y) {
				return m.distance(x.datum, y.datum);
			}

			@Override
			public String getMetricName() {
				return m.getMetricName();
			}
		};
		return new CountedMetric<>(met);
	}

	public static void main(String[] args) throws Exception {
		testVptAngleTree();

	}

	private static void testVptAngleTree() throws Exception {

		final SiftMetricSpace sift = new SiftMetricSpace("/Volumes/Data/SIFT_mu/");
		final DecafMetricSpace decaf = new DecafMetricSpace("/Volumes/Data/profiset/");
		final MfAlexMetricSpace mf = new MfAlexMetricSpace("/Volumes/Data/mf_fc6/");

		MetricSpaceResource<Integer, float[]> space = decaf;
		System.out.println("testing " + space.getClass().getName());

//		System.out.println("testing sift");
//		final SiftMetricSpace sift = new SiftMetricSpace("/Volumes/Data/SIFT_mu/");
		List<IdDatumPair> idps = convert(space.getData());
		List<IdDatumPair> refs = idps.subList(0, 1000);
		List<IdDatumPair> data = idps.subList(1000, idps.size());
		Map<Integer, double[]> qThreshes = space.getThresholds();

		final CountedMetric<IdDatumPair> convertMetric = convertMetric(space.getMetric());

//		VPTree<IdDatumPair> index = new VPTree<>(idps, convertMetric);
		Laesa<IdDatumPair> index = new Laesa<>(data, refs, convertMetric);

		convertMetric.reset();
		List<IdDatumPair> queries = convert(space.getQueries());
		for (IdDatumPair query : queries.subList(0, 100)) {
			double threshold = qThreshes.get(query.id)[10];
			System.out.print(query.id + "\t" + threshold);
			// -0.0005x + 1.519 mf_alex equation... 1.519 - 0.0005 * threshold, sigma about 0.2
			// -0.0013x + 1.602 sift equation, sigma about 0.2
			// decaf equation... 1.6865 - 0.0103 * threshold, sigma about 1.3
			double siftAngle = 1.602 - 0.0013 * threshold;
			double decafAngle = 1.6865 - 0.0103 * threshold;
			double angle = decafAngle;
			List<IdDatumPair> res1 = index.search(query, threshold, Math.PI / 2, Math.PI / 2);
			System.out.print("\t" + res1.size() + "\t" + convertMetric.reset());
			List<IdDatumPair> res2 = index.search(query, threshold, angle, 0.3);
			System.out.print("\t" + res2.size() + "\t" + convertMetric.reset());
			List<IdDatumPair> res3 = index.search(query, threshold, angle, 0.4);
			System.out.println("\t" + res3.size() + "\t" + convertMetric.reset());
		}
	}
}
