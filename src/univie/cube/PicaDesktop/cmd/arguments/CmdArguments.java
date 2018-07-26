package univie.cube.PicaDesktop.cmd.arguments;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import univie.cube.PicaDesktop.out.debug.DebugMode;
import univie.cube.PicaDesktop.out.error.ErrorHandler;
import univie.cube.PicaDesktop.out.logging.CustomLogger;


public abstract class CmdArguments {
	
	private Path inputBins;
	private String featureName;
	private Path outputResults;
	private int threads;
	private Path tmpDir;
	
	public Path getInputBins() {
		return this.inputBins;
	}
	
	public void setInputBins(String inputBinsStr) {
		Path inputBinsPath = Paths.get(inputBinsStr);
		this.directoryExist(inputBinsPath);
		this.inputBins = inputBinsPath;
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
		this.directoryExist(outputResultsPath);
		this.outputResults = outputResultsPath;
	}
	
	public void setDebugMode(boolean debugMode) {
		DebugMode.initializeDebugMode(debugMode);
	}

	public int getThreads() {
		return threads;
	}

	public void setThreads(String threads) {
		this.threads = parseThreads(threads);
	}

	public Path getTmpDir() {
		return tmpDir;
	}

	public void setTmpDir(String tmpDirStr) {
		if(tmpDirStr == null) this.tmpDir = null;
		else {
			Path tmpDirPath = Paths.get(tmpDirStr);
			this.directoryExist(tmpDirPath);
			this.tmpDir = tmpDirPath;
		}
	}
	
	
	protected void directoryExist(Path directory) { 
		if(! Files.exists(directory) || ! Files.isDirectory(directory)) notExistError(directory, "directory");
	}
	
	protected void fileExists(Path file) {
		if(! Files.exists(file) || Files.isDirectory(file)) notExistError(file, "file");
	}
	
	protected void fileOrDirectoryExists(Path file) {
		if(! Files.exists(file)) notExistError(file, "file or directory");
	}
	
	private void notExistError(Path path, String type){
		String errorMessage = "The path " + path.toString() + " does not point to a " + type;
		(new ErrorHandler(new RuntimeException(), ErrorHandler.ErrorWeight.FATAL, errorMessage)).handle();
	}
	
	protected void invalidArgumentError(String parameter) {
		String errorMessage = "Invalid option for parameter [-" + parameter + "]";
		(new ErrorHandler(new RuntimeException(), ErrorHandler.ErrorWeight.FATAL, errorMessage)).handle();
	}
	
	protected void invalidArgumentWarning(String parameter, String defaultOptionStr) {
		String errorMessage = "Invalid option for parameter " + parameter + ". Default option will be used: " + defaultOptionStr;
		(new ErrorHandler(new RuntimeException(), ErrorHandler.ErrorWeight.FATAL, errorMessage)).handle();
	}
	
	protected int parseThreads(String threadsStr) {
		int threads;
		if(threadsStr == null || ! threadsStr.matches("\\d+")) {
			threads = Runtime.getRuntime().availableProcessors();
			String logMessage = "No/invalid option for thread number specified, all available cores (" + threads + ") used";
			CustomLogger.getInstance().log(CustomLogger.LoggingWeight.INFO, logMessage);
		}
		else {
			threads = Integer.parseInt(threadsStr);
			String logMessage = threads + " threads will be used (user specified)";
			CustomLogger.getInstance().log(CustomLogger.LoggingWeight.INFO, logMessage);

		}
		
		return threads;
	}
		
}
