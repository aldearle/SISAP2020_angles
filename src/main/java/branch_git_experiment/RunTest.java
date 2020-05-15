package branch_git_experiment;

import java.util.List;

import eu.similarity.msc.core_concepts.Metric;
import eu.similarity.msc.data.ExperimentalData;
import eu.similarity.msc.data.cartesian.CartesianPoint;

public class RunTest {

	public static void main(String[] args) throws Exception {
		System.out.println("hello git");

		ExperimentalData ed = new ExperimentalData(new DataContext(), ExperimentalData.DataSets.sift);
		Metric<CartesianPoint> euc = ed.getMetric();
		List<CartesianPoint> data = ed.getData1k();
		List<CartesianPoint> queries = ed.getQueries();
		for (CartesianPoint q : queries) {

		}
	}

}
