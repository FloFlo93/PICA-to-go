package univie.cube.PicaDesktop.clustering.comparison;

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

import univie.cube.PicaDesktop.clustering.datatypes.COG;
import univie.cube.PicaDesktop.miscellaneous.Serialize;
import univie.cube.PicaDesktop.pipelines.Pipeline;

public abstract class ClusterComparisonInterface implements Pipeline {
	
	protected Path file1;
	protected Path file2; 
	
	private Map<String, COG> orthogroups1 = null;
	private Map<String, COG> orthogroups2 = null;
	private Integer threads = null;
	
	@Override
	public abstract void startPipeline(String[] args);
	
	protected void inputArgsParsing(String[] args) throws FileNotFoundException {
		Options options = new Options();
		Option input1 = new Option("a", true, "orthogroups json File1; required");
		input1.setRequired(true);
		options.addOption(input1);
		Option input2 = new Option("b", true, "orthogroups json File2; required");
		input2.setRequired(true);
		options.addOption(input2);
		Option threadNum = new Option("t", true, "thread number; default: all available threads");
		threadNum.setRequired(false);
		options.addOption(threadNum);
		
		
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
		threads= (cmd.getOptionValue("t")==null) ? null : Integer.parseInt(cmd.getOptionValue("t"));
		
		//TODO: check if file exists 
		if ( ! file1.toFile().exists() || ! file2.toFile().exists()) throw new FileNotFoundException();
	}
	
	protected Map<String, COG> getOrthogroups1() {
		orthogroups1 = initializeOrthogroups(orthogroups1, file1);
		return orthogroups1;
	}
	
	protected Map<String, COG> getOrthogroups2() {
		orthogroups2 = initializeOrthogroups(orthogroups2, file2);
		return orthogroups2;
	}
	
	private Map<String, COG> initializeOrthogroups(Map<String, COG> orthogroups, Path file) {
		if(orthogroups == null) {
			try {
				orthogroups = Serialize.getOrthogroupsFromFile(file);
			} catch (IOException e) {
				System.out.println("Unexpected Error");
				e.printStackTrace();
				System.exit(2);
			}
		}
		return orthogroups;
	}
	
	protected void enforceThreadLimitation() {
		
	}
}
