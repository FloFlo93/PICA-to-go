package univie.cube.PicaDesktop.clustering.comparison;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang3.tuple.Pair;

import univie.cube.PicaDesktop.clustering.datatypes.COG;

public class RandIndex {
	
	Map<String, COG> orthogroups1;
	Map<String, COG> orthogroups2;
	
	public RandIndex(Map<String, COG> orthogroups1, Map<String, COG> orthogroups2) {
		this.orthogroups1 = orthogroups1;
		this.orthogroups2 = orthogroups2;
	}
	
	public double calcRandIndex() {
		List<String> allGenes = getAllGenes(this.orthogroups1);
		//List<String> randGenes = getRandomGenes(allGenes, 5000);
		
		List<Pair<String, String>> allPairs = getAllPairs(allGenes);
		
		AtomicInteger a = new AtomicInteger(0); //same subset in both cluster sets
		AtomicInteger b = new AtomicInteger(0); //different subset in both cluster sets
		AtomicInteger cd = new AtomicInteger(0);; //in one different in the other same
		allPairs.parallelStream()
			.forEach((pair) -> {
				String gene1 = pair.getLeft();
				String gene2 = pair.getRight();
				Boolean sameSubsetInFirst = isInSameSubset(gene1, gene2, orthogroups1);
				Boolean sameSubsetInSecond = isInSameSubset(gene1, gene2, orthogroups2);
				if(sameSubsetInSecond == null || sameSubsetInFirst == null) return;
				else if(sameSubsetInFirst && sameSubsetInSecond) a.getAndIncrement();
				else if(!sameSubsetInFirst && !sameSubsetInSecond) b.getAndIncrement();
				else cd.getAndIncrement();
			}
			);
		
		//TODO: delete later
		System.out.println("a " + a);
		System.out.println("b " + b);
		System.out.println("cd " + cd);
		//
		
		return (a.get() + b.get() )/((a.get() + b.get() +cd.get())*1.0);
	}
	
	private Boolean isInSameSubset(String gene1, String gene2, Map<String, COG> orthogroups) {
		COG cogGene1 = orthogroups.entrySet().stream()
										.map(entry -> entry.getValue())
										.filter(cog -> cog.getGenes().contains(gene1))
										.findAny()
										.orElse(null);
		
		if(cogGene1 == null) {
			System.err.println(gene1 + " is not in subset at all");
			return null;
		}
		
		return cogGene1.getGenes().contains(gene2);
	}
	
	
	private List<Pair<String, String>> getAllPairs(List<String> genes) {
		List<Pair<String, String>> allPairs = 
				IntStream.range(0, genes.size())
							.parallel()
							.boxed()
							.flatMap(i1 -> 
									IntStream.range(i1 + 1, genes.size())
													.parallel()
													.boxed()
													.map(i2 -> Pair.of(genes.get(i1), genes.get(i2))))
							.collect(Collectors.toList());

		return allPairs; 
	}
	
	private List<String> getAllGenes(Map<String, COG> orthogroups) {
		return orthogroups.entrySet().stream()
									.parallel()
									.map(entryset -> entryset.getValue())
									.map(cog -> cog.getGenes())
									.flatMap(set -> set.stream())
									.collect(Collectors.toList());
	}

}











