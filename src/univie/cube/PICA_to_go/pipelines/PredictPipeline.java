package univie.cube.PICA_to_go.pipelines;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;

import univie.cube.PICA_to_go.clustering.datatypes.GeneClust4Bin;
import univie.cube.PICA_to_go.clustering.datatypes.GeneCluster;
import univie.cube.PICA_to_go.clustering.methods.MMSeqsClusterupdate;
import univie.cube.PICA_to_go.clustering.methods.MMseqsClustering;
import univie.cube.PICA_to_go.cmd.arguments.PredictCmdArguments;
import univie.cube.PICA_to_go.cmd.parsing.PredictCmdParse;
import univie.cube.PICA_to_go.directories.WorkDir;
import univie.cube.PICA_to_go.inputpreparation.InputPreparationPredict;
import univie.cube.PICA_to_go.miscellaneous.Serialize;
import univie.cube.PICA_to_go.out.error.ErrorHandler;
import univie.cube.PICA_to_go.pica.PicaPredict;

public class PredictPipeline extends BasePicaPipeline {
	
	
	@Override
	public void startPipeline(String[] args) {
		
		//-----------INPUT-PARSING--------------//
		
		PredictCmdArguments predictCmdArguments = PredictCmdParse.getInstance().inputArgsParsing(args);
		InputPreparationPredict inputPreparationPredict = new InputPreparationPredict(predictCmdArguments);
		Path predictBinsDir = inputPreparationPredict.getInputClusteringDir();
		Path modelFile = inputPreparationPredict.getModelFile();


		
		//-------CLUSTERUPDATE------------------------------------------------//
		
		Pair<Map<String, GeneCluster>, Map<String, GeneClust4Bin>> geneClusters_geneClustersPerBin = null;
		try {
			MMSeqsClusterupdate mmseqsClusterupdate = new MMSeqsClusterupdate(predictBinsDir, WorkDir.getWorkDir().getTmpDir());
			geneClusters_geneClustersPerBin = mmseqsClusterupdate.clusterUpdate(predictCmdArguments.getThreads());
		} catch (IOException | InterruptedException | RuntimeException e) {
			(new ErrorHandler(e, ErrorHandler.ErrorWeight.FATAL, "MMSEQS Clusterupdate failed")).handle();
		}
		
		Map<String, GeneCluster> geneClusters = geneClusters_geneClustersPerBin.getLeft();
		Map<String, GeneClust4Bin> geneClustersPerBin = geneClusters_geneClustersPerBin.getRight();
		try {
			Serialize.writeGeneClustersToFile(geneClusters, Paths.get(predictCmdArguments.getOutputResults().toString(), "geneClusters.json"));
		} catch (IOException e) {
			(new ErrorHandler(e, ErrorHandler.ErrorWeight.FATAL, "geneClusters could not be written to file")).handle();
		}
		Path picaInput = Paths.get(WorkDir.getWorkDir().getTmpDir().toString(), "inputPica");
		try {
			MMseqsClustering.writePicaInputFile(geneClustersPerBin, picaInput);
		} catch (FileNotFoundException e) {
			(new ErrorHandler(e, ErrorHandler.ErrorWeight.FATAL, "inputFile for PICA could not be generated")).handle();
		}
		
		//--------------GC-before-PICA-predict-is-started---------------------------------------------------//
		
		System.gc();
		
		
		//-----PICA-PREDICT--------------------------------------//
		
		try {
			PicaPredict picaTest = new PicaPredict(picaInput, predictCmdArguments.getOutputResults(), predictCmdArguments.getFeatureName(), modelFile);
			picaTest.call();
		} catch (Exception e) {
			(new ErrorHandler(e, ErrorHandler.ErrorWeight.FATAL, "inputFile for PICA could not be generated")).handle();
		}
		
	}
}
