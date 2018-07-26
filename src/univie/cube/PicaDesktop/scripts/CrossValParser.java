package univie.cube.PicaDesktop.scripts;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import univie.cube.PicaDesktop.miscellaneous.Serialize;

public class CrossValParser {

	private enum TrainFolder {train, train_linclust};
	private enum CaseModels {uppercase, lowercase};

	public static String[] crossValFiles = {"crossvalNoFiltering.json", "pica-crossvalidation-no-filtering.json"};
	
	public static Path rootDir = Paths.get("/home/florian/Studium/Master_Bioinformatics/4nd_semester/MasterThesis/pica2go/models");
	public static TrainFolder trainFolder = TrainFolder.train_linclust;
	public static CaseModels caseModels = CaseModels.lowercase;
	
	private static boolean testCase(String word) {
		if(caseModels.equals(CaseModels.lowercase)) return word.equals(word.toLowerCase());
		else return ! word.equals(word.toLowerCase());
	}
	
	public static void main(String[] args) throws IOException {
		List<Path> filesFilteredTrainMode = Files
											.walk(rootDir)
											.filter(path -> path.getParent().getFileName().toString().equals(trainFolder.toString())) //filter train mode
											.collect(Collectors.toList());
		
		
		List<Path> allCrossValFiles = filesFilteredTrainMode
											.stream()
											.filter(path -> path.toString().contains(crossValFiles[0]) || path.toString().contains(crossValFiles[1]))
											.filter(path -> testCase(path.getParent().getParent().getFileName().toString()))
											.collect(Collectors.toList());
		
		List<String> modelNames = allCrossValFiles
									.stream()
									.map(path -> path.getParent().getParent().getFileName().toString())
									.collect(Collectors.toList());
		
		List<String> balancedAccuracy = allCrossValFiles
											.stream()
											.map(path -> {
												try {
													return Serialize.getFromFile(path);
												} catch (IOException e) {
													throw new RuntimeException();
												}
											})
											.map(map -> map.get("mean_balanced_accuracy"))
											.collect(Collectors.toList());
		
		for(int i=0; i<modelNames.size(); i++) System.out.println(modelNames.get(i) + "\t" + balancedAccuracy.get(i));

	}

}
