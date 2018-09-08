package univie.cube.PICA_to_go.clustering.methods;

import java.nio.file.Path;

import org.apache.commons.lang3.ArrayUtils;

import univie.cube.PICA_to_go.global.Config;

public class LinclustClustering extends MMseqsClustering {

	public LinclustClustering(Path clusteringDirInput, Path outputLogFiles) {
		super(clusteringDirInput, outputLogFiles);
	}
	
	@Override
	protected String clusteringCommandHook() {
		return "linclust";
	}
	
	@Override
	protected String[] addAddOptToClust(String[] commandClust) {
		String[] addOpt = Config.getExecutablePaths().getADD_ARG_MMSEQS_LINCLUST();
		return ArrayUtils.addAll(commandClust, addOpt);
	}
	
}
