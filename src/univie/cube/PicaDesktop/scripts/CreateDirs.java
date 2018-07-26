package univie.cube.PicaDesktop.scripts;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class CreateDirs {

	private static Path rootDir = Paths.get("/home/florian/Studium/Master_Bioinformatics/4nd_semester/MasterThesis/pica2go/models");
	
	public static void main(String[] args) throws IOException {
		List<Path> dirs = Files.walk(rootDir)
							.filter(path -> path.toString().contains("train_linclust_again"))
							.map(path -> Paths.get(path.toString(), "tmp"))
							.collect(Collectors.toList());
		for(Path dir : dirs) {
			Files.createDirectory(dir);
		}
	}
	
}
