package univie.cube.PicaDesktop.scripts;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class RenameFiles {

	private static Path dir = Paths.get("/home/florian/Studium/Master_Bioinformatics/4nd_semester/MasterThesis/pica2go/models/ARCHAEA/proteome");
	
	public static void main(String[] args) throws IOException {
		List<Path> filesToRename = Files.walk(dir)
										.filter(Files::isRegularFile)
										.filter(path -> ! path.toString().contains("."))
										.collect(Collectors.toList());
		
		for(Path path : filesToRename) {
			Path fileName = path.getFileName();
			String newFileName = fileName.toString().substring(0, fileName.toString().length()-1) + ".fna";
			Files.move(path, path.resolveSibling(newFileName));
		}
	}

}
