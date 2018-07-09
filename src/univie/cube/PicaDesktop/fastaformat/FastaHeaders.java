package univie.cube.PicaDesktop.fastaformat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import univie.cube.PicaDesktop.miscellaneous.CmdExecution;

public class FastaHeaders {
	
	public static Map<String, String> getFastaHeadersFromConcatProteomes(Path path) {
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
		String command = "awk '/^>/ {$0=\"" + ">" + fileNameWithoutSuffix + "^_" + "\"substr($0,2)}1' " +  fileName + " &> " + fileNameWithoutSuffix + ".tmp && mv " + fileNameWithoutSuffix + ".tmp " + fileName;
		CmdExecution.Status status = CmdExecution.executePipedSubprocess(command, inputFile.getParent().toFile());
		CmdExecution.printIfErrorOccured(status);
		if(status.errorOccured) throw new RuntimeException();
	}
	
	public static String getFileNameWithoutSuffix(String fileName) {
		String fileNameWithoutSuffix;
		if(fileName.contains(".") 
				&& fileName.substring(fileName.lastIndexOf("."), fileName.length()).matches(".fasta|.fna|.fa|.faa")) {
			fileNameWithoutSuffix = fileName.substring(0, fileName.lastIndexOf("."));
		}
		else fileNameWithoutSuffix = fileName;
		return fileNameWithoutSuffix;
	}
	
	
	
	
	
}
