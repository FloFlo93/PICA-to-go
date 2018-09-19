package univie.cube.PICA_to_go.pica;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import univie.cube.PICA_to_go.clustering.datatypes.GeneCluster;


public class FeatureRankingRefGenome extends FeatureRanking {

	private Map<String, String> fastaHeaders;
	private List<String> refGenomes;
	private Map<String, GeneCluster> geneClusters;
	
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
	public FeatureRankingRefGenome(Path modelFile, Path outputResults, String feature, Map<String, String> fastaHeaders, List<String> refGenomes, Map<String, GeneCluster> geneClusters) throws IOException {
		super(modelFile, outputResults, feature);
		this.fastaHeaders = fastaHeaders;
		this.refGenomes = refGenomes;
		this.geneClusters = geneClusters;
	}

	@Override
	protected String getAnnotation(String representativeSeq) {
		representativeSeq = representativeSeq.replaceAll("\\s+","");
		if(refGenomes == null) 
			return fastaHeaders.get(representativeSeq);
		else {
			Set<String> allGenesOfCluster = geneClusters.get(representativeSeq).getGenes();
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
	

}






