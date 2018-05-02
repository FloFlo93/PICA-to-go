package univie.cube.PicaDesktop.clustering.methods;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;

import univie.cube.PicaDesktop.miscellaneous.CmdExecution;
import univie.cube.PicaDesktop.miscellaneous.CmdExecution.Status;

public class MMSeqsRepresentative {
	
	private Path sequenceDB;
	private Path resultDB;
	private Path subDirTmp;
	
	public MMSeqsRepresentative(Path sequenceDB, Path resultDB, Path workDirPath) throws IOException {
		this.sequenceDB = sequenceDB;
		this.resultDB = resultDB;
		this.subDirTmp = Files.createTempDirectory(workDirPath, "mmseqs_representative");
	}
	
	public Map<String, String> getAll() throws IOException, InterruptedException {
		Path dbOut = Paths.get(subDirTmp.toString(), "repSeqDB");
		Path fastaOut = Paths.get(subDirTmp.toString(), "repSeqDB.fasta");
		String[] commandRepSeq = {"mmseqs", "result2repseq", sequenceDB.toString(), resultDB.toString(), dbOut.toString()};
		String[] commandSeqTFlat = {"mmseqs", "result2flat", sequenceDB.toString(), sequenceDB.toString(), dbOut.toString(), fastaOut.toString(), "--use-fasta-header"};
		Status status1 = CmdExecution.execute(commandRepSeq);
		Status status2 = CmdExecution.execute(commandSeqTFlat);
		if(status1.errorOccured || status2.errorOccured) throw new RuntimeException("rep seq extraction failed");
		String[] singleEntry = Files.readAllLines(fastaOut)
									.stream()
									.collect(Collectors.joining("\n"))
									.split(">");
		FileUtils.deleteDirectory(subDirTmp.toFile());
		return Arrays.stream(singleEntry)
			.collect(Collectors.toMap(
				entry -> entry.split("\n")[0].split("\\s+")[0],
				entry -> Arrays.stream(entry.split("\n")).skip(1).collect(Collectors.joining())
			));
			
	}
	
	
}
