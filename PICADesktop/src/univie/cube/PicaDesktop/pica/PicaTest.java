package univie.cube.PicaDesktop.pica;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.concurrent.Callable;

import univie.cube.PicaDesktop.miscellaneous.CmdExecution;

public class PicaTest extends Pica implements Callable<Void> {

	private Path modelFile;
	
	public PicaTest(Path inputPica, Path picaExecutable, Path outputResults, String feature, Path modelFile) {
		super(inputPica, picaExecutable, outputResults, null, feature);
		this.modelFile = modelFile;
	}

	@Override
	public Void call() throws Exception {
		testPica();
		return null;
	}
	
	private void testPica() throws IOException, InterruptedException {
		String[] commandTestPica = {picaExecutable.toString(), "-s", inputPica.toString(), "-m", modelFile.toString(), "-t", feature};
		System.out.println(Arrays.toString(commandTestPica));
		Path outputPica = Files.createFile(Paths.get(outputResults.toString(), "outputPica.txt"));
		CmdExecution.executePipeToFile(commandTestPica, outputPica.toFile(), null);
	}

}
