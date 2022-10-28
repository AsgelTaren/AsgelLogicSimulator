package asgel.app;

import java.awt.Desktop;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import com.google.gson.JsonParser;

import asgel.app.bundle.BundleDialog;
import asgel.core.model.Model;

@SuppressWarnings("serial")
public class AppMenuBar extends JMenuBar {

	private App app;

	public AppMenuBar(App app) {
		super();
		this.app = app;
	}

	public void init() {
		add(createFileMenu());
		add(createBundleMenu());
	}

	private JMenu createFileMenu() {
		JMenu res = new JMenu("File");

		JMenuItem load = new JMenuItem("Load Model");
		load.addActionListener(e -> {
			JFileChooser chooser = new JFileChooser();
			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			int choice = chooser.showDialog(app.getJFrame(), "Select");
			if (choice == JFileChooser.APPROVE_OPTION) {
				try {
					Model m = new Model(
							JsonParser.parseReader(new FileReader(chooser.getSelectedFile())).getAsJsonObject(),
							app.getGlobalRegistry());
					app.setModel(m, chooser.getSelectedFile());
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});
		res.add(load);

		JMenuItem create = new JMenuItem("New Model");
		create.addActionListener(e -> {
			app.setModel(new Model(), null);
		});
		res.add(create);

		JMenuItem saveAs = new JMenuItem("Save As");
		saveAs.addActionListener(e -> {

			JFileChooser chooser = new JFileChooser();
			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			int choice = chooser.showDialog(app.getJFrame(), "Select");
			if (choice == JFileChooser.APPROVE_OPTION) {
				if (!chooser.getSelectedFile().exists()) {
					try {
						chooser.getSelectedFile().createNewFile();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
				Utils.write(chooser.getSelectedFile(),
						app.getSelectedModelHolder().getModel().convertToJson().toString());
			}
		});
		res.add(saveAs);

		return res;
	}

	private JMenu createBundleMenu() {
		JMenu res = new JMenu("Bundles");

		JMenuItem show = new JMenuItem("Show Bundles");
		show.addActionListener(e -> {
			new BundleDialog(app.getJFrame(), app).showDialog();
		});
		res.add(show);

		JMenuItem openFolder = new JMenuItem("Open Bundle folder");
		openFolder.addActionListener(e -> {
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
