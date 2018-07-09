package univie.cube.PicaDesktop.inputpreparation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.NoSuchElementException;

import univie.cube.PicaDesktop.archive.Unzip;
import univie.cube.PicaDesktop.cmd.arguments.PredictCmdArguments;
import univie.cube.PicaDesktop.directories.WorkDir;
import univie.cube.PicaDesktop.out.error.ErrorHandler;

public class InputPreparationPredict extends InputPreparation {
	
	//TODO: check if modelFile is valid

	private Path inputClusteringDir = null;
	private PredictCmdArguments predictCmdArguments;
	private Path modelFile;

	public InputPreparationPredict(PredictCmdArguments predictCmdArguments) {
		this.predictCmdArguments = predictCmdArguments;
		prepareInput();
	}
	
	public Path getInputClusteringDir() {
		return this.inputClusteringDir;
	}
	
	public Path getModelFile() {
		return this.modelFile;
	}
	
	@Override
	protected void prepareInput() {
		super.createWorkDir(predictCmdArguments.getTmpDir(), predictCmdArguments.getInputFiles());
		Path workDirPath = WorkDir.getWorkDir().getTmpDir();
		Path inputClusteringDir = null;
		try {
			inputClusteringDir = Files.createTempDirectory(workDirPath, "input-clustering-dir");
			super.inputFastaProcessing(predictCmdArguments.getThreads(), predictCmdArguments.getInputFiles(), inputClusteringDir);
		} catch (IOException e) {
			(new ErrorHandler(e, ErrorHandler.ErrorWeight.FATAL, "InputPreparationTrain failed.")).handle();
		}
		this.inputClusteringDir = inputClusteringDir;
		processModelAndDB();
	}
	
	private void processModelAndDB() {
		unzipModelAndDB();
		findModelFile();
	}
	
	private void unzipModelAndDB() {
		try {
			Unzip.unzip(predictCmdArguments.getModelFileZip(), WorkDir.getWorkDir().getTmpDir());
			Unzip.unzip(predictCmdArguments.getDbFileZip(), WorkDir.getWorkDir().getTmpDir());
		} catch (IOException e) {
			(new ErrorHandler(e, ErrorHandler.ErrorWeight.FATAL, "Could not decomress database and/or model. The file may not be valid.")).handle();
		}
	}
	
	private void findModelFile() {
		Path modelFile = null;
		try {
			modelFile = WorkDir.getWorkDir().findInDirectory("rules.pica", "rules.pica.");
		} catch (NoSuchElementException | IOException e) {
			(new ErrorHandler(e, ErrorHandler.ErrorWeight.FATAL, "The pica-rules file seems to be invalid")).handle();
		}
		this.modelFile = modelFile;
	}

}
