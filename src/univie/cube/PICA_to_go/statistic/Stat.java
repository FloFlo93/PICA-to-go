package univie.cube.PICA_to_go.statistic;

import org.apache.commons.math3.stat.descriptive.StatisticalSummary;

public class Stat implements StatisticalSummary {
	protected double mean;
	protected double stdev;
	protected int n;
	public Stat(double mean, double stdev, int n) {
		this.mean = mean;
		this.stdev = stdev;
		this.n = n;
	}
	public double getMean() {return mean;};
	public double getVariance() {return Math.pow(stdev,2);};
	@Override
	public String toString() {
		return mean + "\t" + stdev;
	}
	@Override
	public double getStandardDeviation() {
		return stdev;
	}
	@Override
	public double getMax() {
		throw new RuntimeException("not available");
	}
	@Override
	public double getMin() {
		throw new RuntimeException("not available");
	}
	@Override
	public long getN() {
		return n;
	}
	@Override
	public double getSum() {
		throw new RuntimeException("not available");
	}
}
