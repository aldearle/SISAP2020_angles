package uk.al_richard.branch_git_experiment;

import static uk.al_richard.experimental.angles.Util.square;

import java.util.Iterator;
import java.util.Map;

import eu.similarity.msc.core_concepts.Metric;
import eu.similarity.msc.data.MetricSpaceResource;

public class GenerateAngleHistogram {
	public static class AngleInfo {
		private double mean;
		private double std;

		AngleInfo(double mean, double[] angles) {
			this.mean = mean;
			this.std = getStd(mean, angles);
		}

		private static double getStd(double mean, double[] angles) {
			double acc = 0;
			for (double ang : angles) {
				double diff = mean - ang;
				acc += diff * diff;
			}
			return Math.sqrt(acc / (angles.length - 1));
		}

		public double getMean() {
			return this.mean;
		}

		public double getStd() {
			return this.std;
		}
	}

	private MetricSpaceResource<Integer, float[]> msr;
	Map<Integer, float[]> data;
	Map<Integer, float[]> queries;
	Metric<float[]> metric;
	final Map<Integer, Integer[]> nnIds;
	private Map<Integer, double[]> thresholds;
	private float[] viewpoint;
	private Iterator<Integer> dataSetIdIterator;
	private final String dataset_name;

	public GenerateAngleHistogram(MetricSpaceResource<Integer, float[]> msr) throws Exception {
		this.msr = msr;
		this.data = msr.getData();
		this.queries = msr.getQueries();
		this.metric = this.msr.getMetric();
		this.nnIds = msr.getNNIds();
		this.thresholds = msr.getThresholds();
		this.dataset_name = msr.getClass().getName();
		this.dataSetIdIterator = this.data.keySet().iterator();
		// make viewpoint 2nd data point...!
		this.data.get(this.dataSetIdIterator.next());
		this.viewpoint = this.data.get(this.dataSetIdIterator.next());
	}

	/**
	 * print out all of the angles from count points drawn from the dataset from the
	 * viewpoint
	 *
	 **/
	@SuppressWarnings("boxing")
	public void generateAngles(boolean constrained) {
		System.out.println(this.dataset_name + " " + constrained);

		// randomly selected query, in fact first element of queries
		for (int queryId : this.queries.keySet()) {

			// randomly selected viewpoint, in fact first element of data
			float[] query = this.queries.get(queryId);
			Integer[] nnids = this.nnIds.get(queryId);

			AngleInfo ai = getAngleSample(constrained, this.viewpoint, query, nnids);
			System.out.println(
					queryId + "\t" + this.thresholds.get(queryId)[5] + "\t" + ai.getMean() + "\t" + ai.getStd());
		}
	}

	@SuppressWarnings("boxing")
	private AngleInfo getAngleSample(boolean constrained, float[] a, float[] b, Integer[] nnids) {
		// run through the query nearest neighbours, avoid the first as it may be the
		// query itself in some cases
		boolean first = true;
		// let's just do 100 nns max
		int noToDo = 100;
		double[] angles = new double[nnids.length - 1];
		int ptr = 0;
		double acc = 0;
		for (int nnid : nnids) {
			if (noToDo > 0) {
				if (first) {
					first = false;
				} else {
					float[] c;
					if (constrained) {
						c = this.data.get(nnid);
					} else {
						c = getRandomDatum();
					}

					double aA = this.metric.distance(b, c);
					double bB = this.metric.distance(a, c);
					double cC = this.metric.distance(a, b);

					double theta = Math.acos((square(aA) + square(cC) - square(bB)) / (2 * aA * cC));
					angles[ptr++] = theta;
					acc += theta;
					noToDo--;
				}
			}
		}
		return new AngleInfo(acc / ptr, angles);
	}

	private float[] getRandomDatum() {
		return this.data.get(this.dataSetIdIterator.next());
	}

}
