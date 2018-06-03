package univie.cube.PicaDesktop.clustering.filtering;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang3.tuple.Pair;

import univie.cube.PicaDesktop.clustering.datatypes.BinCOGs;
import univie.cube.PicaDesktop.clustering.datatypes.COG;
import univie.cube.PicaDesktop.directories.WorkDir;

public class LinearFiltering extends ClusterFiltering {

	public LinearFiltering(WorkDir workDir, Path loggingDir) {
		super(workDir, loggingDir);
	}
	
	
	@Override
	public Pair<CrossValPerCutOff, CrossValPerCutOff> filter(Map<String, COG> orthogroups, Map<String, BinCOGs> orthogroupsPerBin,
			Path pathToInputPhenotypes, String feature, int threads)
			throws IOException, InterruptedException, ExecutionException {
		int maxClustSize = getMaxClusterSize(orthogroups);
		final int stepSize = (maxClustSize / 100 == 0) ? 1 : maxClustSize / 100; //filtering in 100 (or less) steps
		List<Integer> cutoffs = IntStream.rangeClosed(0, maxClustSize).boxed().filter(x -> (x % stepSize) == 0).collect(Collectors.toList());
		return filterStartPICAThreads(cutoffs, orthogroups, orthogroupsPerBin, pathToInputPhenotypes, feature, threads);
	}

}
