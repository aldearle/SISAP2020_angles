package uk.al_richard.experimental.angles.MSCDependent;

import static uk.al_richard.experimental.angles.Util.square;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import eu.similarity.msc.core_concepts.Metric;
import eu.similarity.msc.data.MetricSpaceResource;

public class GenerateAngleHistogram {
	private MetricSpaceResource<Integer, float[]> msr;
	private Map<Integer, float[]> data;
	private Map<Integer, float[]> queries;
	private Metric<float[]> metric;
	private Map<Integer, Integer[]> nnIds;
	private Map<Integer, double[]> thresholds;
	private Set<Integer> viewpointIds;
	private Iterator<Integer> dataSetIdIterator;
	private final String dataset_name;
	private int noOfSampledPoints;
	private int noOfQueries;

	public GenerateAngleHistogram(MetricSpaceResource<Integer, float[]> msr, int noOfQueries, int noOfViewpoints,
			int noOfSampledPoints) throws Exception {
		this.msr = msr;
		this.data = msr.getData();
		this.queries = msr.getQueries();
		this.metric = this.msr.getMetric();
		this.nnIds = msr.getNNIds();
		this.thresholds = msr.getThresholds();
		this.dataset_name = msr.getClass().getName();
		this.dataSetIdIterator = this.data.keySet().iterator();
		this.viewpointIds = new TreeSet<>();
		for (int i = 0; i < noOfViewpoints; i++) {
			this.viewpointIds.add(this.dataSetIdIterator.next());
		}
		this.noOfSampledPoints = noOfSampledPoints;
		this.noOfQueries = noOfQueries;
	}

	/**
	 * print out all of the angles from count points drawn from the dataset from the
	 * viewpoint
	 *
	 **/
	@SuppressWarnings("boxing")
	public void generateAngles(boolean constrained) {
		System.out.println(this.dataset_name + " " + constrained);

		int noOfQueriesToTest = this.noOfQueries;
		// randomly selected query, in fact first element of queries
		for (int queryId : this.queries.keySet()) {
			if (noOfQueriesToTest > 0) {
				// randomly selected viewpoint, in fact first element of data
				float[] query = this.queries.get(queryId);
				Integer[] nnids = this.nnIds.get(queryId);

				AngleInfo ai = getAngleSample(constrained, query, nnids);
				System.out.println(queryId + "\t" + this.thresholds.get(queryId)[5] + "\t" + ai.getMean() + "\t"
						+ ai.getStd() + "\t" + ai.getMin() + "\t" + ai.getMax());
				noOfQueriesToTest--;
			}
		}
	}

	@SuppressWarnings("boxing")
	private AngleInfo getAngleSample(boolean constrained, float[] b, Integer[] nnids) {
		int sampledPoints = Math.min(this.noOfSampledPoints, nnids.length - 1);
		double[] angles = new double[this.viewpointIds.size() * sampledPoints];
		double acc = 0;
		int ptr = 0;
		for (int viewPointId : this.viewpointIds) {
			float[] a = this.data.get(viewPointId);
			// run through the query nearest neighbours, avoid the first as it may be the
			// query itself in some cases
			boolean first = true;
			int noToDo = this.noOfSampledPoints;
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

						final double cosine = (square(aA) + square(cC) - square(bB)) / (2 * aA * cC);
						double theta = Math.acos(cosine);
						if (!Double.isNaN(theta)) {
							angles[ptr++] = theta;
							acc += theta;
							noToDo--;
						}
					}
				}
			}
		}
		return new AngleInfo(acc / ptr, angles);
	}

	private float[] getRandomDatum() {
		return this.data.get(this.dataSetIdIterator.next());
	}

}
