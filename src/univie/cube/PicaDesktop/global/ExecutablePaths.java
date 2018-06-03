package univie.cube.PicaDesktop.global;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import univie.cube.PicaDesktop.main.Main;

public class ExecutablePaths {

	private static ExecutablePaths executablePaths;
	public static void initialize(Path pathToConfig) throws IOException, IllegalArgumentException, IllegalAccessException {
		ExecutablePaths executablePathsTmp = new ExecutablePaths();
		Path jarDir = Paths.get(Main.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getParent();
		
		List<String> configContent = Files.readAllLines(pathToConfig);
		for(String line : configContent) {
			String[] lineSplit = line.split("=");
			try {
				Field f = executablePathsTmp.getClass().getField(lineSplit[0]);
				Object newVal;
				if (f.getType().equals(Path.class)) {
					newVal = Paths.get(jarDir.toString(), lineSplit[1].replaceAll("\"", ""));
				}
				else newVal = lineSplit[1].replaceAll("\"", "");
				f.set(executablePathsTmp, newVal);
			} catch (NoSuchFieldException e) {
				continue;
			}
		}
		
		if(executablePathsTmp.MMSEQS_VERSION.equals("AVX2")) executablePathsTmp.MMSEQS_EX =  executablePathsTmp.MMSEQS_AVX2;
		else if(executablePathsTmp.MMSEQS_VERSION.equals("SSE41")) executablePathsTmp.MMSEQS_EX = executablePathsTmp.MMSEQS_SSE41;
		
		boolean nullVar = Arrays.stream(executablePathsTmp.getClass().getFields()).anyMatch(f -> {
			try {
				return f.get(executablePathsTmp) == null;
			} catch (IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
				return true;
			}
		});
		if(nullVar) throw new RuntimeException("Variables in config file could not be resolved");
		
		ExecutablePaths.executablePaths = executablePathsTmp;
	}
	
	public static ExecutablePaths getExecutablePaths() {
		return executablePaths;
	}
	
	
	//Path to Executables
	public Path PICA_CROSSVAL;
	public Path PICA_TEST;
	public Path PICA_TRAIN;
	public Path PICA_FEATURER;
	
	public String MMSEQS_VERSION;
	public Path MMSEQS_AVX2;
	public Path MMSEQS_SSE41;
	public Path MMSEQS_EX;
	
	public Path PRODIGAL_EX;
}
