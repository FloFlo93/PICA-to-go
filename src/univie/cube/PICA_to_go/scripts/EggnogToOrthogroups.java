package univie.cube.PICA_to_go.scripts;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import univie.cube.PICA_to_go.clustering.datatypes.GeneClust4Bin;
import univie.cube.PICA_to_go.clustering.datatypes.GeneCluster;
import univie.cube.PICA_to_go.miscellaneous.EggNOGFileParser;
import univie.cube.PICA_to_go.miscellaneous.Serialize;

//TODO: test program! 

/**
 * 
 * @author Florian Piewald, 2018 
 * 
 * Script takes eggnog output as input and outputs geneClusters.json
 *
 */

public class EggnogToOrthogroups {

	private static Path inputDir;
	private static Path outputFile;
	private static Path outputFilePicaInput;
	
	private static void inputArgsParsing(String[] args) throws FileNotFoundException {
		Options options = new Options();
		Option inputDirOpt = new Option("i", true, "path containing eggnog files (no other files should be in this path); required");
		inputDirOpt.setRequired(true);
		options.addOption(inputDirOpt);
		Option outputFileOpt = new Option("o", true, "output file with geneClusters in json format; required");
		outputFileOpt.setRequired(true);
		options.addOption(outputFileOpt);
		Option outputFilePicaInputOpt = new Option("p", true, "output file with input for pica; optional");
		outputFilePicaInputOpt.setRequired(false);
		options.addOption(outputFilePicaInputOpt);
		
		
		CommandLineParser parser = new DefaultParser();
		CommandLine cmd = null; //program will terminate if try block fails, so this is a save workaround
		try {
			cmd = parser.parse(options, args);
		} catch (ParseException e) {
			System.err.println("ERROR! Wrong command line options : \n");
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp(EggnogToOrthogroups.class.getSimpleName() , options );
			System.exit(1);
		}
		
		inputDir = Paths.get(cmd.getOptionValue("i"));
		outputFile = Paths.get(cmd.getOptionValue("o"));
		outputFilePicaInput = (cmd.getOptionValue("p") != null) ? Paths.get(cmd.getOptionValue("p")) : null;
		
		
		//TODO: check if file exists 
		if ( ! inputDir.toFile().exists() || ! inputDir.toFile().isDirectory() || ! outputFile.toFile().getParentFile().exists() || outputFile.toFile().isDirectory()) throw new FileNotFoundException();
		if(outputFilePicaInput != null && (! outputFilePicaInput.toFile().getParentFile().exists() || outputFilePicaInput.toFile().isDirectory())) throw new FileNotFoundException();
	}
	
	public static void main(String[] args) {
		try {
			inputArgsParsing(args);
		} catch (FileNotFoundException e) {
			System.err.println("ERROR: The paths [i] you specified does not point to a directory or the parent directory of the outputfile [o] does not exist or the name of the outputfile points to a directory");
			System.exit(1);
		}
		
		Map<String, GeneCluster> geneClusters;
		Map<String, GeneClust4Bin> geneClustersPerBin;
		try {
			EggNOGFileParser eggNOGFileParser = new EggNOGFileParser(inputDir);
			geneClusters = eggNOGFileParser.getgeneClusters();
			Serialize.writeGeneClustersToFile(geneClusters, outputFile);
			if(outputFilePicaInput != null) {
				geneClustersPerBin = eggNOGFileParser.getgeneClustersPerBin();
				String outputStr = ""; 
				for(Map.Entry<String, GeneClust4Bin> geneClustersSingleBin : geneClustersPerBin.entrySet()) outputStr += geneClustersSingleBin.getValue().toString() + "\n";
				Serialize.writeToFile(outputFilePicaInput, outputStr);
			}
		} catch (IOException e) {
			System.err.println("ERROR: Writing the results to the outputfile failed. Do you have permission to write on this directory?");
		}
	}
	
}