package univie.cube.PICA_to_go.archive;


import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import univie.cube.PICA_to_go.miscellaneous.Serialize;



public class ZipCreator {
	
	public static void createZip(Path pathToZipFile, List<Path> entries) throws IOException, FileNotFoundException {
		pathToZipFile = Serialize.incrementNameIfFileExists(pathToZipFile);
		ZipOutputStream zipOutputStream = createZipFolder(pathToZipFile);
		for(Path entry : entries) addEntry(entry, zipOutputStream);
		zipOutputStream.close();
	}

    private static ZipOutputStream createZipFolder(Path pathToZipFile) throws FileNotFoundException {
	    FileOutputStream fos = new FileOutputStream(pathToZipFile.toString());
	    return new ZipOutputStream(fos);
    }
    
    private static void addEntry(Path path, ZipOutputStream zipOutputStream) throws IOException {
    	if(path.toFile().isDirectory() || ! path.toFile().exists()) throw new FileNotFoundException("Does not exist or is not a file");
    	ZipEntry e = new ZipEntry(path.getFileName().toString());
    	zipOutputStream.putNextEntry(e);
    	
    	byte[] content = Files.readAllBytes(path);
    	zipOutputStream.write(content);
    	zipOutputStream.flush();
    	zipOutputStream.closeEntry();
    	
    }
	
}
