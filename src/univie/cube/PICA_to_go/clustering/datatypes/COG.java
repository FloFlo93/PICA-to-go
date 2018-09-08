package univie.cube.PICA_to_go.clustering.datatypes;

import java.util.HashSet;
import java.util.Set;

public class COG implements Comparable<COG> {
	
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
	
	public COG(String cogName) {
		this.cogName = cogName;
	}
	
	public COG(Set<String> genes, String cogName) {
		this.genes = genes;
		this.cogName= cogName;
	}
	
	private String cogName;

	private Set<String> genes = new HashSet<String>(); //header files for each gene
	
	public void addGenes(String gene) {
		genes.add(gene);
	}
	
	public String getCOGName() {
		return cogName;
	}
	
	/**
	 * 
	 * @param cogString: white space delimited genes
	 * @return COG object with genes from input
	 */
	public static COG convertToCOG(String cogString, String cogName) {
		COG cog = new COG(cogName);
		String[] genesArray = cogString.split("\\s+");
		for(String gene : genesArray) cog.addGenes(gene);
		return cog;
	}
	/**
	 * 
	 * @return white space delimited genes
	 */
	public String getCogString() {
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
	public int compareTo(COG cog) {
		if(this.genes.size() < cog.genes.size()) return -1;
		else if ((this.genes.size() == cog.genes.size())) return 0;
		else return 1;
	}
	
}
