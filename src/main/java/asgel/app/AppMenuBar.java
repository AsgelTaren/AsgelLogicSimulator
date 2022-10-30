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
import asgel.app.model.ModelHolder;
import asgel.core.model.Model;

@SuppressWarnings("serial")
public class AppMenuBar extends JMenuBar {

	private App app;

	private JMenuItem saveAs, save, close;

	public AppMenuBar(App app) {
		super();
		this.app = app;
	}

	public void init() {
		add(createFileMenu());
		add(createBundleMenu());
		add(createHelpMenu());
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

		saveAs = new JMenuItem("Save As");
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
				app.getSelectedModelHolder().setFile(chooser.getSelectedFile());
			}
		});
		res.add(saveAs);

		save = new JMenuItem("Save");
		save.addActionListener(e -> {
			ModelHolder holder = app.getSelectedModelHolder();
			if (holder.getFile() != null) {
				Utils.write(holder.getFile(), holder.getModel().convertToJson().toString());
			}
		});
		res.add(save);

		close = new JMenuItem("Close Model");
		close.addActionListener(e -> {
			app.getHolderTabs().remove(app.getSelectedModelHolder());
			app.getHolderTabs().repaint();
			app.getJFrame().revalidate();
			app.getJFrame().repaint();
		});
		res.add(close);

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

	public void updateOnChange() {
		saveAs.setEnabled(app.getSelectedModelHolder() != null);
		save.setEnabled(app.getSelectedModelHolder() != null && app.getSelectedModelHolder().getFile() != null);
		close.setEnabled(app.getSelectedModelHolder() != null);
	}

	private JMenu createHelpMenu() {
		JMenu res = new JMenu("Help");

		JMenuItem showLogs = new JMenuItem("Show Logs");
		showLogs.addActionListener(e -> Logger.INSTANCE.setVisible(true));
		res.add(showLogs);

		return res;
	}

}
