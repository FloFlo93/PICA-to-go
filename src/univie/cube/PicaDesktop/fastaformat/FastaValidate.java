package univie.cube.PicaDesktop.fastaformat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Sets;

public class FastaValidate {
	
	private List<String> fileContent;
	private boolean headerUnique;
	
	//TODO: add 'J' or not? 
	private static Set<Character> protAlphabet = Sets.newHashSet('A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'K', 'L', 'M', 'N', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'Y', 'Z', 'X', '*', '-');
	private static Set<Character> nucAlphabet = Sets.newHashSet('A', 'T', 'K', 'M', 'B', 'V', 'C', 'N', 'S', 'W', 'D', '-', 'G', 'U', 'Y', 'R', 'H');

	public static enum SequenceType {PROTEIN, DNA, INVALID};
	
	private SequenceType sequenceType;

	
	public FastaValidate(Path pathToFastaFile) throws IOException {
		fileContent = Files.readAllLines(pathToFastaFile);
		sequenceType = calcSequenceType();
		headerUnique = headerUnique();
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
						.filter(str -> str.charAt(0) != '>') //filter out headers
						.allMatch(str -> sequenceInAlphabet(str, SequenceType.DNA));
		
		isProtein = fileContent.stream()
						.filter(str -> str.charAt(0) != '>') //filter out headers
						.allMatch(str -> sequenceInAlphabet(str, SequenceType.PROTEIN));
		
		if(isDNA) return SequenceType.DNA;
		else if(isProtein) return SequenceType.PROTEIN;
		else return SequenceType.INVALID;
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
						.filter(str -> str.charAt(0) == '>') //filter for headers
						.map(str -> str.split("\\s+")[0]) //get first part seperated by space (this part is used as identifier together with filename later)
						.allMatch(new HashSet<>()::add); //checks if duplicated element is in stream
	}
	
}
