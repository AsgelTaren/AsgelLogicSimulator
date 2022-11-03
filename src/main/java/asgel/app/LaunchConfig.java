package asgel.app;

import java.io.File;

/**
 * @author Florent Guille
 **/

public class LaunchConfig {

	// Hold the configuration file, where working directories can be saved
	private File configFile;

	// Hold the bundle directory, where bundles will be loaded
	private File bundleDir;

	public LaunchConfig() {
		configFile = new File(System.getenv("APPDATA") + "/AsgelLogicSim/config.json");
		bundleDir = new File(System.getenv("APPDATA") + "/AsgelLogicSim/bundles");
	}

	public File getConfigFile() {
		return configFile;
	}

	public void setConfigFile(File configFile) {
		this.configFile = configFile;
	}

	public void setConfigFile(String configFile) {
		if (configFile != null && !"".equals(configFile)) {
			this.configFile = new File(configFile);
		}
	}

	public File getBundleDir() {
		return bundleDir;
	}

	public void setBundleDir(File bundleDir) {
		this.bundleDir = bundleDir;
	}

	public void setBundleDir(String bundleDir) {
		if (bundleDir != null && !"".equals(bundleDir)) {
			this.bundleDir = new File(bundleDir);
		}
	}

}