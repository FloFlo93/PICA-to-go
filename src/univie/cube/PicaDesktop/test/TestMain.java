package univie.cube.PicaDesktop.test;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang3.tuple.Pair;

public class TestMain {
	

	
	public static void main(String[] args) {
		List<Integer> values = IntStream.range(0, 10000).boxed().collect(Collectors.toList()); 
		long b1 = System.nanoTime();
		int parallelism = ForkJoinPool.getCommonPoolParallelism();
		System.out.println(parallelism);
		List<Integer[]> allPairs = 
				IntStream.range(0, values.size())
							//.parallel()
							.boxed()
							.flatMap(i1 -> 
									IntStream.range(i1 + 1, values.size())
													//.parallel()
													.boxed()
													.map((i2) -> {
														Integer[] pair = new Integer[2];
														pair[0] = values.get(i1);
														pair[1] = values.get(i2);
														return pair;
													}))
							.collect(Collectors.toList()); 
		/* List<Pair<Integer, Integer>> result = new ArrayList<Pair<Integer, Integer>>();
		for(int i=0; i<values.size(); i++) {
			for(int a=i+1; a<values.size(); a++) {
				result.add(Pair.of(values.get(i), values.get(a)));
			}
		} */
		long b2 = System.nanoTime(); 
		System.out.println("ParallelStream took " + (b2-b1));
		
	}
}








