package uk.al_richard.experimental.angles.MSCDependent;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import eu.similarity.msc.core_concepts.Metric;
import eu.similarity.msc.data.DataListView;
import eu.similarity.msc.data.DataListView.IdDatumPair;
import eu.similarity.msc.data.DecafMetricSpace;
import eu.similarity.msc.data.GistMetricSpace;
import eu.similarity.msc.data.MetricSpaceResource;
import eu.similarity.msc.data.MfAlexMetricSpace;
import eu.similarity.msc.data.SiftMetricSpace;

public class RunSearch {

	public static void main(String[] args) throws Exception {
//		recreateVladResults();

		final SiftMetricSpace sift = new SiftMetricSpace("/Volumes/Data/SIFT_mu/");
		final DecafMetricSpace decaf = new DecafMetricSpace("/Volumes/Data/profiset/");
		final MfAlexMetricSpace mf = new MfAlexMetricSpace("/Volumes/Data/mf_fc6/");
		MetricSpaceResource<Integer, float[]> gist = new GistMetricSpace("/Volumes/Data/mf_gist/");

		testLaesa("gist", gist);
//		recreateVladResults(gist);
	}

	private static void testLaesa(String name, MetricSpaceResource<Integer, float[]> space) throws Exception {
		System.out.println("testing " + space.getClass().getName());

//		System.out.println("testing sift");
//		final SiftMetricSpace sift = new SiftMetricSpace("/Volumes/Data/SIFT_mu/");
		List<IdDatumPair> idps = DataListView.convert(space.getData());
		List<IdDatumPair> refs = idps.subList(0, 256);
		List<IdDatumPair> data = idps.subList(1000, 101000);
		Map<Integer, double[]> qThreshes = space.getThresholds();
		Map<Integer, Integer[]> nnids = space.getNNIds();

		Metric<IdDatumPair> convertMetric = DataListView.convert(space.getMetric());

		LaesaWithCheatSheet index = new LaesaWithCheatSheet(data, refs, convertMetric, nnids);

		List<IdDatumPair> queries = DataListView.convert(space.getQueries());
		for (IdDatumPair query : queries) {
			@SuppressWarnings("boxing")
			
			double threshold = qThreshes.get(query.id)[99];
			System.out.print(query.id + "\t" + threshold);

			double angle = getAngle(name, threshold);

			List<IdDatumPair> res1 = index.search(query, threshold, Math.PI / 2, Math.PI / 2);
			System.out.print("\t" + res1.size() + "\t" + index.getDistances());
			for (int gap = 30; gap < 70; gap += 5) {
				List<IdDatumPair> res3 = index.search(query, threshold, angle, (float) gap / 100);
				System.out.print("\t" + res3.size() + "\t" + index.getDistances());
			}
			System.out.println();
		}
	}

	private static double getAngle(String name, double threshold) {
		// -0.0005x + 1.519 mf_alex equation... 1.519 - 0.0005 * threshold, sigma about
		// 0.2
		// -0.0013x + 1.602 sift equation, sigma about 0.2
		// decaf equation... 1.6865 - 0.0103 * threshold, sigma about 0.13
		// gist equation: = -2.3503x + 1.5518, sigma about 0.16
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
		case "mf":
			return mfAngle;
		default:
			throw new RuntimeException("no such space: " + name);
		}
	}

	private static double getLidimAngle(String name, double lidim) {
		// -0.0005x + 1.519 mf_alex equation... 1.519 - 0.0005 * threshold, sigma about
		// 0.2
		// -0.0013x + 1.602 sift equation, sigma about 0.2
		// decaf equation... 1.6865 - 0.0103 * threshold, sigma about 0.13
		// gist equation: = -2.3503x + 1.5518, sigma about 0.16
//		double siftAngle = 1.602 - 0.0013 * threshold;
//		
//		double decafAngle = 1.6865 - 0.0103 * threshold;
//		double mfAngle = 1.519 - 0.0005 * threshold;
//		double gistAngle = 1.5518 - 2.3503 * threshold;

		switch (name) {
		case "sift":
			return 0;
		case "gist":
			return 0;
		case "decaf":
			return 0;
		case "mf":
			return 0;
		default:
			throw new RuntimeException("no such space: " + name);
		}
	}

	private static double get3Sigma(String name, double threshold) {

		switch (name) {
		case "sift":
			return 0.6;
		case "gist":
			return 0.45;
		case "decaf":
			return 0.4;
		case "mf":
			return 0.6;
		default:
			throw new RuntimeException("no such space: " + name);
		}
	}

	private static void recreateVladResults(MetricSpaceResource<Integer, float[]> space) throws Exception {

		List<IdDatumPair> idps = DataListView.convert(space.getData());
		List<IdDatumPair> refs = idps.subList(0, 256);
		List<IdDatumPair> data = idps.subList(1000, 101000);
		Map<Integer, double[]> qThreshes = space.getThresholds();
		Map<Integer, Integer[]> nnids = space.getNNIds();

		Metric<IdDatumPair> convertMetric = DataListView.convert(space.getMetric());

		LaesaWithCheatSheetPowered index = new LaesaWithCheatSheetPowered(data, refs, convertMetric, nnids);

		List<IdDatumPair> queries = DataListView.convert(space.getQueries());
		for (IdDatumPair query : queries) {
			@SuppressWarnings("boxing")
			double threshold = qThreshes.get(query.id)[99];
			System.out.print(query.id + "\t" + threshold);

			List<IdDatumPair> res1 = index.search(query, threshold, 1);
			System.out.print("\t" + res1.size() + "\t" + index.getDistances());
			for (int power = 10; power < 25; power += 2) {
				List<IdDatumPair> res3 = index.search(query, threshold, (float) power / 10);
				System.out.print("\t" + res3.size() + "\t" + index.getDistances());
			}
			System.out.println();
		}
	}

}
