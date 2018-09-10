package univie.cube.PICA_to_go.archive;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

/**
 * 
 * @author florian piewald
 *
 * checks if file is gzipped based on magic numbers 
 */
public class GzDetection {
	private static final byte firstByte = (byte) 0x1F;
	private static final byte secondByte = (byte) 0x8B;
	private static final byte thirdByte = (byte) 0x08;
	
	public static boolean isGzipped(Path path) throws IOException {
		InputStream inputStream = new FileInputStream(path.toString());
		BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
		bufferedInputStream.mark(3);
		
		boolean result = (byte) bufferedInputStream.read() == firstByte && (byte) bufferedInputStream.read() == secondByte && (byte) bufferedInputStream.read() == thirdByte;
		bufferedInputStream.reset();
		bufferedInputStream.close();
		return result;
	}
}
