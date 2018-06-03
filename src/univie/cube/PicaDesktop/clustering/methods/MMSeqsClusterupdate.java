package univie.cube.PicaDesktop.clustering.methods;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;

import univie.cube.PicaDesktop.clustering.datatypes.BinCOGs;
import univie.cube.PicaDesktop.clustering.datatypes.COG;
import univie.cube.PicaDesktop.fastaformat.FastaValidate;
import univie.cube.PicaDesktop.global.ExecutablePaths;
import univie.cube.PicaDesktop.miscellaneous.CmdExecution;
import univie.cube.PicaDesktop.miscellaneous.CmdExecution.Status;
import univie.cube.PicaDesktop.prodigal.Prodigal;

public class MMSeqsClusterupdate extends MMSeqs {

	private static final String clustDbName = "DB_clustering.mmseqs";
	private static final String inputDbName = "DB_input.mmseqs";
	
	private Path testGenomes;
	private Path clusteringDir;
	private Path clusteringDirGenes;
	private Path outputLog;
	private List<String> binNames = new ArrayList<String>();
	
	/**
	 * 
	 * @param testGenomes
	 * @param genes
	 * @param clusteringDir
	 * @param outputLog
	 * @throws IOException
	 */
	public MMSeqsClusterupdate(Path testGenomes, Path clusteringDir, Path outputLog) throws IOException {
		this.testGenomes = testGenomes;
		this.outputLog = outputLog;
		this.clusteringDir = clusteringDir;
		this.clusteringDirGenes = Files.createDirectories(Paths.get(clusteringDir.toString(), "genes"));
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
		List<Path> allGenomes = Files.walk(testGenomes)
												.filter(path -> path.toFile().isFile())
												.collect(Collectors.toList());
		
		List<Path> genomes = new ArrayList<Path>();
		List<Path> proteomes = new ArrayList<Path>();
		List<Path> invalidProteomes = new ArrayList<Path>();
		
		for(Path path : allGenomes) {
			FastaValidate fastaValidate = new FastaValidate(path);
			if (! fastaValidate.isHeaderUnique()) System.err.println("WARNING: Fasta Header of file " + path.getFileName().toString() + " is not unique. File will be ignored.");
			else if (fastaValidate.getSequenceType() == FastaValidate.SequenceType.DNA) genomes.add(path);
			else if (fastaValidate.getSequenceType() == FastaValidate.SequenceType.PROTEIN) proteomes.add(path);
			else {
				System.err.println("WARNING: Invalid fasta format for file " + path.getFileName().toString() + ". Forbidden characters will be replaced by 'X'.");
				invalidProteomes.add(path);
			}
		}
		
		Prodigal.runProdigal(threads, genomes, clusteringDirGenes, outputLog);
		
		for(Path path : proteomes) Files.copy(path, Paths.get(clusteringDirGenes.toString(), path.getFileName().toString()));
		
		for(Path path : invalidProteomes) {
			List<String> correctedLines = FastaValidate.removeInvalidChars(Files.readAllLines(path));
			Files.write(Paths.get(clusteringDirGenes.toString(), path.getFileName().toString()), correctedLines);
		}
		
		Files.walk(clusteringDirGenes).filter(path -> Files.isRegularFile(path)).forEach(file -> {
			String fileName = file.getFileName().toString();
			binNames.add(fileName.substring(0, fileName.lastIndexOf(".")));
		});
		Path inputForSeqDB = concatInputFiles(clusteringDirGenes);
		Path concatSeqDB = createInputSeqDB(inputForSeqDB);

		Path tsvFile = clusterupdateCommand(concatSeqDB, threads);
		if(tsvFile == null) throw new RuntimeException("mmseqs clusterupdate failed");
		parseClustOutput(tsvFile, binNames);
		Pair<Map<String, COG>, Map<String, BinCOGs>> orthogroups_orthogroupsPerBin = Pair.of(orthogroups, orthogroupsPerBin);
		return orthogroups_orthogroupsPerBin;
	}
	
	private Path createInputSeqDB(Path inputForSeqDB) throws IOException, InterruptedException {
		Path seqDBNewFiles = Files.createTempFile(clusteringDir, "inputSeqDB_newFiles", ".mmseqs");
		String[] commandCreateSeqDB = {ExecutablePaths.getExecutablePaths().MMSEQS_EX.toString(), "createdb", inputForSeqDB.toString(), seqDBNewFiles.toString()};
		Status statusCreateDB = CmdExecution.execute(commandCreateSeqDB, outputLog, "mmseqs-createdb", clusteringDir.toFile());
		if(statusCreateDB.errorOccured) throw new RuntimeException("createdb failed");
		
		Path concatSeqDB = concatDBs(seqDBNewFiles, Paths.get(clusteringDir.toString(), inputDbName), clusteringDir, outputLog);
		return concatSeqDB; 
	}
	
	private Path clusterupdateCommand(Path concatSeqDB, int threads) throws IOException, InterruptedException {
		String mappedSeq = clusteringDir.toString() + File.separator + "mappedSeq" + ".mmseqs";
		String newClust = clusteringDir.toString() + File.separator + "newClust" + ".mmseqs";
		String[] commandUpdateSeqDB = {ExecutablePaths.getExecutablePaths().MMSEQS_EX.toString(), "clusterupdate", clusteringDir.toString() + File.separator + inputDbName, concatSeqDB.toString(), clusteringDir.toString() + File.separator + clustDbName, mappedSeq.toString(), newClust.toString(), "tmp"};
		
		Status status = CmdExecution.execute(commandUpdateSeqDB, outputLog, "clusterupdate", Files.createTempDirectory(clusteringDir, "mmseqs_clusterupdate_tmp").toFile());
		if(! status.errorOccured) return createTsv(mappedSeq, newClust);
		else return null;
	}
	
	private Path createTsv(String seqDB, String clustDB) throws IOException, InterruptedException {
		Path newSeqClustTsv = Files.createTempFile(clusteringDir, "newSeqClust", ".tsv");
		String[] commandCreateTsv = {ExecutablePaths.getExecutablePaths().MMSEQS_EX.toString(), "createtsv", seqDB.toString(), seqDB.toString(), clustDB.toString(), newSeqClustTsv.toString()};
		Status status = CmdExecution.execute(commandCreateTsv, outputLog, "createtsv");
		if(status.errorOccured) return null;
		else return newSeqClustTsv;
	}
	
}














