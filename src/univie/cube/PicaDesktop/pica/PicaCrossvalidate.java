package univie.cube.PicaDesktop.pica;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.Callable;

import univie.cube.PicaDesktop.directories.WorkDir;
import univie.cube.PicaDesktop.global.ExecutablePaths;
import univie.cube.PicaDesktop.miscellaneous.CmdExecution;
import univie.cube.PicaDesktop.miscellaneous.Serialize;

public class PicaCrossvalidate extends Pica implements Callable<Map<String, String>> {
	
	private Path logging;

	public PicaCrossvalidate(Path inputPica, Path outputResults, Path inputPhenotypes, String feature, Path logging, WorkDir workDir) throws IOException {
		super(inputPica, outputResults, inputPhenotypes, feature, workDir);
		this.logging = logging;
	}

	
	/** executes crossvalidate.py (PICA)
	 * @return crossval-json with mean balanced accuracy
	 */
	private Map<String, String> crossValPica() throws FileNotFoundException, IOException, InterruptedException {
		String[] commandPicaCrossVal = {ExecutablePaths.getExecutablePaths().PICA_CROSSVAL.toString(), "-s", inputPica.toString(), "-c",  inputPhenotypes.toString(), "-t", feature, "-o", outputResults.toString() + "/outputPICA.txt", "-C", outputResults + "/picaCrossValidationStats.json"};  
		CmdExecution.Status status = CmdExecution.execute(commandPicaCrossVal, logging, "pica"); 
		CmdExecution.printIfErrorOccured(status); 
		if(status.errorOccured) throw new RuntimeException();
		Map<String, String> crossVal = Serialize.getFromFile(Paths.get(outputResults + "/picaCrossValidationStats.json"));
		return crossVal;
	}


	@Override
	public Map<String, String> call() {
		try {
			return crossValPica();
		} catch (IOException | InterruptedException e) {
			return null;
		}
	}

}
