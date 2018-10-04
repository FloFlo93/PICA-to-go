package univie.cube.PICA_to_go.pica;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import univie.cube.PICA_to_go.directories.WorkDir;
import univie.cube.PICA_to_go.global.Config;
import univie.cube.PICA_to_go.miscellaneous.CmdExecution;
import univie.cube.PICA_to_go.miscellaneous.Serialize;
import univie.cube.PICA_to_go.out.logging.CustomLogger;

public abstract class FeatureRanking {
	
	private Path modelFile;
	private Path outputResults;
	//protected Map<String, String> representativeSequences;
	
	private String outputFileNameRaw;
	private String outputFileNameAnnotated;
	
	/**
	 * 
	 * @param representativeSequences
	 * @param modelFile
	 * @param outputResults
	 * @param picaExecutable
	 * @param workDir
	 * @throws IOException
	 */
	protected FeatureRanking(Path modelFile, Path outputResults, String feature) throws IOException {
		this.modelFile = modelFile;
		this.outputResults = outputResults;
		this.outputFileNameRaw = feature + ".rank.raw.tsv";
		this.outputFileNameAnnotated = feature + ".rank.annotated.tsv";
	}
	
	public void runFeatureRanking() throws IOException, InterruptedException {
		CustomLogger.getInstance().log(CustomLogger.LoggingWeight.INFO, "Feature ranking started");
		String[] command = {Config.getExecutablePaths().getPYTHON_PATH().toString(), Config.getExecutablePaths().getPICA_FEATURER().toString(), modelFile.toString(), "-o", outputResults.toString() + "/" + outputFileNameRaw}; 
		CmdExecution.execute(command, WorkDir.getWorkDir().getTmpDir(), "feature_ranking");
		
		List<String> ranks = Files.readAllLines(Paths.get(outputResults.toString(), outputFileNameRaw));
	
		String outputFeatureRank = "group_id\tscore\tclass\tdescription\n";
		outputFeatureRank += annotate(ranks).stream().collect(Collectors.joining("\n"));
		Serialize.writeToFile(Paths.get(outputResults.toString(), outputFileNameAnnotated), outputFeatureRank.getBytes());
	}
	
	
	private List<String> annotate(List<String> ranks) {
		List<String> ranksList = ranks.stream()
										.skip(1)
										.limit(this.getLimitLines())
										.map(str -> Arrays.asList(str.split("\t")))
										.map(list -> newLine(list).stream().collect(Collectors.joining("\t")))
										.collect(Collectors.toList());
		return ranksList;
	}
	
	private List<String> newLine(List<String> line) {
		List<String> newList = new ArrayList<String>(line);
		List<String> annotations = Arrays.asList(line.get(0).split(","));
		List<String> annotationResults = annotations.stream().map(arg0 -> {
			try {
				return getAnnotation(arg0);
			} catch (IOException | InterruptedException e) {
				throw new RuntimeException(e.getMessage());
			}
		}).collect(Collectors.toList()); //never parallelize this (-> order will be destroyed, blocking from NCBI possible)
		String annotationResultsDelimited;
		if(annotationResults.stream().anyMatch(str -> str.length() != 0)) annotationResultsDelimited = annotationResults.stream().collect(Collectors.joining(","));
		else annotationResultsDelimited = "";
		newList.add(annotationResultsDelimited);
		return newList;
	}
	
	protected abstract String getAnnotation(String representativeSeq) throws IOException, InterruptedException;
	
	protected abstract int getLimitLines();
	
}
