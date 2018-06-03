package univie.cube.PicaDesktop.fastaformat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Sets;

public class FastaValidate {
	
	private List<String> fileContent;
	private boolean headerUnique;
	private Path newFastaPath; //returns original path or new tmp file (if file had to be modified)
	
	private static Set<Character> protAlphabet = Sets.newHashSet('A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'K', 'L', 'M', 'N', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'Y', 'Z', 'X', '*', '-');
	private static Set<Character> nucAlphabet = Sets.newHashSet('A', 'T', 'K', 'M', 'B', 'V', 'C', 'N', 'S', 'W', 'D', '-', 'G', 'U', 'Y', 'R', 'H');

	public static enum SequenceType {PROTEIN, DNA, INVALIDPROTEIN};
	
	private SequenceType sequenceType;

	
	public FastaValidate(Path pathToFastaFile) throws IOException {
		fileContent = Files.readAllLines(pathToFastaFile);
		sequenceType = calcSequenceType();
		headerUnique = headerUnique();
	}
	
	public Path getNewFastaPath() {
		return newFastaPath;
	}
	
	public SequenceType getSequenceType() {
		return this.sequenceType;
	}
	
	public boolean isHeaderUnique() {
		return headerUnique;
	}
	
	private SequenceType calcSequenceType() throws IOException {
		
		boolean isDNA;
		boolean isProtein;
		
		isDNA = fileContent.stream()
						.filter(str -> str.length() > 0) //filter out empty lines (otherwise exception will be thrown when filtering headers)
						.filter(str -> str.charAt(0) != '>') //filter out headers
						.allMatch(str -> sequenceInAlphabet(str, SequenceType.DNA));
		
		if(isDNA) return SequenceType.DNA;
		
		isProtein = fileContent.stream()
						.filter(str -> str.length() > 0) //filter out empty lines (otherwise exception will be thrown when filtering headers)
						.filter(str -> str.charAt(0) != '>') //filter out headers
						.allMatch(str -> sequenceInAlphabet(str, SequenceType.PROTEIN));
		
		if(isProtein) return SequenceType.PROTEIN;
		else return SequenceType.INVALIDPROTEIN;
	}
	
	private boolean sequenceInAlphabet(String sequence, SequenceType type) {
		
		Set<Character> alphabet;
		
		if(type == SequenceType.PROTEIN) alphabet = protAlphabet;
		else alphabet = nucAlphabet;
		
		boolean matches =  sequence.chars()
								.allMatch(ch -> alphabet.contains((char) ch));
		return matches;
	}
	
	private boolean headerUnique() {
		return fileContent.stream()
						.filter(str -> str.length() > 0) //filter out empty lines (otherwise exception will be thrown when filtering headers)
						.filter(str -> str.charAt(0) == '>') //filter for headers
						.map(str -> str.split("\\s+")[0]) //get first part seperated by space (this part is used as identifier together with filename later)
						.allMatch(new HashSet<>()::add); //checks if duplicated element is in stream
	}
	
	public static List<String> removeInvalidChars(List<String> fileContent) throws IOException {
		List<String> newContent = new ArrayList<String>();
		for(String line : fileContent) {
			if(line.charAt(0) == '>') newContent.add(line);
			else {
				newContent.add(line.chars()
									.map(FastaValidate::checkIfValidOrX)
									.collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
									.toString());
			}
		}
		return newContent;
	}
	
	private static int checkIfValidOrX(int ch) {
		if (protAlphabet.contains((char) ch)) return ch; 
		else {
			return Character.getNumericValue('X');
		}
	}
	
}
