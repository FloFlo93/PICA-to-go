package univie.cube.PicaDesktop.miscellaneous;

import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import univie.cube.PicaDesktop.clustering.datatypes.COG;

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
		Path file = Files.createFile(outputPath);
		BufferedWriter writer = Files.newBufferedWriter(file);
		writer.write(content);
		writer.close();
	}
	
	public static Map<String, COG> getOrthogroupsFromFile(Path path) throws IOException {
		Map<String, String> cogMapStr = getFromFile(path);
		
		Map<String, COG> orthogroups = new HashMap<String, COG>();
		
		for(Map.Entry<String, String> cogStr : cogMapStr.entrySet()) {
			COG cog = COG.convertToCOG(cogStr.getValue(), cogStr.getKey());
			orthogroups.put(cogStr.getKey(), cog);
		} 
		
		return orthogroups;
	}
	
	public static Map<String, String> getFromFile(Path path) throws IOException {
		List<String> jsonStr = Files.readAllLines(path);
		String json = "";
		for(String str : jsonStr) json += str;
		Map<String, String> cogMapStr = Serialize.jsonToMap(json);
		return cogMapStr;
	}

	public static void writeOrthogroupsToFile(Map<String, COG> orthogroups, Path outputFile) throws IOException {
		Map<String, String> cogMapStr = new HashMap<String, String>();
		
		for(Map.Entry<String, COG> cogMap : orthogroups.entrySet()) {
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
