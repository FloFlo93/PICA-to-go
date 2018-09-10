package univie.cube.PICA_to_go.archive;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


/**
 * 
 * @author florian piewald
 * based on https://www.mkyong.com/java/how-to-decompress-files-from-a-zip-file/ 
 *
 * decompresses a zip archive (not to be confused with gzipped files)
 */
public class Unzip {

	public static void unzip(Path source, Path destination) throws IOException {
		byte[] buffer = new byte[1024];
		ZipInputStream zis = new ZipInputStream(new FileInputStream(source.toFile()));
		ZipEntry zipEntry = zis.getNextEntry();
		while(zipEntry != null) {
			File file = Paths.get(destination.toString(), zipEntry.getName()).toFile();
			new File(file.getParent()).mkdirs();
			FileOutputStream fos = new FileOutputStream(file);
			int len;
			while((len = zis.read(buffer)) > 0) {
				fos.write(buffer, 0, len);
				fos.flush();
			}
			fos.close();
			zis.closeEntry();
			zipEntry = zis.getNextEntry();
		}
		zis.closeEntry();
		zis.close();
	}
	
}
