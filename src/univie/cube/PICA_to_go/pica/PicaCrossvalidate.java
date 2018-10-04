package univie.cube.PICA_to_go.pica;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.commons.lang3.ArrayUtils;

import univie.cube.PICA_to_go.directories.WorkDir;
import univie.cube.PICA_to_go.global.Config;
import univie.cube.PICA_to_go.miscellaneous.CmdExecution;
import univie.cube.PICA_to_go.miscellaneous.Serialize;

public class PicaCrossvalidate extends Pica implements Callable<Map<String, String>> {

	private Path inputPica;
	private Path outputResults;
	private Path inputPhenotypes;
	private String feature;
	
	public PicaCrossvalidate(Path inputPica, Path outputResults, Path inputPhenotypes, String feature) throws IOException {
		this.inputPica = inputPica;
		this.outputResults = outputResults; 
		this.inputPhenotypes = inputPhenotypes; 
		this.feature = feature;
	}

	
	/** executes crossvalidate.py (PICA)
	 * @return crossval-json with mean balanced accuracy
	 */
	private Map<String, String> crossValPica() throws FileNotFoundException, IOException, InterruptedException {
		String[] commandPicaCrossVal = {Config.getExecutablePaths().getPYTHON_PATH().toString(), Config.getExecutablePaths().getPICA_CROSSVAL().toString(), "-s", inputPica.toString(), "-c",  inputPhenotypes.toString(), "-t", feature, "-o", outputResults.toString() + "/outputPICA.txt", "-C", outputResults + "/picaCrossValidationStats.json"};  
		commandPicaCrossVal = ArrayUtils.addAll(commandPicaCrossVal, Config.getInstance().getADD_ARG_PICA_CROSSVAL());
		CmdExecution.Status status = CmdExecution.execute(commandPicaCrossVal, WorkDir.getWorkDir().getTmpDir(), "pica-crossval"); 
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
