package univie.cube.PICA_to_go.scripts;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.math3.stat.inference.TTest;

import com.google.gson.Gson;

import univie.cube.PICA_to_go.miscellaneous.Serialize;
import univie.cube.PICA_to_go.statistic.CohensD;
import univie.cube.PICA_to_go.statistic.Stat;

public class CohensDForAllModels {
	
	private static List<Wrapper> getHMMERCrossVal(List<Path> models) throws IOException {
		
		return models.stream()
				.map((path) -> {
					try {
						return Files.walk(path)
									.filter(a -> a.toString().contains(".accuracy.json"))
									.map((anotherPath) -> {
										try {
											return Files.readAllLines(anotherPath).stream().collect(Collectors.joining("\n"));
										} catch (IOException e) {
											throw new RuntimeException(e);
										}
									})
									.map((str) -> {
										Gson gson = new Gson();
										Wrapper[] data = gson.fromJson(str, Wrapper[].class);
										Wrapper wrap = Arrays.asList(data).stream()
																.filter(i -> i.completeness.equals("1.0") && i.contamination.equals("0.0"))
																.findAny()
																.get();
										return wrap;
									})
									.findAny()
									.get();
					} catch (IOException e) {
						throw new RuntimeException();
					}
				})
				.collect(Collectors.toList());
	}
	
	private static List<Wrapper> getMMSeqsCrossVal(List<Path> models, String mode, boolean filtering) throws IOException{
		List<Path> crossValFilesTrain = models
				.stream()
				.map((path) -> {
					try {
						return Files.walk(path)
							.filter(anotherPath -> anotherPath.getParent().getFileName().toString().equals(mode))
							.filter((anotherPath) -> {
								if(!filtering) return anotherPath.toString().contains("crossvalNoFiltering.json") || anotherPath.toString().contains("pica-crossvalidation-no-filtering.json");
								else return anotherPath.toString().contains("crossval.json");
							})
							.findAny()
							.get();
					} catch (IOException e) {
						throw new RuntimeException(); 
					}
				})
				.collect(Collectors.toList()); 
		
		List<Wrapper> balancedAccuracyTrain = crossValFilesTrain
				.stream()
				.map(path -> {
					try {
						return Serialize.getFromFile(path);
					} catch (IOException e) {
						throw new RuntimeException();
					}
				})
				.map((map) -> {
						Wrapper wrapper = new Wrapper();
						wrapper.mean_balanced_accuracy = map.get("mean_balanced_accuracy"); 
						wrapper.stddev_balanced_accuracy = map.get("stddev_balanced_accuracy");
						return wrapper;
					})
				.collect(Collectors.toList());
		
		return balancedAccuracyTrain;
	}
	
