package univie.cube.PICA_to_go.pipelines;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.apache.commons.lang3.tuple.Pair;
import org.mapdb.HTreeMap;

import univie.cube.PICA_to_go.clustering.datatypes.GeneClust4Bin;
import univie.cube.PICA_to_go.clustering.datatypes.GeneCluster;
import univie.cube.PICA_to_go.clustering.filtering.LinearFiltering;
import univie.cube.PICA_to_go.clustering.filtering.ClusterFiltering.CrossValPerCutOff;
import univie.cube.PICA_to_go.clustering.methods.Clustering;
import univie.cube.PICA_to_go.clustering.methods.LinclustClustering;
import univie.cube.PICA_to_go.clustering.methods.MMseqsClustering;
import univie.cube.PICA_to_go.miscellaneous.ClusteringProgram;
import univie.cube.PICA_to_go.out.error.ErrorHandler;
import univie.cube.PICA_to_go.out.logging.CustomLogger;
import univie.cube.PICA_to_go.pica.Annotation;
import univie.cube.PICA_to_go.pica.PicaTrain;

public abstract class BasePicaPipeline implements Pipeline {
	
	@Override
	public abstract void startPipeline(String[] args);
	
	//geneClusters / geneClustersPerBin
	protected HTreeMap<String, GeneCluster> geneClusters = null;
	protected Map<String, GeneClust4Bin> geneClustersPerBin = null;
	protected Clustering clusteringInstance = null;
	
	
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
		try {
			if(clusteringProgram.equals(ClusteringProgram.MMSEQS_CLUSTER)) {
				clusteringInstance = new MMseqsClustering(inputClusteringDir, outputResults);
			}
			else {
				clusteringInstance = new LinclustClustering(inputClusteringDir, outputResults);
			}
			
			clusteringInstance.runClustering(threads);
			clusteringInstance.createZippedDBFile();
			geneClusters = clusteringInstance.getgeneClusters();
			geneClustersPerBin = clusteringInstance.getgeneClustersPerBin();

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
		CustomLogger.getInstance().log(CustomLogger.LoggingWeight.INFO, "Filtering of clusters and crossvalidation started");
		LinearFiltering clusterFiltering = new LinearFiltering();
		try {
			Pair<CrossValPerCutOff, CrossValPerCutOff> crossVal = clusterFiltering.filter(geneClusters, geneClustersPerBin, inputPhenotypes, feature, threads);
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
	
}
