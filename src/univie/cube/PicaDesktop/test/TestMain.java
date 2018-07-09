package univie.cube.PicaDesktop.test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import univie.cube.PicaDesktop.archive.ZipCreator;

public class TestMain {
	
	public static void main(String[] args) throws FileNotFoundException, IOException {
		Path output = Paths.get("/home/florian/Schreibtisch/test.zip");
		List<Path> entriesToAdd = getAllDBs();
		ZipCreator.createZip(output, entriesToAdd);
	}
	
	private static List<Path> getAllDBs() throws IOException, RuntimeException {
		List<Path> allDBs = new ArrayList<Path>();
		
		Files.walk(Paths.get("/tmp/TRAIN_picadesktop2819099444589725339/input-clustering-dir7379852137350983558"))
		.filter(x -> x.toFile().isFile())
		.forEach(path -> allDBs.add(path));
		
		return allDBs;
	}
}








