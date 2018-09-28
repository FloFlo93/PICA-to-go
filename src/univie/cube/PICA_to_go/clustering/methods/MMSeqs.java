package univie.cube.PICA_to_go.clustering.methods;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.mapdb.HTreeMap;
import org.mapdb.Serializer;

import univie.cube.PICA_to_go.clustering.datatypes.GeneClust4Bin;
import univie.cube.PICA_to_go.clustering.datatypes.GeneCluster;
import univie.cube.PICA_to_go.directories.WorkDir;
import univie.cube.PICA_to_go.fastaformat.FastaHeaders;
import univie.cube.PICA_to_go.global.Config;
import univie.cube.PICA_to_go.miscellaneous.CmdExecution;
import univie.cube.PICA_to_go.miscellaneous.CmdExecution.Status;

public abstract class MMSeqs {
	
	protected Map<String, GeneClust4Bin> geneClustersPerBin = null;
	protected HTreeMap<String, GeneCluster> geneClusters = null;

	protected HTreeMap<String, String> fastaHeaders;
	
	/**concats all *fa files to all.fa and renames the header before (filename will be added: filename_)
	 * 
	 * @param inputFolder
	 * @return
	 * @throws IOException
	 * @throws InterruptedException
	 */
	protected Path concatInputFiles(Path inputFolder) throws IOException, InterruptedException {
		Path outputFile = Files.createTempFile(inputFolder, "", ".fa.all");
		List<Path> listOfFiles = Files.list(inputFolder).filter(path -> !path.toString().substring(path.toString().length() - 7).equals(".fa.all")).collect(Collectors.toList());
		for(Path file : listOfFiles) {
			FastaHeaders.renameHeader(file);
			String command = "cat " + file.toString() + " >> " + outputFile.toString();
			CmdExecution.Status status = CmdExecution.executePipedSubprocess(command);
			CmdExecution.printIfErrorOccured(status);
			if(status.errorOccured) throw new RuntimeException();
			file.toFile().delete();
		}
		return outputFile;
	}

	
	protected Path concatDBs(Path newDB, Path oldDB, Path outputDir, Path outputLog) throws IOException, InterruptedException {
		Path concatDB = Paths.get(outputDir.toString(), "concatDB.mmseqs");
		Path concatDB_h = Paths.get(outputDir.toString(), "concatDB.mmseqs_h");
		String[] command_concatDB = {Config.getExecutablePaths().getMMSEQS_EX().toString(), "concatdbs", oldDB.toString(), newDB.toString(), concatDB.toString()}; //TODO: insert preserve keys if not work
		String[] command_concatDB_h = {Config.getExecutablePaths().getMMSEQS_EX().toString(), "concatdbs", oldDB.toString() + "_h", newDB.toString() + "_h", concatDB_h.toString()};
		Status status = CmdExecution.execute(command_concatDB, outputLog, "dbconcat");
		Status status_h = CmdExecution.execute(command_concatDB_h, outputLog, "dbconcat_h");
		if(status.errorOccured || status_h.errorOccured) throw new RuntimeException();
		return concatDB;
	}
	
	protected void parseClustOutput(Path clustOutputPath) throws FileNotFoundException, IOException {
		parseClustOutput(clustOutputPath, null);
	}
	
	protected void parseClustOutput(Path clustOutputPath, List<String> filterBin) throws FileNotFoundException, IOException {
		
		Stream<String> clustOutputStream = Files.lines(clustOutputPath);
		parsegeneClusters(clustOutputStream, filterBin);
		clustOutputStream = Files.lines(clustOutputPath); //necessary as stream gets consumed before
		parsegeneClustersPerBin(clustOutputStream, filterBin);
	}
	
	private void parsegeneClusters(Stream<String> clustOutputStream, List<String> filterBin) {
		HTreeMap<String, GeneCluster> geneClustersTmp = WorkDir.getWorkDir().getDB()
																.hashMap("geneClusters", Serializer.STRING, Serializer.JAVA)
																.create();
		
		clustOutputStream.forEach((String clustOutputLine) -> {
			String[] columns = clustOutputLine.split("\t");
			String cogName = columns[0];
			String cogMember = columns[1];
			if(filterBin != null && filterBin.stream().noneMatch(str -> str.equals(getBinNameFromString(cogMember)))) return;
			if(geneClustersTmp.get(cogName) == null) {
				GeneCluster geneCluster = new GeneCluster(cogName);
				geneCluster.addGenes(cogMember);
				geneClustersTmp.put(cogName, geneCluster);
			}
			else geneClustersTmp.get(cogName).addGenes(cogMember);
		});
		
		
		this.geneClusters = geneClustersTmp;
	}
	
	private void parsegeneClustersPerBin(Stream<String> clustOutputStream, List<String> filterBin) {
		if(geneClusters == null) parsegeneClusters(clustOutputStream, filterBin); //should not be called as parseClustOutput normally should call parsegeneClusters before this method is called
		
		Map<String, GeneClust4Bin> geneClustersPerBinTmp = new HashMap<String, GeneClust4Bin>();
		
		clustOutputStream.forEach((String clustOutputLine) -> {
			String[] columns = clustOutputLine.split("\t");
			String cogName = columns[0];
			String cogMember = columns[1];
			String binName = getBinNameFromString(cogMember);
			
			if(filterBin != null && filterBin.stream().noneMatch(str -> str.equals(getBinNameFromString(cogMember)))) return; 
			
			GeneCluster geneCluster = geneClusters.get(cogName);
			if(geneCluster == null) throw new RuntimeException("gene cluster could not be found! should not happen!");
			
			GeneClust4Bin geneClust4Bin = geneClustersPerBinTmp.get(binName);
			if(geneClust4Bin == null) {
				geneClust4Bin = new GeneClust4Bin(binName);
				geneClustersPerBinTmp.put(binName, geneClust4Bin);
			}
			geneClust4Bin.addGeneCluster(geneCluster);
		});
		
		this.geneClustersPerBin = geneClustersPerBinTmp;
	}
	
	private String getBinNameFromString(String str) {
		String[] strSplit = str.split("\\" + FastaHeaders.getBinGeneSeperator());
		return strSplit[0];
	}
}
