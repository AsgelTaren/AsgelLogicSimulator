package asgel.app;

import javax.swing.UIManager;

public class Main {

	public static void main(String[] args) throws Exception {
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		App app = new App();
		app.start();
	}

}