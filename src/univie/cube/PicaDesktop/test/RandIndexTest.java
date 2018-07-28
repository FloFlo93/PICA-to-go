package univie.cube.PicaDesktop.test;


import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import univie.cube.PicaDesktop.clustering.comparison.RandIndex;

@RunWith(Parameterized.class)
public class RandIndexTest {
	private List<Set<String>> firstCluster;
	private List<Set<String>> secondCluster;
	private double expectedValue;

	private RandIndex randIndex;
	
	@Before
	public void initialize() {
		System.out.println("initialize called");
		this.randIndex = new RandIndex(firstCluster, secondCluster);
	}
	
	public RandIndexTest(List<Set<String>> firstCluster, List<Set<String>> secondCluster, double expectedValue) {
		this.firstCluster = firstCluster;
		this.secondCluster = secondCluster;
		this.expectedValue = expectedValue;
	}
	
	@Parameterized.Parameters
	public static Collection input() {
		RandIndexTestData data = new RandIndexTestData();
		List<RandIndexTestData.TestSet> testSets = data.testSets;
		
		Object[][] returnVal = new Object[testSets.size()][3];
		for(int i=0; i<testSets.size(); i++) {
			Object[] tmp = new Object[3];
			tmp[0] = testSets.get(i).clusterA;
			tmp[1] = testSets.get(i).clusterB;
			tmp[2] = testSets.get(i).randIndex;
			returnVal[i] = tmp;
		}
		return Arrays.asList(returnVal);
	}


	@Test
	public void provideIdenticalClusters() {
		assertEquals(expectedValue, this.randIndex.calcRandIndex(1), 0.01);
		assertEquals(expectedValue, this.randIndex.calcRandIndex(2), 0.01); //test with two threads
	}

}
