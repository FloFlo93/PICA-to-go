package univie.cube.PICA_to_go.cmd.parsing;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import univie.cube.PICA_to_go.cmd.arguments.PredictCmdArguments;
import univie.cube.PICA_to_go.out.logging.CustomLogger;


public class PredictCmdParse extends CmdParse {
	
	private final String MODE = "predict";
	
	private static PredictCmdParse instance = new PredictCmdParse();
	
	private PredictCmdParse() {}
	
	public static PredictCmdParse getInstance() {
		return instance;
	}
	
	@Override
	protected Options initializeAllOptions() {
		Options options = new Options();
		Option dbFileOpt = new Option("s", true, "path to clustering database; required");
		dbFileOpt.setRequired(true);
		options.addOption(dbFileOpt);
		Option modelFileOpt = new Option("m", true, "path to pica- model file; required");
		modelFileOpt.setRequired(true);
		options.addOption(modelFileOpt);
		Option inputFilesOpt = new Option("i", true, "path to directory with bins to predict; required");
		inputFilesOpt.setRequired(true);
		options.addOption(inputFilesOpt);
		Option featureNameOpt = new Option("f", true, "name of class to predict; required");
		featureNameOpt.setRequired(true);
		options.addOption(featureNameOpt);
		Option outputResultOpt = new Option("o", true, "path to directory for output; required");
		outputResultOpt.setRequired(true);
		options.addOption(outputResultOpt);
		Option debugOpt = new Option("d", false, "debug mode");
		options.addOption(debugOpt);
		Option threadsOpt = new Option("t", true, "number of threads, default: all available cores");
		options.addOption(threadsOpt);
		Option tmpDirOpt = new Option("tmpdir", true, "Specify a directory for tmp files. By default /tmp/ will be used. All tmp files are are deleted when program terminates (except if debug mode is turned on)");
		options.addOption(tmpDirOpt);
		Option translationTableOpt = new Option("transtable", true, "Specify a translation table (default: 11)");
		options.addOption(translationTableOpt);
		Option helpOpt = new Option("h", false, "print help section");
		options.addOption(helpOpt);
		return options;
	}
	
	public PredictCmdArguments inputArgsParsing(String[] args) {
		CustomLogger.getInstance().log(CustomLogger.LoggingWeight.INFO, "Starting to parse commandline arguments");
		CommandLine cmd = super.cmdParseInit(args);
		return predictCmdArgumentsFactory(cmd);
	}
	
	

	private PredictCmdArguments predictCmdArgumentsFactory(CommandLine cmd) {
		PredictCmdArguments predictCmdArguments = new PredictCmdArguments();
		predictCmdArguments.setDbFileZip(cmd.getOptionValue("s"));
		predictCmdArguments.setModelFileZip(cmd.getOptionValue("m"));
		predictCmdArguments.setInputBins(cmd.getOptionValue("i"));
		predictCmdArguments.setFeatureName(cmd.getOptionValue("f"));
		predictCmdArguments.setOutputResults(cmd.getOptionValue("o"));
		predictCmdArguments.setDebugMode(cmd.hasOption("d"));
		predictCmdArguments.setThreads(cmd.getOptionValue("t"));
		predictCmdArguments.setTmpDir(cmd.getOptionValue("tmpdir"));
		predictCmdArguments.setTranslationTable(cmd.getOptionValue("transtable", "11"));
		return predictCmdArguments;
	}
	
	@Override
	protected void printHelpSection(Options options) {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("pica-to-go predict" , options );
	}

	@Override
	protected String getMode() {
		return MODE;
	}



	
}
