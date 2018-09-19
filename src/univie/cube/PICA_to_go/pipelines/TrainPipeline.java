package univie.cube.PICA_to_go.pipelines;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;

import univie.cube.PICA_to_go.cmd.arguments.TrainCmdArguments;
import univie.cube.PICA_to_go.cmd.parsing.TrainCmdParse;
import univie.cube.PICA_to_go.directories.WorkDir;
import univie.cube.PICA_to_go.inputpreparation.InputPreparationTrain;
import univie.cube.PICA_to_go.miscellaneous.Serialize;
import univie.cube.PICA_to_go.out.error.ErrorHandler;
import univie.cube.PICA_to_go.out.logging.CustomLogger;
import univie.cube.PICA_to_go.out.logging.CustomLogger.LoggingWeight;
import univie.cube.PICA_to_go.pica.Annotation;
import univie.cube.PICA_to_go.pica.FeatureRanking;
import univie.cube.PICA_to_go.pica.FeatureRankingBlast;
import univie.cube.PICA_to_go.pica.FeatureRankingRefGenome;
import univie.cube.PICA_to_go.pica.Pica;
import univie.cube.PICA_to_go.pica.PicaCrossvalidate;



public class TrainPipeline extends BasePicaPipeline {

	
	public void startPipeline(String[] args) {
		
		String logMessage = "PICA-to-go train started...\n";
		CustomLogger.getInstance().log(CustomLogger.LoggingWeight.INFO, logMessage);
		
		TrainCmdArguments trainCmdArguments = TrainCmdParse.getInstance().inputArgsParsing(args);
		Path inputClusteringDir = new InputPreparationTrain(trainCmdArguments).getInputClusteringDir();
		trainCmdArguments.correctInputPhenotypes();

		
		//--------------CLUSTERING---------------------------------------------------------------------//
				
		clustering(trainCmdArguments.getClusteringProgram(), inputClusteringDir, trainCmdArguments.getOutputResults(), trainCmdArguments.getAnnotation(), trainCmdArguments.getThreads());
		

	    //-------------------FILTERING-----------------------------------------------------------//
	    
	    
	    if(trainCmdArguments.isFilterCOGs()) {
	    
		    Pair<String, String> crossValJson = filterCluster(trainCmdArguments.getInputPhenotypes(), trainCmdArguments.getFeatureName(), trainCmdArguments.getThreads());
		    try {
				Serialize.writeToFile(Paths.get(trainCmdArguments.getOutputResults().toString(), "pica-crossvalidation.json"), crossValJson.getRight());
				Serialize.writeToFile(Paths.get(trainCmdArguments.getOutputResults().toString(), "pica-crossvalidation-no-filtering.json"), crossValJson.getLeft());
		    } catch (IOException | RuntimeException e1) {
		    	(new ErrorHandler(e1, ErrorHandler.ErrorWeight.WARNING, "pica-crossvalidation.json could not be generated in result folder")).handle();
			}
	    }
	    
	    /**
	     * needed for pica-train
	     * has to be performed at this point (filtering is completed if filtering is activated; 
	     * if not activated: inputPica will be needed for crossvalidation too
	     */
	    Path inputPica = Pica.createInputPica(geneClustersPerBin, "");
	    
	    if(! trainCmdArguments.isFilterCOGs()) {
	    	CustomLogger.getInstance().log(LoggingWeight.INFO, "PICA crossvalidation started");
	    	Path tmpPicaCrossVal = null;
	    	try {
		    	tmpPicaCrossVal = Files.createTempDirectory(WorkDir.getWorkDir().getTmpDir(), "pica-crossval");
			} catch (IOException e) {
				(new ErrorHandler(e, ErrorHandler.ErrorWeight.FATAL, "tmp dir for pica-crossvalidation could not be generated")).handle();
			}
	    	PicaCrossvalidate picaCrossVal;
	    	Map<String, String> crossValResult = new HashMap<String, String>();
			try {
				picaCrossVal = new PicaCrossvalidate(inputPica, tmpPicaCrossVal, trainCmdArguments.getInputPhenotypes(), trainCmdArguments.getFeatureName());
				crossValResult = picaCrossVal.call();
			} catch (IOException e1) {
				(new ErrorHandler(e1, ErrorHandler.ErrorWeight.FATAL, "Pica crossvalidation failed")).handle();
			}
			try {
				FileUtils.deleteDirectory(tmpPicaCrossVal.toFile());
			} catch (IOException e1) {
				CustomLogger.getInstance().log(CustomLogger.LoggingWeight.WARNING, "Pica crossval directory could not be deleted");
			}
	    	String resultStr = Serialize.mapToJson(crossValResult);
	    	try {
				Serialize.writeToFile(Paths.get(trainCmdArguments.getOutputResults().toString(), "pica-crossvalidation-no-filtering.json"), resultStr);
			} catch (IOException e) {
				(new ErrorHandler(e, ErrorHandler.ErrorWeight.ERROR, "Could not write the file " + "pica-crossvalidation-no-filtering.json to output directory")).handle();
			}
	    }
	    
	    //----------------GENE-ClUSTERS-TO-FILE--------------------------------------------------//
	    CustomLogger.getInstance().log(LoggingWeight.INFO, "Gene clusters are written to file");
	    try {
			Serialize.writeGeneClustersToFile(geneClusters, Paths.get(trainCmdArguments.getOutputResults().toString() + "/" + "gene_clusters.json"));
		} catch (IOException | RuntimeException e) {
			(new ErrorHandler(e, ErrorHandler.ErrorWeight.ERROR, "Could not write gene_clusters.json to output directory")).handle();
		}
	    
	    
	    //---------------------------PICA---------------------------------------------------------------//
	    
		picaTrain(inputPica, trainCmdArguments.getOutputResults(), trainCmdArguments.getInputPhenotypes(), trainCmdArguments.getFeatureName());
		
		
		
		//----------------------FEATURE-RANKING-------------------------------------------------//
		
		try {
			FeatureRanking featureRanking;
			if(trainCmdArguments.getAnnotation() == Annotation.BLAST)
				featureRanking = new FeatureRankingBlast(modelFile, trainCmdArguments.getOutputResults(), trainCmdArguments.getFeatureName(), super.clusteringInstance, trainCmdArguments.getLimitBlast());
			else  {
				Map<String, String> fastaHeaders = super.clusteringInstance.getFastaHeaders();
				featureRanking = new FeatureRankingRefGenome(modelFile, trainCmdArguments.getOutputResults(), trainCmdArguments.getFeatureName(), fastaHeaders, trainCmdArguments.getRefGenomes(), geneClusters);
			}
			featureRanking.runFeatureRanking();
		} catch (IOException | InterruptedException e) {
			(new ErrorHandler(e, ErrorHandler.ErrorWeight.ERROR, "Feature ranking failed")).handle();
		}
		
		CustomLogger.getInstance().log(CustomLogger.LoggingWeight.INFO, "Completed! View your results in " + trainCmdArguments.getOutputResults());	}
	

}
