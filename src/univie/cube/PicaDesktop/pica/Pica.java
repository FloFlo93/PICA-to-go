package univie.cube.PicaDesktop.pica;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import univie.cube.PicaDesktop.directories.WorkDir;
import univie.cube.PicaDesktop.fastaformat.FastaHeaders;

//TODO: Callable is implemented, but concurracy is never used!!

public abstract class Pica {
	protected Path inputPica;
	protected Path outputResults;
	protected Path inputPhenotypes;
	protected String feature;
	
	public Pica(Path inputPica, Path outputResults, Path inputPhenotypes, String feature, WorkDir workDir) throws IOException {
		this.inputPica = inputPica;
		this.inputPhenotypes = correctInputPhenotypes(inputPhenotypes, workDir);
		this.outputResults = outputResults;
		this.feature = feature;
	}
	
	private Path correctInputPhenotypes(Path inputPhenotypes, WorkDir workDir) throws IOException {
		if(inputPhenotypes == null) return null;
		List<String> content = Files.readAllLines(inputPhenotypes);
		List<String> contentCorrected = new ArrayList<String>();
		
		int counter = 0;
		for(String line : content) {
			if(counter == 0) contentCorrected.add(line);
			else {
				String binName = line.split("\t")[0];
				String classifications = Arrays.stream(line.split("\t")).skip(1).collect(Collectors.joining("\t"));
				binName = FastaHeaders.getFileNameWithoutSuffix(binName);
				String newLine = binName + "\t" + classifications;
				contentCorrected.add(newLine);
			}
			++counter;
		}
		
		Path dest = Files.createTempFile(workDir.getTmpDir(), "inputPhenotypes", "");
		Files.write(dest, contentCorrected);
		return dest;
	}
	
}
	

	

