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

public class LaesaLidimCheatSheet extends LaesaWithCheatSheet {

	public LaesaLidimCheatSheet(List<IdDatumPair> data, List<IdDatumPair> refPoints, Metric<IdDatumPair> metric,
			Map<Integer, Integer[]> nnids) {
		super(data, refPoints, metric, nnids);
	}

	@SuppressWarnings("boxing")
	public List<IdDatumPair> search(IdDatumPair query, double t, Function<Double, Double> lidimToAngle,
			double plusOrMinus) throws Exception {

		List<IdDatumPair> res = new ArrayList<>();

		// calculate query to pivot distances
		double[] qDists = new double[this.refPoints.size()];
		int refPtr = 0;
		for (IdDatumPair rPoint : this.refPoints) {
			qDists[refPtr++] = this.metric.distance(rPoint, query);
		}
		// Calculate the LIDIM of the query and get the angle
		double lidim = Util.LIDimLevinaBickel(qDists);
		double meanAngle = lidimToAngle.apply(lidim);
		double maxCosTheta = Math.cos(meanAngle - plusOrMinus);
		double minCosTheta = Math.cos(meanAngle + plusOrMinus);

		this.distancesCalculatedForLastSearch = this.refPoints.size();
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
}
