package univie.cube.PicaDesktop.pipelines;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
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
import org.apache.commons.lang3.tuple.Pair;

import univie.cube.PicaDesktop.archive.Unzip;
import univie.cube.PicaDesktop.clustering.datatypes.BinCOGs;
import univie.cube.PicaDesktop.clustering.datatypes.COG;
import univie.cube.PicaDesktop.clustering.methods.MMSeqsClusterupdate;
import univie.cube.PicaDesktop.clustering.methods.MMseqsClustering;
import univie.cube.PicaDesktop.directories.WorkDir;
import univie.cube.PicaDesktop.miscellaneous.Serialize;
import univie.cube.PicaDesktop.pica.PicaTest;

public class PredictPipeline extends Pipeline {

	//constant
	private static final Path picaTestExecutable = Paths.get("libs/PICA/py/test.py");
	
	//depend on user input
	private Path dbFileZip;
	private Path modelFileZip;
	private Path inputFiles;
	private String featureName;
	private Path outputResults;
	private boolean debugMode;
	private int threads;
	
	private WorkDir workDir;
	
	@Override
	public void startPipeline(String[] args) {
		
		//-----------INPUT-PARSING--------------//
		
		inputArgsParsing(args);
		if(! filesExist(new Path[] {dbFileZip, modelFileZip}, new Path[] {inputFiles, outputResults})) {
			System.err.println("FATAL: One of the arguments [s, i, m, o] did not point to an existing file/folder");
			System.exit(2);
		}
		
		//TODO: test if feature exists (how?) 
		
		//-----------WorkDir-creation-----------//
		
		try {
			workDir = new WorkDir(new String[] {}, true, outputResults);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.err.println("FATAL ERROR: WorkDir could not be created");
			System.exit(1);
		}
		
		//TODO: check if modelFile is valid
		
		//--------UNZIPPING---------------------------------------------------//
		
		
		try {
			Unzip.unzip(dbFileZip, workDir.getTmpDir());
			Unzip.unzip(modelFileZip, workDir.getTmpDir());
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("FATAL ERROR: Model file and/or database file could not be unzipped");
			System.exit(2);
		}
		
		try {
			modelFile = Files.walk(workDir.getTmpDir())	.filter(path -> 	path.getFileName().toString().contains("rules.pica") 
																	   	&& ! path.getFileName().toString().contains("rules.pica."))
														.findFirst().get();
		} catch (IOException e1) {
			e1.printStackTrace();
			System.err.println("FATAL ERROR: No model file in zipped model file");
			System.exit(3);
		}
		
		//-------CLUSTERUPDATE------------------------------------------------//
		
		Pair<Map<String, COG>, Map<String, BinCOGs>> orthogroups_orthogroupsPerBin = null;
		try {
			MMSeqsClusterupdate mmseqsClusterupdate = new MMSeqsClusterupdate(inputFiles, workDir.getTmpDir(), outputResults);
			orthogroups_orthogroupsPerBin = mmseqsClusterupdate.clusterUpdate(threads);
		} catch (IOException | InterruptedException | RuntimeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.err.println("FATAL ERROR: MMSEQS Clusterupdate failed");
			System.exit(3);
		}
		
		Map<String, COG> orthogroups = orthogroups_orthogroupsPerBin.getLeft();
		Map<String, BinCOGs> orthogroupsPerBin = orthogroups_orthogroupsPerBin.getRight();
		try {
			Serialize.writeOrthogroupsToFile(orthogroups, Paths.get(outputResults.toString(), "orthogroups.json"));
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("WARNING: Orthogroups could not be written to file");
		}
		Path picaInput = Paths.get(workDir.getTmpDir().toString(), "inputPica");
		try {
			MMseqsClustering.writePicaInputFile(orthogroupsPerBin, picaInput);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.err.println("FATAL ERROR: inputFile for PICA could not be generated");
			System.exit(4);
		}
		
		//-----PICA-TEST--------------------------------------//
		
		PicaTest picaTest = new PicaTest(picaInput, picaTestExecutable, outputResults, featureName, modelFile);
		try {
			picaTest.call();
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("FATAL ERROR: PICA test.py failed");
			System.exit(5);
		}
		
	}
	
	private void inputArgsParsing(String[] args) {
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
		Option featureNameOpt = new Option("f", true, "name of feature to predict; required");
		featureNameOpt.setRequired(true);
		options.addOption(featureNameOpt);
		Option outputResultOpt = new Option("o", true, "path to directory for output; required");
		outputResultOpt.setRequired(true);
		options.addOption(outputResultOpt);
		Option debugOpt = new Option("d", false, "debug mode");
		options.addOption(debugOpt);
		Option threadsOpt = new Option("t", true, "number of threads, default: all available cores");
		options.addOption(threadsOpt);
		
		CommandLineParser parser = new DefaultParser();
		CommandLine cmd = null; //program will terminate if try block fails, so this is a save workaround
		try {
			cmd = parser.parse(options, args);
		} catch (ParseException | RuntimeException e) {
			e.printStackTrace();
			System.err.println("ERROR! Wrong command line options : \n");
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("pica-to-go predict" , options );
			System.exit(1);
		}
		
		dbFileZip = Paths.get(cmd.getOptionValue("s"));
		modelFileZip = Paths.get(cmd.getOptionValue("m"));
		inputFiles = Paths.get(cmd.getOptionValue("i"));
		featureName = cmd.getOptionValue("f");
		outputResults = Paths.get(cmd.getOptionValue("o"));
		debugMode = cmd.hasOption("d");
		String threadsStr = cmd.getOptionValue("t");
		threads = parseThreads(threadsStr);
	}
}
