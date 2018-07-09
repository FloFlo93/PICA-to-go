package univie.cube.PicaDesktop.clustering.methods;



import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;

import univie.cube.PicaDesktop.archive.ZipCreator;
import univie.cube.PicaDesktop.clustering.datatypes.BinCOGs;
import univie.cube.PicaDesktop.clustering.datatypes.COG;
import univie.cube.PicaDesktop.directories.WorkDir;
import univie.cube.PicaDesktop.fastaformat.FastaHeaders;
import univie.cube.PicaDesktop.global.ExecutablePaths;
import univie.cube.PicaDesktop.miscellaneous.CmdExecution;


public class MMseqsClustering extends MMSeqs implements Clustering {
	
	private Path clusteringDirInput;
	private Path outputDB;
	
	private static final String tsvClustFileName = "clu.tsv";
	private static final String inputDbName = "DB_input.mmseqs";
	private static final String resultDbName = "DB_clustering.mmseqs";
	private static final String zippedDBName = "database.mmseqs";
	

	public MMseqsClustering(Path clusteringDirInput, Path outputDB) {
		this.clusteringDirInput = clusteringDirInput;
		this.outputDB = outputDB;
	}
	
	/**
	 * @param addOptions: additional options to cluster command (mmseqs linclust)
	 */
	public void runClustering(int threadNum, String[] addOptions) throws IOException, InterruptedException, RuntimeException {
		String mmseqsEx = ExecutablePaths.getExecutablePaths().MMSEQS_EX.toString();
		Path inputFile = concatInputFiles(clusteringDirInput);
		initializeFastaHeaders(inputFile);
		String inputDbName = "DB_input.mmseqs";
		String resultDbName = "DB_clustering.mmseqs";
		
		String[] commandCreateDB = {mmseqsEx, "createdb", inputFile.getFileName().toString(), inputDbName};
		String[] commandClust = {mmseqsEx, clusteringCommandHook(), inputDbName, resultDbName, "tmp", "--threads",Integer.toString(threadNum)};
		commandClust = ArrayUtils.addAll(commandClust, addOptions);
		String[] commandCreateTsv = {mmseqsEx, "createtsv", inputDbName, inputDbName, resultDbName, tsvClustFileName};
		CmdExecution.Status status = CmdExecution.execute(commandCreateDB, WorkDir.getWorkDir().getTmpDir(), "clust_createdb", clusteringDirInput.toFile());
		CmdExecution.printIfErrorOccured(status);
		if(status.errorOccured) throw new RuntimeException();
		inputFile.toFile().delete();
		CmdExecution.Status status2 = CmdExecution.execute(commandClust, WorkDir.getWorkDir().getTmpDir(), "clust", clusteringDirInput.toFile());
		CmdExecution.printIfErrorOccured(status2);
		if(status2.errorOccured) throw new RuntimeException();
		FileUtils.deleteDirectory(Paths.get(clusteringDirInput.toString(), "tmp").toFile());
		CmdExecution.Status status3 = CmdExecution.execute(commandCreateTsv, WorkDir.getWorkDir().getTmpDir(), "clust_createtsv", clusteringDirInput.toFile());
		CmdExecution.printIfErrorOccured(status3);
		if(status3.errorOccured) throw new RuntimeException();
		
		
	}

	public void createZippedDBFile() throws IOException, RuntimeException {
		List<Path> allDBs = getAllDBs();
		ZipCreator.createZip(Paths.get(outputDB.toString(), zippedDBName), allDBs);
	}
	
	private List<Path> getAllDBs() throws IOException, RuntimeException {
		List<Path> allDBs = new ArrayList<Path>();
		
		Files.walk(clusteringDirInput)
		.filter(path -> path.toFile().getName().contains(inputDbName) || path.toFile().getName().contains(resultDbName))
		.forEach(path -> allDBs.add(path));
		
		return allDBs;
	}
	
	protected String clusteringCommandHook() {
		return "cluster";
	}
	
	private void initializeFastaHeaders(Path concatProteomeFile) {
		super.fastaHeaders = FastaHeaders.getFastaHeadersFromConcatProteomes(concatProteomeFile);
	}
	
	@Override
	public Map<String, String> getFastaHeaders() {
		return super.fastaHeaders;
	}
	
	@Override
	public void runClustering(int threadNum) throws IOException, InterruptedException, RuntimeException {
		runClustering(threadNum, new String[0]);
	}
	
	
	

	@Override
	public Map<String, BinCOGs> getOrthogroupsPerBin() throws IOException {
		if(orthogroupsPerBin == null) readResultFileAndParseClustOutput();
		return this.orthogroupsPerBin;
	}

	@Override
	public Map<String, COG> getOrthogroups() throws IOException {
		if(orthogroups == null) readResultFileAndParseClustOutput();
		return this.orthogroups;
	}
	
	@Override
	public Optional<String> getRepresentativeSequence(String key) throws IOException, InterruptedException {
		MMSeqsRepresentative.initializeSingleton(Paths.get(clusteringDirInput.toString(), inputDbName), Paths.get(clusteringDirInput.toString(), resultDbName), clusteringDirInput);
		return MMSeqsRepresentative.getInstance().get(key);
	}

	@Override
	public void preparePicaInput(Path file) throws IOException {
		readResultFileAndParseClustOutput();
		writePicaInputFile(this.orthogroupsPerBin, file);	
	}
	
	private void readResultFileAndParseClustOutput() throws IOException {
		List<Path> clustOutput = Files.walk(clusteringDirInput).filter(path -> path.endsWith(tsvClustFileName)).collect(Collectors.toList());
		if (clustOutput.size() != 1) throw new RuntimeException("Exactly one clu.tsv file should be in output; found " + clustOutput.size());
		parseClustOutput(clustOutput.get(0));
		clustOutput.get(0).toFile().delete();
	}
	
	
	public static void writePicaInputFile(Map<String, BinCOGs> orthogroupsPerBin, Path file) throws FileNotFoundException {
		PrintWriter writer = new PrintWriter(file.toFile());
		
		for(Map.Entry<String, BinCOGs> bin : orthogroupsPerBin.entrySet()){
			writer.println(bin.getValue().toString());
		}
		
		writer.close();
	}
	

}
