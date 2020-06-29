package uk.al_richard.experimental.angles.MSCDependent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import eu.similarity.msc.core_concepts.Metric;
import eu.similarity.msc.data.DataListView.IdDatumPair;
import uk.al_richard.experimental.angles.Util;

public class LaesaLidimCheatSheet<K, T> extends LaesaWithCheatSheet<K, T> {

	public LaesaLidimCheatSheet(List<IdDatumPair<K, T>> data, List<IdDatumPair<K, T>> refPoints,
			Metric<IdDatumPair<K, T>> metric, Map<K, List<K>> nnids) {
		super(data, refPoints, metric, nnids);
	}

	@SuppressWarnings("boxing")
	public List<IdDatumPair<K, T>> search(IdDatumPair<K, T> query, double t, Function<Double, Double> lidimToAngle,
			double plusOrMinus) throws Exception {

		List<IdDatumPair<K, T>> res = new ArrayList<>();

		// calculate query to pivot distances
		double[] qDists = new double[this.refPoints.size()];
		int refPtr = 0;
		for (IdDatumPair<K, T> rPoint : this.refPoints) {
			qDists[refPtr++] = this.metric.distance(rPoint, query);
		}
		// Calculate the LIDIM of the query and get the angle
		double lidim = Util.LIDimLevinaBickel(qDists);
		double meanAngle = lidimToAngle.apply(lidim);
		double maxCosTheta = Math.cos(meanAngle - plusOrMinus);
		double minCosTheta = Math.cos(meanAngle + plusOrMinus);

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
}
