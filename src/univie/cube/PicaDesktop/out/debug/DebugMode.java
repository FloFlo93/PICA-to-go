package univie.cube.PicaDesktop.out.debug;

public class DebugMode {
	
	private boolean debugMode;
	
	private DebugMode(boolean debugMode) {
		this.debugMode = debugMode;
	}
	
	public boolean isDebugMode() {
		return this.debugMode;
	}
	
	//static methods to retrieve/initialize the Singleton
	
	private static DebugMode debugModeSingleton = null;
	
	public static DebugMode getInstance() {
		return debugModeSingleton;
	}
	
	public static void initializeDebugMode(boolean debugMode) {
		DebugMode.debugModeSingleton = new DebugMode(debugMode);
	}
}
