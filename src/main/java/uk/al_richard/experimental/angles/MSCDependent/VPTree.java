package uk.al_richard.experimental.angles.MSCDependent;

import java.util.ArrayList;
import java.util.List;

import eu.similarity.msc.core_concepts.Metric;
import eu.similarity.msc.util.ObjectWithDistance;
import eu.similarity.msc.util.Quicksort;

public class VPTree<T> {

	public final static class VPTreeNode<T1> {
		private T1 pivot;
		private double mu;
		private VPTreeNode<T1> left;
		private VPTreeNode<T1> right;

		VPTreeNode(List<T1> data, Metric<T1> metric) {
			if (data.size() == 0) {
				// do nothing
			} else if (data.size() == 1) {
				this.pivot = data.get(0);
			} else {
				this.pivot = data.get(0);
				@SuppressWarnings("unchecked")
				ObjectWithDistance<T1>[] objs = new ObjectWithDistance[data.size() - 1];
				int ptr = 0;
				for (T1 datum : data.subList(1, data.size())) {
					objs[ptr++] = new ObjectWithDistance<>(datum, metric.distance(this.pivot, datum));
				}
				Quicksort.placeMedian(objs);

				List<T1> leftList = new ArrayList<>();
				for (int i = 0; i < objs.length / 2; i++) {
					leftList.add(objs[i].getValue());
				}
				this.mu = objs[objs.length / 2].getDistance();
				List<T1> rightList = new ArrayList<>();
				for (int i = objs.length / 2; i < objs.length; i++) {
					rightList.add(objs[i].getValue());
				}
				if (leftList.size() > 0) {
					this.left = new VPTreeNode<>(leftList, metric);
				}
				if (rightList.size() > 0) {
					this.right = new VPTreeNode<>(rightList, metric);
				}
			}
		}

		public void search(T1 query, double t, List<T1> results, Metric<T1> metric, double angle) {
			double pq = metric.distance(this.pivot, query);
//			if (this.mu != 0) {
//				System.out.println(this.size + "\t" + this.mu + "\t" + pq);
//			}
			if (pq <= t) {
				results.add(this.pivot);
			}
			if (this.left != null && !(pq >= this.mu + t) && !(getAngleB(pq, this.mu, t) < angle)) {
				this.left.search(query, t, results, metric, angle);
			}
			if (this.right != null && !(pq + t <= this.mu)) {
				this.right.search(query, t, results, metric, angle);
			}
		}

		public static double getAngleB(double aA, double bB, double cC) {
			double cosTheta = (aA * aA + cC * cC - bB * bB) / (2 * aA * cC);
			return Math.acos(cosTheta);
		}
	}

	private VPTreeNode<T> index;
	private Metric<T> metric;

	public VPTree(List<T> data, Metric<T> metric) {
		this.metric = metric;
		this.index = new VPTreeNode<>(data, metric);
	}

	public List<T> search(T query, double threshold, double angle) {
		List<T> res = new ArrayList<>();
		this.index.search(query, threshold, res, this.metric, angle);
		return res;
	}

}