	private static void calcAndPrintStat(List<String> modelNames, List<Wrapper> firstGroup, List<Wrapper> secondGroup, String mode, boolean upperCase) {
		List<Pair<Stat, Stat>> allStat = new ArrayList<Pair<Stat, Stat>>();
		for(int i=0; i<modelNames.size(); i++) {
			Pair<Stat, Stat> statPair;
			Stat firstStat = new Stat(Double.valueOf(firstGroup.get(i).mean_balanced_accuracy), Double.valueOf(firstGroup.get(i).stddev_balanced_accuracy), 50);
			Stat secondStat = new Stat(Double.valueOf(secondGroup.get(i).mean_balanced_accuracy), Double.valueOf(secondGroup.get(i).stddev_balanced_accuracy), 50);
			statPair = Pair.of(firstStat, secondStat);
			allStat.add(statPair);
		}
		
		//-----calc cohens d----------------------//
		
		CohensD cohensD = new CohensD(allStat);
		List<Double> indexCohenD = cohensD.calc();
		
		//----calc welch t-test-------------//
		
		List<Double> allPVal = new ArrayList<Double>();
		allStat.stream().forEach((pair) -> {
			TTest ttest = new TTest();
			double pVal = ttest.tTest(pair.getLeft(), pair.getRight());
			allPVal.add(pVal);
		});
		
		Map<String, String> results = new HashMap<String, String>();
		for(int i=0; i<modelNames.size(); i++) {
			String res = "";
			String key = modelNames.get(i);
			res += firstGroup.get(i).mean_balanced_accuracy +"\t";
			res += firstGroup.get(i).stddev_balanced_accuracy +"\t";
			res += secondGroup.get(i).mean_balanced_accuracy + "\t";
			res += secondGroup.get(i).stddev_balanced_accuracy + "\t";
			if(allPVal.get(i) > 0.05 && mode.equals("detail")) continue;
			if(allPVal.get(i) < 0.05 && allPVal.get(i) > 0.01) res += "*" + "\t";
			else if(allPVal.get(i) < 0.01 && allPVal.get(i) > 0.001) res += "**" + "\t";
			else if(allPVal.get(i) < 0.001) res += "***" + "\t";
			else res += "\t";
			if(allPVal.get(i) < 0.05 && mode.equals("detail")) {
				if(indexCohenD.get(i) > 0.2 && indexCohenD.get(i) < 0.5) res += "*" + "\t";
				else if(indexCohenD.get(i) > 0.5 && indexCohenD.get(i) < 0.8) res += "**" + "\t";
				else if(indexCohenD.get(i) > 0.8) res += "***" + "\t";
				else res += "\t";			 
				double rmd = Math.abs(Double.parseDouble(firstGroup.get(i).mean_balanced_accuracy) - Double.parseDouble(secondGroup.get(i).mean_balanced_accuracy));
				if(rmd > 0.04 && rmd < 0.10) res += "*\t";
				else if(rmd > 0.10 && rmd < 0.20) res += "**\t";
				else if(rmd > 0.20) res += "***\t";
				else res += "\t";
			}
			
			results.put(key, res);
		}
		System.out.println("MODEL\tMBA1\tSD1\tMBA2\tSD2\tsignificance\tCohensD\tRMD");
		results.entrySet().stream()
		.filter((val) -> {
			if(mode.equals("detail")) return true;
			else if(upperCase) return ! Character.isLowerCase(val.getKey().charAt(0));
			else return Character.isLowerCase(val.getKey().charAt(0));
		})
		.forEach((val) -> {
			System.out.print(val.getKey() + "\t");
			System.out.println(val.getValue());
		});
	}
	
	public static void main(String[] args) throws IOException {
		Path root = Paths.get("/home/florian/Studium/Master_Bioinformatics/4nd_semester/MasterThesis/pica2go/models/");
		
		List<Path> modelFiles = Files.walk(root, 1)
				.filter(path -> path.toFile().isDirectory())
				.filter(path -> ! path.getFileName().toString().equals("models"))
				.collect(Collectors.toList());
		
		//List<Wrapper> crossValHMMER = getHMMERCrossVal(modelFiles);
		
		List<Wrapper> balancedAccuracyTrainLinclust = getMMSeqsCrossVal(modelFiles, "train_linclust", false);
		
		List<Wrapper> balancedAccuracyTrainLinclust000 = getMMSeqsCrossVal(modelFiles, "train_linclust_MIN_SEQ_ID_0", false);

		//List<Wrapper> balancedAccuracyTrainFilter = getMMSeqsCrossVal(modelFiles, "train", true);
		
		
		
		//List<Wrapper> balancedAccuracyTrainLinclust = getMMSeqsCrossVal(modelFiles, "train_linclust");
		
		List<String> modelNames = modelFiles
										.stream()
										.map(path -> path.getFileName().toString())
										.collect(Collectors.toList());
									
		calcAndPrintStat(modelNames, balancedAccuracyTrainLinclust, balancedAccuracyTrainLinclust000 ,"detail", false);
		

	}
	
	public static Map<String, String> serializeFlat(Path path) {
		try {
			return Serialize.getFromFile(path);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(1);
		}
		return null;
	}

	private static class Wrapper {
		String completeness;
		String contamination;
		String stddev_balanced_accuracy;
		String mean_balanced_accuracy;
		@Override
		public String toString() {
			return completeness + "\t" + contamination + "\t" + stddev_balanced_accuracy + "\t" + mean_balanced_accuracy;
		}
	}
}

	
	
	
	
	
	
	
	
	
	
	
	
