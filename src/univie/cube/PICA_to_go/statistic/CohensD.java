package univie.cube.PICA_to_go.statistic;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;

public class CohensD {

	private List<Pair<Stat, Stat>> data;
	
	public CohensD(List<Pair<Stat, Stat>> data) {
		this.data = data;
	}
	
	public List<Double> calc() {
		return data.stream()
					.map((p) -> {
						Stat firstStat = p.getLeft();
						Stat secondStat = p.getRight();
						double stdEstimation = getStdEst(firstStat.stdev, secondStat.stdev);
						return Math.abs((firstStat.mean - secondStat.mean) / stdEstimation);
					})
					.collect(Collectors.toList());
	}
	
	private double getStdEst(double firstStdev, double secondStdev) {
		return squareRoot((quadrat(firstStdev) + quadrat(secondStdev))/2);
	}
	
	private double quadrat(double val) {
		return Math.pow(val, 2);
	}
	
	private double squareRoot(double val) {
		return Math.pow(val, 0.5);
	}
	
}
