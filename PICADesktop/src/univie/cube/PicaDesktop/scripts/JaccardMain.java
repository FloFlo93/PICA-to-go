package univie.cube.PicaDesktop.scripts;

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

import univie.cube.PicaDesktop.clustering.comparison.Jaccard;
import univie.cube.PicaDesktop.clustering.datatypes.COG;
import univie.cube.PicaDesktop.miscellaneous.Serialize;

/**
 * 
 * @author florian piewald
 *	program to compare two clusterings
 */
public class JaccardMain {

	private static Path file1;
	private static Path file2; 
	private static Path outputFile;
	
	private static void inputArgsParsing(String[] args) throws FileNotFoundException {
		Options options = new Options();
		Option input1 = new Option("a", true, "orthogroups json File1; required");
		input1.setRequired(true);
		options.addOption(input1);
		Option input2 = new Option("b", true, "orthogroups json File2; required");
		input2.setRequired(true);
		options.addOption(input2);
		Option outputFileOpt = new Option("o", true, "outputFile; required");
		outputFileOpt.setRequired(true);
		options.addOption(outputFileOpt);
		
		
		CommandLineParser parser = new DefaultParser();
		CommandLine cmd = null; //program will terminate if try block fails, so this is a save workaround
		try {
			cmd = parser.parse(options, args);
		} catch (ParseException e) {
			System.err.println("ERROR! Wrong command line options : \n");
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("PICADesktop" , options );
			System.exit(1);
		}
		
		file1 = Paths.get(cmd.getOptionValue("a"));
		file2 = Paths.get(cmd.getOptionValue("b"));
		outputFile = Paths.get(cmd.getOptionValue("o"));
		
		//TODO: check if file exists 
		if ( ! file1.toFile().exists() || ! file2.toFile().exists() || ! outputFile.toFile().getParentFile().exists()) throw new FileNotFoundException();
	}
	
	public static void main(String[] args) {
		
		//---------------PARSING-OF-ARGUMENTS--------------------------------------------//
		
		try {
			inputArgsParsing(args);
		}
		catch(FileNotFoundException e) {
			System.err.println("ERROR: One of the paths [a, b] you specified does not point to a file or the parent directory of the outputfile [o] does not exist");
			System.exit(1);
		}
		
		//---------------JSON-TO-ORTHOGROUP-MAP------------------------------------------//
		
		Map<String, COG> orthogroups1 = null;
		Map<String, COG> orthogroups2 = null;
		
		try {
			orthogroups1 = Serialize.getOrthogroupsFromFile(file1);
			orthogroups2 = Serialize.getOrthogroupsFromFile(file2);
		}
		catch(IOException | RuntimeException e) {
			System.out.println("Unexpected Error");
			e.printStackTrace();
			System.exit(2);
		}
		
		Jaccard jaccard = new Jaccard(orthogroups1, orthogroups2);
		Map<String, Double> jaccardIndex = jaccard.forEachCalcJaccard();
		
		int countIdenticalCluster = 0;
		int countNearIdenticalCluster = 0;
		int countVerySimilarCluster = 0;
		int countSimilarCluster = 0;
		int countRelatedCluster = 0;
		int countDistantCluster = 0;
		int countUnrelatedCluster = 0;
		
		for(Map.Entry<String, Double> entry : jaccardIndex.entrySet()){
			if (entry.getValue() == 1) ++countIdenticalCluster;
			if (entry.getValue() > 0.95 && entry.getValue() != 1) ++countNearIdenticalCluster;
			if (entry.getValue() > 0.80 && entry.getValue() < 0.95) ++countVerySimilarCluster;
			if (entry.getValue() > 0.60 && entry.getValue() < 0.80) ++countSimilarCluster;
			if (entry.getValue() > 0.30 && entry.getValue() < 0.60) ++countRelatedCluster;
			if (entry.getValue() > 0.10 && entry.getValue() < 0.30) ++countDistantCluster;
			if (entry.getValue() < 0.10) ++countUnrelatedCluster;
		}
		
		System.out.println("[a] \t " + file1.toString());
		
		System.out.println("[b] \t " + file2.toString());
		
		System.out.println("number of clusters input [a]: \t" + orthogroups1.size());
		
		System.out.println("number of clusters input [b]: \t" + orthogroups2.size() + "\n");
		
		System.out.println("CLUSTER STATISTICS: \n");
		
		System.out.println("identical_cluster \t [1] \t" + countIdenticalCluster);
		System.out.println("nearly_identical_cluster \t [0.95, 1[ \t" + countNearIdenticalCluster);
		System.out.println("very_similar_cluster \t [0.80, 0.95] \t" + countVerySimilarCluster);
		System.out.println("similar_cluster \t [0.60, 0.80] \t" + countSimilarCluster);
		System.out.println("related_cluster \t [0.30, 0.60] \t" + countRelatedCluster);
		System.out.println("distant_cluster \t [0.10, 0.30] \t" + countDistantCluster);
		System.out.println("unrelated_cluster \t ]0, 0.10] \t" + countUnrelatedCluster);
		
		try {
			Serialize.writeJaccardIndexToFile(jaccardIndex, outputFile);
		} catch (IOException e) {
			System.err.println("ERROR: Writing the results to the outputfile failed. Do you have permission to write on this directory?");
			//e.printStackTrace();
		}
	}

}
