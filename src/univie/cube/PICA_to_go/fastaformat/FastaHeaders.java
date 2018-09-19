package univie.cube.PICA_to_go.fastaformat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import univie.cube.PICA_to_go.miscellaneous.CmdExecution;

public class FastaHeaders {
	
	private static String binGeneSeperator = "^_";
	
	public static Map<String, String> getFastaHeadersFromConcatproteins(Path path) {
		//get chunks of fasta header + sequence
		List<String> chunks;
		try {
			chunks = Arrays.asList(Files.readAllLines(path).stream()
									.collect(Collectors.joining("\n"))
									.split(">"));
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage());
		}
		
		Map<String, String> fastaHeaders = chunks.stream()
											  .map(line -> line.split("\n")[0]) //extract fasta header
											  .collect(Collectors.toMap(
													  fasta -> fasta.split("\\s+")[0], 
													  fasta -> fasta));
		return fastaHeaders;
	}
	
	public static void renameHeader(Path inputFile) throws IOException, InterruptedException {
		String fileName = inputFile.getFileName().toString();
		String fileNameWithoutSuffix;
		fileNameWithoutSuffix = getFileNameWithoutSuffix(fileName);
		String command = "awk '/^>/ {$0=\"" + ">" + fileNameWithoutSuffix + binGeneSeperator + "\"substr($0,2)}1' " +  fileName + " &> " + fileNameWithoutSuffix + ".tmp && mv " + fileNameWithoutSuffix + ".tmp " + fileName;
		CmdExecution.Status status = CmdExecution.executePipedSubprocess(command, inputFile.getParent().toFile());
		CmdExecution.printIfErrorOccured(status);
		if(status.errorOccured) throw new RuntimeException();
	}
	
	/**
	 * 
	 * @param fileName
	 * @return returns the filename without the suffix (if suffix is faa, fna, fasta, txt or fa and/or gz) e.g. ERR23322.1.faa.gz will be ERR23322.1, the same is true for ERR23322.1.faa, ERR23322.1.gz or ERR23322.1
	 */
	public static String getFileNameWithoutSuffix(String fileName) {
		return fileName.replaceAll("(|\\.(faa|fna|fasta|txt|fa))(\\.gz|)(?!.)", "");
	}
	
	public static String getBinGeneSeperator() {
		return binGeneSeperator;
	}
	
	
	
}
