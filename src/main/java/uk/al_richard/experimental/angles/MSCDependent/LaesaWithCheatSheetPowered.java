package uk.al_richard.experimental.angles.MSCDependent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import eu.similarity.msc.core_concepts.Metric;
import eu.similarity.msc.data.DataListView.IdDatumPair;

public class LaesaWithCheatSheetPowered<K, T> extends Laesa<IdDatumPair<K, T>> {

	private Map<K, List<K>> nnids;
	private int distancesCalculatedForLastSearch = 0;

	public LaesaWithCheatSheetPowered(List<IdDatumPair<K, T>> data, List<IdDatumPair<K, T>> refPoints,
			Metric<IdDatumPair<K, T>> metric, Map<K, List<K>> nnids) {
		super(data, refPoints, metric);
		this.nnids = nnids;
	}

	public List<IdDatumPair<K, T>> search(IdDatumPair<K, T> query, double t, double power) {
		List<IdDatumPair<K, T>> res = new ArrayList<>();
		double[] qDists = new double[this.refPoints.size()];
		int refPtr = 0;
		this.distancesCalculatedForLastSearch = this.refPoints.size();
		for (IdDatumPair<K, T> rPoint : this.refPoints) {
			qDists[refPtr++] = this.metric.distance(rPoint, query);
		}
		int dPtr = 0;
		List<K> solutions = this.nnids.get(query.id);

		double raisedT = Math.pow(t, power);
		for (IdDatumPair<K, T> datum : this.data) {
			if (!canExclude(qDists, this.refDists[dPtr++], raisedT, power)) {
				if (solutions.contains(datum.id)) {
					res.add(datum);
				}
				this.distancesCalculatedForLastSearch++;
			}
		}

		return res;
	}

	private static boolean canExclude(double[] qDists, double[] rDists, double raisedT, double power) {
		boolean excluded = false;
		int i = 0;
		while (i < qDists.length && !excluded)
			if (!excluded) {
				final double pq = Math.pow(qDists[i], power);
				final double ps = Math.pow(rDists[i], power);
				excluded = canExclude(raisedT, ps, pq);
				i++;
			}
		return excluded;
	}

	private static boolean canExclude(double t, double ps, double pq) {
		return pq + t < ps || pq > ps + t;
	}

	public int getDistances() {
		return this.distancesCalculatedForLastSearch;
	}
}
