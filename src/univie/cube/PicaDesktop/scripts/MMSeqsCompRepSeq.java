package univie.cube.PicaDesktop.scripts;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;

import univie.cube.PicaDesktop.clustering.filtering.ClusterFiltering;
import univie.cube.PicaDesktop.directories.WorkDir;
import univie.cube.PicaDesktop.miscellaneous.Serialize;

/** compares the key of json files and counts the identical ones (used to compare orthogroups.json)
 * 
 * @author Florian Piewald, 2018
 *
 */



//!!!!!!!!!!!! tsv files direkt vergleichen!!!!!!!!!!!
public class MMSeqsCompRepSeq {

	private static Path orthogroupsFile1 = Paths.get("/home/florian/git/PICADesktop/output/penicillin_model/test/orthogroups.json");
	private static Path orthogroupsFile2 = Paths.get("/home/florian/git/PICADesktop/output/penicillin_model/train/orthogroups.json");
	
	public static void main(String[] args) throws IOException {
		Map<String, String> orthogroupsFile1Map = Serialize.getFromFile(orthogroupsFile1);
		Map<String, String> orthogroupsFile2Map = Serialize.getFromFile(orthogroupsFile2);
		
		System.out.println(orthogroupsFile1Map.size());
		
		System.out.println(orthogroupsFile1Map.entrySet().stream().filter(i -> i.getValue().split(" ").length > 1).count());
		
		
		System.out.println();
		
		System.out.println(orthogroupsFile2Map.size());
		
		orthogroupsFile1Map.keySet().retainAll(orthogroupsFile2Map.keySet());
		
		System.out.println(orthogroupsFile1Map.size());
	}
	
}
