package univie.cube.PicaDesktop.clustering.comparison;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;

import univie.cube.PicaDesktop.clustering.datatypes.COG;

public class RandIndexAlternative {

	Map<String, COG> orthogroups1;
	Map<String, COG> orthogroups2;
	
	private AtomicInteger aTwice = new AtomicInteger(0);
	private AtomicInteger cd = new AtomicInteger(0);
	private int n;
	
	private ProcessedPairs processedPairs = new ProcessedPairs();
	
	public RandIndexAlternative(Map<String, COG> orthogroups1, Map<String, COG> orthogroups2) {
		this.orthogroups1 = orthogroups1;
		this.orthogroups2 = orthogroups2;
		this.n = countAllGenes(orthogroups1);
		int n_ = countAllGenes(orthogroups2);
		if(this.n != n_) System.err.println("WARNING: Different size for orthogroup files, rand index may not be valid: " + this.n + " vs " + n_);
	}
	
	public double calcRandIndex() throws InterruptedException, ExecutionException {
		Runnable task1 = () -> clusterComparisonSingleSet(orthogroups1, orthogroups2);
		Runnable task2 = () -> clusterComparisonSingleSet(orthogroups2, orthogroups1);
		Future future1 = ForkJoinPool.commonPool().submit(task1);
		Future future2 = ForkJoinPool.commonPool().submit(task2);
		future1.get();
		future2.get();
		int a = aTwice.get() / 2;
		int b= calcB(a, this.cd.get(), this.n);
		System.out.println("a " + a);
		System.out.println("b " + b);
		System.out.println("cd " + cd);
		System.out.println("n " + n);
		return ((a+b)*1.0)/n;
	}
	
	private int calcB(int a, int cd, int n) {
		int all = (n)*(n-1)/2;
		int b = all - a - cd;
		return b;
	}
	
	private int countAllGenes(Map<String, COG> orthogroups) {
		return orthogroups.entrySet().stream()
								.map(entry -> entry.getValue())
								.map(val -> val.getGenes().size())
								.mapToInt(i -> i.intValue())
								.sum();
	}
	
	private void clusterComparisonSingleSet(Map<String, COG> orthogroupsUseCogs, Map<String, COG> orthogroupsAgainst) {
		System.out.println("clusterComparisonSingleSet started...");
		AtomicInteger counter = new AtomicInteger(0);
		List<COG> cogs = orthogroupsUseCogs.entrySet().stream().map(entry -> entry.getValue()).collect(Collectors.toList());
		cogs.stream()
			.forEach((cog) -> {
				System.out.println("progress " + counter.getAndIncrement()); 
				compareSingleClustToClustSet(cog, orthogroupsAgainst);
			});
	}
	
	private void compareSingleClustToClustSet(COG cluster, Map<String, COG> orthogroups) {
		System.out.println("Compare cluster " + cluster.getCOGName());
		List<Pair<String, String>> pairs = getPairs(cluster);
		pairs.stream()
			.forEach((pair) -> {
				String gene1 = pair.getLeft();
				String gene2 = pair.getRight();
				Boolean sameSubset = isInSameSubset(gene1, gene2, orthogroups);
				if(sameSubset == null) return;
				else if(sameSubset) aTwice.getAndIncrement();
				else cd.getAndIncrement();
		}
		);
	}
	
	private Boolean isInSameSubset(String gene1, String gene2, Map<String, COG> orthogroups) {
		if(processedPairs.isInSetAndAdd(gene1, gene2)) return null;
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
	
	private List<Pair<String, String>> getPairs(COG cluster) {
		List<String> genes = cluster.getGenes().stream().collect(Collectors.toList());
		List<Pair<String, String>> pairs = new ArrayList<Pair<String, String>>();
		
		for(int i=0; i<genes.size(); i++) {
			for(int a=i+1; a<genes.size(); a++) {
				pairs.add(Pair.of(genes.get(i), genes.get(a)));
			}
		}
		return pairs;
	}
	
	private static class ProcessedPairs {
		private Set<String> compared = new HashSet<String>();
		public boolean isInSetAndAdd(String gene1, String gene2) {
			if(compared.contains(gene1 + gene2) || compared.contains(gene2 + gene1)) return true;
			else compared.add(gene1 + gene2);
			return false;
		}
	}
	
	
}
