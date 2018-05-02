package univie.cube.PicaDesktop.clustering.comparison;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import univie.cube.PicaDesktop.clustering.datatypes.COG;

public class Jaccard {
	
	Map<String, COG> orthogroups1;
	Map<String, COG> orthogroups2;
	
	public Jaccard(Map<String, COG> orthogroups1, Map<String, COG> orthogroups2) {
		this.orthogroups1 = orthogroups1;
		this.orthogroups2 = orthogroups2;
	}
	
	/**
	 * 
	 * @return key = name of the two orthogroups seperated by whitespace, left one corresponds to orthogroup1, right one to orthogroup2; value = score
	 */
	public Map<String, Double> forEachCalcJaccard() {
		Map<String, Double> jaccardIndex = new HashMap<String, Double>(); 
		for(Map.Entry<String, COG> entry1 : orthogroups1.entrySet()) {
			for(Map.Entry<String, COG> entry2 : orthogroups2.entrySet()) {
				double score = calcJaccardIndex(entry1.getValue().getGenes(), entry2.getValue().getGenes());
				if(score != 0) {
					String keyResult = entry1.getKey() + " " + entry2.getKey();
					jaccardIndex.put(keyResult, score);
				}
			}
		}
		return jaccardIndex;
	}
	
	
	private double calcJaccardIndex(Set<String> geneset1, Set<String> geneset2) {
		int intersection = compareTwoGenesets(geneset1, geneset2);
		int union = calcUnion(geneset1.size(), geneset2.size(), intersection);
		return ((double) intersection) / union; 
	}
	
	
	/**
	 * 
	 * @param geneset1
	 * @param geneset2
	 * @return identical genes in both sets (intersection = "Schnittmenge")
	 */
	private int compareTwoGenesets(Set<String> geneset1, Set<String> geneset2) {
		int numIdenticalGenes = 0;
		for (String gene : geneset1) if (geneset2.contains(gene)) ++numIdenticalGenes;
		return numIdenticalGenes;
	}
	
	private int calcUnion(int numGenes1, int numGenes2, int intersection) {
		return numGenes1 + numGenes2 - intersection;
	}
}
