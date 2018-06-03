package univie.cube.PicaDesktop.pipelines;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;

import univie.cube.PicaDesktop.clustering.datatypes.BinCOGs;
import univie.cube.PicaDesktop.clustering.datatypes.COG;
import univie.cube.PicaDesktop.clustering.filtering.LinearFiltering;
import univie.cube.PicaDesktop.clustering.filtering.ClusterFiltering.CrossValPerCutOff;
import univie.cube.PicaDesktop.clustering.methods.Clustering;
import univie.cube.PicaDesktop.clustering.methods.LinclustClustering;
import univie.cube.PicaDesktop.clustering.methods.MMseqsClustering;
import univie.cube.PicaDesktop.directories.WorkDir;
import univie.cube.PicaDesktop.fastaformat.FastaValidate;
import univie.cube.PicaDesktop.miscellaneous.ClusteringProgram;
import univie.cube.PicaDesktop.pica.PicaTrain;
import univie.cube.PicaDesktop.pipelines.TrainPipeline.Annotation;
import univie.cube.PicaDesktop.prodigal.Prodigal;

public abstract class Pipeline {
	
	public abstract void startPipeline(String[] args);
	
	//orthogroups / orthogroupsPerBin
	protected Map<String, COG> orthogroups = null;
	protected Map<String, BinCOGs> orthogroupsPerBin = null;
	protected Clustering clusteringInstance = null;
	
	//protected Map<String, String> representativeSeq;
	
	protected Path modelFile = null;

	/**
	 * 
	 * @param files
	 * @param directories
	 * @return
	 */
	protected boolean filesExist(Path[] files, Path[] directories) {
		for(Path file : files) if(! Files.exists(file) || Files.isDirectory(file)) return false;
		for(Path directory : directories) if(! Files.exists(directory) || ! Files.isDirectory(directory)) return false;
		return true;
	}
	
	/**
	 * 
	 * @param file
	 * @param nucleotideSeq
	 * @return
	 */
	protected boolean checkIfFasta(Path file, boolean nucleotideSeq) {
		//TODO:implement
		return true;
	}
	
	//TODO: maybe validate phenotype file and model / DB file?
	
	/**
	 * 
	 * @param inputPhenotypes
	 * @param feature
	 * @return
	 * @throws IOException
	 */
	protected boolean featureExists(Path inputPhenotypes, String feature) throws IOException {
		List<String> inputPhenotypesReadFile = Files.readAllLines(inputPhenotypes);
		String[] allFeatures = inputPhenotypesReadFile.get(0).split("\t");
		long occurances = Arrays.stream(allFeatures).filter(obj -> obj.equals(feature)).count();
		return (occurances == 1);
	}
	
	/**
	 * 
	 * @param threads
	 * @param inputBins
	 * @param inputClusteringDir
	 * @param outputResults
	 * @param debugMode
	 * @param workDir
	 */
	private void findGenes(int threads, List<Path> inputBinsNucleotide, Path inputClusteringDir, Path outputResults, boolean debugMode, WorkDir workDir) {
		try {
			Prodigal.runProdigal(threads, inputBinsNucleotide, inputClusteringDir, outputResults);
		} catch (InterruptedException e1) {
			System.err.println("ERROR: Process Prodigal failed");
			if(!debugMode) {
				boolean workDirRemoved = workDir.removeWorkDir();
				if(! workDirRemoved) System.out.println("WARNING: tmp directory could not be deleted");
			}
			System.exit(10);
		}
	}
	
	/**
	 * 
	 * @param threads
	 * @param inputBins
	 * @param inputClusteringDir
	 * @param outputResults
	 * @param debugMode
	 * @param workDir
	 * @throws IOException 
	 */
	protected void inputFastaProcessing(int threads, Path inputBins, Path inputClusteringDir, Path outputResults, boolean debugMode, WorkDir workDir) throws IOException {
		List<Path> allFiles = Files.walk(inputBins).filter(Files::isRegularFile).collect(Collectors.toList());
		List<Path> genomes = new ArrayList<Path>();
		List<Path> proteomes = new ArrayList<Path>();
		List<Path> invalidProteomes = new ArrayList<Path>();
		
		for(Path path : allFiles) {
			FastaValidate fastaValidate = new FastaValidate(path);
			if(!fastaValidate.isHeaderUnique()) System.err.println("WARNING: The fasta header of " + path.getFileName().toString() + " is not unique. This file will not be processed");
			else if (fastaValidate.getSequenceType() == FastaValidate.SequenceType.DNA) genomes.add(path);
			else if (fastaValidate.getSequenceType() == FastaValidate.SequenceType.PROTEIN) proteomes.add(path);
			else {
				System.err.println("WARNING: " + path.getFileName().toString() + " is not a valid fasta file. All invalid characters are converted to 'X'.");
				invalidProteomes.add(path);
			}
		}
		
		findGenes(threads, genomes, inputClusteringDir, outputResults, debugMode, workDir);
		
		for(Path path : proteomes) {
			Files.copy(path, Paths.get(inputClusteringDir.toString(), path.getFileName().toString()));
		}
		
		for(Path path : invalidProteomes) {
			List<String> correctedLines = FastaValidate.removeInvalidChars(Files.readAllLines(path));
			Files.write(Paths.get(inputClusteringDir.toString(), path.getFileName().toString()), correctedLines);
		}
	}
	
