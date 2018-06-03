package univie.cube.PicaDesktop.directories;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.io.FileUtils;

public class WorkDir {
	
	private Path tmpDir;
	
	public WorkDir(String[] subdirectories, boolean debugMode, Path tmpDirParent, long inputBinSize) throws IOException, RuntimeException {
		if(tmpDirParent != null) this.tmpDir = Files.createTempDirectory(tmpDirParent, "picadesktop");
		else tmpDir = Files.createTempDirectory("picadesktop");
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
	        public void run() {
	        	if(!debugMode) removeWorkDir();
	        	else System.err.println("WARNING: The debug mode is turned on.So all tmp files were not deleted. Path to tmp files: " + tmpDir.toString());
	        }
	    }, "removeWorkDirOnShutdown"));
		if (tmpDir.toFile().getFreeSpace() < inputBinSize*5) throw new RuntimeException("The available space in the tmp dir may be not sufficient. Please specify another directory!");
		
		String pathTmpDir = tmpDir.toFile().getAbsolutePath();
		for(String subdirectory : subdirectories) {
			if(! new File(pathTmpDir + "/" + subdirectory).mkdirs()) throw new IOException();
		}
	}
	
	public Path getTmpDir() {
		return tmpDir;
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
	
}
