package asgel.app;

import java.awt.Desktop;
import java.io.File;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import asgel.app.bundle.BundleDialog;

@SuppressWarnings("serial")
public class AppMenuBar extends JMenuBar {

	private App app;

	public AppMenuBar(App app) {
		super();
		this.app = app;
	}

	public void init() {
		add(createBundleMenu());
	}

	private JMenu createBundleMenu() {
		JMenu res = new JMenu("Bundles");

		JMenuItem show = new JMenuItem("Show Bundles");
		show.addActionListener(e -> {
			new BundleDialog(app.getJFrame(), app).showDialog();
		});
		res.add(show);
		
		JMenuItem openFolder = new JMenuItem("Open Bundle folder");
		openFolder.addActionListener(e ->{
			try {
				Desktop.getDesktop().open(new File(System.getenv("APPDATA") + "/AsgelLogicSim/bundles"));
			} catch (Exception err) {
				err.printStackTrace();
			}
			
		});
		res.add(openFolder);
		
		return res;
	}

}
