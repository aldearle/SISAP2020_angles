package uk.al_richard.experimental.angles.MSCDependent;

import java.util.ArrayList;
import java.util.List;

import eu.similarity.msc.core_concepts.Metric;

public class LaesaJamesButNotAsWeKnowIt<T> {

	protected List<T> data;
	protected Metric<T> metric;
	protected List<T> refPoints;
	protected double[][] refDists;
	protected double[][] interRefDistTable;
	protected double[] refDistRandomMeans;
	protected double[] refDistRandomStds;

	public LaesaJamesButNotAsWeKnowIt(List<T> data, List<T> refPoints, Metric<T> metric) {
		this.data = data;
		this.metric = metric;
		this.refPoints = refPoints;
		this.refDists = new double[data.size()][refPoints.size()];
		int datPtr = 0;
		for (T dPoint : data) {
			// for each datum, calculate all distances to reference points
			int refPtr = 0;
			double acc = 0;
			for (T rPoint : refPoints) {
				final double distance = this.metric.distance(rPoint, dPoint);
				this.refDists[datPtr][refPtr] = distance;
				acc += distance;
				refPtr++;
			}
			// but now also store mean and standard deviation of angles...
			datPtr++;
		}
	}

	public List<T> search(T query, double t, double meanAngle, double threeSigma) {
		double maxCosTheta = Math.cos(meanAngle - threeSigma);
		double minCosTheta = Math.cos(meanAngle + threeSigma);
		List<T> res = new ArrayList<>();
		double[] qDists = new double[this.refPoints.size()];
		int refPtr = 0;
		for (T rPoint : this.refPoints) {
			qDists[refPtr++] = this.metric.distance(rPoint, query);
		}
		int dPtr = 0;
		for (T datum : this.data) {
			if (!canExclude(qDists, this.refDists[dPtr++], t, maxCosTheta, minCosTheta)) {
				if (this.metric.distance(query, datum) <= t) {
					res.add(datum);
				}
			}
		}

		return res;
	}

	protected static boolean cosThetaOutOfRange(double aA, double bB, double cC, double maxCosTheta,
			double minCosTheta) {
		try {
			double cosTheta = (aA * aA + cC * cC - bB * bB) / (2 * aA * cC);
			return cosTheta > maxCosTheta || cosTheta < minCosTheta;
		} catch (RuntimeException e) {
			return true;
		}
	}

	protected static boolean canExclude(double[] qDists, double[] rDists, double t, double maxCosTheta,
			double minCosTheta) {
		boolean excluded = false;
		int i = 0;
		while (i < qDists.length && !excluded)
			if (!excluded) {
				final double pq = qDists[i];
				final double ps = rDists[i];
				excluded = cosThetaOutOfRange(t, ps, pq, maxCosTheta, minCosTheta);
				i++;
			}
		return excluded;
	}
}
