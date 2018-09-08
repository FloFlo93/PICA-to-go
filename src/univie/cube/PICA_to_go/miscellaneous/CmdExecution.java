package univie.cube.PICA_to_go.miscellaneous;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import univie.cube.PICA_to_go.out.debug.DebugMode;
import univie.cube.PICA_to_go.out.logging.CustomLogger;

public class CmdExecution {
	
	private static Map<Integer, Process> processes = new HashMap<Integer, Process>();
	private static boolean shutDownHookCalled = false;
	
	public static class Status {
		public boolean errorOccured;
		public int errorCode;
		public String command;
	}
	
	private static void shutDownHook() {
		if (shutDownHookCalled) return;
		shutDownHookCalled = true;
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
	        public void run() {
	            for(Map.Entry<Integer, Process> process : processes.entrySet()) process.getValue().destroy(); 
	        }
	    }, "destroyProcessesOnShutdown"));
	}

	//execute command without permanent stdout log file
	public static Status execute(String[] command) throws IOException, InterruptedException {
		return execute(command, null, "PICA_to_go_unnamed");
	}
	
	public static Status executePipedSubprocess(String command) throws IOException, InterruptedException {
		return executePipedSubprocess(command, null);
	} 
	
	//TODO: change to Path
	public static Status executePipedSubprocess(String command, File startFromDirectory) throws IOException, InterruptedException {
		Status status = new Status();
		
		ProcessBuilder builder = new ProcessBuilder("/bin/sh", "-c", command);
		if(startFromDirectory != null) builder.directory(startFromDirectory);
		final Process process = builder.start();
		processes.put(process.hashCode(), process);
		shutDownHook();
		process.waitFor();
		processes.remove(process.hashCode());
		status.errorCode = process.exitValue();
		if(status.errorCode != 0) status.errorOccured = true;
	    else status.errorOccured = false;
	    
	    status.command = command;
	    return status;
	}
	
	public static Status execute(String[] command, Path pathToLogfile, String commandIdentifier) throws IOException, InterruptedException {
		return execute(command, pathToLogfile, commandIdentifier, null);
	}
	
	/** execute command with permanent stdout log file (if path to logfile != null)
	 * 
	 * @param command
	 * @param pathToLogfile
	 * @param commandIdentifier
	 * @param startFromDirectory
	 * @return Status
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static Status execute(String[] command, Path pathToLogfile, String commandIdentifier, File startFromDirectory) throws IOException, InterruptedException {
		
		File tmp = null;
		if(pathToLogfile == null) {
			tmp = File.createTempFile(commandIdentifier, null);
			tmp.deleteOnExit();
		}
		else {
			tmp = File.createTempFile("." + commandIdentifier, ".log", pathToLogfile.toFile());
		}
		
		Status status = executePipeToFile(command, tmp, startFromDirectory);
		
		if(pathToLogfile == null) tmp.delete();
		
		return status;
	}
	
	public static Status executePipeToFile(String[] command, File file, File startFromDirectory) throws IOException, InterruptedException {
		ProcessBuilder builder = new ProcessBuilder(command);
		if(startFromDirectory != null) builder.directory(startFromDirectory);
		builder.redirectErrorStream(true).redirectOutput(file);
	    final Process process = builder.start();
	    
	    processes.put(process.hashCode(), process);
	    shutDownHook();
	    Status status = new Status();
	    process.waitFor();
	    processes.remove(process.hashCode());
	    
	    status.errorCode = process.exitValue();
	    if(status.errorCode != 0) status.errorOccured = true;
	    else status.errorOccured = false;
	    
	    status.command = "";
	    for(int i=0; i<command.length; i++) status.command += command[i] + " ";
	    
	    return status;
	}
	
	public static void printIfErrorOccured(Status status) {
		if (status.errorOccured && DebugMode.getInstance().isDebugMode()) {
			String errorMessage = "ERROR occured in subprocess \n ORIGINAL COMMAND: " + status.command;
			CustomLogger.getInstance().log(CustomLogger.LoggingWeight.ERROR, errorMessage);
		}
	}
	
	
}
