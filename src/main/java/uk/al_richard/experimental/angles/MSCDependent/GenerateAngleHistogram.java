package uk.al_richard.experimental.angles.MSCDependent;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import eu.similarity.msc.core_concepts.Metric;
import eu.similarity.msc.data.DataListView;
import eu.similarity.msc.data.DataListView.IdDatumPair;
import eu.similarity.msc.data.MetricSpaceResource;
import uk.al_richard.experimental.angles.Util;

public class GenerateAngleHistogram {
	Map<Integer, float[]> allData;
	private Map<Integer, float[]> allQueries;
	private List<IdDatumPair> data;
	private List<IdDatumPair> queries;
	private Metric<IdDatumPair> metric;
	private Map<Integer, Integer[]> nnIds;
	private Map<Integer, double[]> thresholds;

	List<IdDatumPair> references;
	List<IdDatumPair> witnesses;

	private final String dataset_name;
	private int noOfQueries;

	public GenerateAngleHistogram(MetricSpaceResource<Integer, float[]> space, int noOfQueries, int noOfWitnesses)
			throws Exception {

		this.allData = space.getData();
		this.data = DataListView.convert(allData);
		this.allQueries = space.getQueries();
		this.queries = DataListView.convert(allQueries);
		this.references = RunSearch.removeRandom(this.data, noOfQueries);
		this.witnesses = RunSearch.removeRandom(this.data, noOfWitnesses);

		this.thresholds = space.getThresholds();
		this.nnIds = space.getNNIds();
		this.metric = DataListView.convert(space.getMetric());

		this.noOfQueries = noOfQueries;
		this.dataset_name = space.getClass().getName();

	}

	/**
	 * print out all of the angles from count points drawn from the dataset from the
	 * viewpoint
	 * 
	 * @throws Exception
	 *
	 **/
	@SuppressWarnings("boxing")
	public void generateAngles() throws Exception {
		// for each query, calculate the angle pqs

		// calculate up front the reference to witness distance table
		double[][] referenceWitnessDists = new double[this.references.size()][this.witnesses.size()];
		System.out.print("\t");
		for (int i = 0; i < this.references.size(); i++) {
			for (int j = 0; j < this.witnesses.size(); j++) {
				referenceWitnessDists[i][j] = this.metric.distance(this.references.get(i), this.witnesses.get(j));
			}
			System.out.print("\t" + Util.LIDimLevinaBickel(referenceWitnessDists[i]));
		}
		System.out.println();

		// for each query...
		for (IdDatumPair query : this.queries.subList(0, this.noOfQueries)) {
			double[] queryWitnessDists = new double[this.witnesses.size()];
			for (int i = 0; i < this.witnesses.size(); i++) {
				queryWitnessDists[i] = this.metric.distance(query, this.witnesses.get(i));
			}
			double queryLidim = Util.LIDimLevinaBickel(queryWitnessDists);
			System.out.print(query.id + "\t" + queryLidim);
			double[] allMeans = new double[this.references.size()];
			double allMeansAcc = 0;
			for (IdDatumPair reference : this.references) {
				double pq = this.metric.distance(query, reference);

				if (pq != 0) {
					double solutionAngleAcc = 0;
//				double[] solutionAngles = new double[this.nnIds.size() - 1];
					int count = 0;
					for (int i : this.nnIds.get(query.id)) {
						count++;
						// horrible test because sometimes the query is one of its own solutions,
						// sometimes it isn't!
						if (i != query.id) {
							float[] nn = this.allData.get(i);
							IdDatumPair nnp = new IdDatumPair(i, nn);
							double ps = this.metric.distance(nnp, reference);
							double qs = this.metric.distance(nnp, query);
							double angle = 0;
							try {
								// happens sometimes, pq or ps must be zero
								angle = AngleInfo.getAngle(pq, ps, qs);
							} catch (Throwable t) {
//								Logger.getLogger(this.getClass().getName()).info(t.toString());
								count--;
							}
//						solutionAngles[ptr++] = angle;
							solutionAngleAcc += angle;
						}
					}
//				AngleInfo solutionAngleDist = new AngleInfo(solutionAngleAcc / this.nnIds.size(), solutionAngles);
					System.out.print("\t" + solutionAngleAcc / count);
				} else {
					System.out.print("\t" + "-");
				}
			}
			System.out.println();
		}
	}
}