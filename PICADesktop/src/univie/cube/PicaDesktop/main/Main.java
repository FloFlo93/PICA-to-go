package univie.cube.PicaDesktop.main;

import java.util.Arrays;

import univie.cube.PicaDesktop.pipelines.Pipeline;
import univie.cube.PicaDesktop.pipelines.PredictPipeline;
import univie.cube.PicaDesktop.pipelines.TrainPipeline;

public class Main {

	public static void main(String[] args) {
		String mode = (args.length == 0) ? "" : args[0];
		Pipeline pipeline = null;
		
		if(mode.equals("predict")) {
			pipeline = new PredictPipeline();
		}
		
		else if(mode.equals("train")) {
			pipeline = new TrainPipeline();
		}
		
		else {
			System.err.println("ERROR " + mode + " is an unknown mode, known modes: {predict,train}");
			System.exit(1);
		}
		
		args = Arrays.copyOfRange(args, 1, args.length); //removes first item of array 
		
		pipeline.startPipeline(args);
	}
	
}
