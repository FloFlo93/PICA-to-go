package univie.cube.PICA_to_go.archive;


import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;

import univie.cube.PICA_to_go.directories.WorkDir;

public class FastaGzHandler implements Closeable {
	
	private Path inputBins;
	private Path decompressedDir; //directory containing all decompressed files, will be deleted with destroy command (except if debug mode)
	
	public FastaGzHandler(Path inputBins) throws IOException {
		this.inputBins = inputBins;
		Path workDir = WorkDir.getWorkDir().getTmpDir();
		this.decompressedDir = Files.createTempDirectory(workDir, "decompressed_fasta");
	}
	
	public List<Path> getFiles() throws IOException, RuntimeException {
		List<Path> allFiles = Files.walk(inputBins).filter(Files::isRegularFile).collect(Collectors.toList()); 
		List<Path> gzippedFiles = allFiles.stream().filter(t -> {
			try {
				return GzDetection.isGzipped(t);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		})
		.collect(Collectors.toList());
		
		List<Path> decompressedFiles = decompressAllFiles(gzippedFiles);
		
		return joinFilesToList(allFiles, decompressedFiles, gzippedFiles);
	}
	
	@Override
	public void close() throws IOException {
		FileUtils.deleteDirectory(decompressedDir.toFile());
	}
	
	private List<Path> decompressAllFiles(List<Path> gzippedFiles) {
		return gzippedFiles.stream()
							.map(path -> {
								try {
									return GzipDecompress.decompressInDirectory(path, this.decompressedDir);
								} catch (IOException e) {
									throw new RuntimeException(e);
								}
							})
							.collect(Collectors.toList());
	}
	
	private List<Path> joinFilesToList(List<Path> allFiles, List<Path> decompressedFiles, List<Path> gzippedFiles) {
		List<Path> joinFiles = (new ArrayList<Path>(allFiles));
		joinFiles.removeAll(gzippedFiles);
		joinFiles.addAll(decompressedFiles);
		return joinFiles;
	}
	
}
