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

import univie.cube.PICA_to_go.clustering.datatypes.BinCOGs;
import univie.cube.PICA_to_go.clustering.datatypes.COG;
import univie.cube.PICA_to_go.fastaformat.FastaHeaders;
import univie.cube.PICA_to_go.global.Config;
import univie.cube.PICA_to_go.miscellaneous.CmdExecution;
import univie.cube.PICA_to_go.miscellaneous.CmdExecution.Status;

public abstract class MMSeqs {
	
	protected Map<String, BinCOGs> orthogroupsPerBin = null;
	protected Map<String, COG> orthogroups = null;

	protected Map<String, String> fastaHeaders;
	
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
		String[] command_concatDB = {Config.getExecutablePaths().getMMSEQS_EX().toString(), "concatdbs", newDB.toString(), oldDB.toString(), concatDB.toString()};
		String[] command_concatDB_h = {Config.getExecutablePaths().getMMSEQS_EX().toString(), "concatdbs", newDB.toString() + "_h", oldDB.toString() + "_h", concatDB_h.toString()};
		Status status = CmdExecution.execute(command_concatDB, outputLog, "dbconcat");
		Status status_h = CmdExecution.execute(command_concatDB_h, outputLog, "dbconcat_h");
		if(status.errorOccured || status_h.errorOccured) throw new RuntimeException();
		return concatDB;
	}
	
	protected void parseClustOutput(Path clustOutputPath) throws FileNotFoundException, IOException {
		parseClustOutput(clustOutputPath, null);
	}
	
	protected void parseClustOutput(Path clustOutputPath, List<String> filterBin) throws FileNotFoundException, IOException {
		List<String> clustOutput = Files.readAllLines(clustOutputPath);
		
		parseOrthogroups(clustOutput, filterBin);
		parseOrthogroupsPerBin(clustOutput, filterBin);
	}
	
	private void parseOrthogroups(List<String> clustOutput, List<String> filterBin) {
		Map<String, COG> orthogroupsTmp = new HashMap<String, COG>();
		
		for(String clustOutputLine : clustOutput) {
			String[] columns = clustOutputLine.split("\t");
			String cogName = columns[0];
			String cogMember = columns[1];
			if(filterBin != null && filterBin.stream().noneMatch(str -> str.equals(getBinNameFromString(cogMember)))) continue;
			if(orthogroupsTmp.get(cogName) == null) {
				COG cog = new COG(cogName);
				cog.addGenes(cogMember);
				orthogroupsTmp.put(cogName, cog);
			}
			else orthogroupsTmp.get(cogName).addGenes(cogMember);
		}
		
		this.orthogroups = orthogroupsTmp;
	}
	
	private void parseOrthogroupsPerBin(List<String> clustOutput, List<String> filterBin) {
		if(orthogroups == null) parseOrthogroups(clustOutput, filterBin); //should not be called as parseClustOutput normally should call parseOrthogroups before this method is called
		Map<String, BinCOGs> orthogroupsPerBinTmp = new HashMap<String, BinCOGs>();
		
		for(String clustOutputLine : clustOutput) {
			String[] columns = clustOutputLine.split("\t");
			String cogName = columns[0];
			String cogMember = columns[1];
			String binName = getBinNameFromString(cogMember);
			
			if(filterBin != null && filterBin.stream().noneMatch(str -> str.equals(getBinNameFromString(cogMember)))) continue; 
			
			COG cog = orthogroups.get(cogName);
			if(cog == null) throw new RuntimeException("orthogroup could not be found! should not happen!");
			
			BinCOGs binCOGs = orthogroupsPerBinTmp.get(binName);
			if(binCOGs == null) {
				binCOGs = new BinCOGs(binName);
				orthogroupsPerBinTmp.put(binName, binCOGs);
			}
			binCOGs.addCOG(cog);
		}
		this.orthogroupsPerBin = orthogroupsPerBinTmp;
	}
	
	private String getBinNameFromString(String str) {
		String[] strSplit = str.split("\\" + FastaHeaders.getBinGeneSeperator());
		return strSplit[0];
	}
}
