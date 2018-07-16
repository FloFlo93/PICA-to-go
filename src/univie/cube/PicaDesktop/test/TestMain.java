package univie.cube.PicaDesktop.test;

import java.io.FileNotFoundException;
import java.io.IOException;

import univie.cube.PicaDesktop.async.CallWhenAvailable;

public class TestMain {
	
	private static String x;
	

	
	public static void main(String[] args) throws FileNotFoundException, IOException {
		CallWhenAvailable.call(() -> x !=null, () -> System.out.println(x));
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		x = "bla";
	}
}








