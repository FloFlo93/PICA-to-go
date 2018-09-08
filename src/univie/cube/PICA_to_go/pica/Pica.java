package univie.cube.PICA_to_go.pica;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import univie.cube.PICA_to_go.clustering.datatypes.BinCOGs;
import univie.cube.PICA_to_go.clustering.methods.MMseqsClustering;
import univie.cube.PICA_to_go.directories.WorkDir;
import univie.cube.PICA_to_go.fastaformat.FastaHeaders;
import univie.cube.PICA_to_go.out.error.ErrorHandler;


public abstract class Pica {
	
	
	public static Path correctInputPhenotypes(Path inputPhenotypes) throws IOException {
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
		
		Path dest = Files.createTempFile(WorkDir.getWorkDir().getTmpDir(), "inputPhenotypes", "");
		Files.write(dest, contentCorrected);
		return dest;
	}
	
	
	public static Path createInputPica(Path dir, Map<String, BinCOGs> orthogroupsPerBin, String pattern) {
		Path inputPicaForTrain = null;
		try {
			inputPicaForTrain = Files.createTempFile(dir, "input-pica_" + pattern, "");
			MMseqsClustering.writePicaInputFile(orthogroupsPerBin, inputPicaForTrain);
		} catch (IOException e) {
			(new ErrorHandler(e, ErrorHandler.ErrorWeight.FATAL, "input-pica could not be generated")).handle();
		}
		return inputPicaForTrain;
	}
	
	public static Path createInputPica(Map<String, BinCOGs> orthogroupsPerBin, String pattern) {
		return createInputPica(WorkDir.getWorkDir().getTmpDir(), orthogroupsPerBin, pattern);
	}
}
	

	

