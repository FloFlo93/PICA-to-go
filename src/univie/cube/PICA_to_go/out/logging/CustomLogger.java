package univie.cube.PICA_to_go.out.logging;

import java.sql.Timestamp;

public class CustomLogger {

	public enum LoggingWeight {INFO, WARNING, ERROR, FATAL};
	
	public void log(LoggingWeight weight, String message) {
		 String fullLogMessage = getSkeleton(weight) + message;
		 forwardLog(weight, fullLogMessage);
	}
	
	private String getSkeleton(LoggingWeight weight) {
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		return timestamp + " " + weight.toString() + ": ";
	}
	
	private void forwardLog(LoggingWeight weight, String fullLogMessage) {
		if(weight.equals(LoggingWeight.INFO) || weight.equals(LoggingWeight.WARNING)) System.out.println(fullLogMessage);
		else System.err.println(fullLogMessage);
	}
	
	//static methods (to get/initialize Singleton)
	
	private static CustomLogger CustomLoggerSingleton = null;
	
	public static CustomLogger getInstance() {
		if(CustomLogger.CustomLoggerSingleton == null) CustomLogger.CustomLoggerSingleton = new CustomLogger();
		return CustomLogger.CustomLoggerSingleton;
	}
}
