package univie.cube.PICA_to_go.miscellaneous;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import univie.cube.PICA_to_go.clustering.datatypes.GeneClust4Bin;
import univie.cube.PICA_to_go.clustering.datatypes.GeneCluster;
import univie.cube.PICA_to_go.fastaformat.FastaHeaders;

public class EggNOGFileParser {
	
	private Path pathToDir;
	private Map<String, GeneClust4Bin> geneClustersPerBin;
	private Map<String, GeneCluster> geneClusters;
	
	public EggNOGFileParser(Path pathToDir) throws IOException {
		this.pathToDir = pathToDir;
		parse();
	}
	
	public Map<String, GeneClust4Bin> getgeneClustersPerBin() {
		return geneClustersPerBin;
	}
	
	public Map<String, GeneCluster> getgeneClusters() {
		return geneClusters;
	}
	
	private void parse() throws IOException {
		parsegeneClusters();
		parsegeneClustersPerBin();
	}
	
	private void parsegeneClusters() throws IOException {
		Map<String, GeneCluster> geneClusters = new HashMap<String, GeneCluster>();
		
		for(Path pathToFile : Files.walk(pathToDir).filter(Files::isRegularFile).collect(Collectors.toList())) geneClustersFromSingleFile(pathToFile, geneClusters);
		this.geneClusters = geneClusters;
	}
	
	private void parsegeneClustersPerBin() throws IOException {
		if (geneClusters == null) parsegeneClusters(); //this should never happen as the method parse should call parsegeneClusters before
		Map<String, GeneClust4Bin> geneClustersPerBin = new HashMap<String, GeneClust4Bin>();
		
		for(Path pathToFile : Files.walk(pathToDir).filter(Files::isRegularFile).collect(Collectors.toList())) geneClustersPerBinFromSingleFile(pathToFile, geneClustersPerBin);
		this.geneClustersPerBin = geneClustersPerBin;
	}
	
	


	
	private void geneClustersPerBinFromSingleFile(Path pathToFile, Map<String, GeneClust4Bin> geneClustersPerBin) throws IOException {
		String binName = getBinName(pathToFile);
		List<String> contentFile = Files.readAllLines(pathToFile);
		
		GeneClust4Bin geneClust4Bin = new GeneClust4Bin(binName);
		
		for(String line : contentFile) {
			String[] column = line.split("\t");
			String eggnogID = column[1];
			GeneCluster cogTmp = geneClusters.get(eggnogID);
			geneClust4Bin.addGeneCluster(cogTmp);
		}
		geneClustersPerBin.put(binName, geneClust4Bin);
	}
	

	private void geneClustersFromSingleFile(Path pathToFile, Map<String, GeneCluster> geneClusters) throws IOException {
		String binName = getBinName(pathToFile);
		List<String> contentFile = Files.readAllLines(pathToFile);
		
		for (String line : contentFile) {
			String[] column = line.split("\t");
			String geneName = binName + FastaHeaders.getBinGeneSeperator() + column[0];
			String eggnogID = column[1];
			GeneCluster geneCluster = geneClusters.get(eggnogID);
			if(geneCluster == null) {
				geneCluster = new GeneCluster(eggnogID);
				geneClusters.put(eggnogID, geneCluster);
			}
			geneCluster.addGenes(geneName);
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
