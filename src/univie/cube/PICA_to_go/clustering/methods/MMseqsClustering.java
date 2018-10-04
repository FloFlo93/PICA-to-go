package univie.cube.PICA_to_go.clustering.methods;



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
import org.mapdb.HTreeMap;

import univie.cube.PICA_to_go.archive.ZipCreator;
import univie.cube.PICA_to_go.clustering.datatypes.GeneClust4Bin;
import univie.cube.PICA_to_go.clustering.datatypes.GeneCluster;
import univie.cube.PICA_to_go.directories.WorkDir;
import univie.cube.PICA_to_go.fastaformat.FastaHeaders;
import univie.cube.PICA_to_go.global.Config;
import univie.cube.PICA_to_go.miscellaneous.CmdExecution;
import univie.cube.PICA_to_go.out.logging.CustomLogger;


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
		String mmseqsEx = Config.getExecutablePaths().getMMSEQS_EX().toString();
		String c = Config.getExecutablePaths().getMMSEQS_C(); //coverage cutoff
		String e = Config.getExecutablePaths().getMMSEQS_E(); // e value cutoff
		String minSeqId = Config.getExecutablePaths().getMMSEQS_MIN_SEQ_ID(); //min seq id cutoff
		Path inputFile = concatInputFiles(clusteringDirInput);
		initializeFastaHeaders(inputFile);
		
		CustomLogger.getInstance().log(CustomLogger.LoggingWeight.INFO, "Start to cluster genes using MMSeqs2");
		
		String inputDbName = "DB_input.mmseqs";
		String resultDbName = "DB_clustering.mmseqs";
		
		String[] commandCreateDB = {mmseqsEx, "createdb", inputFile.getFileName().toString(), inputDbName};
		String[] commandClust = {mmseqsEx, clusteringCommandHook(), inputDbName, resultDbName, "tmp", "-c", c, "-e", e, "--min-seq-id", minSeqId ,"--threads",Integer.toString(threadNum)};
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
	
	protected String[] addAddOptToClust(String[] commandClust) {
		String[] addOpt = Config.getExecutablePaths().getADD_ARG_MMSEQS_CLUSTER();
		return ArrayUtils.addAll(commandClust, addOpt);
	}
	
	private void initializeFastaHeaders(Path concatproteinFile) throws IOException {
		super.fastaHeaders = FastaHeaders.getFastaHeadersFromConcatproteins(concatproteinFile);
	}
	
	@Override
	public HTreeMap<String, String> getFastaHeaders() {
		return super.fastaHeaders;
	}
	
	@Override
	public void runClustering(int threadNum) throws IOException, InterruptedException, RuntimeException {
		runClustering(threadNum, new String[0]);
	}
	
	
	

	@Override
	public Map<String, GeneClust4Bin> getgeneClustersPerBin() throws IOException {
		if(geneClustersPerBin == null) readResultFileAndParseClustOutput();
		return super.geneClustersPerBin;
	}

	@Override
	public HTreeMap<String, GeneCluster> getgeneClusters() throws IOException {
		if(geneClusters == null) readResultFileAndParseClustOutput();
		return super.geneClusters;
	}
	
	@Override
	public Optional<String> getRepresentativeSequence(String key) throws IOException, InterruptedException {
		MMSeqsRepresentative.initializeSingleton(Paths.get(clusteringDirInput.toString(), inputDbName), Paths.get(clusteringDirInput.toString(), resultDbName), clusteringDirInput);
		return MMSeqsRepresentative.getInstance().get(key);
	}

	@Override
	public void preparePicaInput(Path file) throws IOException {
		readResultFileAndParseClustOutput();
		writePicaInputFile(this.geneClustersPerBin, file);	
	}
	
	private void readResultFileAndParseClustOutput() throws IOException {
		List<Path> clustOutput = Files.walk(clusteringDirInput).filter(path -> path.endsWith(tsvClustFileName)).collect(Collectors.toList());
		if (clustOutput.size() != 1) throw new RuntimeException("Exactly one clu.tsv file should be in output; found " + clustOutput.size());
		parseClustOutput(clustOutput.get(0));
		clustOutput.get(0).toFile().delete();
	}
	
	
	public static void writePicaInputFile(Map<String, GeneClust4Bin> geneClustersPerBin, Path file) throws FileNotFoundException {
		PrintWriter writer = new PrintWriter(file.toFile());
		
		for(Map.Entry<String, GeneClust4Bin> bin : geneClustersPerBin.entrySet()){
			writer.println(bin.getValue().toString());
		}
		
		writer.close();
	}
	

}
