package univie.cube.PicaDesktop.main;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import univie.cube.PicaDesktop.global.ExecutablePaths;
import univie.cube.PicaDesktop.pipelines.Pipeline;
import univie.cube.PicaDesktop.pipelines.PredictPipeline;
import univie.cube.PicaDesktop.pipelines.TrainPipeline;

public class Main {

	public static void main(String[] args) {
		
		//----choose train or predict pipeline depending on commandline argument--------
		
		String mode = (args.length < 2) ? "" : args[0];
		Pipeline pipeline = null;
		if(mode.equals("predict")) {
			pipeline = new PredictPipeline();
		}
		else if(mode.equals("train")) {
			pipeline = new TrainPipeline();
		}
		else {
			if (! mode.equals("")) System.err.println("FATAL ERROR " + mode + " is an unknown mode, known modes: {predict,train}");
			else System.err.println("FATAL ERROR: No mode specified, known modes: {predict,train}");
			System.exit(1);
		}
		
		//----check if script was launched from launch script-------------------------------//
		//(needed to check if dependencies are available, to compile prodigal and find right mmseqs version depending on CPU architecture
		
		boolean calledFromLauncher = (args[args.length-1].equals("launcher") ? true : false);
		if(! calledFromLauncher) {
			System.err.println("FATAL ERROR: It seems that you tried to execute the .jar file directly without using the launcher script. Please use the launcher script!");
			System.exit(1);
		}
		
		//----initialize global variables (path of executables)------------------------//
		
		try {
			Path jarDir = Paths.get(Main.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getParent();
			ExecutablePaths.initialize(Paths.get(jarDir.toString(), "config"));
		} catch (IllegalArgumentException | IllegalAccessException | IOException e) {
			e.printStackTrace();
			System.err.println("Fatal Error: Configuration file could not be processed");
			System.exit(1);
		}
		
		//----start pipeline------------------------------------------------------//
		
		args = Arrays.copyOfRange(args, 1, args.length -1); //removes first item of array and the last one ("launcher" part).
		
		pipeline.startPipeline(args);
	}
	
}
