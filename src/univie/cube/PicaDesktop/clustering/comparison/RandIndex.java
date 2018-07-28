package univie.cube.PicaDesktop.clustering.comparison;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.lang3.tuple.Pair;


public class RandIndex {

	List<Set<String>> firstClusterSet;
	List<Set<String>> secondClusterSet;
	
	private long a = 0;
	private long cd = 0; 
	private long n;
	
	private int threads;
	
	private ProcessedPairs processedPairs = new ProcessedPairs();
	
	public RandIndex(List<Set<String>> firstClusterSet, List<Set<String>> secondClusterSet) {
		this.firstClusterSet = firstClusterSet;
		this.secondClusterSet = secondClusterSet;
		this.n = countAllItems(firstClusterSet);
		int n_ = countAllItems(secondClusterSet);
		if(this.n != n_) System.err.println("WARNING: Different size of items in the cluster sets, rand index may not be valid: " + this.n + " vs " + n_);
	}
	
	public double calcRandIndex(int threads) {
		this.threads = threads;
		try {
			clusterComparisonSingleSet(firstClusterSet, secondClusterSet);
			clusterComparisonSingleSet(secondClusterSet, firstClusterSet);
		} catch (InterruptedException | ExecutionException e) {
			throw new RuntimeException(e);
		}
		long b= calcB(a, this.cd, this.n);
		/* System.out.println("a " + a);
		System.out.println("b " + b);
		System.out.println("cd " + cd);
		System.out.println("n " + n); */
		return ((a+b)*1.0)/(a+b+cd);
	}
	
	private long calcB(long a, long cd, long n) {
		long all = (n)*(n-1)/2;
		long b = all - a - cd;
		return b;
	}
	
	private int countAllItems(List<Set<String>> orthogroups) {
		return orthogroups.stream()
								.map(val -> val.size())
								.mapToInt(i -> i.intValue())
								.sum();
	}
	
	private void clusterComparisonSingleSet(List<Set<String>> orthogroupsUseCogs, List<Set<String>> orthogroupsAgainst) throws InterruptedException, ExecutionException {
		MultiThreadHandler handler  = new MultiThreadHandler(threads, orthogroupsUseCogs, orthogroupsAgainst);
		handler.run();
	}
	
	private class MultiThreadHandler {
		private int threads;
		private List<Set<String>> cogs;
		private int ratio;
		private List<Set<String>> orthogroupsAgainst;
		
		public MultiThreadHandler(int threads, List<Set<String>> cogs, List<Set<String>> orthogroupsAgainst) {
			this.threads = threads;
			this.cogs = cogs;
			this.orthogroupsAgainst = orthogroupsAgainst;
			this.ratio = getRatio();
		}
		
		public void run() {
			List<Future<Pair<Integer, Integer>>> allThreads = new ArrayList<Future<Pair<Integer, Integer>>>();
			ExecutorService executor = Executors.newFixedThreadPool(threads);
			for(int i=0; i<threads; i++) {
				List<Set<String>> sublist;
				if(i == threads-1) sublist = cogs.subList(ratio*(i), cogs.size());
				else sublist = cogs.subList(ratio*(i), ratio*(i+1));
				MultiThreadRandProcessing task = new MultiThreadRandProcessing(sublist, orthogroupsAgainst);
				Future<Pair<Integer, Integer>> future = executor.submit(task);
				allThreads.add(future);
			}
			
			
			for(Future<Pair<Integer, Integer>> future : allThreads) {
				
				try {
					Pair<Integer, Integer> result = future.get();
					RandIndex.this.a += result.getLeft();
					RandIndex.this.cd += result.getRight();
				} catch (InterruptedException | ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					throw new RuntimeException();
				}
			}
		}
		
		private int getRatio() {
			return cogs.size() / threads;
		}
	}
	
	private class MultiThreadRandProcessing implements Callable<Pair<Integer, Integer>> {

		private List<Set<String>> cogs;
		private List<Set<String>> orthogroupsAgainst;
		
		public MultiThreadRandProcessing(List<Set<String>> cogs, List<Set<String>> orthogroupsAgainst) {
			this.cogs = cogs;
			this.orthogroupsAgainst = orthogroupsAgainst;
		}
		
		@Override
		public Pair<Integer, Integer> call() {
			int a = 0;
			int cd = 0;
			for(Set<String> cog : cogs) {
				Integer[] a_cd = compareSingleClustToClustSet(cog, orthogroupsAgainst);
				if(a_cd == null) continue;
				a += a_cd[0];
				cd += a_cd[1];
			}
			return Pair.of(a, cd);
		}
		
	}
	
	private Integer[] compareSingleClustToClustSet(Set<String> cluster, List<Set<String>> orthogroups) {
		List<Pair<String, String>> pairs = getPairs(cluster);
		Integer[] a_cd = new Integer[2];
		a_cd[0] = 0;
		a_cd[1] = 0;
		for(Pair<String, String> pair : pairs) {
			String gene1 = pair.getLeft();
			String gene2 = pair.getRight();
			Boolean sameSubset = isInSameSubset(gene1, gene2, orthogroups);
			if(sameSubset == null) return null;
			else if(sameSubset) ++a_cd[0];
			else ++a_cd[1];
		}
		return a_cd;
	}
	
	private Boolean isInSameSubset(String gene1, String gene2, List<Set<String>> orthogroups) {
		if(processedPairs.isInSetAndAdd(gene1, gene2)) return null;
		Set<String> genesInCluster = null;
		for(Set<String> cog : orthogroups) {
			if (cog.contains(gene1)) {
				genesInCluster = cog;
				break;
			}
		}
		
		if(genesInCluster == null) {
			System.err.println(gene1 + " is not in subset at all");
			return null;
		}
		
		return genesInCluster.contains(gene2);
	}
	
	private List<Pair<String, String>> getPairs(Set<String> cluster) {
		List<String> genes = new ArrayList<String>(cluster);
		List<Pair<String, String>> pairs = new ArrayList<Pair<String, String>>();
		
		for(int i=0; i<genes.size(); i++) {
			for(int a=i+1; a<genes.size(); a++) {
				pairs.add(Pair.of(genes.get(i), genes.get(a)));
			}
		}
		return pairs;
	}
	
	private class ProcessedPairs {
		private Set<String> compared =  Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());

		public boolean isInSetAndAdd(String gene1, String gene2) {
			if(compared.contains(gene1 + gene2) || compared.contains(gene2 + gene1)) return true;
			else compared.add(gene1 + gene2);
			return false;
		}
		
	}
	
	
}
