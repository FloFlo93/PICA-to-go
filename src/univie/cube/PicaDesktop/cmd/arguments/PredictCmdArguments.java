package univie.cube.PicaDesktop.cmd.arguments;

import java.nio.file.Path;
import java.nio.file.Paths;

import univie.cube.PicaDesktop.out.debug.DebugMode;

public class PredictCmdArguments extends CmdArguments {
	private Path dbFileZip;
	private Path modelFileZip;
	private Path inputFiles;
	private String featureName;
	private Path outputResults;
	private int threads;
	private Path tmpDir;

	public Path getDbFileZip() {
		return dbFileZip;
	}
	public void setDbFileZip(String dbFileZipStr) {
		Path dbFileZipPath = Paths.get(dbFileZipStr);
		super.fileExists(dbFileZipPath);
		this.dbFileZip = dbFileZipPath;
	}
	public Path getModelFileZip() {
		return modelFileZip;
	}
	public void setModelFileZip(String modelFileZipStr) {
		Path modelFileZipPath = Paths.get(modelFileZipStr);
		super.fileExists(modelFileZipPath);
		this.modelFileZip = modelFileZipPath;
	}
	public Path getInputFiles() {
		return inputFiles;
	}
	public void setInputFiles(String inputFilesStr) {
		Path inputFilesPath = Paths.get(inputFilesStr);
		super.directoryExist(inputFilesPath);
		this.inputFiles = inputFilesPath;
	}
	public String getFeatureName() {
		return featureName;
	}
	public void setFeatureName(String featureName) {
		this.featureName = featureName;
	}
	public Path getOutputResults() {
		return outputResults;
	}
	public void setOutputResults(String outputResultsStr) {
		Path outputResultsPath = Paths.get(outputResultsStr);
		super.directoryExist(outputResultsPath);
		this.outputResults = outputResultsPath;
	}
	public void setDebugMode(boolean debugMode) {
		DebugMode.initializeDebugMode(debugMode);
	}
	public int getThreads() {
		return threads;
	}
	public void setThreads(String threads) {
		this.threads = super.parseThreads(threads);
	}
	public Path getTmpDir() {
		return tmpDir;
	}
	public void setTmpDir(String tmpDirStr) {
		if(tmpDirStr == null) this.tmpDir = null;
		else {
			Path tmpDirPath = Paths.get(tmpDirStr);
			super.fileExists(tmpDirPath);
			this.tmpDir = tmpDirPath;
		}
	}
	
}
