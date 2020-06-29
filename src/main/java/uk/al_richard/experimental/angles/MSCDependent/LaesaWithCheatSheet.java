package uk.al_richard.experimental.angles.MSCDependent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import eu.similarity.msc.core_concepts.Metric;
import eu.similarity.msc.data.DataListView.IdDatumPair;

public class LaesaWithCheatSheet<K, T> extends Laesa<IdDatumPair<K, T>> {

	protected Map<K, List<K>> nnids;
	protected int distancesCalculatedForLastSearch = 0;

	public LaesaWithCheatSheet(List<IdDatumPair<K, T>> data, List<IdDatumPair<K, T>> refPoints,
			Metric<IdDatumPair<K, T>> metric, Map<K, List<K>> nnids) {
		super(data, refPoints, metric);
		this.nnids = nnids;
	}

	@Override
	public List<IdDatumPair<K, T>> search(IdDatumPair<K, T> query, double t, double meanAngle, double threeSigma) {
		double maxCosTheta = Math.cos(meanAngle - threeSigma);
		double minCosTheta = Math.cos(meanAngle + threeSigma);
		List<IdDatumPair<K, T>> res = new ArrayList<>();

		// calculate query to pivot distances
		double[] qDists = new double[this.refPoints.size()];
		int refPtr = 0;
		for (IdDatumPair<K, T> rPoint : this.refPoints) {
			qDists[refPtr++] = this.metric.distance(rPoint, query);
		}

		this.distancesCalculatedForLastSearch = this.refPoints.size();
		int dPtr = 0;
		List<K> solutions = this.nnids.get(query.id);

		for (IdDatumPair<K, T> datum : this.data) {
			if (!canExclude(qDists, this.refDists[dPtr++], t, maxCosTheta, minCosTheta)) {
				if (solutions.contains(datum.id)) {
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
