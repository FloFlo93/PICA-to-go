package univie.cube.PicaDesktop.pipelines;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.tuple.Pair;

import univie.cube.PicaDesktop.clustering.methods.MMseqsClustering;
import univie.cube.PicaDesktop.directories.WorkDir;
import univie.cube.PicaDesktop.fastaformat.FastaHeaders;
import univie.cube.PicaDesktop.miscellaneous.ClusteringProgram;
import univie.cube.PicaDesktop.miscellaneous.Serialize;
import univie.cube.PicaDesktop.pica.FeatureRanking;
import univie.cube.PicaDesktop.pica.FeatureRankingBlast;
import univie.cube.PicaDesktop.pica.FeatureRankingRefGenome;
import univie.cube.PicaDesktop.pica.PicaCrossvalidate;

//TODO: extend debug option (log files only in debug mode, useful ?)



public class TrainPipeline extends Pipeline {

	//depend on user input
	private Path inputBins;
	private Path inputPhenotypes;
	private Path outputResults;
	private boolean debugMode = false;
	private ClusteringProgram clusteringProgram;
	private int threads;
	private String feature;
	private Annotation annotation;
	private List<String> refGenomes;
	private boolean filterCOGs;
	private Path tmpDir = null;
	
	
	//predefined constant variables
	private static final String inputClusteringDirName = "clustering";
	private static final String inputPicaDirName = "pica";
	private static final String picaInputFileName = "picaInput";
	
	//temporary folder
	private WorkDir workDir = null;
	private Path inputClusteringDir;
	private Path inputPicaDir;
	
