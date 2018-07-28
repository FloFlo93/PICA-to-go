package univie.cube.PicaDesktop.test;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import univie.cube.PicaDesktop.miscellaneous.Serialize;

public class RandIndexTestData {
	
	public static void main(String[] args) {
		RandIndexTestData test = new RandIndexTestData();
	}
	
	public RandIndexTestData() {
		try {
			this.initialize();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();  
		}
	}
	
	public List<TestSet> testSets = new ArrayList<TestSet>(); 
	
	private final Path testCasesPath = Paths.get("testdata/RandIndexTest/randindextest.json");
	
	private void initialize() throws IOException {
		
		List<String> lines = Files.readAllLines(testCasesPath);
		
		for(String line : lines) {
			Gson gson = new Gson();
			String[][][] rawLine = gson.fromJson(lines.get(0), String[][][].class);
			TestSet testSet = initializeSingleTestSet(rawLine);
			testSets.add(testSet);
		}
		
	}
	
	private TestSet initializeSingleTestSet(String[][][] rawLine) {
		TestSet testSet = new TestSet();
		
		for(int i=0; i<rawLine.length; i++) {
			List<Set<String>> clusterSet = new ArrayList<Set<String>>();
			for(int a=0; a<rawLine[i].length; a++) {
				Set<String> cluster = new HashSet<String>();
				for(int b=0; b<rawLine[i][a].length; b++) {
					cluster.add(rawLine[i][a][b]);
				}
				clusterSet.add(cluster);
			}
			if(i==0) {
				testSet.clusterA = clusterSet;
			}
			if(i==1) {
				testSet.clusterB = clusterSet;
			}
			if(i==2) {
				testSet.randIndex = clusterSet.get(0).stream().map(x -> Double.parseDouble(x)).findFirst().get();
			}
		}
		return testSet;
	}
	
	public static class TestSet {
		public List<Set<String>> clusterA;
		public List<Set<String>> clusterB;
		public double randIndex; 
	}
}
