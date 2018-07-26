package univie.cube.PicaDesktop.async;

import java.util.function.BooleanSupplier;

public class CallWhenAvailable {

	private static final int sleepTimeMillis = 500;
	
	public static void call(BooleanSupplier startCondition, Runnable callback) {
		exCallbackOnStartCond(startCondition, callback);
	}

	private static void exCallbackOnStartCond(BooleanSupplier startCondition, Runnable callback) {
		Thread t = new Thread() {
		    public void run() {
		    	while(true) {
			        if (! startCondition.getAsBoolean())
			        {
						try {
							Thread.sleep(sleepTimeMillis);
						} catch (InterruptedException e) {}
			        }
			        else {
			        	callback.run();
			        	break;
			        }
		    	}
		    }
		};
		t.start();
	}
	
}
