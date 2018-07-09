package univie.cube.PicaDesktop.clustering.methods;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;

import univie.cube.PicaDesktop.clustering.datatypes.BinCOGs;
import univie.cube.PicaDesktop.clustering.datatypes.COG;
import univie.cube.PicaDesktop.directories.WorkDir;
import univie.cube.PicaDesktop.global.ExecutablePaths;
import univie.cube.PicaDesktop.miscellaneous.CmdExecution;
import univie.cube.PicaDesktop.miscellaneous.CmdExecution.Status;
import univie.cube.PicaDesktop.out.error.ErrorHandler;


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
	 * @return Pair of orthogroups (left; Map<String, COG>) and orthogroupsPerBin (right; Map<String, BinCOGs>)
	 * @throws InterruptedException
	 * @throws IOException
	 * @throws RuntimeException
	 */
	public Pair<Map<String, COG>, Map<String, BinCOGs>> clusterUpdate(int threads) throws InterruptedException, IOException, RuntimeException {		
		Files.walk(predictBinsDir).filter(path -> Files.isRegularFile(path)).forEach(file -> {
			String fileName = file.getFileName().toString();
			binNames.add(fileName.substring(0, fileName.lastIndexOf(".")));
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
		Pair<Map<String, COG>, Map<String, BinCOGs>> orthogroups_orthogroupsPerBin = Pair.of(orthogroups, orthogroupsPerBin);
		return orthogroups_orthogroupsPerBin;
	}
	
	private Path createInputSeqDB(Path inputForSeqDB) throws IOException, InterruptedException {
		Path seqDBNewFiles = Files.createTempFile(unzippedDbDir, "inputSeqDB_newFiles", ".mmseqs");
		String[] commandCreateSeqDB = {ExecutablePaths.getExecutablePaths().MMSEQS_EX.toString(), "createdb", inputForSeqDB.toString(), seqDBNewFiles.toString()};
		Status statusCreateDB = CmdExecution.execute(commandCreateSeqDB, WorkDir.getWorkDir().getTmpDir(), "mmseqs-createdb", unzippedDbDir.toFile());
		if(statusCreateDB.errorOccured) throw new RuntimeException("createdb failed");
		
		Path concatSeqDB = concatDBs(seqDBNewFiles, Paths.get(unzippedDbDir.toString(), inputDbName), unzippedDbDir, WorkDir.getWorkDir().getTmpDir());
		return concatSeqDB; 
	}
	
	
	
	private Path clusterupdateCommand(Path concatSeqDB, int threads) throws IOException, InterruptedException {
		String mappedSeq = unzippedDbDir.toString() + File.separator + "mappedSeq" + ".mmseqs";
		String newClust = unzippedDbDir.toString() + File.separator + "newClust" + ".mmseqs";
		String[] commandUpdateSeqDB = {ExecutablePaths.getExecutablePaths().MMSEQS_EX.toString(), "clusterupdate", unzippedDbDir.toString() + File.separator + inputDbName, concatSeqDB.toString(), unzippedDbDir.toString() + File.separator + clustDbName, mappedSeq.toString(), newClust.toString(), "tmp"};
		
		Status status = CmdExecution.execute(commandUpdateSeqDB, WorkDir.getWorkDir().getTmpDir(), "clusterupdate", Files.createTempDirectory(unzippedDbDir, "mmseqs_clusterupdate_tmp").toFile());
		if(! status.errorOccured) return createTsv(mappedSeq, newClust);
		else return null;
	}
	
	private Path createTsv(String seqDB, String clustDB) throws IOException, InterruptedException {
		Path newSeqClustTsv = Files.createTempFile(unzippedDbDir, "newSeqClust", ".tsv");
		String[] commandCreateTsv = {ExecutablePaths.getExecutablePaths().MMSEQS_EX.toString(), "createtsv", seqDB.toString(), seqDB.toString(), clustDB.toString(), newSeqClustTsv.toString()};
		Status status = CmdExecution.execute(commandCreateTsv, WorkDir.getWorkDir().getTmpDir(), "createtsv");
		if(status.errorOccured) return null;
		else return newSeqClustTsv;
	}
	
}














