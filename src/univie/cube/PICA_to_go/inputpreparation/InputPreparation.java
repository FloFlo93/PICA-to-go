package univie.cube.PICA_to_go.inputpreparation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import univie.cube.PICA_to_go.archive.FastaGzHandler;
import univie.cube.PICA_to_go.cmd.arguments.CmdArguments;
import univie.cube.PICA_to_go.directories.WorkDir;
import univie.cube.PICA_to_go.fastaformat.FastaValidateWeak;
import univie.cube.PICA_to_go.out.error.ErrorHandler;
import univie.cube.PICA_to_go.out.logging.CustomLogger;
import univie.cube.PICA_to_go.out.logging.CustomLogger.LoggingWeight;
import univie.cube.PICA_to_go.prodigal.Prodigal;

public abstract class InputPreparation {

	private CmdArguments cmdArguments;
	protected Path inputClusteringDir = null;
	
	protected InputPreparation(CmdArguments cmdArguments) {
		this.cmdArguments = cmdArguments;
		CustomLogger.getInstance().log(CustomLogger.LoggingWeight.INFO, "Starting to validate the input");
	}
	
	protected void inputFastaProcessing(int threads, Path inputBins, Path inputClusteringDir, String translationTable) throws IOException {
		FastaGzHandler fastaGzHandler = new FastaGzHandler(inputBins);
		List<Path> allFiles = fastaGzHandler.getFiles();
		List<Path> genomes = new ArrayList<Path>();
		
		//TODO: protein files and genome files correction (AND change name to proteins instead of proteins), additional dir needed im tmp
		
		Path correctedGenomesSubDir = Files.createTempDirectory(WorkDir.getWorkDir().getTmpDir(), "corrected_genomes");
		
		for(Path path : allFiles) {
			FastaValidateWeak fastaValidateWeak = new FastaValidateWeak(path);
			if(!fastaValidateWeak.isHeaderUnique()) {
				CustomLogger.getInstance().log(LoggingWeight.WARNING, "The fasta header of " + path.getFileName().toString() + " is not unique. This file will not be processed");
			}
			else if (fastaValidateWeak.getSequenceType() == FastaValidateWeak.SequenceType.DNA) {
				List<String> correctedLines = fastaValidateWeak.removeInvalidChars();
				if(fastaValidateWeak.hasInvalidChars()) {
					CustomLogger.getInstance().log(LoggingWeight.WARNING, "Fasta file " + path.getFileName().toString() + " contains invalid characters.These characters will be changed to 'N'");
					Path destFile = Paths.get(correctedGenomesSubDir.toString(), path.getFileName().toString());
					Files.write(destFile, correctedLines);
					genomes.add(destFile);
				}
				else genomes.add(path);
			}
			else if (fastaValidateWeak.getSequenceType() == FastaValidateWeak.SequenceType.PROTEIN) {
				List<String> correctedLines = fastaValidateWeak.removeInvalidChars();
				if(fastaValidateWeak.hasInvalidChars()) 					
					CustomLogger.getInstance().log(LoggingWeight.ERROR, "Fasta file " + path.getFileName().toString() + " contains invalid characters.These characters will be changed to 'X'");
				Files.write(Paths.get(inputClusteringDir.toString(), path.getFileName().toString()), correctedLines);
			}
		}
		

		
		findGenes(threads, genomes, inputClusteringDir, translationTable);
		FileUtils.deleteDirectory(correctedGenomesSubDir.toFile());
		
		fastaGzHandler.close();
	}
	
	private void findGenes(int threads, List<Path> inputBinsNucleotide, Path inputClusteringDir, String translationTable) {
		try {
			Prodigal.runProdigal(threads, inputBinsNucleotide, inputClusteringDir, translationTable);
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
	
	//throws fatal error if feature is not present
	protected abstract void featureExistsHook();
	protected abstract void processModelAndDBHook();
	
	/**
	 * 
	 * @return inputClusteringDir
	 */
	protected void prepareInput() {
		createWorkDir(cmdArguments.getTmpDir(), cmdArguments.getInputBins());
		this.featureExistsHook();
		Path workDirPath = WorkDir.getWorkDir().getTmpDir();
		Path inputClusteringDir = null;
		try {
			inputClusteringDir = Files.createTempDirectory(workDirPath, "input-clustering-dir");
			inputFastaProcessing(cmdArguments.getThreads(), cmdArguments.getInputBins(), inputClusteringDir, cmdArguments.getTranslationTable());
		} catch (IOException e) {
			(new ErrorHandler(e, ErrorHandler.ErrorWeight.FATAL, "InputPreparationTrain failed.")).handle();
		}
		this.inputClusteringDir = inputClusteringDir;
		processModelAndDBHook();
	}
	
	protected abstract Path getInputClusteringDir(); 
	
}
