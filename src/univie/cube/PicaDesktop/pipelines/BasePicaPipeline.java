package univie.cube.PicaDesktop.pipelines;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.apache.commons.lang3.tuple.Pair;

import univie.cube.PicaDesktop.clustering.datatypes.BinCOGs;
import univie.cube.PicaDesktop.clustering.datatypes.COG;
import univie.cube.PicaDesktop.clustering.filtering.LinearFiltering;
import univie.cube.PicaDesktop.clustering.filtering.ClusterFiltering.CrossValPerCutOff;
import univie.cube.PicaDesktop.clustering.methods.Clustering;
import univie.cube.PicaDesktop.clustering.methods.LinclustClustering;
import univie.cube.PicaDesktop.clustering.methods.MMseqsClustering;
import univie.cube.PicaDesktop.miscellaneous.ClusteringProgram;
import univie.cube.PicaDesktop.out.error.ErrorHandler;
import univie.cube.PicaDesktop.out.logging.CustomLogger;
import univie.cube.PicaDesktop.pica.Annotation;
import univie.cube.PicaDesktop.pica.PicaTrain;

public abstract class BasePicaPipeline implements Pipeline {
	
	@Override
	public abstract void startPipeline(String[] args);
	
	//orthogroups / orthogroupsPerBin
	protected Map<String, COG> orthogroups = null;
	protected Map<String, BinCOGs> orthogroupsPerBin = null;
	protected Clustering clusteringInstance = null;
	
	//protected Map<String, String> representativeSeq;
	
	protected Path modelFile = null;

	
	/**
	 * 
	 * @param clusteringProgram
	 * @param inputClusteringDir
	 * @param outputResults
	 * @param annotation
	 * @param debugMode
	 * @param workDir
	 * @param threads
	 */
	protected void clustering(ClusteringProgram clusteringProgram, Path inputClusteringDir, Path outputResults, Annotation annotation, int threads) {
		CustomLogger.getInstance().log(CustomLogger.LoggingWeight.INFO, "Start to cluster genes using MMSeqs2");
		try {
			if(clusteringProgram.equals(ClusteringProgram.MMSEQS_CLUSTER)) {
				clusteringInstance = new MMseqsClustering(inputClusteringDir, outputResults);
			}
			else {
				clusteringInstance = new LinclustClustering(inputClusteringDir, outputResults);
			}
			
			clusteringInstance.runClustering(threads);
			clusteringInstance.createZippedDBFile();
			orthogroups = clusteringInstance.getOrthogroups();
			orthogroupsPerBin = clusteringInstance.getOrthogroupsPerBin();

		} catch (IOException | RuntimeException | InterruptedException e1) {
			String errorMessage = "Clustering process(" + clusteringProgram.toString().toLowerCase() + ")failed";
			(new ErrorHandler(e1, ErrorHandler.ErrorWeight.FATAL, errorMessage)).handle();
		}
	}
	

	/**
	 * 
	 * @param inputPhenotypes
	 * @param feature
	 * @param threads
	 * @param debugMode
	 * @return json string with crossval
	 */
	protected Pair<String, String> filterCluster(Path inputPhenotypes, String feature, int threads) {
		CustomLogger.getInstance().log(CustomLogger.LoggingWeight.INFO, "Filtering of clusters started");
		LinearFiltering clusterFiltering = new LinearFiltering();
		try {
			Pair<CrossValPerCutOff, CrossValPerCutOff> crossVal = clusterFiltering.filter(orthogroups, orthogroupsPerBin, inputPhenotypes, feature, threads);
			return Pair.of(crossVal.getLeft().toString(), crossVal.getRight().toString());
		} catch (IOException | InterruptedException | ExecutionException | RuntimeException e) {
			(new ErrorHandler(e, ErrorHandler.ErrorWeight.FATAL, "Process filterCluster failed")).handle();;
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
	protected void picaTrain(Path inputPicaFile, Path outputResults, Path inputPhenotypes, String feature) {
		CustomLogger.getInstance().log(CustomLogger.LoggingWeight.INFO, "Training of PICA models started");
		try {
			PicaTrain pica = new PicaTrain(inputPicaFile, outputResults, inputPhenotypes, feature);
			pica.call();
			modelFile = pica.getModelFile();
		} catch (Exception e) {
			(new ErrorHandler(e, ErrorHandler.ErrorWeight.FATAL, "in PICA train")).handle();
		}
	}
	
	protected int parseThreads(String threadsStr) {
		int threads;
		if(threadsStr == null || ! threadsStr.matches("\\d+")) {
			threads = Runtime.getRuntime().availableProcessors();
			String logMessage = "INFO: No/invalid option for thread number specified, all available cores (" + threads + ") used";
			CustomLogger.getInstance().log(CustomLogger.LoggingWeight.INFO, logMessage);
		}
		else {
			threads = Integer.parseInt(threadsStr);
			String logMessage = "INFO: " + threads + " threads will be used (user specified)";
			CustomLogger.getInstance().log(CustomLogger.LoggingWeight.INFO, logMessage);
		}
		
		return threads;
	}
	
}
