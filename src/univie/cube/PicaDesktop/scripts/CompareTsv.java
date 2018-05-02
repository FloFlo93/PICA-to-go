package univie.cube.PicaDesktop.scripts;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class CompareTsv {

	private static Path tsv1 = Paths.get("/home/florian/git/PICADesktop/output/penicillin_model/test/picadesktop4567954743963511839/newSeqClust7596653141853959767.tsv");
	private static Path tsv2 = Paths.get("/home/florian/git/PICADesktop/output/penicillin_model/train/picadesktop3320280790746150693/clustering/clu.tsv");
	
	/** compares two tsv files (representative sequences) generated from mmseqs database
	 * @throws IOException 
	 * 
	 * 
	 */
	public static void main(String[] args) throws IOException {
		List<String> lines1 = Files.readAllLines(tsv1);
		List<String> lines2 = Files.readAllLines(tsv2);
		
		List<String> repSeq1 = lines1.stream().map(str -> str.split("\t")[0]).collect(Collectors.toList());
		List<String> repSeq2 = lines2.stream().map(str -> str.split("\t")[0]).collect(Collectors.toList());

		System.out.println(repSeq1.size());
		repSeq1.retainAll(repSeq2);
		System.out.println(repSeq1.size());
	}

}
