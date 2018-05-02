package univie.cube.PicaDesktop.test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import univie.cube.PicaDesktop.clustering.methods.Clustering;
import univie.cube.PicaDesktop.clustering.methods.LinclustClustering;
import univie.cube.PicaDesktop.clustering.methods.MMseqsClustering;
import univie.cube.PicaDesktop.directories.WorkDir;
import univie.cube.PicaDesktop.miscellaneous.CmdExecution;
import univie.cube.PicaDesktop.pica.Pica;

//start PICA from genes (no prodigal)

public class PICAFromGenes {
	
	private final static Path picaCrossvalidateExecutable = Paths.get("libs/PICA/py/crossvalidate.py");
	
	private static Path outputResult = Paths.get("/home/florian/Studium/Master_Bioinformatics/4nd_semester/MasterThesis/pica2go/symbiont_model/result_mmseqs_cluster_default");
	private static Path inputGenes = Paths.get("/home/florian/Studium/Master_Bioinformatics/4nd_semester/MasterThesis/pica2go/symbiont_model/genes/");
	
	public static void main(String[] args) throws IOException, InterruptedException {
		String[] subdirs = {"clustering", "pica"};
		WorkDir workDir = new WorkDir(subdirs, true, outputResult);
		String[] copyCommand = {"cp", "-r", inputGenes.toAbsolutePath().toString() + "/.", workDir.getTmpDir().getAbsolutePath() + "/clustering"};
		CmdExecution.execute(copyCommand);
		
		
		Clustering clustering = new LinclustClustering(Paths.get(workDir.getTmpDir().getAbsolutePath() + "/clustering"), outputResult);
		
		long start = System.nanoTime();
		
		clustering.runClustering(4);
		
		long end = System.nanoTime();
		
		System.out.print("time needed for clustering: ");
		System.out.println((end - start)*1.0 / 1000000000 / 60);
		
		clustering.preparePicaInput(Paths.get(workDir.getTmpDir().getAbsolutePath() + "/pica"), "inputPica3");
		
		Pica pica = new Pica(Paths.get(workDir.getTmpDir().getAbsolutePath() + "/pica/" + "inputPica2"), picaCrossvalidateExecutable, outputResult, Paths.get("/home/florian/Studium/Master_Bioinformatics/4nd_semester/MasterThesis/pica2go/symbiont_model/symbiont.phenotype"));
		pica.crossValPica("SYMBIONT");
	}
}
