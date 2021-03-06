package univie.cube.PICA_to_go.main;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidParameterException;
import java.util.Arrays;

import univie.cube.PICA_to_go.global.Config;
import univie.cube.PICA_to_go.out.error.ErrorHandler;
import univie.cube.PICA_to_go.pipelines.Pipeline;
import univie.cube.PICA_to_go.pipelines.PredictPipeline;
import univie.cube.PICA_to_go.pipelines.TrainPipeline;

public class Main {
	
	private static final String help_info = "PICA-to-go\n\n A program to train/predict PICA models.\n Author: Florian Piewald, 2018\n\n Two programs are available in this package:\n - train\n - predict\n\n For detailed information, view the user guide and the documentation at: https://github.com/FloFlo93/PICA-to-go/ \n";

	public static void main(String[] args) {
		
		//----choose train or predict pipeline depending on commandline argument--------
		
		String mode = (args.length < 2) ? "" : args[0];
		Pipeline pipeline = null;
		
		String knownModes = "predict, train"; 
		
		if(mode.equals("predict")) {
			pipeline = new PredictPipeline();
		}
		else if(mode.equals("train")) {
			pipeline = new TrainPipeline();
		}
		else if(mode.equals("-h")) {
			System.out.println(help_info);
			System.exit(0);
		}
		else {
			if (! mode.equals("")) {
				(new ErrorHandler(new InvalidParameterException(), ErrorHandler.ErrorWeight.FATAL, mode + " is an unknown mode, known modes: {" + knownModes + "}")).handle();
			}
			else {
				(new ErrorHandler(new InvalidParameterException(), ErrorHandler.ErrorWeight.FATAL, "No mode specified, known modes: {" + knownModes + "}")).handle();
			}
		}
		//----check if script was launched from launch script-------------------------------//
		//(needed to check if dependencies are available, to compile prodigal and find right mmseqs version depending on CPU architecture
		
		boolean calledFromLauncher = (args[args.length-1].equals("launcher") ? true : false);
		if(! calledFromLauncher) {
			(new ErrorHandler(new RuntimeException(), ErrorHandler.ErrorWeight.WARNING, "It seems that you tried to execute the .jar file directly without using the launcher script. Please use the launcher script!")).handle();
		}
		
		//----initialize global variables (path of executables)------------------------//
		
		try {
			Path jarDir = Paths.get(Main.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getParent();
			Config.initialize(Paths.get(jarDir.toString(), "config"));
		} catch (IllegalArgumentException | IllegalAccessException | IOException e) {
			e.printStackTrace();
			(new ErrorHandler(e, ErrorHandler.ErrorWeight.FATAL, "Configuration file could not be processed")).handle();
		}
		
		//----start pipeline------------------------------------------------------//
		
		args = Arrays.copyOfRange(args, 1, args.length -1); //removes first item of array and the last one ("launcher" part).
		
		pipeline.startPipeline(args);
	}
	
}
