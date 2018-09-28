package univie.cube.PICA_to_go.clustering.datatypes;


import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;


public class GeneClust4Bin implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private String binIdentifier;
	private Map<String, GeneClusterAbundance> cogAbundances = new LinkedHashMap<String, GeneClusterAbundance>();
	
	public GeneClust4Bin(String binItentifier) {
		this.binIdentifier = binItentifier;
	}
	
	public String getBinIdentifier() {
		return binIdentifier;
	}
	
	public void addGeneCluster(GeneCluster geneCluster) {
		GeneClusterAbundance singleCOGAbundance = cogAbundances.get(geneCluster.getClusterName());
		if(singleCOGAbundance != null) singleCOGAbundance.abundance += 1;
		else {
			singleCOGAbundance = new GeneClusterAbundance(geneCluster, 1);
			cogAbundances.put(geneCluster.getClusterName(), singleCOGAbundance);
		}
	}
	
	/**
	 * 
	 * @param geneCluster
	 * @param abundance: this value will be added to the former abundance (if entry already exists), otherwise a new entry with the specified abundance will be created
	 */
	public void addGeneCluster(GeneCluster geneCluster, long addAbundance) {
		GeneClusterAbundance singleCOGAbundance = cogAbundances.get(geneCluster.getClusterName());
		if(singleCOGAbundance != null) singleCOGAbundance.abundance += addAbundance;
		else {
			singleCOGAbundance = new GeneClusterAbundance(geneCluster, addAbundance);
			cogAbundances.put(geneCluster.getClusterName(), singleCOGAbundance);
		}
	}
	
	public Map<String, GeneClusterAbundance> getGeneClusterAbundances() {
		Map<String, GeneClusterAbundance> cogAbundancesInCurrentIndex = new LinkedHashMap<String, GeneClusterAbundance>();
		for(Map.Entry<String, GeneClusterAbundance> entry : cogAbundances.entrySet()) if(entry.getValue().geneCluster.isInCurrentIndex()) cogAbundancesInCurrentIndex.put(entry.getKey(), entry.getValue());
		return cogAbundances;
	}
	
	
	@Override
	public String toString() {
		StringBuilder b = new StringBuilder(); //improves performance
		b.append(binIdentifier);
		b.append("\t");
		for(GeneClusterAbundance abundanceSingleCOG : cogAbundances.values()) {
			if(abundanceSingleCOG.geneCluster.isInCurrentIndex()) {
				for(int a=0; a < abundanceSingleCOG.abundance; a++) {
					b.append(abundanceSingleCOG.geneCluster.getClusterName()); 
					b.append("\t");
				}
			}
		}
		return b.toString();
	}
	
	public static class GeneClusterAbundance implements Serializable {
		public GeneCluster geneCluster;
		public long abundance;
		
		public GeneClusterAbundance(GeneCluster geneCluster, long abundance) {
			this.geneCluster = geneCluster; 
			this.abundance = abundance;
		}
	}

	
}
