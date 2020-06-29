package uk.al_richard.experimental.angles.MSCDependent;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import eu.similarity.msc.core_concepts.Metric;
import eu.similarity.msc.data.DataListView;
import eu.similarity.msc.data.DataListView.IdDatumPair;
import eu.similarity.msc.data.DecafMetricSpace;
import eu.similarity.msc.data.GistMetricSpace;
import eu.similarity.msc.data.MetricSpaceResource;
import eu.similarity.msc.data.MfAlexMetricSpace;
import eu.similarity.msc.data.SiftMetricSpace;

public class RunSearch {

	@SuppressWarnings("unused")
	public static void main(String[] args) throws Exception {
//		recreateVladaResults();

		final SiftMetricSpace sift = new SiftMetricSpace("/Volumes/Data/SIFT_mu/");
		final DecafMetricSpace decaf = new DecafMetricSpace("/Volumes/Data/profiset/");
		final MfAlexMetricSpace mfAlex = new MfAlexMetricSpace("/Volumes/Data/mf_fc6/");
		MetricSpaceResource<Integer, float[]> gist = new GistMetricSpace("/Volumes/Data/mf_gist/");

		testLaesa("mfAlex", mfAlex);
//		testLaesa("decaf", decaf);
//		testLaesa("gist", gist);
//		testLaesa("decaf", decaf);
//		recreateVladResults(gist);
	}

	private static <K, T> void testLaesa(String name, MetricSpaceResource<K, T> space) throws Exception {
		System.out.println("testing " + space.getClass().getName());

		List<IdDatumPair<K, T>> data = DataListView.convert(space.getData());
		List<IdDatumPair<K, T>> refs = DataListView.removeRandom(data, 256);

		Map<K, double[]> qThreshes = space.getThresholds();
		Map<K, List<K>> nnids = space.getNNIds();

		Metric<IdDatumPair<K, T>> convertMetric = DataListView.convert(space.getMetric());

//		LaesaWithCheatSheet index = new LaesaWithCheatSheet(data, refs, convertMetric, nnids);
		LaesaLidimCheatSheet<K, T> index = new LaesaLidimCheatSheet<>(data, refs, convertMetric, nnids);

		List<IdDatumPair<K, T>> queries = DataListView.convert(space.getQueries());
		for (IdDatumPair<K, T> query : queries.subList(500, queries.size())) {
			@SuppressWarnings("boxing")

			double threshold = qThreshes.get(query.id)[99];
			System.out.print(query.id + "\t" + threshold);

			List<IdDatumPair<K, T>> res1 = index.search(query, threshold, Math.PI / 2, Math.PI / 2);
			System.out.print("\t" + res1.size() + "\t" + index.getDistances());
			for (int gap = 30; gap < 70; gap += 5) {
				List<IdDatumPair<K, T>> res3 = index.search(query, threshold, getLidimAngle(name), (float) gap / 100);
				System.out.print("\t" + res3.size() + "\t" + index.getDistances());
			}
			System.out.println();
		}
	}

	@SuppressWarnings("unused")
	private static double getAngle(String name, double threshold) {
		double siftAngle = 1.602 - 0.0013 * threshold;
		double decafAngle = 1.6865 - 0.0103 * threshold;
		double mfAngle = 1.519 - 0.0005 * threshold;
		double gistAngle = 1.5518 - 2.3503 * threshold;

		switch (name) {
		case "sift":
			return siftAngle;
		case "gist":
			return gistAngle;
		case "decaf":
			return decafAngle;
		case "mfAlex":
			return mfAngle;
		default:
			throw new RuntimeException("no such space: " + name);
		}
	}

	@SuppressWarnings("boxing")
	private static Function<Double, Double> getLidimAngle(String name) {
		// equations of best fit quadratic taken from Excel(!)
		switch (name) {
		case "sift":
			return (x) -> 0.0155 * x * x - 0.1613 * x + 1.7228;
		case "decaf":
			return (x) -> 0.0467 * x * x - 0.4077 * x + 1.8161;
		case "mfAlex":
			return (x) -> 0.0235 * x * x - 0.2234 * x + 1.5008;
		case "gist":
			return (x) -> 0.0626 * x * x - 0.3921 * x + 1.6445;
		default:
			throw new RuntimeException("space: " + name + " doesn't have lidim function defined");
		}
	}

	@SuppressWarnings("unused")
	private static <K, T> void recreateVladaResults(MetricSpaceResource<K, T> space) throws Exception {

		List<IdDatumPair<K, T>> idps = DataListView.convert(space.getData());
		List<IdDatumPair<K, T>> refs = idps.subList(0, 256);
		List<IdDatumPair<K, T>> data = idps.subList(1000, 101000);
		Map<K, double[]> qThreshes = space.getThresholds();
		Map<K, List<K>> nnids = space.getNNIds();

		Metric<IdDatumPair<K, T>> convertMetric = DataListView.convert(space.getMetric());

		LaesaWithCheatSheetPowered<K, T> index = new LaesaWithCheatSheetPowered<>(data, refs, convertMetric, nnids);

		List<IdDatumPair<K, T>> queries = DataListView.convert(space.getQueries());
		for (IdDatumPair<K, T> query : queries) {
			@SuppressWarnings("boxing")
			double threshold = qThreshes.get(query.id)[99];
			System.out.print(query.id + "\t" + threshold);

			List<IdDatumPair<K, T>> res1 = index.search(query, threshold, 1);
			System.out.print("\t" + res1.size() + "\t" + index.getDistances());
			for (int power = 10; power < 25; power += 2) {
				List<IdDatumPair<K, T>> res3 = index.search(query, threshold, (float) power / 10);
				System.out.print("\t" + res3.size() + "\t" + index.getDistances());
			}
			System.out.println();
		}
	}

}
