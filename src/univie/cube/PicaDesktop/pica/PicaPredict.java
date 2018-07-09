package univie.cube.PicaDesktop.pica;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Callable;

import univie.cube.PicaDesktop.global.ExecutablePaths;
import univie.cube.PicaDesktop.miscellaneous.CmdExecution;

public class PicaPredict extends Pica implements Callable<Void> {

	private Path inputPica;
	private Path outputResults;
	private String feature;
	private Path modelFile;
	
	public PicaPredict(Path inputPica, Path outputResults, String feature, Path modelFile) throws IOException {
		this.inputPica = inputPica;
		this.outputResults = outputResults; 
		this.feature = feature;
		this.modelFile = modelFile;
	}

	@Override
	public Void call() throws Exception {
		testPica();
		return null;
	}
	
	private void testPica() throws IOException, InterruptedException {
		String[] commandTestPica = {ExecutablePaths.getExecutablePaths().PICA_TEST.toString(), "-s", inputPica.toString(), "-m", modelFile.toString(), "-t", feature};
		Path outputPica = Files.createFile(Paths.get(outputResults.toString(), "outputPica.txt"));
		CmdExecution.executePipeToFile(commandTestPica, outputPica.toFile(), null);
	}

}
