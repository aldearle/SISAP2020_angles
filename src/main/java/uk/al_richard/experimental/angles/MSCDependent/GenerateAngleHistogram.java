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

public class GenerateAngleHistogram<K, T> {

	private Map<K, T> allData;
	private Map<K, T> allQueries;
	private List<IdDatumPair<K, T>> listData;
	private List<IdDatumPair<K, T>> listQueries;
	private Metric<IdDatumPair<K, T>> metric;
	private Map<K, List<K>> nnIds;

	private List<IdDatumPair<K, T>> references;
	private List<IdDatumPair<K, T>> witnesses;

	private int noOfQueries;
	private Random rand;

	public GenerateAngleHistogram(MetricSpaceResource<K, T> space, int noOfQueries, int noOfWitnesses)
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
		for (IdDatumPair<K, T> query : this.listQueries.subList(0, this.noOfQueries)) {
			double[] queryWitnessDists = new double[this.witnesses.size()];
			for (int i = 0; i < this.witnesses.size(); i++) {
				queryWitnessDists[i] = this.metric.distance(query, this.witnesses.get(i));
			}
			double queryLidim = Util.LIDimLevinaBickel(queryWitnessDists);
			System.out.print(query.id + "\t" + queryLidim);

			for (IdDatumPair<K, T> reference : this.references) {
				double pq = this.metric.distance(query, reference);

				if (pq != 0) {
					double solutionAngleAcc = 0;
					int count = 0;
					for (K i : this.nnIds.get(query.id)) {
						count++;

						T nn = this.allData.get(i);
						IdDatumPair<K, T> nnp = new IdDatumPair<>(i, nn);
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
		for (IdDatumPair<K, T> query : this.listQueries.subList(0, this.noOfQueries)) {
			System.out.print("<" + query.id + ">");
			double[] queryWitnessDists = new double[this.witnesses.size()];
			for (int i = 0; i < this.witnesses.size(); i++) {
				queryWitnessDists[i] = this.metric.distance(query, this.witnesses.get(i));
			}
			double queryLidim = Util.LIDimLevinaBickel(queryWitnessDists);
			pw.print(query.id + "\t" + queryLidim);

			for (IdDatumPair<K, T> reference : this.references) {
				double pq = this.metric.distance(query, reference);

				if (pq != 0) {
					double solutionAngleAcc = 0;
					int count = 0;

					final List<K> nnids = this.nnIds.get(query.id);
					for (int i = 0; i < nnids.size(); i++) {
						IdDatumPair<K, T> nnp = this.listData.get(this.rand.nextInt(this.listData.size()));
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
			}
			pw.println();
			pw.flush();
			System.out.println();
		}
	}
}