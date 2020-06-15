package uk.al_richard.experimental.angles.MSCDependent;

import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.Random;

import eu.similarity.msc.core_concepts.Metric;
import eu.similarity.msc.data.DataListView;
import eu.similarity.msc.data.DataListView.IdDatumPair;
import eu.similarity.msc.data.MetricSpaceResource;
import uk.al_richard.experimental.angles.Util;

public class GenerateAngleHistogram {

	private Map<Integer, float[]> allData;
	private Map<Integer, float[]> allQueries;
	private List<IdDatumPair> listData;
	private List<IdDatumPair> listQueries;
	private Metric<IdDatumPair> metric;
	private Map<Integer, Integer[]> nnIds;

	private List<IdDatumPair> references;
	private List<IdDatumPair> witnesses;

	private int noOfQueries;
	private Random rand;

	public GenerateAngleHistogram(MetricSpaceResource<Integer, float[]> space, int noOfQueries, int noOfWitnesses)
			throws Exception {

		this.allData = space.getData();
		this.listData = DataListView.convert(this.allData);
		this.allQueries = space.getQueries();
		this.listQueries = DataListView.convert(this.allQueries);
		this.references = DataListView.removeRandom(this.listData, noOfQueries);
		this.witnesses = DataListView.removeRandom(this.listData, noOfWitnesses);

		this.nnIds = space.getNNIds();
		this.metric = DataListView.convert(space.getMetric());

		this.noOfQueries = noOfQueries;
		this.rand = new Random(0);

	}

	/**
	 * print out all of the angles from count points drawn from the dataset from the
	 * viewpoint
	 * 
	 * @throws Exception
	 *
	 **/
	@SuppressWarnings("boxing")
	public void generateQuerySolutionAngles() throws Exception {
		// for each query, calculate the distribution of angles pqs

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
		for (IdDatumPair query : this.listQueries.subList(0, this.noOfQueries)) {
			double[] queryWitnessDists = new double[this.witnesses.size()];
			for (int i = 0; i < this.witnesses.size(); i++) {
				queryWitnessDists[i] = this.metric.distance(query, this.witnesses.get(i));
			}
			double queryLidim = Util.LIDimLevinaBickel(queryWitnessDists);
			System.out.print(query.id + "\t" + queryLidim);

			for (IdDatumPair reference : this.references) {
				double pq = this.metric.distance(query, reference);

				if (pq != 0) {
					double solutionAngleAcc = 0;
					int count = 0;
					for (int i : this.nnIds.get(query.id)) {
						count++;

						float[] nn = this.allData.get(i);
						IdDatumPair nnp = new IdDatumPair(i, nn);
						double ps = this.metric.distance(nnp, reference);
						double qs = this.metric.distance(nnp, query);
						if (ps != 0 && qs != 0) {
							double angle = AngleInfo.getAngle(pq, ps, qs);
							solutionAngleAcc += angle;
						}
					}

					System.out.print("\t" + solutionAngleAcc / count);
				} else {
					System.out.print("\t" + "-");
				}
			}
			System.out.println();
		}
	}

	/**
	 * print out all of the angles from count points drawn from the dataset from the
	 * viewpoint
	 * 
	 * @throws Exception
	 *
	 **/
	@SuppressWarnings("boxing")
	public void generateQueryNonSolutionAngles(PrintWriter pw) throws Exception {
		// for each query, calculate the distribution of angles pqw

		// calculate up front the reference to witness distance table
		double[][] referenceWitnessDists = new double[this.references.size()][this.witnesses.size()];
		pw.print("\t");
		for (int i = 0; i < this.references.size(); i++) {
			for (int j = 0; j < this.witnesses.size(); j++) {
				referenceWitnessDists[i][j] = this.metric.distance(this.references.get(i), this.witnesses.get(j));
			}
			pw.print("\t" + Util.LIDimLevinaBickel(referenceWitnessDists[i]));
		}
		pw.println();

		// for each query...
		for (IdDatumPair query : this.listQueries.subList(0, this.noOfQueries)) {
			System.out.print("<" + query.id + ">");
			double[] queryWitnessDists = new double[this.witnesses.size()];
			for (int i = 0; i < this.witnesses.size(); i++) {
				queryWitnessDists[i] = this.metric.distance(query, this.witnesses.get(i));
			}
			double queryLidim = Util.LIDimLevinaBickel(queryWitnessDists);
			pw.print(query.id + "\t" + queryLidim);

			for (IdDatumPair reference : this.references) {
				System.out.print("<" + reference.id + ">");

				double pq = this.metric.distance(query, reference);

				if (pq != 0) {
					double solutionAngleAcc = 0;
					int count = 0;

					if (query.id == 761609 && reference.id == 1678590) {
						System.out.println();
						System.out.println("about to fail!!!");
						System.out.println();
					}
					final Integer[] nnids = this.nnIds.get(query.id);
					for (int i : nnids) {
						if (count % 20 == 0) {
							System.out.print(".");
						}
						// in this loop, the program is quitting...?!

						IdDatumPair nnp = this.listData.get(this.rand.nextInt(this.listData.size()));
						double ps = this.metric.distance(nnp, reference);
						double qs = this.metric.distance(nnp, query);
						if (ps != 0 && qs != 0) {
							double angle = AngleInfo.getAngle(pq, ps, qs);
							solutionAngleAcc += angle;
							count++;
						}
					}

					pw.print("\t" + solutionAngleAcc / count);
				} else {
					pw.print("\t" + "-");
				}
				System.out.print("</" + reference.id + ">");
			}
			pw.println();
			pw.flush();
			System.out.println();
		}
	}
}