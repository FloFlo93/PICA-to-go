package univie.cube.PICA_to_go.cmd.parsing;


import java.util.Arrays;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import univie.cube.PICA_to_go.cmd.arguments.TrainCmdArguments;
import univie.cube.PICA_to_go.out.logging.CustomLogger;
import univie.cube.PICA_to_go.pica.Annotation;

public class TrainCmdParse extends CmdParse {
	
	private final String MODE = "train"; 
	
	private static TrainCmdParse instance = new TrainCmdParse();
	
	private TrainCmdParse() {}
	
	public static TrainCmdParse getInstance() {
		return instance;
	}
	
	public TrainCmdArguments inputArgsParsing(String[] args) {
		CustomLogger.getInstance().log(CustomLogger.LoggingWeight.INFO, "Starting to parse commandline arguments");
		CommandLine cmd = super.cmdParseInit(args);
		return trainCmdArgumentsFactory(cmd);
	}
	
	@Override
	protected Options initializeAllOptions() {
		Options options = new Options();
		Option input = new Option("i", true, "path to the folder, containing the bins (fasta-format); required");
		input.setRequired(true);
		options.addOption(input);
		Option output = new Option("o", true, "path to an existing folder for the results; required");
		output.setRequired(true);
		options.addOption(output);
		Option phenotype = new Option("p", true, "path to the phenotype file; required");
		phenotype.setRequired(true);
		options.addOption(phenotype);
		Option debugModeOpt = new Option("d", false, "debug mode (e.g. tmp dir in output folder); optional");
		options.addOption(debugModeOpt);
		Option clusteringOpt = new Option("c", true, "clustering program {mmseqs_linclust, mmseqs_cluster}; default: mmseqs_cluster");
		clusteringOpt.setRequired(false);
		options.addOption(clusteringOpt);
		Option threadsOpt = new Option("t", true, "number of threads; default: maximal available threads on system");
		options.addOption(threadsOpt);
		Option featureOpt = new Option("f", true, "feature for pica model to consider; required"); //TODO: multiple options (second step), check if exists before starting pipeline
		featureOpt.setRequired(true);
		options.addOption(featureOpt);
		Option annotationOpt = new Option("a", true, "annotation of the feature rank groups {refgenome, blast}, default: refgenome; warning: blast is done by a remote service to NCBI, may be slow");
		options.addOption(annotationOpt);
		Option refgenomesOpt = new Option("r", true, "filename of reference genomes, default: all genomes are considered as ref. genomes");
		refgenomesOpt.setArgs(Option.UNLIMITED_VALUES); //allows to add more than one (space seperated) values to this argument
		options.addOption(refgenomesOpt);
		Option filterCOGsOpt = new Option("filter", false, "Reduces number of COGs (by size limit) based on improvements of balanced accuracy in crossvalidation. Will increase accuracy");
		options.addOption(filterCOGsOpt);
		Option tmpDirOpt = new Option("tmpdir", true, "Specify a directory for tmp files. By default /tmp/ will be used. All tmp files are are deleted when program terminates");
		options.addOption(tmpDirOpt);
		Option limitLinesBlast = new Option("limit_bl", true, "Limit the number of features, for feature ranking with remote blast, to the most relevant ones {default: 10}; drastically affects the runtime of the program");
		options.addOption(limitLinesBlast);
		Option translationTableOpt = new Option("transtable", true, "Specify a translation table (default: 11)");
		options.addOption(translationTableOpt);
		Option helpOpt = new Option("h", false, "print help section");
		options.addOption(helpOpt);
		return options;
	}
	

	protected TrainCmdArguments trainCmdArgumentsFactory(CommandLine cmd) {
		TrainCmdArguments trainCmdArguments = new TrainCmdArguments();
		
		trainCmdArguments.setInputBins(cmd.getOptionValue("i"));
		trainCmdArguments.setInputPhenotypes(cmd.getOptionValue("p"));
		trainCmdArguments.setOutputResults(cmd.getOptionValue("o"));
		trainCmdArguments.setDebugMode(cmd.hasOption("d"));
		trainCmdArguments.setFeatureName(cmd.getOptionValue("f"));
		trainCmdArguments.setAnnotation(cmd.getOptionValue("a"), "a", Annotation.REFGENOMES);
		if (cmd.hasOption("r")) trainCmdArguments.setRefGenomes(Arrays.asList(cmd.getOptionValues("r")));
		else trainCmdArguments.setRefGenomes(null);
		trainCmdArguments.setThreads(cmd.getOptionValue("t"));
		trainCmdArguments.setClusteringProgram(cmd.getOptionValue("c", "mmseqs_cluster").toUpperCase(), "c");
		trainCmdArguments.setFilterCOGs(cmd.hasOption("filter"));
		trainCmdArguments.setTmpDir(cmd.getOptionValue("tmpdir"));
		trainCmdArguments.setLimitBlast(cmd.getOptionValue("limit_bl", "10"));
		trainCmdArguments.setTranslationTable(cmd.getOptionValue("transtable", "11"));
		return trainCmdArguments;
	}

	@Override
	protected String getMode() {
		return MODE;
	}
	
	
}
