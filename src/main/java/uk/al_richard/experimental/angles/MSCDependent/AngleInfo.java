package uk.al_richard.experimental.angles.MSCDependent;

import static uk.al_richard.experimental.angles.Util.square;

public class AngleInfo {
	private double mean;
	private double std;
	private double min;
	private double max;

	AngleInfo(double mean, double[] angles) {
		this.mean = mean;
		this.min = Double.MAX_VALUE;
		this.max = 0;
		this.std = getStd(angles);
	}

	private double getStd(double[] angles) {
		double acc = 0;
		for (double ang : angles) {
			this.min = Math.min(ang, this.getMin());
			this.max = Math.max(ang, this.getMax());
			double diff = this.mean - ang;
			acc += diff * diff;
		}
		return Math.sqrt(acc / (angles.length - 1));
	}

	public double getMean() {
		final double res = this.mean;
		if (Double.isNaN(res)) {
			throw new RuntimeException("getMean is NaN");
		}
		return res;
	}

	public double getStd() {
		return this.std;
	}

	public double getMin() {
		return this.min;
	}

	public double getMax() {
		return this.max;
	}

	public static double getAngle(double aA, double bB, double cC) {
		try {
			final double cosine = (square(aA) + square(cC) - square(bB)) / (2 * aA * cC);
			double theta = Math.acos(cosine);
			if (!Double.isNaN(theta)) {
				return theta;
			} else {
				throw new RuntimeException("angle was NaN in AngleInfo (A was " + aA + ", C was" + cC + ")");
			}
		} catch (Throwable e) {
			System.out.println("oh fuck there is an arithmetic exception in getAngle");
			return 0;
		}
	}
}