	public static enum Annotation {REFGENOMES, BLAST}
	
	
	public void startPipeline(String[] args) {
		
		
		
		//-----------------------INPUT-PARSING----------------------------------------------------------//
		
		
		inputArgsParsing(args);
		
		if(! filesExist(new Path[] {inputPhenotypes}, new Path[] {inputBins, outputResults})) {
			System.err.println("FATAL: One of the arguments [i, o, p] did not point to an existing file/folder");
			System.exit(2);
		}
		
		if(tmpDir != null && !filesExist(new Path[] {}, new Path[] {tmpDir})) {
			System.err.println("FATAL: tmp_dir did not point to an existing folder");
			System.exit(2);
		}
		
		try {
			if(! featureExists(inputPhenotypes, feature)) {
				System.err.println("FATAL: The feature you specified does not exist or your feature file is wrongly formatted");
				System.exit(3);
			}
		}
		catch(IOException | RuntimeException e) {
			e.printStackTrace();
			System.err.println("FATAL ERROR: Unknown cause");
			System.exit(4);
		}
		
		//----------------------CREATE-WORK-DIR--------------------------------------------------------//
		
		String[] workDirArg =  {inputClusteringDirName, inputPicaDirName};
		try {
			long folderSizeInputBins = Files.walk(inputBins)
											.filter(p -> p.toFile().isFile())
											.mapToLong(p -> p.toFile().length())
											.sum();
			workDir = new WorkDir(workDirArg, debugMode, tmpDir, folderSizeInputBins);
			inputClusteringDir = Paths.get(workDir.getTmpDir() + "/" + inputClusteringDirName);
			inputPicaDir = Paths.get(workDir.getTmpDir() + "/" + inputPicaDirName);
		} catch (IOException e) {
			System.err.println("tmp dir could not be created");
			e.printStackTrace();
			System.exit(5);
		} catch(RuntimeException e) {
			System.err.println("FATAL ERROR: " + e.getMessage());
			System.exit(6);
		}
				
		
		
		//----------GENE-FINDING-------------------------------------------------------------------------//
		
		try {
			inputFastaProcessing(threads, inputBins, inputClusteringDir, outputResults, debugMode, workDir);
		} catch (IOException e2) {
			e2.printStackTrace();
			System.exit(10);
		}

		
		//--------------CLUSTERING---------------------------------------------------------------------//
		
		


		java.sql.Timestamp timestamp2 = new java.sql.Timestamp(System.currentTimeMillis());
		System.out.println("start Clustering, starttime: " + timestamp2);
		
		clustering(clusteringProgram, inputClusteringDir, outputResults, annotation, debugMode, workDir, threads);
		
	    long timeClusteringEnd = System.nanoTime();
	    
	    

	    //-------------------FILTERING-----------------------------------------------------------//
	    
	    
	    if(filterCOGs) {
	    
		    Pair<String, String> crossValJson = filterCluster(orthogroups, orthogroupsPerBin, workDir, inputPhenotypes, feature, threads, debugMode, outputResults);
		    try {
				Serialize.writeToFile(Paths.get(outputResults.toString(), "crossval.json"), crossValJson.getRight());
				Serialize.writeToFile(Paths.get(outputResults.toString(), "crossvalNoFiltering.json"), crossValJson.getLeft());
		    } catch (IOException | RuntimeException e1) {
				if(debugMode) e1.printStackTrace();
				System.err.println("WARNING: crossval.json could not be generated in result folder");
			}
	    }

	    else {
	    	Path inputPica = null;
	    	Path tmpPicaCrossVal = null;
	    	try {
	    	tmpPicaCrossVal = Files.createTempDirectory(workDir.getTmpDir(), "pica-crossval");
	    	inputPica = Paths.get(tmpPicaCrossVal.toString(), "inputPica");
			MMseqsClustering.writePicaInputFile(orthogroupsPerBin, inputPica);
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(20);
			}
	    	PicaCrossvalidate picaCrossVal;
	    	Map<String, String> crossValResult = new HashMap<String, String>();
			try {
				picaCrossVal = new PicaCrossvalidate(inputPica, tmpPicaCrossVal, inputPhenotypes, feature, outputResults, workDir);
				crossValResult = picaCrossVal.call();
			} catch (IOException e1) {
				e1.printStackTrace();
				System.exit(21);
				//TODO: error message
			}
	    	String resultStr = Serialize.mapToJson(crossValResult);
	    	try {
				Serialize.writeToFile(Paths.get(outputResults.toString(), "crossval.json"), resultStr);
			} catch (IOException e) {
				e.printStackTrace();
				//TODO: System.err Warnung
			}
	    }
	    
	    //----------------ORTHOGROUPS-TO-FILE--------------------------------------------------//
	    
	    try {
			Serialize.writeOrthogroupsToFile(orthogroups, Paths.get(outputResults.toString() + "/" + "orthogroups.json"));
		} catch (IOException | RuntimeException e) {
			e.printStackTrace();
			System.err.println("WARNING: orthogroups.json file could not be generated");
		}
	    
	    
	    //---------------------------PICA---------------------------------------------------------------//
	    
	    
	    try {
			MMseqsClustering.writePicaInputFile(orthogroupsPerBin, Paths.get(inputPicaDir.toString(), picaInputFileName.toString()));
		} catch (FileNotFoundException | RuntimeException e) {
			e.printStackTrace();
			System.err.println("FATAL ERROR: Unknown cause");
			System.exit(7);
		}
	    
	    java.sql.Timestamp timestamp3 = new java.sql.Timestamp(System.currentTimeMillis());
		System.out.println("start Pica, starttime: " + timestamp3);
	    
		
		picaTrain(Paths.get(inputPicaDir.toString() + "/" + picaInputFileName), outputResults, inputPhenotypes, feature, workDir);
		
		long timePICAStart = System.nanoTime();
		
		System.out.println("PICA time: " + (timePICAStart-timeClusteringEnd));
		java.sql.Timestamp timestamp4 = new java.sql.Timestamp(System.currentTimeMillis());
		System.out.println("end program, time: " + timestamp4); 
		
		
		//----------------------FEATURE-RANKING-------------------------------------------------//
		
		try {
			FeatureRanking featureRanking;
			if(annotation == Annotation.BLAST)
				featureRanking = new FeatureRankingBlast(modelFile, outputResults, feature, super.clusteringInstance);
			else  {
				Map<String, String> fastaHeaders = FastaHeaders.getFastaHeaders(this.inputBins);
				featureRanking = new FeatureRankingRefGenome(modelFile, outputResults, feature, fastaHeaders, refGenomes, orthogroups);
			}
			featureRanking.runFeatureRanking();
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
			System.err.println("ERROR: Feature ranking failed");
		}
		
		
		//----------------------REMOVE-WORK-DIR-----------------------------------------------------//

		
		
		if(!debugMode) {
			boolean workDirRemoved = workDir.removeWorkDir();
			if(! workDirRemoved) System.out.println("WARNING: tmp directory could not be deleted");
		}
	}
	
	
	
