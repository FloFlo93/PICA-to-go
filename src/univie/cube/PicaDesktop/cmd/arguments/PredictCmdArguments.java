package univie.cube.PicaDesktop.cmd.arguments;

import java.nio.file.Path;
import java.nio.file.Paths;

public class PredictCmdArguments extends CmdArguments {
	private Path dbFileZip;
	private Path modelFileZip;


	public Path getDbFileZip() {
		return dbFileZip;
	}
	public void setDbFileZip(String dbFileZipStr) {
		Path dbFileZipPath = Paths.get(dbFileZipStr);
		super.fileExists(dbFileZipPath);
		this.dbFileZip = dbFileZipPath;
	}
	public Path getModelFileZip() {
		return modelFileZip;
	}
	public void setModelFileZip(String modelFileZipStr) {
		Path modelFileZipPath = Paths.get(modelFileZipStr);
		super.fileExists(modelFileZipPath);
		this.modelFileZip = modelFileZipPath;
	}
}
