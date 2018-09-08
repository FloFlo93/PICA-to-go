package univie.cube.PICA_to_go.global;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import univie.cube.PICA_to_go.main.Main;

public class Config {

	private static Config executablePaths;
	public static void initialize(Path pathToConfig) throws IOException, IllegalArgumentException, IllegalAccessException {
		Config executablePathsTmp = new Config();
		Path jarDir = Paths.get(Main.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getParent();
		
		List<String> configContent = Files.readAllLines(pathToConfig);
		for(String line : configContent) {
			String[] lineSplit = line.split("=");
			try {
				Field f = executablePathsTmp.getClass().getDeclaredField(lineSplit[0]);
				f.setAccessible(true);
				Object newVal = null;
				if (f.getType().equals(Path.class)) {
					newVal = Paths.get(jarDir.toString(), lineSplit[1].replaceAll("\"", ""));
				}
				else if(f.getType().equals(String.class)) 
					newVal = lineSplit[1].replaceAll("\"", "");
				else if(f.getType().equals(String[].class)) {
					newVal = lineSplit[1].replaceAll("\\[|\\]", "")
									.replaceAll("\"", "")
									.replaceAll("\\s+","")
									.split(",");
				}
				f.set(executablePathsTmp, newVal);
			} catch (NoSuchFieldException e) {
				continue;
			}
		}
		
		if(executablePathsTmp.MMSEQS_VERSION.equals("AVX2")) executablePathsTmp.MMSEQS_EX =  executablePathsTmp.MMSEQS_AVX2;
		else if(executablePathsTmp.MMSEQS_VERSION.equals("SSE41")) executablePathsTmp.MMSEQS_EX = executablePathsTmp.MMSEQS_SSE41;
		
		boolean nullVar = Arrays.stream(executablePathsTmp.getClass().getFields())
								.anyMatch(f -> {
									try {
										return f.get(executablePathsTmp) == null;
									} catch (IllegalArgumentException | IllegalAccessException e) {
										return true;
									}
								});
		if(nullVar) throw new RuntimeException("Variables in config file could not be resolved");
		
		Config.executablePaths = executablePathsTmp;
	}
	
	public static Config getExecutablePaths() {
		return executablePaths;
	}
	
	//TODO: change to this method and delete getExecutablePaths
	public static Config getInstance() {
		return executablePaths;
	}
	
	
	//Path to Executables and MMSeqs binary version
	private Path PICA_CROSSVAL;
	private Path PICA_TEST;
	private Path PICA_TRAIN;
	private Path PICA_FEATURER;
	
	private String MMSEQS_VERSION = System.getenv("MMSEQS_VERSION");
	private Path MMSEQS_AVX2;
	private Path MMSEQS_SSE41;
	private Path MMSEQS_EX;
	
	private Path PRODIGAL_EX;
	
	//MMSeqs properties

	private String MMSEQS_E;
	private String MMSEQS_C;
	private String MMSEQS_MIN_SEQ_ID;
	
	protected String[] ADD_ARG_MMSEQS_CLUSTER;
	protected String[] ADD_ARG_MMSEQS_LINCLUST;
	
	//PICA properties 
	private String[] ADD_ARG_PICA_TRAIN;
	private String[] ADD_ARG_PICA_TEST;
	private String[] ADD_ARG_PICA_CROSSVAL;
	
	
	public Path getPICA_CROSSVAL() {
		return PICA_CROSSVAL;
	}

	public Path getPICA_TEST() {
		return PICA_TEST;
	}

	public Path getPICA_TRAIN() {
		return PICA_TRAIN;
	}

	public Path getPICA_FEATURER() {
		return PICA_FEATURER;
	}

	public String getMMSEQS_VERSION() {
		return MMSEQS_VERSION;
	}

	public Path getMMSEQS_AVX2() {
		return MMSEQS_AVX2;
	}

	public Path getMMSEQS_SSE41() {
		return MMSEQS_SSE41;
	}

	public Path getMMSEQS_EX() {
		return MMSEQS_EX;
	}

	public Path getPRODIGAL_EX() {
		return PRODIGAL_EX;
	}

	public String getMMSEQS_E() {
		return MMSEQS_E;
	}

	public String getMMSEQS_C() {
		return MMSEQS_C;
	}

	public String getMMSEQS_MIN_SEQ_ID() {
		return MMSEQS_MIN_SEQ_ID;
	}

	public String[] getADD_ARG_MMSEQS_CLUSTER() {
		return ADD_ARG_MMSEQS_CLUSTER;
	}

	public String[] getADD_ARG_MMSEQS_LINCLUST() {
		return ADD_ARG_MMSEQS_LINCLUST;
	}

	public String[] getADD_ARG_PICA_TRAIN() {
		return ADD_ARG_PICA_TRAIN;
	}

	public String[] getADD_ARG_PICA_TEST() {
		return ADD_ARG_PICA_TEST;
	}

	public String[] getADD_ARG_PICA_CROSSVAL() {
		return ADD_ARG_PICA_CROSSVAL;
	}
}
