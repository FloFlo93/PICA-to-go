package univie.cube.PicaDesktop.pipelines;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;

import univie.cube.PicaDesktop.cmd.arguments.TrainCmdArguments;
import univie.cube.PicaDesktop.cmd.parsing.TrainCmdParse;
import univie.cube.PicaDesktop.directories.WorkDir;
import univie.cube.PicaDesktop.inputpreparation.InputPreparationTrain;
import univie.cube.PicaDesktop.miscellaneous.Serialize;
import univie.cube.PicaDesktop.out.error.ErrorHandler;
import univie.cube.PicaDesktop.out.logging.CustomLogger;
import univie.cube.PicaDesktop.pica.Annotation;
import univie.cube.PicaDesktop.pica.FeatureRanking;
import univie.cube.PicaDesktop.pica.FeatureRankingBlast;
import univie.cube.PicaDesktop.pica.FeatureRankingRefGenome;
import univie.cube.PicaDesktop.pica.Pica;
import univie.cube.PicaDesktop.pica.PicaCrossvalidate;

//TODO: extend debug option (log files only in debug mode, useful ?)



public class TrainPipeline extends Pipeline {

	
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
	    
		    Pair<String, String> crossValJson = filterCluster(trainCmdArguments.getInputPhenotypes(), trainCmdArguments.getFeature(), trainCmdArguments.getThreads());
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
	    Path inputPica = Pica.createInputPica(orthogroupsPerBin, "");
	    
	    if(! trainCmdArguments.isFilterCOGs()) {
	    	Path tmpPicaCrossVal = null;
	    	try {
		    	tmpPicaCrossVal = Files.createTempDirectory(WorkDir.getWorkDir().getTmpDir(), "pica-crossval");
			} catch (IOException e) {
				(new ErrorHandler(e, ErrorHandler.ErrorWeight.FATAL, "tmp dir for pica-crossvalidation could not be generated")).handle();
			}
	    	PicaCrossvalidate picaCrossVal;
	    	Map<String, String> crossValResult = new HashMap<String, String>();
			try {
				picaCrossVal = new PicaCrossvalidate(inputPica, tmpPicaCrossVal, trainCmdArguments.getInputPhenotypes(), trainCmdArguments.getFeature());
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
	    
	    //----------------ORTHOGROUPS-TO-FILE--------------------------------------------------//
	    
	    try {
			Serialize.writeOrthogroupsToFile(orthogroups, Paths.get(trainCmdArguments.getOutputResults().toString() + "/" + "orthogroups.json"));
		} catch (IOException | RuntimeException e) {
			(new ErrorHandler(e, ErrorHandler.ErrorWeight.ERROR, "Could not write orthogroups.json to output directory")).handle();
		}
	    
	    
	    //---------------------------PICA---------------------------------------------------------------//
	    
		picaTrain(inputPica, trainCmdArguments.getOutputResults(), trainCmdArguments.getInputPhenotypes(), trainCmdArguments.getFeature());
		
		
		
		//----------------------FEATURE-RANKING-------------------------------------------------//
		
		try {
			FeatureRanking featureRanking;
			if(trainCmdArguments.getAnnotation() == Annotation.BLAST)
				featureRanking = new FeatureRankingBlast(modelFile, trainCmdArguments.getOutputResults(), trainCmdArguments.getFeature(), super.clusteringInstance, trainCmdArguments.getLimitBlast());
			else  {
				Map<String, String> fastaHeaders = super.clusteringInstance.getFastaHeaders();
				featureRanking = new FeatureRankingRefGenome(modelFile, trainCmdArguments.getOutputResults(), trainCmdArguments.getFeature(), fastaHeaders, trainCmdArguments.getRefGenomes(), orthogroups);
			}
			featureRanking.runFeatureRanking();
		} catch (IOException | InterruptedException e) {
			(new ErrorHandler(e, ErrorHandler.ErrorWeight.ERROR, "Feature ranking failed")).handle();
		}
		
		CustomLogger.getInstance().log(CustomLogger.LoggingWeight.INFO, "Completed! View your results in " + trainCmdArguments.getOutputResults());	}
	

}
