package univie.cube.PicaDesktop.directories;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.io.FileUtils;

public class WorkDir {
	
	private Path tmpDir;
	
	public WorkDir(String[] subdirectories, boolean debugMode, Path outputResultPath) throws IOException {
		Path tmpDirPath;
		if(debugMode) tmpDirPath = Files.createTempDirectory(outputResultPath, "picadesktop");
		else tmpDirPath = Files.createTempDirectory("picadesktop");
		this.tmpDir = tmpDirPath; 
		if(!debugMode) {
			tmpDir.toFile().deleteOnExit(); //only deleted directory when JVM terminates, not content of dir
			//deletes work folder when program is shut down
			Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
		        public void run() {
		            removeWorkDir();
		        }
		    }, "removeWorkDirOnShutdown"));
		}
		
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
