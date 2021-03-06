package univie.cube.PICA_to_go.clustering.methods;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

import univie.cube.PICA_to_go.global.Config;
import univie.cube.PICA_to_go.miscellaneous.CmdExecution;
import univie.cube.PICA_to_go.miscellaneous.CmdExecution.Status;

public class MMSeqsRepresentative {
	
	private Path sequenceDB;
	private Path resultDB;
	private Path subDirTmp;
	private Path fastaOut;
	private Path dbOut;
	
	
	private MMSeqsRepresentative(Path sequenceDB, Path resultDB, Path workDirPath) throws IOException {
		this.sequenceDB = sequenceDB;
		this.resultDB = resultDB;
		this.subDirTmp = Files.createTempDirectory(workDirPath, "mmseqs_representative");
		this.dbOut = Paths.get(subDirTmp.toString(), "repSeqDB");
		this.fastaOut = Paths.get(subDirTmp.toString(), "repSeqDB.fasta");
	}
	
	private static MMSeqsRepresentative instance = null;
	
	public static boolean initializeSingleton(Path sequenceDB, Path resultDB, Path workDirPath) throws IOException {
		if(MMSeqsRepresentative.instance != null) return false;
		else {
			MMSeqsRepresentative.instance = new MMSeqsRepresentative(sequenceDB, resultDB, workDirPath);
			return true;
		}
	}
	
	public static MMSeqsRepresentative getInstance() {
		return MMSeqsRepresentative.instance;
	}
	
	public Optional<String> get(String key) throws IOException, InterruptedException {
		if(! Files.exists(fastaOut)) runRepSeq();
		return searchFastaFile(key, fastaOut);
	}
	
	private void runRepSeq() throws IOException, InterruptedException, RuntimeException {
		String[] commandRepSeq = {Config.getExecutablePaths().getMMSEQS_EX().toString(), "result2repseq", sequenceDB.toString(), resultDB.toString(), dbOut.toString()};
		String[] commandSeqTFlat = {Config.getExecutablePaths().getMMSEQS_EX().toString(), "result2flat", sequenceDB.toString(), sequenceDB.toString(), dbOut.toString(), fastaOut.toString(), "--use-fasta-header"};
		Status status1 = CmdExecution.execute(commandRepSeq);
		Status status2 = CmdExecution.execute(commandSeqTFlat);
		if(status1.errorOccured || status2.errorOccured) throw new RuntimeException("rep seq extraction failed");
	}
	
	private Optional<String> searchFastaFile(String key, Path fastaOut) throws IOException {
		String[] singleEntry = Files.lines(fastaOut)
				.collect(Collectors.joining("\n"))
				.split(">");
		return Arrays.stream(singleEntry)
						.filter(x -> x.split("\n")[0].split("\\s+")[0].equals(key)) //checks if any keys are in the fasta header
						.map(entry -> entry.split("\n")[1])
						.findAny();
	}
	
}