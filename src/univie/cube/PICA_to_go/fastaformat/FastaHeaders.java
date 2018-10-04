package univie.cube.PICA_to_go.fastaformat;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;
import univie.cube.PICA_to_go.directories.WorkDir;
import univie.cube.PICA_to_go.miscellaneous.CmdExecution;


public class FastaHeaders {
	
	private static String binGeneSeperator = "^_";
	
	public static HTreeMap<String, String> getFastaHeadersFromConcatproteins(Path path) throws IOException {

		HTreeMap<String, String> fastaHeaders = WorkDir.getWorkDir().getDB().hashMap("fastaHeaders", Serializer.STRING,Serializer.STRING ).create();
		
		BufferedReader br = null;
		br = new BufferedReader(new FileReader(path.toFile()));
		
		String line;
		while((line = br.readLine()) != null) {
			if(line.charAt(0) != '>') continue;
			line = line.substring(1);
			String id = line.split("\\s+")[0];
			fastaHeaders.put(id, line);
		}
		
		br.close();
		
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
