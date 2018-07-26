package univie.cube.PicaDesktop.scripts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class RandomJaccard {

	private static int clusterSize = 10;
	private static int numClusters = 50;
	
	public static void main(String[] args) {
		Map<Integer, List<Integer>> cluster_set1 = new HashMap<Integer, List<Integer>>();
		for(int i=0; i<numClusters; i++) {
			List<Integer> items = IntStream.range(clusterSize*i, clusterSize*(i+1)).boxed().collect(Collectors.toList());
			cluster_set1.put(i, items);
		}
		Map<Integer, List<Integer>> second_cluster_set =  generateSecondCluster(cluster_set1);
		//second_cluster_set.entrySet().stream().map(a -> a.getKey() + " " + a.getValue().toString()).forEach(System.out::println);;
		int[] jaccard_dist = forEachcalcJaccard(cluster_set1, second_cluster_set);
		Arrays.stream(jaccard_dist).forEach(System.out::println);
	}
	
	private static int[] forEachcalcJaccard(Map<Integer, List<Integer>> first_cluster_set, Map<Integer, List<Integer>> second_cluster_set) {
		List<Double> jaccard_all = first_cluster_set
											.entrySet()
											.parallelStream()
											.flatMap(a -> compareEntry1ToAllEntry2(a, second_cluster_set))
											.collect(Collectors.toList());
		double average = jaccard_all.stream()
							.sorted((a,b) -> Double.compare(b, a))
							.limit(numClusters)
							.mapToDouble(x->x)
							.average()
							.getAsDouble();
		
		System.out.println(average + "\n");
					
		
		int[] clusterDist = new int[11];
		for(Double entry : jaccard_all){
			if (entry > 0.90) ++clusterDist[0];
			if (entry > 0.80 && entry < 0.90) ++clusterDist[1];
			if (entry > 0.70 && entry < 0.80) ++clusterDist[2];
			if (entry > 0.60 && entry < 0.70) ++clusterDist[3];
			if (entry > 0.50 && entry < 0.60) ++clusterDist[4];
			if (entry > 0.40 && entry < 0.50) ++clusterDist[5];
			if (entry > 0.30 && entry < 0.40) ++clusterDist[6];
			if (entry > 0.20 && entry < 0.30) ++clusterDist[7];
			if (entry > 0.10 && entry < 0.20) ++clusterDist[8];
			if (entry > 0.00 && entry < 0.10) ++clusterDist[9];
			if (entry == 0) ++clusterDist[10];
		}
		return clusterDist;
	}
	
	private static Stream<Double> compareEntry1ToAllEntry2(Map.Entry<Integer, List<Integer>> entry1, Map<Integer, List<Integer>> second_cluster_set) {
		return second_cluster_set
					.entrySet()
					.stream()
					.map(entry2 -> calcJaccardIndex(entry1.getValue(), entry2.getValue()));
					//.filter(num -> num != 0);
	}
	
	private static Double calcJaccardIndex(List<Integer> entry1, List<Integer> entry2) {
		int intersection = getIntersection(entry1, entry2);
		int union = entry1.size() + entry2.size() - intersection;
		return ((double) intersection) / union; 
	}
	
	private static int getIntersection(List<Integer> entry1, List<Integer> entry2) {
		int numIdenticalItems = 0;
		for(Integer i : entry1) if(entry2.contains(i)) ++numIdenticalItems;
		return numIdenticalItems;
	}
	
	private static Map<Integer, List<Integer>> generateSecondCluster(Map<Integer, List<Integer>> firstClusterSet) {
		int randomIt = 2;
		Map<Integer, List<Integer>> secondClusterSet = generateDeepCopy(firstClusterSet);
		for(int i=0; i<randomIt; i++) {
			int[] randomPosSet1 = getRandomPosition();
			int[] randomPosSet2 = getRandomPosition();
			Integer val_1 = secondClusterSet.get(randomPosSet1[0]).get(randomPosSet1[1]);
			Integer val_2 = secondClusterSet.get(randomPosSet2[0]).get(randomPosSet2[1]);
			secondClusterSet.get(randomPosSet1[0]).set(randomPosSet1[1], val_2);
			secondClusterSet.get(randomPosSet2[0]).set(randomPosSet2[1], val_1);
		}
		return secondClusterSet;
	}
	
	private static int[] getRandomPosition() {
		int[] pos = new int[2];
		Random r = new Random();
		pos[0] =  r.ints(0, numClusters).limit(1).findFirst().getAsInt();
		pos[1] =  r.ints(0, clusterSize).limit(1).findFirst().getAsInt();
		return pos;
	}
	
	private static Map<Integer, List<Integer>> generateDeepCopy(Map<Integer, List<Integer>> cluster_set) {
		Map<Integer, List<Integer>> cluster_set_copy = new HashMap<Integer, List<Integer>>();
		for(Map.Entry<Integer, List<Integer>> entry : cluster_set.entrySet()) {
			Integer key = entry.getKey();
			List<Integer> value_copy = new ArrayList<Integer>(entry.getValue());
			cluster_set_copy.put(key, value_copy);
		}
		return cluster_set_copy;
	}
	
}
