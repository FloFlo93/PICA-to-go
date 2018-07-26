package univie.cube.PicaDesktop.clustering.comparison.pipelines;

import java.io.FileNotFoundException;
import java.util.concurrent.ExecutionException;

import univie.cube.PicaDesktop.clustering.comparison.ClusterComparisonInterface;
import univie.cube.PicaDesktop.clustering.comparison.RandIndex;
import univie.cube.PicaDesktop.clustering.comparison.RandIndexAlternative;

public class RandPipeline extends ClusterComparisonInterface {
	
	public static void main(String[] args) {
		RandPipeline pipeline = new RandPipeline();
		pipeline.startPipeline(args);
	}

	public void startPipeline(String[] args) {
		try {
			inputArgsParsing(args);
		}
		catch(FileNotFoundException e) {
			System.err.println("ERROR: One of the paths [a, b] you specified does not point to a file or the parent directory of the outputfile [o] does not exist");
			System.exit(1);
		}
		RandIndexAlternative randIndex = new RandIndexAlternative(getOrthogroups1(), getOrthogroups2());
		double index = 0;
		try {
			index = randIndex.calcRandIndex();
		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(1);
		}
		System.out.println(index);
	}

}
