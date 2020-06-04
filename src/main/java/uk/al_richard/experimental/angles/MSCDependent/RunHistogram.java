package uk.al_richard.experimental.angles.MSCDependent;

import eu.similarity.msc.data.DecafMetricSpace;
import eu.similarity.msc.data.GistMetricSpace;
import eu.similarity.msc.data.MetricSpaceResource;
import eu.similarity.msc.data.MfAlexMetricSpace;
import eu.similarity.msc.data.SiftMetricSpace;

public class RunHistogram {
	public static void main(String[] args) throws Exception {

		System.out.println("testing GistMetricSpace");
		MetricSpaceResource<Integer, float[]> gist = new GistMetricSpace("/Volumes/Data/mf_gist/");
		getSpaceHistograms(gist);
	}

	private static void getSpaceHistograms(MetricSpaceResource<Integer, float[]> msr) throws Exception {
		System.out.println("testing " + msr.getClass().getName());
		GenerateAngleHistogram h = new GenerateAngleHistogram(msr, 100, 100, 100);
		h.generateAngles(true);
		h.generateAngles(false);
	}

	private static void testDecaf() throws Exception {
		System.out.println("testing decaf");
		final DecafMetricSpace decaf = new DecafMetricSpace("/Volumes/Data/profiset/");
		GenerateAngleHistogram h = new GenerateAngleHistogram(decaf, 100, 100, 100);
		h.generateAngles(true);
		h.generateAngles(false);
	}

	private static void testSift() throws Exception {
		System.out.println("testing sift");
		final SiftMetricSpace decaf = new SiftMetricSpace("/Volumes/Data/SIFT_mu/");
		GenerateAngleHistogram h = new GenerateAngleHistogram(decaf, 100, 100, 100);
		h.generateAngles(true);
		h.generateAngles(false);
	}

	private static void testMf() throws Exception {
		System.out.println("testing mf");
		final MfAlexMetricSpace mfa = new MfAlexMetricSpace("/Volumes/Data/mf_fc6/");
		GenerateAngleHistogram h = new GenerateAngleHistogram(mfa, 100, 100, 100);
		h.generateAngles(true);
		h.generateAngles(false);
	}
}
