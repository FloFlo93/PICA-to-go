package univie.cube.PicaDesktop.prodigal;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import univie.cube.PicaDesktop.global.ExecutablePaths;
import univie.cube.PicaDesktop.miscellaneous.CmdExecution;

public class Prodigal implements Runnable {
	
	private Path outputResults; 
	private Path inputFile;
	private Path inputClusteringDir;


	public static void runProdigal(int maxThreads, List<Path> inputBinsNucleotide, Path inputClusteringDir, Path outputResults) throws InterruptedException {
		ExecutorService es = Executors.newWorkStealingPool(maxThreads);
		for(Path path : inputBinsNucleotide) {
			Prodigal prodigal = new Prodigal(path, inputClusteringDir, outputResults);
			es.execute(prodigal);
		}
		es.shutdown();
		while(!es.awaitTermination(5, TimeUnit.SECONDS));
							
	}

	private Prodigal(Path inputFile, Path inputClusteringDir, Path outputResults) {
		this.inputFile = inputFile;
		this.inputClusteringDir = inputClusteringDir;
		this.outputResults = outputResults;
	}
	
	@Override
	public void run() {
		CmdExecution.Status status;
		String inputProdigal = inputFile.toString();
		int filePrefixIndex = inputFile.getFileName().toString().lastIndexOf(".");
		if(filePrefixIndex == -1) filePrefixIndex = inputFile.getFileName().toString().length();
		String outputProdigal = inputClusteringDir +"/" + inputFile.getFileName().toString().substring(0, filePrefixIndex) + ".fa";
		String[] commandProdigal = {ExecutablePaths.getExecutablePaths().PRODIGAL_EX.toString(), "-i", inputProdigal, "-a", outputProdigal};
		System.out.println(Arrays.toString(commandProdigal));
		try {
			status = CmdExecution.execute(commandProdigal, outputResults, "prodigal");
			CmdExecution.printIfErrorOccured(status);
			if(status.errorOccured) throw new RuntimeException();
		} catch (IOException | InterruptedException | RuntimeException e) {
			System.err.println("WARNING: Process Prodigal failed for " + inputFile.getFileName().toString());
			//TODO: fatal error if no sequence (of all) could be processed
		}
	}
	
}
