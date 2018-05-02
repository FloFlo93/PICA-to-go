package univie.cube.PicaDesktop.pica;

import java.nio.file.Path;

//TODO: Callable is implemented, but concurracy is never used!!

public abstract class Pica {
	protected Path inputPica;
	protected Path picaExecutable;
	protected Path outputResults;
	protected Path inputPhenotypes;
	protected String feature;
	
	public Pica(Path inputPica, Path picaExecutable, Path outputResults, Path inputPhenotypes, String feature) {
		this.inputPica = inputPica;
		this.inputPhenotypes = inputPhenotypes;
		this.picaExecutable = picaExecutable;
		this.outputResults = outputResults;
		this.feature = feature;
	}
	
}
	

	

