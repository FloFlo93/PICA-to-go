package univie.cube.PicaDesktop.clustering.datatypes;

import java.util.LinkedHashMap;
import java.util.Map;

public class BinCOGs {
	
	private String binIdentifier;
	private Map<String, COGAbundance> cogAbundances = new LinkedHashMap<String, COGAbundance>();
	
	public BinCOGs(String binItentifier) {
		this.binIdentifier = binItentifier;
	}
	
	public String getBinIdentifier() {
		return binIdentifier;
	}
	
	public void addCOG(COG cog) {
		COGAbundance singleCOGAbundance = cogAbundances.get(cog.getCOGName());
		if(singleCOGAbundance != null) singleCOGAbundance.abundance += 1;
		else {
			singleCOGAbundance = new COGAbundance(cog, 1);
			cogAbundances.put(cog.getCOGName(), singleCOGAbundance);
		}
	}
	
	/**
	 * 
	 * @param cog
	 * @param abundance: this value will be added to the former abundance (if entry already exists), otherwise a new entry with the specified abundance will be created
	 */
	public void addCOG(COG cog, long addAbundance) {
		COGAbundance singleCOGAbundance = cogAbundances.get(cog.getCOGName());
		if(singleCOGAbundance != null) singleCOGAbundance.abundance += addAbundance;
		else {
			singleCOGAbundance = new COGAbundance(cog, addAbundance);
			cogAbundances.put(cog.getCOGName(), singleCOGAbundance);
		}
	}
	
	public Map<String, COGAbundance> getCOGAbundances() {
		Map<String, COGAbundance> cogAbundancesInCurrentIndex = new LinkedHashMap<String, COGAbundance>();
		for(Map.Entry<String, COGAbundance> entry : cogAbundances.entrySet()) if(entry.getValue().cog.isInCurrentIndex()) cogAbundancesInCurrentIndex.put(entry.getKey(), entry.getValue());
		return cogAbundances;
	}
	
	
	@Override
	public String toString() {
		StringBuilder b = new StringBuilder(); //improves performance
		b.append(binIdentifier);
		b.append("\t");
		for(COGAbundance abundanceSingleCOG : cogAbundances.values()) {
			if(abundanceSingleCOG.cog.isInCurrentIndex()) {
				for(int a=0; a < abundanceSingleCOG.abundance; a++) {
					b.append(abundanceSingleCOG.cog.getCOGName()); 
					b.append("\t");
				}
			}
		}
		return b.toString();
	}
	
	public static class COGAbundance {
		public COG cog;
		public long abundance;
		
		public COGAbundance(COG cog, long abundance) {
			this.cog = cog; 
			this.abundance = abundance;
		}
	} 
	
}
