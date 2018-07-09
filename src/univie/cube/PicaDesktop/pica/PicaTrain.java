package univie.cube.PicaDesktop.pica;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import univie.cube.PicaDesktop.archive.ZipCreator;
import univie.cube.PicaDesktop.directories.WorkDir;
import univie.cube.PicaDesktop.global.ExecutablePaths;
import univie.cube.PicaDesktop.miscellaneous.CmdExecution;
import univie.cube.PicaDesktop.miscellaneous.CmdExecution.Status;

public class PicaTrain implements Callable<Void> {
	
	private Path inputPica;
	private Path outputResults;
	private Path inputPhenotypes;
	private String feature;
	
	private final String ruleFileName;
	private Path resultDirTmp;
	private String zippedModelName;
	

	public PicaTrain(Path inputPica, Path outputResults, Path inputPhenotypes, String feature) throws IOException {
		this.inputPica = inputPica;
		this.outputResults = outputResults;
		this.inputPhenotypes = inputPhenotypes;
		this.feature = feature;
		this.ruleFileName = (new StringBuilder(feature + "_rules.pica")).toString();
		this.resultDirTmp = Files.createTempDirectory(WorkDir.getWorkDir().getTmpDir(), "pica_train");
		this.zippedModelName = feature + ".picamodel";
	}

	
	private Path trainPica() throws FileNotFoundException, IOException, InterruptedException {
		String resultFilePathStr;
		StringBuilder s = new StringBuilder(resultDirTmp.toString());
		s.append("/");
		s.append(ruleFileName);
		resultFilePathStr = s.toString();
		
		String[] picaTrainCommand = {ExecutablePaths.getExecutablePaths().PICA_TRAIN.toString(), "-s", inputPica.toString(), "-c", inputPhenotypes.toString(), "-o", resultFilePathStr, "-t", feature, "-b", "1"};
		Status status = CmdExecution.execute(picaTrainCommand, WorkDir.getWorkDir().getTmpDir(), "pica-train");
		CmdExecution.printIfErrorOccured(status); 
		if(status.errorOccured) throw new RuntimeException();
		
		zipModel();
		
		return Paths.get(resultFilePathStr);
	}
	
	private void zipModel() throws IOException {
		List<Path> allModelFiles = Files.walk(resultDirTmp)
										.filter(path -> path.toFile().isFile())
										.collect(Collectors.toList());
		ZipCreator.createZip(Paths.get(outputResults.toString(), zippedModelName), allModelFiles);
	}

	@Override
	/**
	 * @return: tmp Path where rule-file is stored for further computation (all model files are also copied as zip-file in result dir) 
	 */
	public Void call() throws Exception {
		trainPica();
		return null;
	}
	
	public Path getModelFile() {
		return Paths.get(resultDirTmp.toString(), ruleFileName);
	}

}
