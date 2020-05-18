package uk.al_richard.branch_git_experiment;

import java.util.List;

import eu.similarity.msc.data.ExperimentalData;
import eu.similarity.msc.data.ExperimentalData.DataSets;
import eu.similarity.msc.data.cartesian.CartesianPoint;
import eu.similarity.msc.local_context.LocalContext;

public class FirstExample {
	public static void main(String[] args) {
		try {
			LocalContext lc = new MyLocalContext();
			DataSets myDataContext = DataSets.sift;
			ExperimentalData myDataSet = new ExperimentalData(lc, myDataContext);

			List<CartesianPoint> data = myDataSet.getData10k();
			List<CartesianPoint> queries = myDataSet.getQueries();

			double[] nns = myDataSet.getNNinfo(10);

			CartesianPoint query = queries.get(21);
			int datum = 0;
			for (CartesianPoint s : data) {
				double dist = myDataSet.getMetric().distance(query, s);
				if (dist <= nns[21]) {
					System.out.println("datum " + datum + " is in 5nn, distance " + dist);
				}
				datum++;
			}

		} catch (Throwable t) {
			System.out.println(t.toString());
		}
	}
}
