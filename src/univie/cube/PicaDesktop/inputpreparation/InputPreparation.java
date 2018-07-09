package univie.cube.PicaDesktop.inputpreparation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import univie.cube.PicaDesktop.directories.WorkDir;
import univie.cube.PicaDesktop.fastaformat.FastaValidate;
import univie.cube.PicaDesktop.out.error.ErrorHandler;
import univie.cube.PicaDesktop.out.logging.CustomLogger;
import univie.cube.PicaDesktop.out.logging.CustomLogger.LoggingWeight;
import univie.cube.PicaDesktop.prodigal.Prodigal;

public abstract class InputPreparation {

	
	protected void inputFastaProcessing(int threads, Path inputBins, Path inputClusteringDir) throws IOException {
		List<Path> allFiles = Files.walk(inputBins).filter(Files::isRegularFile).collect(Collectors.toList());
		List<Path> genomes = new ArrayList<Path>();
		List<Path> proteomes = new ArrayList<Path>();
		List<Path> invalidProteomes = new ArrayList<Path>();
		
		for(Path path : allFiles) {
			FastaValidate fastaValidate = new FastaValidate(path);
			if(!fastaValidate.isHeaderUnique()) {
				CustomLogger.getInstance().log(LoggingWeight.WARNING, "The fasta header of " + path.getFileName().toString() + " is not unique. This file will not be processed");
			}
			else if (fastaValidate.getSequenceType() == FastaValidate.SequenceType.DNA) genomes.add(path);
			else if (fastaValidate.getSequenceType() == FastaValidate.SequenceType.PROTEIN) proteomes.add(path);
			else {
				CustomLogger.getInstance().log(CustomLogger.LoggingWeight.WARNING, path.getFileName().toString() + " is not a valid fasta file. All invalid characters are converted to 'X'.");
				invalidProteomes.add(path);
			}
		}
		
		findGenes(threads, genomes, inputClusteringDir);
		
		for(Path path : proteomes) {
			Files.copy(path, Paths.get(inputClusteringDir.toString(), path.getFileName().toString()));
		}
		
		for(Path path : invalidProteomes) {
			List<String> correctedLines = FastaValidate.removeInvalidChars(Files.readAllLines(path));
			Files.write(Paths.get(inputClusteringDir.toString(), path.getFileName().toString()), correctedLines);
		}
	}
	
	private void findGenes(int threads, List<Path> inputBinsNucleotide, Path inputClusteringDir) {
		try {
			Prodigal.runProdigal(threads, inputBinsNucleotide, inputClusteringDir);
		} catch (InterruptedException e1) {
			(new ErrorHandler(e1, ErrorHandler.ErrorWeight.FATAL, "Process Prodigal failed")).handle();
		}
	}
	
	protected void createWorkDir(Path tmpDirParent, Path inputBins) {
		try {
			long inputBinSize = getInputSizeBins(inputBins);
			WorkDir.createWorkDir(tmpDirParent, inputBinSize);
		} catch (IOException e) {
			(new ErrorHandler(e, ErrorHandler.ErrorWeight.FATAL, "WorkDir could not be created")).handle();
		}
	}
	
	private long getInputSizeBins(Path inputBins) throws IOException {
		long folderSizeInputBins = Files.walk(inputBins)
				.filter(p -> p.toFile().isFile())
				.mapToLong(p -> p.toFile().length())
				.sum();
		return folderSizeInputBins;
	}
	
	//TODO: maybe validate phenotype file and model / DB file?
	
	protected boolean featureExists(Path inputPhenotypes, String feature) {
		List<String> inputPhenotypesReadFile = null;
		try {
			inputPhenotypesReadFile = Files.readAllLines(inputPhenotypes);
		} catch (IOException e) {
			(new ErrorHandler(e, ErrorHandler.ErrorWeight.FATAL, "Feature specified does not exist in feature file")).handle();
		}
		String[] allFeatures = inputPhenotypesReadFile.get(0).split("\t");
		long occurances = Arrays.stream(allFeatures).filter(obj -> obj.equals(feature)).count();
		return (occurances == 1);
	} 
	
	/**
	 * 
	 * @return inputClusteringDir
	 */
	protected abstract void prepareInput();
	
}
