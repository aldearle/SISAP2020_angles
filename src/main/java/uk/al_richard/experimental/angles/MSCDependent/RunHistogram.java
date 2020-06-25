package uk.al_richard.experimental.angles.MSCDependent;

import java.io.PrintWriter;

import eu.similarity.msc.data.DecafMetricSpace;
import eu.similarity.msc.data.GistMetricSpace;
import eu.similarity.msc.data.MetricSpaceResource;
import eu.similarity.msc.data.MfAlexMetricSpace;
import eu.similarity.msc.data.MfAlexMetricSpace_old;
import eu.similarity.msc.data.SiftMetricSpace;

public class RunHistogram {
	public static void main(String[] args) throws Exception {

		final MetricSpaceResource<Integer, float[]> gist = new GistMetricSpace("/Volumes/Data/mf_gist/");
		final SiftMetricSpace sift = new SiftMetricSpace("/Volumes/Data/SIFT_mu/");
		final DecafMetricSpace decaf = new DecafMetricSpace("/Volumes/Data/profiset/");
		final MfAlexMetricSpace mfa = new MfAlexMetricSpace("/Volumes/Data/mf_fc6/");

		createHistogramInfo(mfa);
	}

	private static void createHistogramInfo(MetricSpaceResource<Integer, float[]> space) throws Exception {

		GenerateAngleHistogram h = new GenerateAngleHistogram(space, 500, 500);
//		h.generateQuerySolutionAngles();
		final PrintWriter pw = new PrintWriter("/Volumes/Data/temp/mfAlexQueryNonSolutions.txt");
		try {
			h.generateQueryNonSolutionAngles(pw);
		} catch (Throwable e) {
			e.printStackTrace();
		}
		pw.close();
	}

}
