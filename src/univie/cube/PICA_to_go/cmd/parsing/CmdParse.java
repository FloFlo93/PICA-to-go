package univie.cube.PICA_to_go.cmd.parsing;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import univie.cube.PICA_to_go.out.error.ErrorHandler;


public abstract class CmdParse {

	protected CommandLine cmdParseInit(String[] args) {

		Options options = initializeAllOptions();
		
		helpHook(args, options); //prints help section and terminates program if "-h" is used as parameter
		
		CommandLineParser parser = new DefaultParser();
		CommandLine cmd = null; //program will terminate if try block fails, so this is a save workaround
		try {
			cmd = parser.parse(options, args);
		} catch (ParseException | RuntimeException e) {
			(new ErrorHandler(e, ErrorHandler.ErrorWeight.FATAL, "Wrong command line options, get all options with '-h'")).handle();
		}
		return cmd;
	}
	
	protected abstract String getMode();
	protected abstract Options initializeAllOptions();
	
	protected void printHelpSection(Options options) {
		HelpFormatter formatter = new HelpFormatter();
		String mode = getMode();
		formatter.printHelp("pica-to-go " + mode , options );
	}
	
	//low level function, args has to be parsed directly, because only adding "-h" to the program causes a ParseException
	private void helpHook(String[] args, Options options) {
		if(args.length > 0 && args[0].equals("-h")) {
			printHelpSection(options);
			System.exit(0);
		}
	}
	
}
