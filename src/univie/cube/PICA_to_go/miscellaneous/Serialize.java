package univie.cube.PICA_to_go.miscellaneous;

import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import univie.cube.PICA_to_go.clustering.datatypes.GeneCluster;
import univie.cube.PICA_to_go.out.logging.CustomLogger;

public class Serialize {
	
	public static Map<String, String> jsonToMap(String json) {
		Gson gson = new Gson();
		Map<String, String> map = new HashMap<String, String>();
		Type type = new TypeToken<Map<String, String>>(){}.getType();
		map = gson.fromJson(json, type);
		return map;
	}
	
	public static String mapToJson(Map<String, String> map) {
		Gson gson = new Gson();
		String json = gson.toJson(map);
		return json;
	}
	
	public static void writeToFile(Path outputPath, String content) throws IOException {
		outputPath = incrementNameIfFileExists(outputPath);
		Path file = Files.createFile(outputPath);
		BufferedWriter writer = Files.newBufferedWriter(file);
		writer.write(content);
		writer.close();
	}
	
	public static void writeToFile(Path outputPath, byte[] content) throws IOException {
		outputPath = incrementNameIfFileExists(outputPath);
		Path file = Files.createFile(outputPath);
		Files.write(file, content);
	}
	
	public static Path incrementNameIfFileExists(Path outputPath) {
		Path outputPathOriginal = outputPath;
		int counter = 0;
		while(outputPath.toFile().exists()) {
			outputPath = Paths.get(outputPathOriginal.toString() + "(" + counter + ")");
			++counter;
		}
		if(counter > 0) {
			String message = outputPathOriginal.getFileName().toString() + " already exists in the output directory, the name has been changed to " + outputPath.getFileName().toString(); 
			CustomLogger.getInstance().log(CustomLogger.LoggingWeight.WARNING, message);
		}
		return outputPath;
	}
	
	public static Map<String, GeneCluster> getgeneClustersFromFile(Path path) throws IOException {
		Map<String, String> cogMapStr = getFromFile(path);
		
		Map<String, GeneCluster> geneClusters = new HashMap<String, GeneCluster>();
		
		for(Map.Entry<String, String> cogStr : cogMapStr.entrySet()) {
			GeneCluster geneCluster = GeneCluster.convertToCOG(cogStr.getValue(), cogStr.getKey());
			geneClusters.put(cogStr.getKey(), geneCluster);
		} 
		
		return geneClusters;
	}
	
	public static Map<String, String> getFromFile(Path path) throws IOException {
		List<String> jsonStr = Files.readAllLines(path);
		String json = "";
		for(String str : jsonStr) json += str;
		Map<String, String> cogMapStr = Serialize.jsonToMap(json);
		return cogMapStr;
	}

	public static void writeGeneClustersToFile(Map<String, GeneCluster> geneClusters, Path outputFile) throws IOException {
		Map<String, String> cogMapStr = new HashMap<String, String>();
		
		for(Map.Entry<String, GeneCluster> cogMap : geneClusters.entrySet()) {
			String key = cogMap.getKey();
			String value = cogMap.getValue().getCogString();
			cogMapStr.put(key, value);
		}
		
		String json = Serialize.mapToJson(cogMapStr);
		writeToFile(outputFile, json);
	}
	
	public static void writeJaccardIndexToFile(Map<String, Double> jaccardIndex, Path outputFile) throws IOException{
		Map<String, String> inputJsonConversion = new HashMap<String, String>();
		for(Map.Entry<String, Double> entry : jaccardIndex.entrySet()) inputJsonConversion.put(entry.getKey(), entry.getValue().toString());
		String json = mapToJson(inputJsonConversion);
		writeToFile(outputFile, json);
	}
	
}
