package asgel.app;

import javax.swing.UIManager;

public class Main {

	public static void main(String[] args) throws Exception {
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

		// Use local value for config
		LaunchConfig config = new LaunchConfig();
		config.setBundleDir(System.getenv("ASGEL_BUNDLE_DIR"));
		config.setConfigFile(System.getenv("ASGEL_CONFIG_FILE"));

		App app = new App();
		app.start(config);
	}

}