package uk.al_richard.experimental.angles.MSCDependent;

import eu.similarity.msc.data.DecafMetricSpace;
import eu.similarity.msc.data.MfAlexMetricSpace;
import eu.similarity.msc.data.SiftMetricSpace;
import uk.al_richard.experimental.angles.MSCDependent.GenerateAngleHistogram;

public class RunHistogram {
	public static void main(String[] args) throws Exception {

		testDecaf();

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
