package univie.cube.PicaDesktop.pica;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import univie.cube.PicaDesktop.clustering.datatypes.COG;

public class FeatureRankingRefGenome extends FeatureRanking {

	private Map<String, String> fastaHeaders;
	private List<String> refGenomes;
	private Map<String, COG> orthogroups;
	
	//"overloaded" variables with default values 
	protected int limitLines = Integer.MAX_VALUE;
	protected int limitFeaturesForGroup = Integer.MAX_VALUE;
	
	/**
	 * 
	 * @param representativeSequences
	 * @param modelFile
	 * @param outputResults
	 * @param picaExecutable
	 * @param feature
	 * @param fastaHeaders (fasta headers with id as key)
	 * @param refGenomes if null -> all genomes are reference genomes
	 * @throws IOException
	 */
	public FeatureRankingRefGenome(Map<String, String> representativeSequences, Path modelFile, Path outputResults, Path picaExecutable, String feature, Map<String, String> fastaHeaders, List<String> refGenomes, Map<String, COG> orthogroups) throws IOException {
		super(representativeSequences, modelFile, outputResults, picaExecutable, feature);
		this.fastaHeaders = fastaHeaders;
		this.refGenomes = refGenomes;
		this.orthogroups = orthogroups;
	}

	@Override
	protected String getAnnotation(String representativeSeq) {
		representativeSeq = representativeSeq.replaceAll("\\s+","");
		
		if(refGenomes == null) return fastaHeaders.get(representativeSeq);
		else {
			System.out.println("singleSeqId " + representativeSeq);
			Set<String> allGenesOfCluster = orthogroups.get(representativeSeq).getGenes();
			
			System.out.println("allGenesOfCluster");
			System.out.println(allGenesOfCluster.toString());
			System.out.println("ref genome");
			System.out.println(refGenomes);
			
			Optional<String> alternativeRepSeq = allGenesOfCluster.stream()
										.filter(gene -> refGenomes.contains(gene.split("\\^_")[0]))
										.findAny();
			
			String annotation;
			if(alternativeRepSeq.isPresent()) annotation = fastaHeaders.get(alternativeRepSeq.get());
			else annotation = "";
			return annotation;
		}
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






