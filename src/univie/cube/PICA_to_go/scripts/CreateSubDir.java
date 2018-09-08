package univie.cube.PICA_to_go.scripts;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class CreateSubDir {

	public static void main(String[] args) throws IOException {
		Path rootDir = Paths.get("/home/florian/Studium/Master_Bioinformatics/4nd_semester/MasterThesis/pica2go/models");
		List<Path> allFiles = Files.walk(rootDir)
								.filter(p -> p.getFileName().toString().equals("proteome"))
								.collect(Collectors.toList());
								
		for(Path file : allFiles) {
			Files.move(file, file.resolveSibling("source"));
		}
	}

}
