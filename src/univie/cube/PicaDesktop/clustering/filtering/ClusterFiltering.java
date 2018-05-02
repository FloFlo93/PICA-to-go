package univie.cube.PicaDesktop.clustering.filtering;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
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

import univie.cube.PicaDesktop.clustering.datatypes.BinCOGs;
import univie.cube.PicaDesktop.clustering.datatypes.COG;
import univie.cube.PicaDesktop.clustering.methods.MMseqsClustering;
import univie.cube.PicaDesktop.directories.WorkDir;
import univie.cube.PicaDesktop.miscellaneous.Serialize;
import univie.cube.PicaDesktop.pica.PicaCrossvalidate;

public abstract class ClusterFiltering {
	/**
	 * 
	 * @param orthogroups
	 * @param orthogroupsPerBin
	 * @param picaCrossVal
	 * @param pathToInputPhenotypes
	 * @param feature
	 * @param threads
	 * @return Pair of CrossValPerCutOff (first: crossValWithoutCutoff, second: crossValBestCutoff)
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	public abstract Pair<CrossValPerCutOff, CrossValPerCutOff> filter(Map<String, COG> orthogroups, Map<String, BinCOGs> orthogroupsPerBin, Path picaCrossVal, Path pathToInputPhenotypes, String feature, int threads) throws IOException, InterruptedException, ExecutionException;
	

	
	protected WorkDir workDir;
	protected Path loggingDir;
	
	protected ClusterFiltering(WorkDir workDir, Path loggingDir) {
		this.workDir = workDir;
		this.loggingDir = loggingDir;
	}
	
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
	

	protected Future<Map<String, String>> picaCrossVal(Map<String, BinCOGs> orthogroupsPerBin, Path picaCrossVal, Path inputPhenotypes, String feature, ExecutorService es) throws IOException, InterruptedException {
		Path tmpDir = Files.createTempDirectory(workDir.getTmpDir(), "picaCrossvalFiltering");
		final String fileName = "picaCrossValInput";
		MMseqsClustering.writePicaInputFile(orthogroupsPerBin, Paths.get(tmpDir.toString(), fileName.toString()));
		Path inputPica = Paths.get(tmpDir.toString() + "/" + fileName);
		PicaCrossvalidate pica = new PicaCrossvalidate(inputPica, picaCrossVal, tmpDir, inputPhenotypes, feature, loggingDir);
		CompletableFuture<Map<String, String>> future = CompletableFuture.supplyAsync(() -> pica.call(), es);
		future.whenComplete((task, throwable) -> {
			try {
				FileUtils.deleteDirectory(tmpDir.toFile());
			} catch (IOException e) {}
		});
		return future;
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
	protected Pair<CrossValPerCutOff, CrossValPerCutOff> filterStartPICAThreads(List<Integer> cutoffs, Map<String, COG> orthogroups, Map<String, BinCOGs> orthogroupsPerBin, Path pathToInputPhenotypes, Path picaCrossVal, String feature, int threads) throws IOException, InterruptedException, ExecutionException, RuntimeException {
		CrossValPerCutOff bestCrossValCutoff = new CrossValPerCutOff();
		CrossValPerCutOff crossValWithoutCutoff = new CrossValPerCutOff();
		
		ExecutorService es = Executors.newWorkStealingPool(threads);
		Runtime.getRuntime().addShutdownHook(new Thread() {
		    public void run() {
		        es.shutdownNow();
		    }
		});
		
		Map<Integer, Future<Map<String, String>>> picaThreads = new HashMap<Integer, Future<Map<String, String>>>();
		
		for(Integer cutoff : cutoffs) {
			allCOGsToIndex(orthogroups);
			removeCOGs(orthogroups, cutoff);
			Future<Map<String, String>> future = picaCrossVal(orthogroupsPerBin, picaCrossVal, pathToInputPhenotypes, feature, es);
			picaThreads.put(cutoff, future);
		}
		
		
		for(Map.Entry<Integer, Future<Map<String, String>>> entry : picaThreads.entrySet()) {
			Double crossValTmp = Double.valueOf(entry.getValue().get().get("mean_balanced_accuracy"));
			if(crossValTmp == null) throw new RuntimeException("Unknown Error: PICA crossvalidation in filtering step failed");
			if (crossValTmp > bestCrossValCutoff.crossval) {
				bestCrossValCutoff.crossval = crossValTmp;
				bestCrossValCutoff.cutoff = entry.getKey();
				bestCrossValCutoff.crossValJson = entry.getValue().get();
				bestCrossValCutoff.crossValJson.put("cluster-filtering-cutoff",entry.getKey().toString());
			}
			
			if(entry.getKey() == 0) {
				crossValWithoutCutoff.crossval = crossValTmp;
				crossValWithoutCutoff.cutoff = entry.getKey();
				crossValWithoutCutoff.crossValJson = entry.getValue().get();
				crossValWithoutCutoff.crossValJson.put("cluster-filtering-cutoff",entry.getKey().toString());
			}
		}
		es.shutdown();
		
		allCOGsToIndex(orthogroups);
		removeCOGs(orthogroups, bestCrossValCutoff.cutoff);
		return Pair.of(crossValWithoutCutoff, bestCrossValCutoff);
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
	

	protected static class NumberClassifications {
		
		private long yes;
		private long no;
		
		public NumberClassifications(Path pathToInputPhenotypes, String feature) throws IOException {
			calcYesNo(pathToInputPhenotypes, feature);
		}
		
		private void calcYesNo(Path pathToInputPhenotypes, String feature) throws IOException {
			List<String> lines = Files.readAllLines(pathToInputPhenotypes);
			String[] allFeatures = lines.get(0).split("\t");
			int columnFeature = Arrays.asList(allFeatures).indexOf(feature); 
			
			yes = lines.stream().filter(obj -> obj.split("\t")[columnFeature].equals("YES")).count();
			no = lines.stream().filter(obj -> obj.split("\t")[columnFeature].equals("NO")).count();
		}
		
		public long getNumberYes() {
			return yes;
		}
		
		public long getNumberNo() {
			return no;
		}
	}
}