	private void inputArgsParsing(String[] args) {
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
		clusteringOpt.setRequired(true);
		options.addOption(clusteringOpt);
		Option threadsOpt = new Option("t", true, "number of threads; default: maximal available threads on system");
		options.addOption(threadsOpt);
		Option featureOpt = new Option("f", true, "feature for pica model to consider; required"); //TODO: multiple options (second step), check if exists before starting pipeline
		featureOpt.setRequired(true);
		options.addOption(featureOpt);
		Option annotationOpt = new Option("a", true, "annotation of the feature rank groups {refgenome, blast}, default: refgenome");
		options.addOption(annotationOpt);
		Option refgenomesOpt = new Option("r", true, "filename of reference genomes, default: all genomes are considered as ref. genomes");
		refgenomesOpt.setArgs(Option.UNLIMITED_VALUES); //allows to add more than one (space seperated) values to this argument
		options.addOption(refgenomesOpt);
		Option filterCOGsOpt = new Option("filter", false, "Reduces number of COGs (by size limit) based on improvements of balanced accuracy in crossvalidation. Will increase accuracy");
		options.addOption(filterCOGsOpt);
		Option tmpDirOpt = new Option("tmp_dir", true, "Specify a directory for tmp files. By default /tmp/ will be used. All tmp files are are deleted when program terminates");
		options.addOption(tmpDirOpt);
		CommandLineParser parser = new DefaultParser();
		CommandLine cmd = null; //program will terminate if try block fails, so this is a save workaround
		try {
			cmd = parser.parse(options, args);
		} catch (ParseException | RuntimeException e) {
			e.printStackTrace();
			System.err.println("ERROR! Wrong command line options : \n");
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("pica-to-go train" , options );
			System.exit(1);
		}
		
		inputBins = Paths.get(cmd.getOptionValue("i"));
		inputPhenotypes = Paths.get(cmd.getOptionValue("p"));
		outputResults = Paths.get(cmd.getOptionValue("o"));
		debugMode = cmd.hasOption("d");
		feature = cmd.getOptionValue("f");
		try {annotation = Annotation.valueOf(cmd.getOptionValue("a").toUpperCase());}
		catch(RuntimeException e) {annotation = Annotation.REFGENOMES;}
		try {refGenomes = Arrays.asList(cmd.getOptionValues("r"));}
		catch(RuntimeException e) {refGenomes = null;}
		String threadsStr = cmd.getOptionValue("t");
		threads = parseThreads(threadsStr);

		String clusteringProgramStr = cmd.getOptionValue("c", "mmseqs_cluster").toUpperCase();
		
		//TODO: split try catch (default options for everything does not make sense when only a single argument is false)
		try {
			clusteringProgram = ClusteringProgram.valueOf(clusteringProgramStr);
		}
		catch(RuntimeException e) {
			System.err.println("Invalid option for parameter [-c]");
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("pica-to-go train" , options );
			System.exit(63);
		}
		
		filterCOGs = cmd.hasOption("filter");
		
		//System.out.println(cmd.getOptionValue("tmp_dir"));
		tmpDir = (cmd.getOptionValue("tmp_dir") != null) ? Paths.get(cmd.getOptionValue("tmp_dir")) : null;
		//System.out.println(tmpDir.toString());
	}
	
	public static class cmdArguments {
		
	} 
	

}
