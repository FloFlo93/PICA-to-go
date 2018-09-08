package univie.cube.PICA_to_go.prodigal;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import univie.cube.PICA_to_go.directories.WorkDir;
import univie.cube.PICA_to_go.global.Config;
import univie.cube.PICA_to_go.miscellaneous.CmdExecution;
import univie.cube.PICA_to_go.out.error.ErrorHandler;
import univie.cube.PICA_to_go.out.logging.CustomLogger;

public class Prodigal implements Runnable {
	
	private Path inputFile;
	private Path inputClusteringDir;
	private String translationTable;


	public static void runProdigal(int maxThreads, List<Path> inputBinsNucleotide, Path inputClusteringDir, String translationTable) throws InterruptedException {
		if(inputBinsNucleotide.size() == 0) return;
		CustomLogger.getInstance().log(CustomLogger.LoggingWeight.INFO, "Gene prediction with Prodigal started");
		ExecutorService es = Executors.newWorkStealingPool(maxThreads);
		for(Path path : inputBinsNucleotide) {
			Prodigal prodigal = new Prodigal(path, inputClusteringDir, translationTable);
			es.execute(prodigal);
		}
		es.shutdown();
		while(!es.awaitTermination(5, TimeUnit.SECONDS));
							
	}

	private Prodigal(Path inputFile, Path inputClusteringDir, String translationTable) {
		this.inputFile = inputFile;
		this.inputClusteringDir = inputClusteringDir;
		this.translationTable = translationTable;
	}
	
	@Override
	public void run() {
		CmdExecution.Status status;
		String inputProdigal = inputFile.toString();
		int filePrefixIndex = inputFile.getFileName().toString().lastIndexOf(".");
		if(filePrefixIndex == -1) filePrefixIndex = inputFile.getFileName().toString().length();
		String outputProdigal = inputClusteringDir +"/" + inputFile.getFileName().toString().substring(0, filePrefixIndex) + ".fa";
		String[] commandProdigal = {Config.getExecutablePaths().getPRODIGAL_EX().toString(),"-g", translationTable, "-i", inputProdigal, "-a", outputProdigal};
		try {
			status = CmdExecution.execute(commandProdigal, WorkDir.getWorkDir().getTmpDir(), "prodigal");
			CmdExecution.printIfErrorOccured(status);
			if(status.errorOccured) throw new RuntimeException();
		} catch (IOException | InterruptedException | RuntimeException e) {
			(new ErrorHandler(e, ErrorHandler.ErrorWeight.WARNING, "Process Prodigal failed for " + inputFile.getFileName().toString())).handle();
		}
	}
	
}
