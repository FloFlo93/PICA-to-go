package univie.cube.PICA_to_go.miscellaneous;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import univie.cube.PICA_to_go.clustering.datatypes.BinCOGs;
import univie.cube.PICA_to_go.clustering.datatypes.COG;
import univie.cube.PICA_to_go.fastaformat.FastaHeaders;

public class EggNOGFileParser {
	
	private Path pathToDir;
	private Map<String, BinCOGs> orthogroupsPerBin;
	private Map<String, COG> orthogroups;
	
	public EggNOGFileParser(Path pathToDir) throws IOException {
		this.pathToDir = pathToDir;
		parse();
	}
	
	public Map<String, BinCOGs> getOrthogroupsPerBin() {
		return orthogroupsPerBin;
	}
	
	public Map<String, COG> getOrthogroups() {
		return orthogroups;
	}
	
	private void parse() throws IOException {
		parseOrthogroups();
		parseOrthogroupsPerBin();
	}
	
	private void parseOrthogroups() throws IOException {
		Map<String, COG> orthogroups = new HashMap<String, COG>();
		
		for(Path pathToFile : Files.walk(pathToDir).filter(Files::isRegularFile).collect(Collectors.toList())) orthogroupsFromSingleFile(pathToFile, orthogroups);
		this.orthogroups = orthogroups;
	}
	
	private void parseOrthogroupsPerBin() throws IOException {
		if (orthogroups == null) parseOrthogroups(); //this should never happen as the method parse should call parseOrthogroups before
		Map<String, BinCOGs> orthogroupsPerBin = new HashMap<String, BinCOGs>();
		
		for(Path pathToFile : Files.walk(pathToDir).filter(Files::isRegularFile).collect(Collectors.toList())) orthogroupsPerBinFromSingleFile(pathToFile, orthogroupsPerBin);
		this.orthogroupsPerBin = orthogroupsPerBin;
	}
	
	


	
	private void orthogroupsPerBinFromSingleFile(Path pathToFile, Map<String, BinCOGs> orthogroupsPerBin) throws IOException {
		String binName = getBinName(pathToFile);
		List<String> contentFile = Files.readAllLines(pathToFile);
		
		BinCOGs binCOGs = new BinCOGs(binName);
		
		for(String line : contentFile) {
			String[] column = line.split("\t");
			String eggnogID = column[1];
			COG cogTmp = orthogroups.get(eggnogID);
			binCOGs.addCOG(cogTmp);
		}
		orthogroupsPerBin.put(binName, binCOGs);
	}
	

	private void orthogroupsFromSingleFile(Path pathToFile, Map<String, COG> orthogroups) throws IOException {
		String binName = getBinName(pathToFile);
		List<String> contentFile = Files.readAllLines(pathToFile);
		
		for (String line : contentFile) {
			String[] column = line.split("\t");
			String geneName = binName + FastaHeaders.getBinGeneSeperator() + column[0];
			String eggnogID = column[1];
			COG cog = orthogroups.get(eggnogID);
			if(cog == null) {
				cog = new COG(eggnogID);
				orthogroups.put(eggnogID, cog);
			}
			cog.addGenes(geneName);
		}
	}
	
	/**
	 * 
	 * @param pathToFile
	 * @return bin name as string (if filename is identical to bin name, e.g. ERR410035.faa.out for bin ERR410035)
	 */
	private static String getBinName(Path pathToFile) {
		String fileName = pathToFile.toFile().getName();
		String binName = fileName.split("\\.")[0].isEmpty() ? fileName : fileName.split("\\.")[0];
		return binName;
	}
	
}
