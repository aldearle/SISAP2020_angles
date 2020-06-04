package uk.al_richard.experimental.angles.MSCDependent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import eu.similarity.msc.data.DataListView;
import eu.similarity.msc.data.DecafMetricSpace;
import eu.similarity.msc.data.GistMetricSpace;
import eu.similarity.msc.data.MetricSpaceResource;
import eu.similarity.msc.data.SiftMetricSpace;

import uk.al_richard.experimental.angles.MSCDependent.MarginBlasterDeepCopy.Util;

public class LidimNNdistChecker {

	public static void main(String[] args) throws Exception {
//		final SiftMetricSpace space = new SiftMetricSpace("/Volumes/Data/SIFT_mu/");
//		final DecafMetricSpace space = new DecafMetricSpace("/Volumes/Data/profiset/");
//		final MfAlexMetricSpace space = new MfAlexMetricSpace("/Volumes/Data/mf_fc6/");

//		System.out.println("testing " + space.getClass().getName());
//
		System.out.println("testing GistMetricSpace");
		GistMetricSpace space = new GistMetricSpace("/Volumes/Data/mf_gist/");

		Map<Integer, float[]> data = space.getData(1);
		Map<Integer, float[]> queries = space.getQueries();
////
////		final Map<Integer, float[]> data2 = space.getData();
////		List<IdDatumPair> allData = DataListView.convert(data2);
//		DataListView dlv;
//		List<DataListView.IdDatumPair> queries = DataListView.convert(queries2);
////		List<IdDatumPair> refs = allData.subList(0, 1000);
////		List<IdDatumPair> data = allData.subList(1000, allData.size());
////		Map<Integer, Integer[]> nnids = space.getNNIds();
//
		System.out.println("query id" + "\t" + "local idim");
		int required = 100;
		for (int qid : queries.keySet()) {
			if (required-- > 0) {
				float[] query = queries.get(qid);
				List<Double> dists = new ArrayList<>();
				for (float[] d : data.values()) {
					dists.add(space.getMetric().distance(query, d));
				}
				double ldim = Util.LIDimLevinaBickel(dists);
				System.out.println(qid + "\t" + ldim);
			}
		}

	}

	private static void isDecafNormalised(SiftMetricSpace space) {

		try {
			Map<Integer, float[]> data = space.getData(10);
			float[] origin = new float[data.values().iterator().next().length];
			for (float[] d : data.values()) {
				System.out.println(space.getMetric().distance(d, origin));
			}
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
