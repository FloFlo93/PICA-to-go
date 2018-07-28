package univie.cube.PicaDesktop.clustering.comparison.pipelines;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import univie.cube.PicaDesktop.clustering.comparison.ClusterComparisonInterface;
import univie.cube.PicaDesktop.clustering.comparison.RandIndex;
import univie.cube.PicaDesktop.clustering.comparison.RandIndex;
import univie.cube.PicaDesktop.clustering.datatypes.COG;

public class RandPipeline extends ClusterComparisonInterface {
	
	public static void main(String[] args) {
		RandPipeline pipeline = new RandPipeline();
		pipeline.startPipeline(args); 
	}

	public void startPipeline(String[] args) {
		try {
			inputArgsParsing(args);
		}
		catch(FileNotFoundException e) {
			System.err.println("ERROR: One of the paths [a, b] you specified does not point to a file or the parent directory of the outputfile [o] does not exist");
			System.exit(1);
		}
		List<Set<String>> orthogroupsConv1 = orthogroupsConverter(getOrthogroups1());
		List<Set<String>> orthogroupsConv2 = orthogroupsConverter(getOrthogroups2());
		
		RandIndex randIndex = new RandIndex(orthogroupsConv1, orthogroupsConv2);
		double index = 0;
		index = randIndex.calcRandIndex(threads);
		System.out.println(index);
	}
	
	private List<Set<String>> orthogroupsConverter(Map<String, COG> orthogroups) {
		return orthogroups.entrySet().stream()
									.map(entry -> entry.getValue())
									.map(entry -> entry.getGenes())
									.collect(Collectors.toList());
	}

}
