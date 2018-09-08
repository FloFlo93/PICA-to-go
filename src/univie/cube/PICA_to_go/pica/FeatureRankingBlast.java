package univie.cube.PICA_to_go.pica;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

import univie.cube.PICA_to_go.clustering.methods.Clustering;
import univie.cube.PICA_to_go.out.error.ErrorHandler;
import univie.cube.PICA_to_go.out.logging.CustomLogger;
import univie.cube.PICA_to_go.remote.Blast;

public class FeatureRankingBlast extends FeatureRanking {
	
	//"overloaded" variables with default values 
	protected int limitLines;
	protected Clustering clustering;
	
	public FeatureRankingBlast(Path modelFile, Path outputResults, String feature, Clustering clustering, int limitLines) throws IOException {
		super(modelFile, outputResults, feature);
		this.clustering = clustering;
		this.limitLines = limitLines;
	}

	@Override
	protected String getAnnotation(String representativeSeq) throws IOException, InterruptedException {
		representativeSeq = representativeSeq.replaceAll("\\s+","");
		String blastResult = "";
		Optional<String> repSeqResult = clustering.getRepresentativeSequence(representativeSeq);
		if(repSeqResult.isPresent()) blastResult = runBlast(repSeqResult.get(), representativeSeq);
		return blastResult;
	}

	private String runBlast(String seq, String representativeSeq) {
		Blast blast = new Blast(seq);
		String blastResult = "";
		try {
			blastResult = blast.runBlast();
			CustomLogger.getInstance().log(CustomLogger.LoggingWeight.INFO, "Remote BLAST done for " + representativeSeq);
		} catch (Exception e) {
			(new ErrorHandler(e, ErrorHandler.ErrorWeight.WARNING, "Remote BLAST failed for " + representativeSeq)).handle();
		}
		return blastResult;
	}
	
	@Override
	protected int getLimitLines() {
		return this.limitLines;
	}
	
}
