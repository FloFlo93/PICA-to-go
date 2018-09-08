package univie.cube.PICA_to_go.inputpreparation;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import univie.cube.PICA_to_go.cmd.arguments.TrainCmdArguments;
import univie.cube.PICA_to_go.out.error.ErrorHandler;


public class InputPreparationTrain extends InputPreparation {
	
	private TrainCmdArguments trainCmdArguments;
	
	
	public InputPreparationTrain(TrainCmdArguments trainCmdArguments) {
		super(trainCmdArguments);
		this.trainCmdArguments = trainCmdArguments;
		prepareInput();
	}
	
	@Override
	public Path getInputClusteringDir() {
		return super.inputClusteringDir;
	}


	@Override
	protected void featureExistsHook() {
		Path inputPhenotypes = this.trainCmdArguments.getInputPhenotypes();
		String feature = this.trainCmdArguments.getFeatureName();
		List<String> inputPhenotypesReadFile = null;
		try {
			inputPhenotypesReadFile = Files.readAllLines(inputPhenotypes);
		} catch (IOException e) {
			(new ErrorHandler(e, ErrorHandler.ErrorWeight.FATAL, "Feature specified does not exist in feature file")).handle();
		}
		String[] allFeatures = inputPhenotypesReadFile.get(0).split("\t");
		long occurances = Arrays.stream(allFeatures).filter(obj -> obj.equals(feature)).count();
		if(occurances != 1) {
			(new ErrorHandler(new RuntimeException("feature not in feature file"), ErrorHandler.ErrorWeight.FATAL, "The feature specified is not present in the phenotype file")).handle();
		}
	}

	@Override
	protected void processModelAndDBHook() {
		// not available for train mode
	}
	

}
