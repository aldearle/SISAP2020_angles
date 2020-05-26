package uk.al_richard.branch_git_experiment;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import eu.similarity.msc.core_concepts.Metric;
import eu.similarity.msc.data.DataListView.IdDatumPair;

public class LaesaWithCheatSheetPowered extends Laesa<IdDatumPair> {

	private Map<Integer, Integer[]> nnids;
	private int distancesCalculatedForLastSearch = 0;

	public LaesaWithCheatSheetPowered(List<IdDatumPair> data, List<IdDatumPair> refPoints, Metric<IdDatumPair> metric,
			Map<Integer, Integer[]> nnids) {
		super(data, refPoints, metric);
		this.nnids = nnids;
	}

	@SuppressWarnings("boxing")
	public List<IdDatumPair> search(IdDatumPair query, double t, double power) {
		List<IdDatumPair> res = new ArrayList<>();
		double[] qDists = new double[this.refPoints.size()];
		int refPtr = 0;
		this.distancesCalculatedForLastSearch = this.refPoints.size();
		for (IdDatumPair rPoint : this.refPoints) {
			qDists[refPtr++] = this.metric.distance(rPoint, query);
		}
		int dPtr = 0;
		Integer[] solutions = this.nnids.get(query.id);
		Set<Integer> sols = new HashSet<>();
		for (int i = 0; i < 100; i++) {
			sols.add(solutions[i]);
		}

		double raisedT = Math.pow(t, power);
		for (IdDatumPair datum : this.data) {
			if (!canExclude(qDists, this.refDists[dPtr++], raisedT, power)) {
				if (sols.contains(datum.id)) {
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
