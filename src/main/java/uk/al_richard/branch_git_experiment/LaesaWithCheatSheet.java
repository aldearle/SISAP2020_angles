package uk.al_richard.branch_git_experiment;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import eu.similarity.msc.core_concepts.Metric;
import eu.similarity.msc.data.DataListView.IdDatumPair;

public class LaesaWithCheatSheet extends Laesa<IdDatumPair> {

	private Map<Integer, Integer[]> nnids;
	private int distancesCalculatedForLastSearch = 0;

	public LaesaWithCheatSheet(List<IdDatumPair> data, List<IdDatumPair> refPoints, Metric<IdDatumPair> metric,
			Map<Integer, Integer[]> nnids) {
		super(data, refPoints, metric);
		this.nnids = nnids;
	}

	@SuppressWarnings("boxing")
	@Override
	public List<IdDatumPair> search(IdDatumPair query, double t, double meanAngle, double threeSigma) {
		double maxCosTheta = Math.cos(meanAngle - threeSigma);
		double minCosTheta = Math.cos(meanAngle + threeSigma);
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

		for (IdDatumPair datum : this.data) {
			if (!canExclude(qDists, this.refDists[dPtr++], t, maxCosTheta, minCosTheta)) {
				if (sols.contains(datum.id)) {
					res.add(datum);
				}
				this.distancesCalculatedForLastSearch++;
			}
		}

		return res;
	}

	public int getDistances() {
		return this.distancesCalculatedForLastSearch;
	}
}
