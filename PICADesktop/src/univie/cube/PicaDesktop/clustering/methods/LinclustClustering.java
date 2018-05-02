package univie.cube.PicaDesktop.clustering.methods;

import java.nio.file.Path;

public class LinclustClustering extends MMseqsClustering {

	public LinclustClustering(Path clusteringDirInput, Path outputLogFiles) {
		super(clusteringDirInput, outputLogFiles, outputLogFiles);
	}
	
	@Override
	protected String clusteringCommandHook() {
		return "linclust";
	}
	
}
