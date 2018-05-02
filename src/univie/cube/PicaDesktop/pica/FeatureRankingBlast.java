package univie.cube.PicaDesktop.pica;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import univie.cube.PicaDesktop.remote.Blast;

public class FeatureRankingBlast extends FeatureRanking {
	
	//"overloaded" variables with default values 
	protected int limitLines = 10;
	protected int limitFeaturesForGroup = 3;

	public FeatureRankingBlast(Map<String, String> representativeSequences, Path modelFile, Path outputResults, Path picaExecutable, String feature) throws IOException {
		super(representativeSequences, modelFile, outputResults, picaExecutable, feature);
	}
	
	public FeatureRankingBlast(Map<String, String> representativeSequences, Path modelFile, Path outputResults, Path picaExecutable, String feature, int limitLines, int limitFeaturesForGroup) throws IOException {
		this(representativeSequences, modelFile, outputResults, picaExecutable, feature);
		this.limitLines = limitLines;
		this.limitFeaturesForGroup = limitFeaturesForGroup;
	}

	@Override
	protected String getAnnotation(String representativeSeq) {
		representativeSeq = representativeSeq.replaceAll("\\s+","");
		String blastResult = "";
		
		if(representativeSequences.get(representativeSeq) == null) {
			System.out.println("singleSeqId returns null " + representativeSeq);
			return blastResult;
		}
		
		Blast blast = new Blast(representativeSequences.get(representativeSeq));
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
