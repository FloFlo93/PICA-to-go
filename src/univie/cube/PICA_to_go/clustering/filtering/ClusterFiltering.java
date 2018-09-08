package univie.cube.PICA_to_go.clustering.filtering;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;

import univie.cube.PICA_to_go.clustering.datatypes.BinCOGs;
import univie.cube.PICA_to_go.clustering.datatypes.COG;
import univie.cube.PICA_to_go.directories.WorkDir;
import univie.cube.PICA_to_go.miscellaneous.Serialize;
import univie.cube.PICA_to_go.pica.Pica;
import univie.cube.PICA_to_go.pica.PicaCrossvalidate;

public abstract class ClusterFiltering {
	
	Path picaInputBestCutoff = null;
	
	/**
	 * 
	 * @param orthogroups, datastructure will be modified (=filtered) 
	 * @param orthogroupsPerBin
	 * @param picaCrossVal
	 * @param pathToInputPhenotypes
	 * @param feature
	 * @param threads
	 * @return Pair of CrossValPerCutOff (first: crossValWithoutCutoff, second: crossValBestCutoff); orthogroups will also be modified 
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	public abstract Pair<CrossValPerCutOff, CrossValPerCutOff> filter(Map<String, COG> orthogroups, Map<String, BinCOGs> orthogroupsPerBin, Path pathToInputPhenotypes, String feature, int threads) throws IOException, InterruptedException, ExecutionException;
	
	protected void removeCOGs(Map<String, COG> orthogroups, int cutoff) {
		for(Map.Entry<String, COG> entry : orthogroups.entrySet()) {
			if(entry.getValue().getGenes().size() < cutoff) {
				COG cog = entry.getValue();
				cog.removeFromCurrentIndex();
			}
		}
	}
	
	protected void allCOGsToIndex(Map<String, COG> orthogroups) {
		for(Map.Entry<String, COG> entry : orthogroups.entrySet()) entry.getValue().setInCurrentIndex();
	}
	

	protected CrossVal picaCrossVal(Map<String, BinCOGs> orthogroupsPerBin, Path inputPhenotypes, String feature, ExecutorService es) throws IOException, InterruptedException {
		Path tmpDir = Files.createTempDirectory(WorkDir.getWorkDir().getTmpDir(), "picaCrossvalFiltering");
		Path picaInputFile = Pica.createInputPica(tmpDir, orthogroupsPerBin, "");
		PicaCrossvalidate pica = new PicaCrossvalidate(picaInputFile, tmpDir, inputPhenotypes, feature);
		CompletableFuture<Map<String, String>> future = CompletableFuture.supplyAsync(() -> pica.call(), es);
		future.whenComplete((task, throwable) -> {
			try {
				FileUtils.deleteDirectory(tmpDir.toFile());
			} catch (IOException e) {}
		});
		CrossVal crossVal = new CrossVal();
		crossVal.crossValJsonFuture = future;
		crossVal.inputPica = picaInputFile;
		return crossVal;
	}
	
	protected int getMaxClusterSize(Map<String, COG> orthogroups) {
		return orthogroups.entrySet().parallelStream().max(Map.Entry.comparingByValue()).get().getValue().getGenes().size();
	}
	
	/**
	 * 
	 * @param cutoffs
	 * @param orthogroups
	 * @param orthogroupsPerBin
	 * @param pathToInputPhenotypes
	 * @param picaCrossVal
	 * @param feature
	 * @param threads
	 * @return Pair of CrossValPerCutOff (first: crossValWithoutCutoff, second: crossValBestCutoff)
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @throws RuntimeException
	 */
	protected Pair<CrossValPerCutOff, CrossValPerCutOff> filterStartPICAThreads(List<Integer> cutoffs, Map<String, COG> orthogroups, Map<String, BinCOGs> orthogroupsPerBin, Path pathToInputPhenotypes, String feature, int threads) throws IOException, InterruptedException, ExecutionException, RuntimeException {
		CrossValPerCutOff bestCrossValCutoff = new CrossValPerCutOff();
		CrossValPerCutOff crossValWithoutCutoff = new CrossValPerCutOff();
		
		ExecutorService es = Executors.newWorkStealingPool(threads); 
		Runtime.getRuntime().addShutdownHook(new Thread() {
		    public void run() {
		        es.shutdownNow();
		    }
		});
		
		Map<Integer, CrossVal> picaThreads = new HashMap<Integer, CrossVal>();
		
		
		for(Integer cutoff : cutoffs) {
			CrossVal crossVal = filterAnCrossVal(orthogroups, orthogroupsPerBin, pathToInputPhenotypes, feature, es, cutoff);
			picaThreads.put(cutoff, crossVal);
		}
		
		
		for(Map.Entry<Integer, CrossVal> entry : picaThreads.entrySet()) {
			Double crossValTmp = Double.valueOf(entry.getValue().crossValJsonFuture.get().get("mean_balanced_accuracy"));
			if(crossValTmp == null) throw new RuntimeException("PICA crossvalidation in filtering step failed");
			if (crossValTmp > bestCrossValCutoff.crossval) {
				bestCrossValCutoff.crossval = crossValTmp;
				bestCrossValCutoff.cutoff = entry.getKey();
				bestCrossValCutoff.crossValJson = entry.getValue().crossValJsonFuture.get();
				bestCrossValCutoff.crossValJson.put("cluster-filtering-cutoff",entry.getKey().toString());
			}
			
			if(entry.getKey() == 0) {
				crossValWithoutCutoff.crossval = crossValTmp;
				crossValWithoutCutoff.cutoff = entry.getKey();
				crossValWithoutCutoff.crossValJson = entry.getValue().crossValJsonFuture.get();
				crossValWithoutCutoff.crossValJson.put("cluster-filtering-cutoff",entry.getKey().toString());
			}
		}
		
		//all COGs are filtered for the best cutoff(the filtering always has a global effect as orthogroups is a reference) 
		allCOGsToIndex(orthogroups);
		removeCOGs(orthogroups, bestCrossValCutoff.cutoff);
		
		//recalculation of best cutoff (as it may be biased)
		
		CrossVal crossValTmp = filterAnCrossVal(orthogroups, orthogroupsPerBin, pathToInputPhenotypes, feature, es, bestCrossValCutoff.cutoff);
		bestCrossValCutoff.crossValJson = crossValTmp.crossValJsonFuture.get();
		bestCrossValCutoff.crossval = Double.valueOf(bestCrossValCutoff.crossValJson.get("mean_balanced_accuracy"));
		
		es.shutdown();
		
		return Pair.of(crossValWithoutCutoff, bestCrossValCutoff);
	}
	
	private CrossVal filterAnCrossVal(Map<String, COG> orthogroups, Map<String, BinCOGs> orthogroupsPerBin, Path pathToInputPhenotypes, String feature, ExecutorService es, Integer cutoff) throws IOException, InterruptedException {
		allCOGsToIndex(orthogroups);
		removeCOGs(orthogroups, cutoff);
		CrossVal crossVal = picaCrossVal(orthogroupsPerBin, pathToInputPhenotypes, feature, es);
		return crossVal;
	}
	
	public static class CrossValPerCutOff {
		protected int cutoff;
		protected double crossval = 0;
		protected Map<String, String> crossValJson;
		public int getCutOff() {return cutoff;}
		public double getCrossVal() {return crossval;}
		@Override
		public String toString() {
			return Serialize.mapToJson(crossValJson);
		}
	}
	
	private static class CrossVal {
		protected Future<Map<String, String>> crossValJsonFuture;
		protected Path inputPica;
	}
}
