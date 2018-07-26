package univie.cube.PicaDesktop.clustering.comparison.pipelines;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import univie.cube.PicaDesktop.clustering.comparison.ClusterComparisonInterface;
import univie.cube.PicaDesktop.clustering.comparison.JaccardIndex;
import univie.cube.PicaDesktop.clustering.datatypes.COG;
import univie.cube.PicaDesktop.miscellaneous.Serialize;

/**
 * 
 * @author florian piewald
 *	program to compare two clusterings
 */
public class JaccardPipeline extends ClusterComparisonInterface {
	
	@Override
	public void startPipeline(String[] args) {
		
		//---------------PARSING-OF-ARGUMENTS--------------------------------------------//
		
		try {
			inputArgsParsing(args);
		}
		catch(FileNotFoundException e) {
			System.err.println("ERROR: One of the paths [a, b] you specified does not point to a file or the parent directory of the outputfile [o] does not exist");
			System.exit(1);
		}
		
		//---------------JSON-TO-ORTHOGROUP-MAP------------------------------------------//
		
		
		JaccardIndex jaccard = new JaccardIndex(getOrthogroups1(), getOrthogroups2());
		List<Double> jaccardIndex = jaccard.forEachCalcJaccard();
		
		/* 
		try {
			Serialize.writeToFile(outputFile, jaccardIndex.stream().map(num -> num.toString()).collect(Collectors.joining(",")));
		} catch (IOException e) {
			System.err.println("ERROR: Writing the results to the outputfile failed. Do you have permission to write on this directory?");
		} */
		
		int[] clusterDist = new int[10];
		
		
		
		for(Double entry : jaccardIndex){
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
		}
		
		System.out.println("[a] \t " + file1.toString());
		
		System.out.println("[b] \t " + file2.toString());
		
		System.out.println("number of clusters input [a]: \t" + getOrthogroups1().size());
		
		System.out.println("number of clusters input [b]: \t" + getOrthogroups2().size() + "\n");
		
		System.out.println("CLUSTER STATISTICS: \n");
		
		for(int i=0; i<clusterDist.length; i++) {
			System.out.println(clusterDist[i]);
		}
		
	}

}
