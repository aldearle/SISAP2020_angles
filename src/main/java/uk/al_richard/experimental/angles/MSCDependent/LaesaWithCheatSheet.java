package uk.al_richard.experimental.angles.MSCDependent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import eu.similarity.msc.core_concepts.Metric;
import eu.similarity.msc.data.DataListView.IdDatumPair;

public class LaesaWithCheatSheet extends Laesa<IdDatumPair<float[]>> {

	protected Map<Integer, Integer[]> nnids;
	protected int distancesCalculatedForLastSearch = 0;

	public LaesaWithCheatSheet(List<IdDatumPair<float[]>> data, List<IdDatumPair<float[]>> refPoints, Metric<IdDatumPair<float[]>> metric,
			Map<Integer, Integer[]> nnids) {
		super(data, refPoints, metric);
		this.nnids = nnids;
	}

	@SuppressWarnings("boxing")
	@Override
	public List<IdDatumPair<float[]>> search(IdDatumPair<float[]> query, double t, double meanAngle, double threeSigma) {
		double maxCosTheta = Math.cos(meanAngle - threeSigma);
		double minCosTheta = Math.cos(meanAngle + threeSigma);
		List<IdDatumPair<float[]>> res = new ArrayList<>();

		// calculate query to pivot distances
		double[] qDists = new double[this.refPoints.size()];
		int refPtr = 0;
		for (IdDatumPair<float[]> rPoint : this.refPoints) {
			qDists[refPtr++] = this.metric.distance(rPoint, query);
		}

		this.distancesCalculatedForLastSearch = this.refPoints.size();
		int dPtr = 0;
		Integer[] solutions = this.nnids.get(query.id);
		Set<Integer> sols = new HashSet<>();
		for (int i = 0; i < 100; i++) {
			sols.add(solutions[i]);
		}

		for (IdDatumPair<float[]> datum : this.data) {
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
