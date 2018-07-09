package univie.cube.PicaDesktop.inputpreparation;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import univie.cube.PicaDesktop.cmd.arguments.TrainCmdArguments;
import univie.cube.PicaDesktop.directories.WorkDir;
import univie.cube.PicaDesktop.out.error.ErrorHandler;
import univie.cube.PicaDesktop.out.logging.CustomLogger;


public class InputPreparationTrain extends InputPreparation {
	
	private TrainCmdArguments trainCmdArguments;
	private Path inputClusteringDir = null;
	
	
	public InputPreparationTrain(TrainCmdArguments trainCmdArguments) {
		CustomLogger.getInstance().log(CustomLogger.LoggingWeight.INFO, "Starting to validate the input");
		this.trainCmdArguments = trainCmdArguments;
		prepareInput();
	}
	
	public Path getInputClusteringDir() {
		return this.inputClusteringDir;
	}

	@Override
	protected void prepareInput() {
		super.createWorkDir(trainCmdArguments.getTmpDir(), trainCmdArguments.getInputBins());
		super.featureExists(trainCmdArguments.getInputPhenotypes(), trainCmdArguments.getFeature());
		Path workDirPath = WorkDir.getWorkDir().getTmpDir();
		Path inputClusteringDir = null;
		try {
			inputClusteringDir = Files.createTempDirectory(workDirPath, "input-clustering-dir");
			super.inputFastaProcessing(trainCmdArguments.getThreads(), trainCmdArguments.getInputBins(), inputClusteringDir);
		} catch (IOException e) {
			(new ErrorHandler(e, ErrorHandler.ErrorWeight.FATAL, "InputPreparationTrain failed.")).handle();
		}
		this.inputClusteringDir = inputClusteringDir;
	}
	
	
	

}
