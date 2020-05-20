package uk.al_richard.branch_git_experiment;

import static uk.al_richard.experimental.angles.Util.square;

import java.util.Iterator;
import java.util.Map;

import eu.similarity.msc.core_concepts.Metric;
import eu.similarity.msc.data.MetricSpaceResource;
import eu.similarity.msc.data.SiftMetricSpace;

public class GenerateAngleHistogram {

	private MetricSpaceResource<Integer, float[]> msr;
	Map<Integer, float[]> data;
	Map<Integer, float[]> queries;
	Metric<float[]> metric;
	final Map<Integer, Integer[]> nnIds;
	private Iterator<Integer> dataSetIdIterator;
	private final String dataset_name;

	public GenerateAngleHistogram(MetricSpaceResource<Integer, float[]> msr) throws Exception {
		this.msr = msr;
		this.data = msr.getData();
		this.queries = msr.getQueries();
		this.metric = this.msr.getMetric();
		this.nnIds = msr.getNNIds();
		this.dataset_name = msr.getClass().getName();
		this.dataSetIdIterator = this.data.keySet().iterator();
	}

	/**
	 * print out all of the angles from count points drawn from the dataset from the
	 * viewpoint
	 *
	 **/
	@SuppressWarnings("boxing")
	public void generateAngles(boolean constrained) {
		System.out.println(this.dataset_name + " " + constrained);

		// randomly selected viewpoint, in fact first element of data
		float[] viewpoint = this.data.get(this.dataSetIdIterator.next());

		// randomly selected query, in fact first element of queries
//		int queryId = this.queries.keySet().iterator().next();
		int queryId = 834618;
		/*
		 * 0000834618 query in profiset has small query radius 0001143758 has big one
		 */

		// randomly selected viewpoint, in fact first element of data
		float[] centre = this.queries.get(queryId);

		// run through the query nearest neighbours, avoid the first as it may be the
		// query itself in some cases
		boolean first = true;
		Integer[] nnids = this.nnIds.get(queryId);
		for (int nnid : nnids) {
			if (first) {
				first = false;
			} else {
				float[] some_point;
				if (constrained) {
					some_point = this.data.get(nnid);
				} else {
					some_point = getRandomDatum();
				}

				double dqpi = this.metric.distance(centre, some_point);
				double p1pi = this.metric.distance(viewpoint, some_point);
				double d_viewpoint_q = this.metric.distance(viewpoint, centre);

				double theta = Math
						.acos((square(dqpi) + square(d_viewpoint_q) - square(p1pi)) / (2 * dqpi * d_viewpoint_q));

				System.out.println(theta);

			}
		}
	}

	private float[] getRandomDatum() {
		return this.data.get(this.dataSetIdIterator.next());
	}

}
