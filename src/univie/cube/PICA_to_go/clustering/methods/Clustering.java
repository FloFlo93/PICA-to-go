package univie.cube.PICA_to_go.clustering.methods;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

import univie.cube.PICA_to_go.clustering.datatypes.BinCOGs;
import univie.cube.PICA_to_go.clustering.datatypes.COG;

public interface Clustering {
	public void runClustering(int threadNum) throws IOException, InterruptedException, RuntimeException;
	public Map<String, BinCOGs> getOrthogroupsPerBin() throws IOException;
	public Map<String, COG> getOrthogroups() throws IOException;
	public void preparePicaInput(Path file) throws IOException;
	public void runClustering(int threadNum, String[] additionalOpt) throws IOException, InterruptedException, RuntimeException;
	public void createZippedDBFile() throws IOException, RuntimeException;
	public Optional<String> getRepresentativeSequence(String key) throws IOException, InterruptedException;
	public Map<String, String> getFastaHeaders();
}
