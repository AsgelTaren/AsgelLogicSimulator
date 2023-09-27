package asgel.app;

import com.formdev.flatlaf.FlatDarkLaf;

/**
 * @author Florent Guille
 **/

public class Main {

	public static void main(String[] args) throws Exception {
		FlatDarkLaf.setup();

		// Use local value for config
		LaunchConfig config = new LaunchConfig();
		config.setBundleDir(System.getenv("ASGEL_BUNDLE_DIR"));
		config.setConfigFile(System.getenv("ASGEL_CONFIG_FILE"));

		App app = new App();
		app.start(config);
	}

}