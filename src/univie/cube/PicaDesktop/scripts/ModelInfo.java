package univie.cube.PicaDesktop.scripts;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;

public class ModelInfo {

	private static Path rootDir = Paths.get("/home/florian/Studium/Master_Bioinformatics/4nd_semester/MasterThesis/pica2go/models");
	
	public static void main(String[] args) throws IOException {
		Files.walk(rootDir)
			.filter(path -> path.toString().contains(".rank"))
			.filter(path -> ! path.toString().contains(".rank."))
			.forEach(ModelInfo::printModified);
	}
	
	private static void printModified(Path path) {
		String modelName = path.getParent().getFileName().toString();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
		System.out.println(modelName + "\t" + sdf.format(path.toFile().lastModified()));
	}

}
