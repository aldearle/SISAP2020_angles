package uk.al_richard.branch_git_experiment;

import java.util.List;
import java.util.Map;

import eu.similarity.msc.core_concepts.Metric;
import eu.similarity.msc.data.DataListView;
import eu.similarity.msc.data.DataListView.IdDatumPair;
import eu.similarity.msc.data.DecafMetricSpace;
import eu.similarity.msc.data.MetricSpaceResource;
import eu.similarity.msc.data.MfAlexMetricSpace;
import eu.similarity.msc.data.SiftMetricSpace;

public class RunSearch {

	public static void main(String[] args) throws Exception {
//		recreateVladResults();

		final SiftMetricSpace sift = new SiftMetricSpace("/Volumes/Data/SIFT_mu/");
		final DecafMetricSpace decaf = new DecafMetricSpace("/Volumes/Data/profiset/");
		final MfAlexMetricSpace mf = new MfAlexMetricSpace("/Volumes/Data/mf_fc6/");

//		testLaesa("sift", sift);
		recreateVladResults(sift);
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
			// -0.0005x + 1.519 mf_alex equation... 1.519 - 0.0005 * threshold, sigma about
			// 0.2
			// -0.0013x + 1.602 sift equation, sigma about 0.2
			// decaf equation... 1.6865 - 0.0103 * threshold, sigma about 0.13
			double siftAngle = 1.602 - 0.0013 * threshold;
			double decafAngle = 1.6865 - 0.0103 * threshold;
			double mfAngle = 1.519 - 0.0005 * threshold;
			double angle = (name.equals("sift")) ? siftAngle : name.equals("decaf") ? decafAngle : mfAngle;
			List<IdDatumPair> res1 = index.search(query, threshold, Math.PI / 2, Math.PI / 2);
			System.out.print("\t" + res1.size() + "\t" + index.getDistances());
			for (int gap = 30; gap < 70; gap += 5) {
				List<IdDatumPair> res3 = index.search(query, threshold, angle, (float) gap / 100);
				System.out.print("\t" + res3.size() + "\t" + index.getDistances());
			}
			System.out.println();
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
			for (int power = 10; power < 35; power += 2) {
				List<IdDatumPair> res3 = index.search(query, threshold, (float) power / 10);
				System.out.print("\t" + res3.size() + "\t" + index.getDistances());
			}
			System.out.println();
		}
	}

}
