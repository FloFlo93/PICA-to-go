package univie.cube.PICA_to_go.out.error;

import univie.cube.PICA_to_go.out.debug.DebugMode;
import univie.cube.PICA_to_go.out.logging.CustomLogger;
import univie.cube.PICA_to_go.out.logging.CustomLogger.LoggingWeight;

public class ErrorHandler {
	public static enum ErrorWeight {WARNING, ERROR, FATAL};
	
	private ErrorWeight weight;
	private Exception exception;
	private String message;
	
	public ErrorHandler(Exception exception, ErrorWeight weight, String message) {
		this.exception = exception;
		this.weight = weight;
		this.message = message;
	}
	
	public void handle() {
		//stack trace is only printed if debug mode is on and if debugMode is initialized yet (is not initialized if used in Main class)
		if(DebugMode.getInstance() != null && DebugMode.getInstance().isDebugMode())
			this.exception.printStackTrace();
		LoggingWeight logging_weight = LoggingWeight.valueOf(this.weight.toString());
		CustomLogger.getInstance().log(logging_weight, this.message);
		if(weight.equals(ErrorWeight.FATAL)) System.exit(1);
	}
	
}