	/**
	 * 
	 * @param clusteringProgram
	 * @param inputClusteringDir
	 * @param orthofinderExecutable
	 * @param searchProgram
	 * @param outputResults
	 * @param debugMode
	 * @param workDir
	 * @param threads
	 * @return Pair<orthogroups, orthogroupsPerBin>; orthogroups -> Map<String, COG>, orthogroupsPerBin -> Map<String, BinCOGs>
	 */
	protected void clustering(ClusteringProgram clusteringProgram, Path inputClusteringDir, Path outputResults, Annotation annotation, boolean debugMode, WorkDir workDir, int threads) {

		try {
			if(clusteringProgram.equals(ClusteringProgram.MMSEQS_CLUSTER)) {
				clusteringInstance = new MMseqsClustering(inputClusteringDir, outputResults, outputResults);
			}
			else {
				clusteringInstance = new LinclustClustering(inputClusteringDir, outputResults);
			}
			
			clusteringInstance.runClustering(threads);
			clusteringInstance.createZippedDBFile();
			orthogroups = clusteringInstance.getOrthogroups();
			orthogroupsPerBin = clusteringInstance.getOrthogroupsPerBin();

		} catch (IOException | RuntimeException | InterruptedException e1) {
			System.err.println("ERROR: Clustering process(" + clusteringProgram.toString().toLowerCase() + ")failed");
			e1.printStackTrace();
			if(!debugMode) {
				boolean workDirRemoved = workDir.removeWorkDir();
				if(! workDirRemoved) System.out.println("WARNING: tmp directory could not be deleted"); 
			}
			java.sql.Timestamp timestampExceptionOrtho = new java.sql.Timestamp(System.currentTimeMillis());
			System.out.println("timestamp: " + timestampExceptionOrtho);
			System.exit(20);
		}
	}
	

	/**
	 * 
	 * @param orthogroups
	 * @param orthogroupsPerBin
	 * @param workDir
	 * @param inputPhenotypes
	 * @param picaCrossvalidateExecutable
	 * @param feature
	 * @param threads
	 * @param debugMode
	 * 
	 * @return json string with crossvalidation
	 */
	protected Pair<String, String> filterCluster(Map<String, COG> orthogroups, Map<String, BinCOGs> orthogroupsPerBin, WorkDir workDir, Path inputPhenotypes, String feature, int threads, boolean debugMode, Path loggingDir) {
		LinearFiltering clusterFiltering = new LinearFiltering(workDir, loggingDir);
		try {
			Pair<CrossValPerCutOff, CrossValPerCutOff> crossVal = clusterFiltering.filter(orthogroups, orthogroupsPerBin, inputPhenotypes, feature, threads);
			return Pair.of(crossVal.getLeft().toString(), crossVal.getRight().toString());
		} catch (IOException | InterruptedException | ExecutionException | RuntimeException e) {
			System.err.println("ERROR: Process filterCluster failed");
			e.printStackTrace();
			if(!debugMode) {
				boolean workDirRemoved = workDir.removeWorkDir();
				if(! workDirRemoved) System.out.println("WARNING: tmp directory could not be deleted"); 
			}
			java.sql.Timestamp timestampExceptionPica = new java.sql.Timestamp(System.currentTimeMillis());
			System.out.println("timestamp: " + timestampExceptionPica);
			System.exit(40);
		}
		return null; //cannot be reached as System exists before if try causes Exception
	}
	
	/**
	 * 
	 * @param inputPicaDir
	 * @param picaCrossvalidateExecutable
	 * @param outputResults
	 * @param inputPhenotypes
	 * @param feature
	 */
	protected void picaTrain(Path inputPicaFile, Path outputResults, Path inputPhenotypes, String feature, WorkDir workDir) {
		try {
			PicaTrain pica = new PicaTrain(inputPicaFile, outputResults, inputPhenotypes, feature, workDir);
			pica.call();
			modelFile = pica.getModelFile();
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("FATAL ERROR in PICA crossvalidation Thread");
			System.exit(35);
		}
	}
	
	protected void clusterUpdate() {
		
	}
	
	protected int parseThreads(String threadsStr) {
		int threads;
		if(threadsStr == null || ! threadsStr.matches("\\d+")) {
			threads = Runtime.getRuntime().availableProcessors();
			System.out.println("INFO: No/invalid option for thread number specified, all available cores (" + threads + ") used");
		}
		else {
			threads = Integer.parseInt(threadsStr);
			System.out.println("INFO: " + threads + " threads will be used (user specified)");
		}
		
		return threads;
	}
	
}
