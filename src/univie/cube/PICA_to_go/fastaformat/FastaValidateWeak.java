package univie.cube.PICA_to_go.fastaformat;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Random;
import java.util.stream.Collectors;

public class FastaValidateWeak extends FastaValidate {

	private final static double nucAcidCutoff = 0.9; //file is treated as nucleotide if 90% of its sequence consist of DNA characters ('A', 'T', 'C', 'N', 'G', 'U'), otherwise as protein
	private final static long numChars = 1000; //takes random 1000 characters at different places in the sequence (header is of course excluded) 
	
	public FastaValidateWeak(Path pathToFastaFile) throws IOException {
		super(pathToFastaFile);
	}

	@Override
	protected SequenceType calcSequenceType() throws IOException {
		String sequence = fileContent.stream()
									.filter(str -> str.charAt(0) != '>')
									.collect(Collectors.joining());
		Random rand = new Random();
		long dnaTypicalChars = 0;
		for(int i=0; i<numChars; i++) {
			int index = rand.nextInt(sequence.length());
			char randomChar = sequence.charAt(index);
			if(FastaValidate.nucAlphabet.contains(randomChar)) ++dnaTypicalChars;
		}
		if(dnaTypicalChars*1.0/numChars > nucAcidCutoff) return SequenceType.DNA;
		else return SequenceType.PROTEIN;
	}

}
