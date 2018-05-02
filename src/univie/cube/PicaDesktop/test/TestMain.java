package univie.cube.PicaDesktop.test;

import java.io.IOException;
import java.nio.file.Paths;

import univie.cube.PicaDesktop.fastaformat.FastaValidate;

public class TestMain {
	
	public static void main(String[] args) throws IOException {
		FastaValidate test = new FastaValidate(Paths.get("/home/florian/Schreibtisch/374463.fa"));
		System.out.println(test.getSequenceType()); 
		System.out.println(test.isHeaderUnique()); 
	}
}





