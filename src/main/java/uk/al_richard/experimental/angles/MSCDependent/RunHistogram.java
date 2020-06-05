package uk.al_richard.experimental.angles.MSCDependent;

import eu.similarity.msc.data.DecafMetricSpace;
import eu.similarity.msc.data.GistMetricSpace;
import eu.similarity.msc.data.MetricSpaceResource;
import eu.similarity.msc.data.MfAlexMetricSpace;
import eu.similarity.msc.data.SiftMetricSpace;

public class RunHistogram {
	public static void main(String[] args) throws Exception {

		MetricSpaceResource<Integer, float[]> gist = new GistMetricSpace("/Volumes/Data/mf_gist/");
		final SiftMetricSpace sift = new SiftMetricSpace("/Volumes/Data/SIFT_mu/");
		final DecafMetricSpace decaf = new DecafMetricSpace("/Volumes/Data/profiset/");
		final MfAlexMetricSpace mfa = new MfAlexMetricSpace("/Volumes/Data/mf_fc6/");

		createHistogramInfo(gist);

	}

	private static void createHistogramInfo(MetricSpaceResource<Integer, float[]> space) throws Exception {

		GenerateAngleHistogram h = new GenerateAngleHistogram(space, 500, 500);
		h.generateAngles();
	}

}
