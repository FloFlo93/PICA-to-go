package univie.cube.PICA_to_go.archive;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.GZIPInputStream;

/**
 * 
 * @author florian piewald
 * decompresses a gzip file (not to be confused with a zip archive)
 * based on: https://www.mkyong.com/java/how-to-decompress-file-from-gzip-file/ 
 */
public class GzipDecompress {
	
	public static Path decompressInDirectory(Path input, Path outputDirectory) throws IOException {
		String fileNameInput = input.getFileName().toString();
		String fileNameOutput = getDecompressedName(fileNameInput);
		Path outputFile = Paths.get(outputDirectory.toString(), fileNameOutput);
		decompress(input, outputFile);
		return outputFile;
	}
	
	private static String getDecompressedName(String fileName) {
		if (fileName.substring(fileName.length()-3, fileName.length()).equals(".gz")) return fileName.substring(0, fileName.length()-3);
		else return fileName;
	}
	
	private static void decompress(Path input, Path output) throws IOException{
		 
	     byte[] buffer = new byte[1024];
	 
    	 GZIPInputStream gzipStream = 
    		new GZIPInputStream(new FileInputStream(input.toString()));
 
    	 FileOutputStream outputStream = 
            new FileOutputStream(output.toString());
 
        int len;
        while ((len = gzipStream.read(buffer)) > 0) {
        	outputStream.write(buffer, 0, len);
        }
 
        gzipStream.close();
        outputStream.close();
	   } 
}
