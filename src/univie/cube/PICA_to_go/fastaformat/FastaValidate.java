package univie.cube.PICA_to_go.fastaformat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Sets;


public abstract class FastaValidate {

	protected List<String> fileContent;
	protected boolean headerUnique;
	protected Path newFastaPath; //returns original path or new tmp file (if file had to be modified)
	protected Boolean containsInvalidChars = null; 
	
	protected static final Set<Character> protAlphabet = Sets.newHashSet('A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'K', 'L', 'M', 'N', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'Y', 'Z', 'X', '*');
	protected static final Set<Character> nucAlphabet = Sets.newHashSet('A', 'T', 'C', 'N', 'G', 'U');

	public static enum SequenceType {PROTEIN, DNA};
	
	protected SequenceType sequenceType;

	
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
	
	/**
	 * only call this method after removeInvalidChars has been called, otherwise it returns null (as the whole file has not been read
	 * @return
	 */
	public Boolean hasInvalidChars() {
		return containsInvalidChars;
	}
	
	
	abstract protected SequenceType calcSequenceType() throws IOException;
	
	private boolean headerUnique() {
		return fileContent.stream()
						.filter(str -> str.length() > 0) //filter out empty lines (otherwise exception will be thrown when filtering headers)
						.filter(str -> str.charAt(0) == '>') //filter for headers
						.map(str -> str.split("\\s+")[0]) //get first part seperated by space (this part is used as identifier together with filename later)
						.allMatch(new HashSet<>()::add); //checks if duplicated element is in stream
	}
	
	public List<String> removeInvalidChars() throws IOException {
		List<String> newContent = new ArrayList<String>();
		for(String line : fileContent) {
			if(line.charAt(0) == '>') newContent.add(line);
			else {
				StringBuilder newLine = new StringBuilder();
				for(char ch : line.toCharArray()) {
					char newChar;
					if(sequenceType == SequenceType.PROTEIN) newChar = checkIfValidOrSub(ch, protAlphabet, 'X');
					else newChar = checkIfValidOrSub(ch, nucAlphabet, 'N');
					newLine.append(newChar);
				}
				newContent.add(newLine.toString());
			}
		}
		if(containsInvalidChars == null) containsInvalidChars = false;
		return newContent;
	}
	
	private char checkIfValidOrSub(char ch, Set<Character> alphabet, char substitution) {
			if(alphabet.contains((char) ch)) return ch;
			else {
				containsInvalidChars = true;
				return substitution;
			}
	}
	
}
