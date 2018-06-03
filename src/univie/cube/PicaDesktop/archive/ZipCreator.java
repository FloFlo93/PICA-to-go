package univie.cube.PicaDesktop.archive;


import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

//based on http://www.baeldung.com/java-compress-and-uncompress

public class ZipCreator {
	
	public static void createZip(Path pathToZipFile, List<Path> entries) throws IOException, FileNotFoundException {
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

    	LineIterator lineIt = FileUtils.lineIterator(path.toFile());
    	try {
    		while(lineIt.hasNext()) {
    			String l = lineIt.nextLine();
    			if(lineIt.hasNext()) l += '\n';
    			zipOutputStream.write(l.getBytes());
    		}
    	}
    	finally {
    		zipOutputStream.flush();
    		LineIterator.closeQuietly(lineIt);
    	}

    	zipOutputStream.closeEntry();
    }
	
}
