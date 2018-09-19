package univie.cube.PICA_to_go.pica;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Callable;

import org.apache.commons.lang3.ArrayUtils;

import univie.cube.PICA_to_go.global.Config;
import univie.cube.PICA_to_go.miscellaneous.CmdExecution;
import univie.cube.PICA_to_go.miscellaneous.Serialize;

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
		String[] commandTestPica = {Config.getExecutablePaths().getPICA_TEST().toString(), "-s", inputPica.toString(), "-m", modelFile.toString(), "-t", feature};
		ArrayUtils.addAll(commandTestPica, Config.getExecutablePaths().getADD_ARG_PICA_TEST());
		Path outputPica = Paths.get(outputResults.toString(), "outputPica.txt");
		Serialize.writeToFile(outputPica, "");
		CmdExecution.executePipeToFile(commandTestPica, outputPica.toFile(), null);
	}

}
