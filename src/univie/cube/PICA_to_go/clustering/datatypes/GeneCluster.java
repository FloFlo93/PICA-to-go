package univie.cube.PICA_to_go.clustering.datatypes;

import java.util.HashSet;
import java.util.Set;

public class GeneCluster implements Comparable<GeneCluster> {
	
	public void removeFromCurrentIndex() {
		this.inCurrentIndex = false;
	}
	
	public boolean isInCurrentIndex() {
		return inCurrentIndex;
	}
	

	public void setInCurrentIndex() {
		this.inCurrentIndex = true;
	}
	
	private boolean inCurrentIndex = true;
	
	public GeneCluster(String geneClusterName) {
		this.geneClusterName = geneClusterName;
	}
	
	public GeneCluster(Set<String> genes, String geneClusterName) {
		this.genes = genes;
		this.geneClusterName= geneClusterName;
	}
	
	private String geneClusterName;

	private Set<String> genes = new HashSet<String>(); //header files for each gene
	
	public void addGenes(String gene) {
		genes.add(gene);
	}
	
	public String getClusterName() {
		return geneClusterName;
	}
	
	/**
	 * 
	 * @param cogString: white space delimited genes
	 * @return GeneCluster object with genes from input
	 */
	public static GeneCluster convertToCOG(String cogString, String cogName) {
		GeneCluster geneCluster = new GeneCluster(cogName);
		String[] genesArray = cogString.split("\\s+");
		for(String gene : genesArray) geneCluster.addGenes(gene);
		return geneCluster;
	}
	/**
	 * 
	 * @return white space delimited genes
	 */
	public String getGeneClusterString() {
		StringBuilder cogString = new StringBuilder("");
		for(String gene : genes) {
			cogString.append(gene); 
			cogString.append(" ");
		}
		return cogString.toString();
	} 
	
	public Set<String> getGenes() {
		return genes;
	}

	@Override
	public int compareTo(GeneCluster geneCluster) {
		if(this.genes.size() < geneCluster.genes.size()) return -1;
		else if ((this.genes.size() == geneCluster.genes.size())) return 0;
		else return 1;
	}
	
}
