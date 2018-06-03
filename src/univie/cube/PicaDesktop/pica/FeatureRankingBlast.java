package univie.cube.PicaDesktop.pica;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import univie.cube.PicaDesktop.clustering.methods.Clustering;
import univie.cube.PicaDesktop.remote.Blast;

public class FeatureRankingBlast extends FeatureRanking {
	
	//"overloaded" variables with default values 
	protected int limitLines = 10;
	protected int limitFeaturesForGroup = 3;
	protected Clustering clustering;

	public FeatureRankingBlast(Path modelFile, Path outputResults, String feature, Clustering clustering) throws IOException {
		super(modelFile, outputResults, feature);
		this.clustering = clustering;
	}
	
	public FeatureRankingBlast(Path modelFile, Path outputResults, String feature, Clustering clustering, int limitLines, int limitFeaturesForGroup) throws IOException {
		this(modelFile, outputResults, feature, clustering);
		this.limitLines = limitLines;
		this.limitFeaturesForGroup = limitFeaturesForGroup;
	}

	@Override
	protected String getAnnotation(String representativeSeq) throws IOException, InterruptedException {
		representativeSeq = representativeSeq.replaceAll("\\s+","");
		String blastResult = "";
		
		Optional<String> repSeqResult = clustering.getRepresentativeSequence(representativeSeq);
		
		if(repSeqResult.isPresent()) blastResult = runBlast(repSeqResult.get(), representativeSeq);
		else System.out.println("singleSeqId returns null " + representativeSeq);
		
		return blastResult;
	}

	private String runBlast(String seq, String representativeSeq) {
		Blast blast = new Blast(seq);
		String blastResult = "";
		try {
			blastResult = blast.runBlast();
		} catch (ParserConfigurationException | SAXException | IOException | RuntimeException e) {
			System.err.println("WARNING: Remote BLAST failed for " + representativeSeq);
		}
		return blastResult;
	}
	
	@Override
	protected int getLimitLines() {
		return this.limitLines;
	}

	@Override
	protected int getLimitFeaturesForGroup() {
		return this.limitFeaturesForGroup;
	}
	
}
