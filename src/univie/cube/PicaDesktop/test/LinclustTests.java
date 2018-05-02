package univie.cube.PicaDesktop.test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;

import univie.cube.PicaDesktop.clustering.methods.Clustering;
import univie.cube.PicaDesktop.clustering.methods.LinclustClustering;
import univie.cube.PicaDesktop.clustering.methods.MMseqsClustering;
import univie.cube.PicaDesktop.miscellaneous.CmdExecution;

public class LinclustTests {
/* 
	public static void main(String[] args) throws IOException, InterruptedException, RuntimeException {
		Path inputClusteringDir = Paths.get("/home/florian/git/PICADesktop/precalc_prodigal");
		Path outputDir = Paths.get("/home/florian/git/PICADesktop/output/linclust_tests/e/");
		Path outputPicaDir = Paths.get("/home/florian/git/PICADesktop/output/linclust_tests/e/pica");
		
		Path tmpDirClust = Files.createTempDirectory("clust");
		tmpDirClust.toFile().deleteOnExit();
		try {
			String copyFaFilesCommand = "cp *fa " + tmpDirClust.toAbsolutePath().toString() + "/";
			System.out.println(copyFaFilesCommand);
			CmdExecution.executePipedSubprocess(copyFaFilesCommand, inputClusteringDir.toFile());
			
			Clustering clustering = new LinclustClustering(tmpDirClust, outputDir);
			String[] addOptions = {"-e", "0.0"};
			clustering.runClustering(4, addOptions);
			clustering.preparePicaInput(outputPicaDir, "picaInput_" + "0.0");
			FileUtils.deleteDirectory(tmpDirClust.toFile());
		}
		catch(Exception e) {
			FileUtils.deleteDirectory(tmpDirClust.toFile());
		}
		
 
	} */

}
