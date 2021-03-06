package univie.cube.PICA_to_go.clustering.methods;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;

import univie.cube.PICA_to_go.clustering.datatypes.GeneClust4Bin;
import univie.cube.PICA_to_go.clustering.datatypes.GeneCluster;
import univie.cube.PICA_to_go.directories.WorkDir;
import univie.cube.PICA_to_go.fastaformat.FastaHeaders;
import univie.cube.PICA_to_go.global.Config;
import univie.cube.PICA_to_go.miscellaneous.CmdExecution;
import univie.cube.PICA_to_go.miscellaneous.CmdExecution.Status;
import univie.cube.PICA_to_go.out.error.ErrorHandler;
import univie.cube.PICA_to_go.out.logging.CustomLogger;
import univie.cube.PICA_to_go.out.logging.CustomLogger.LoggingWeight;


public class MMSeqsClusterupdate extends MMSeqs {

	private static final String clustDbName = "DB_clustering.mmseqs";
	private static final String inputDbName = "DB_input.mmseqs";
	
	private Path predictBinsDir;
	private Path unzippedDbDir;
	private List<String> binNames = new ArrayList<String>();
	
	/**
	 * 
	 * @param testGenomes
	 * @param genes
	 * @param clusteringDir
	 * @param outputLog
	 * @throws IOException
	 */
	public MMSeqsClusterupdate(Path predictBinsDir, Path unzippedDbDir) throws IOException {
		this.predictBinsDir = predictBinsDir;
		this.unzippedDbDir = unzippedDbDir;
	}
	
	/**
	 * 
	 * @param threads
	 * @return Pair of geneClusters (left; Map<String, GeneCluster>) and geneClustersPerBin (right; Map<String, GeneClust4Bin>)
	 * @throws InterruptedException
	 * @throws IOException
	 * @throws RuntimeException
	 */
	public Pair<Map<String, GeneCluster>, Map<String, GeneClust4Bin>> clusterUpdate(int threads) throws InterruptedException, IOException, RuntimeException {		
		CustomLogger.getInstance().log(LoggingWeight.INFO, "Cluster-Update started");
		Files.walk(predictBinsDir).filter(path -> Files.isRegularFile(path)).forEach(file -> {
			String fileName = file.getFileName().toString();
			binNames.add(FastaHeaders.getFileNameWithoutSuffix(fileName));
		});
		Path inputForSeqDB = concatInputFiles(predictBinsDir);
		Path concatSeqDB = null;
		try {
			concatSeqDB = createInputSeqDB(inputForSeqDB);
		}
		catch(RuntimeException e) {
			(new ErrorHandler(e, ErrorHandler.ErrorWeight.FATAL, "concatSeqDB failed")).handle();
		}

		Path tsvFile = clusterupdateCommand(concatSeqDB, threads);
		if(tsvFile == null) throw new RuntimeException("mmseqs clusterupdate failed");
		parseClustOutput(tsvFile, binNames);
		Pair<Map<String, GeneCluster>, Map<String, GeneClust4Bin>> geneClusters_geneClustersPerBin = Pair.of(geneClusters, geneClustersPerBin);
		return geneClusters_geneClustersPerBin;
	}
	
	private Path createInputSeqDB(Path inputForSeqDB) throws IOException, InterruptedException {
		Path seqDBNewFiles = Files.createTempFile(unzippedDbDir, "inputSeqDB_newFiles", ".mmseqs");
		String[] commandCreateSeqDB = {Config.getExecutablePaths().getMMSEQS_EX().toString(), "createdb", inputForSeqDB.toString(), seqDBNewFiles.toString()};
		Status statusCreateDB = CmdExecution.execute(commandCreateSeqDB, WorkDir.getWorkDir().getTmpDir(), "mmseqs-createdb", unzippedDbDir.toFile());
		if(statusCreateDB.errorOccured) throw new RuntimeException("createdb failed");
		
		Path concatSeqDB = concatDBs(seqDBNewFiles, Paths.get(unzippedDbDir.toString(), inputDbName), unzippedDbDir, WorkDir.getWorkDir().getTmpDir());
		return concatSeqDB; 
	}
	
	
	
	private Path clusterupdateCommand(Path concatSeqDB, int threads) throws IOException, InterruptedException {
		String mappedSeq = unzippedDbDir.toString() + File.separator + "mappedSeq" + ".mmseqs";
		String newClust = unzippedDbDir.toString() + File.separator + "newClust" + ".mmseqs";
		String[] commandUpdateSeqDB = {Config.getExecutablePaths().getMMSEQS_EX().toString(), "clusterupdate", unzippedDbDir.toString() + File.separator + inputDbName, concatSeqDB.toString(), unzippedDbDir.toString() + File.separator + clustDbName, mappedSeq.toString(), newClust.toString(), "tmp", "-e", Config.getInstance().getMMSEQS_E(), "-c", Config.getInstance().getMMSEQS_C(), "--min-seq-id", Config.getInstance().getMMSEQS_MIN_SEQ_ID()};
		
		Status status = CmdExecution.execute(commandUpdateSeqDB, WorkDir.getWorkDir().getTmpDir(), "clusterupdate", Files.createTempDirectory(unzippedDbDir, "mmseqs_clusterupdate_tmp").toFile());
		if(! status.errorOccured) return createTsv(mappedSeq, newClust);
		else return null;
	}
	
	private Path createTsv(String seqDB, String clustDB) throws IOException, InterruptedException {
		Path newSeqClustTsv = Files.createTempFile(unzippedDbDir, "newSeqClust", ".tsv");
		String[] commandCreateTsv = {Config.getExecutablePaths().getMMSEQS_EX().toString(), "createtsv", seqDB.toString(), seqDB.toString(), clustDB.toString(), newSeqClustTsv.toString()};
		Status status = CmdExecution.execute(commandCreateTsv, WorkDir.getWorkDir().getTmpDir(), "createtsv");
		if(status.errorOccured) return null;
		else return newSeqClustTsv;
	}
	
}














