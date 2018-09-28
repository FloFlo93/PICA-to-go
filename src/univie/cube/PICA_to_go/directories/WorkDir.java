package univie.cube.PICA_to_go.directories;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.NoSuchElementException;

import org.apache.commons.io.FileUtils;
import org.mapdb.DB;
import org.mapdb.DBMaker;

import univie.cube.PICA_to_go.out.debug.DebugMode;
import univie.cube.PICA_to_go.out.error.ErrorHandler;
import univie.cube.PICA_to_go.out.logging.CustomLogger;

public class WorkDir {
	
	private Path tmpDir;
	private final int inputBinSize_limit_factor = 5;
	
	private Path tmpDirParent;
	private long inputBinSize;
	
	private DB db;
	
	private static WorkDir workDirSingleton;
	
	public static void createWorkDir(Path tmpDirParent, long inputBinSize) throws IOException {
		WorkDir workDir = new WorkDir(tmpDirParent, inputBinSize);
		workDir.create();
		WorkDir.workDirSingleton = workDir;
	}
	
	public static WorkDir getWorkDir() {
		return WorkDir.workDirSingleton;
	}
	
	private WorkDir(Path tmpDirParent, long inputBinSize) {
		this.tmpDirParent = tmpDirParent;
		this.inputBinSize = inputBinSize;
	}
	
	private void create() throws IOException {
		if(tmpDirParent != null) this.tmpDir = Files.createTempDirectory(tmpDirParent, "PICA_to_go");
		else tmpDir = Files.createTempDirectory("PICA_to_go");
		createDB();
		spaceCheck();
		shutdownHook();
	}
	
	private void createDB() {
		Path dbFile = Paths.get(this.tmpDir.toString(), "mapdb");
		this.db = DBMaker
				.fileDB(dbFile.toFile())
				.fileMmapEnableIfSupported()
				.fileMmapPreclearDisable()
				.make();
	}
	
	private void shutdownHook() {
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
	        public void run() {
	        	if(! DebugMode.getInstance().isDebugMode()) {
	        		boolean removed = removeWorkDir();
	        		if(!removed) {
	        			(new ErrorHandler(new RuntimeException(), ErrorHandler.ErrorWeight.WARNING, "The tmp dir could not be removed. Path to tmp files: " + tmpDir.toString())).handle();
	        		}
	        	}
	        	else {
        			CustomLogger.getInstance().log(CustomLogger.LoggingWeight.WARNING, "The debug mode is turned on.So all tmp files were not deleted. Path to tmp files: " + tmpDir.toString());
	        	}
	    }}, "removeWorkDirOnShutdown"));
	}
	
	private void spaceCheck() {
		if (tmpDir.toFile().getFreeSpace() < inputBinSize*inputBinSize_limit_factor) throw new RuntimeException("The available space in the tmp dir may be not sufficient. Please specify another directory!");
	}
	
	public Path getTmpDir() {
		return tmpDir;
	}
	
	public DB getDB() {
		return this.db;
	}
	
	//removes tmp dir + content, TODO: should be called every time a fatal exception happens in finally block
	public boolean removeWorkDir() {
		try {
			FileUtils.deleteDirectory(tmpDir.toFile());
		}
		catch(IOException e) {
			return false;
		}
		return true;
	}
	
	public Path findInDirectory(String contains, String doesNotContain) throws IOException, NoSuchElementException {
		Path file = Files.walk(WorkDir.getWorkDir().getTmpDir())
				.filter(path -> path.getFileName().toString().contains(contains)) 
				.filter(path -> ! path.getFileName().toString().contains(doesNotContain)) //do not get any files with additional suffix after .pica (e.g. .pica.classmap)
				.findFirst()
				.get();
		return file;
	}
	
}
