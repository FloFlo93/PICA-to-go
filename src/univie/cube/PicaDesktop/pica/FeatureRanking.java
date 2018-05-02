package univie.cube.PicaDesktop.pica;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


import univie.cube.PicaDesktop.miscellaneous.CmdExecution;

public abstract class FeatureRanking {
	
	private Path modelFile;
	private Path outputResults;
	private Path picaExecutable;
	protected Map<String, String> representativeSequences;
	
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
	protected FeatureRanking(Map<String, String> representativeSequences, Path modelFile, Path outputResults, Path picaExecutable, String feature) throws IOException {
		this.modelFile = modelFile;
		this.outputResults = outputResults;
		this.picaExecutable = picaExecutable;
		this.representativeSequences = representativeSequences;
		this.outputFileNameRaw = feature + ".rank.raw.tsv";
		this.outputFileNameAnnotated = feature + ".rank.annotated.tsv";
	}
	
	public void runFeatureRanking() throws IOException, InterruptedException {
		String[] command = {picaExecutable.toString(), modelFile.toString(), "-o", outputResults.toString() + "/" + outputFileNameRaw}; 
		CmdExecution.execute(command, outputResults, "feature_ranking");
		
		List<String> ranks = Files.readAllLines(Paths.get(outputResults.toString(), outputFileNameRaw));
	
	String outputFeatureRank = annotate(ranks).stream().collect(Collectors.joining("\n"));
	Path outputPath = Files.createFile(Paths.get(outputResults.toString(), outputFileNameAnnotated));
	Files.write(outputPath, outputFeatureRank.getBytes());
	}
	
	
	private List<String> annotate(List<String> ranks) {
		List<String> ranksMap = ranks.stream()
										.skip(1)
										.limit(this.getLimitLines())
										.map(str -> Arrays.asList(str.split("\t")))
										.map(list -> newLine(list).stream().collect(Collectors.joining("\t")))
										.collect(Collectors.toList());
		return ranksMap;
	}
	
	private List<String> newLine(List<String> line) {
		List<String> newList = new ArrayList<String>(line);
		List<String> annotations = Arrays.asList(line.get(0).split(","));
		if(annotations.size() > this.getLimitFeaturesForGroup()) return newList;
		List<String> annotationResults = annotations.stream().map(this::getAnnotation).collect(Collectors.toList()); //never parallelize this (-> order will be destroyed, blocking from NCBI possible)
		String annotationResultsDelimited;
		if(annotationResults.stream().anyMatch(str -> str.length() != 0)) annotationResultsDelimited = annotationResults.stream().collect(Collectors.joining(","));
		else annotationResultsDelimited = "";
		newList.add(annotationResultsDelimited);
		return newList;
	}
	
	protected abstract String getAnnotation(String representativeSeq);
	
	protected abstract int getLimitLines();
	protected abstract int getLimitFeaturesForGroup();
	
}
