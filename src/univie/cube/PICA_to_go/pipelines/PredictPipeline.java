package univie.cube.PICA_to_go.pipelines;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;

import univie.cube.PICA_to_go.clustering.datatypes.BinCOGs;
import univie.cube.PICA_to_go.clustering.datatypes.COG;
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
		
		Pair<Map<String, COG>, Map<String, BinCOGs>> orthogroups_orthogroupsPerBin = null;
		try {
			MMSeqsClusterupdate mmseqsClusterupdate = new MMSeqsClusterupdate(predictBinsDir, WorkDir.getWorkDir().getTmpDir());
			orthogroups_orthogroupsPerBin = mmseqsClusterupdate.clusterUpdate(predictCmdArguments.getThreads());
		} catch (IOException | InterruptedException | RuntimeException e) {
			(new ErrorHandler(e, ErrorHandler.ErrorWeight.FATAL, "MMSEQS Clusterupdate failed")).handle();
		}
		
		Map<String, COG> orthogroups = orthogroups_orthogroupsPerBin.getLeft();
		Map<String, BinCOGs> orthogroupsPerBin = orthogroups_orthogroupsPerBin.getRight();
		System.out.println("orthogroups size " + orthogroups.size()); //TODO: delete
		System.out.println("orthogroupsPerBin size " + orthogroupsPerBin.size()); //TODO: delete
		try {
			Serialize.writeGeneClustersToFile(orthogroups, Paths.get(predictCmdArguments.getOutputResults().toString(), "orthogroups.json"));
		} catch (IOException e) {
			(new ErrorHandler(e, ErrorHandler.ErrorWeight.FATAL, "Orthogroups could not be written to file")).handle();
		}
		Path picaInput = Paths.get(WorkDir.getWorkDir().getTmpDir().toString(), "inputPica");
		try {
			MMseqsClustering.writePicaInputFile(orthogroupsPerBin, picaInput);
		} catch (FileNotFoundException e) {
			(new ErrorHandler(e, ErrorHandler.ErrorWeight.FATAL, "inputFile for PICA could not be generated")).handle();
		}
		
		//-----PICA-PREDICT--------------------------------------//
		
		try {
			PicaPredict picaTest = new PicaPredict(picaInput, predictCmdArguments.getOutputResults(), predictCmdArguments.getFeatureName(), modelFile);
			picaTest.call();
		} catch (Exception e) {
			(new ErrorHandler(e, ErrorHandler.ErrorWeight.FATAL, "inputFile for PICA could not be generated")).handle();
		}
		
	}
}
