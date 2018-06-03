package univie.cube.PicaDesktop.test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import univie.cube.PicaDesktop.archive.ZipCreator;

public class TestMain {
	
	public static void main(String[] args) throws IOException  {
		Path zipDir = Paths.get("/home/florian/Schreibtisch/test.zip");
		List<Path> entries = Arrays.asList(Paths.get("/home/florian/Studium/Master_Bioinformatics/4nd_semester/MasterThesis/pica2go/models/ARCHAEA/train/picadesktop3985470021671301419/clustering/DB_input.mmseqs"), Paths.get("/home/florian/Studium/Master_Bioinformatics/4nd_semester/MasterThesis/pica2go/models/ARCHAEA/train/picadesktop3985470021671301419/clustering/DB_input.mmseqs_h"));
		ZipCreator.createZip(zipDir, entries);
	}
}





