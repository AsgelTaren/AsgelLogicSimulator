package asgel.app.bundle;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import asgel.app.App;
import asgel.app.LaunchConfig;
import asgel.app.Logger;
import asgel.app.Utils;
import asgel.core.bundle.Bundle;

/**
 * @author Florent Guille
 **/

@SuppressWarnings("serial")
public class BundleLoadingPanel extends JPanel {

	private ArrayList<BundleHolder> bundles;

	private File dir;

	private App app;

	public BundleLoadingPanel(LaunchConfig config, Logger log, App app) {
		super();
		this.app = app;

		dir = config.getBundleDir();

		setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(5, 5, 5, 5);
		gbc.gridwidth = 2;
		gbc.gridheight = 1;

		JTable table = new JTable(new BundleLoadingModel(this, app));
		table.setPreferredScrollableViewportSize(new Dimension(600, 400));
		table.setDefaultRenderer(Bundle.class, new BundleCellRenderer());
		table.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		add(new JScrollPane(table), gbc);

		JTextArea area = new JTextArea();
		area.setBorder(BorderFactory.createTitledBorder(app.getText("loading.bundle.desc")));
		area.setEditable(false);
		area.setPreferredSize(new Dimension(600, 100));
		table.getSelectionModel().addListSelectionListener(e -> {
			if (table.getSelectedRow() >= 0) {
				area.setText(bundles.get(table.getSelectedRow()).b.getName() + "\n"
						+ bundles.get(table.getSelectedRow()).b.getDesc());
			}
		});
		gbc.gridy = 1;
		add(area, gbc);

		JPanel loc = new JPanel();
		loc.setLayout(new GridBagLayout());
		loc.setBorder(BorderFactory.createTitledBorder(app.getText("loading.bundle.location")));

		gbc.gridx = 0;
		gbc.gridwidth = 1;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 0.8;
		JTextField dirField = new JTextField(dir.getAbsolutePath());
		dirField.setPreferredSize(new Dimension(400, 25));
		dirField.setEditable(false);
		loc.add(dirField, gbc);

		gbc.gridx = 1;
		gbc.weightx = 0.2;
		JButton browse = new JButton(app.getText("loading.bundle.browse"));
		browse.addActionListener(e -> {
			JFileChooser chooser = new JFileChooser();
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			chooser.setCurrentDirectory(dir);
			int choice = chooser.showDialog(this, app.getText("loading.bundle.approve"));
			if (choice == JFileChooser.APPROVE_OPTION) {
				dir = chooser.getSelectedFile();
				load(dir, log, app);
				dirField.setText(dir.getAbsolutePath());
			}
		});
		loc.add(browse, gbc);

		gbc.gridwidth = 2;
		gbc.gridy = 2;
		gbc.gridx = 0;
		gbc.fill = GridBagConstraints.BOTH;
		add(loc, gbc);

		gbc.anchor = GridBagConstraints.EAST;
		gbc.fill = GridBagConstraints.NONE;
		gbc.gridy = 3;
		gbc.gridwidth = 1;

		load(dir, log, app);
	}

	public ArrayList<BundleHolder> getBundles() {
		return bundles;
	}

	private void load(File dir, Logger log, App app) {
		bundles = new ArrayList<BundleHolder>();
		for (File f : dir.listFiles()) {
			if (f.getName().endsWith(".jar")) {
				try {
					JsonArray bundles = JsonParser
							.parseString(new String(Utils.openStreamInJar(f, "bundles.json").readAllBytes()))
							.getAsJsonObject().get("bundles").getAsJsonArray();
					for (JsonElement element : bundles) {
						JsonObject bundleData = (JsonObject) element;
						Bundle bundle = Bundle.preLoadBundle(f, bundleData.get("id").getAsString(),
								bundleData.get("main").getAsString(), app.getGlobalRegistry(),
								app.getParametersRequester());
						try {
							log.log("Detected bundle from " + bundle.getFile());
							bundle.loadDetails(log);
							log.log("Loaded details for " + bundle.getID() + " : " + bundle.getName());
						} catch (Exception e) {
							log.log("Failed to load data for bundle with id: " + bundle.getID());
						}
						this.bundles.add(new BundleHolder(bundle, true));
					}
				} catch (Exception e) {
					log.log("Failed to load file " + f);
				}
			}
		}
		revalidate();
		repaint();
	}

	private class BundleLoadingModel extends AbstractTableModel {

		private BundleLoadingPanel load;
		private App app;

		private BundleLoadingModel(BundleLoadingPanel load, App app) {
			super();
			this.load = load;
			this.app = app;
		}

		@Override
		public Class<?> getColumnClass(int i) {
			switch (i) {
			case 0:
				return Boolean.class;
			case 1:
				return Bundle.class;
			default:
				return String.class;
			}
		}

		@Override
		public int getRowCount() {
			return load.bundles.size();
		}

		@Override
		public int getColumnCount() {
			return 4;
		}

		@Override
		public String getColumnName(int i) {
			switch (i) {
			case 0:
				return app.getText("loading.bundle.used");
			case 1:
				return app.getText("loading.bundle.bundle");
			case 2:
				return app.getText("loading.bundle.id");
			case 3:
				return app.getText("loading.bundle.desc");
			}
			return null;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			BundleHolder b = load.bundles.get(rowIndex);
			switch (columnIndex) {
			case 0:
				return b.used;
			case 1:
				return b.b;
			case 2:
				return b.b.getID();
			case 3:
				return b.b.getDesc();
			}
			return null;
		}

		@Override
		public boolean isCellEditable(int i, int j) {
			return j == 0;
		}

		@Override
		public void setValueAt(Object val, int i, int j) {
			if (j == 0) {
				load.bundles.get(i).used = (boolean) val;
			}
		}

	}

	public class BundleHolder {
		public Bundle b;
		public boolean used;

		public BundleHolder(Bundle b, boolean used) {
			this.b = b;
			this.used = used;
		}
	}

	@Override
	public String toString() {
		return app.getText("loading.bundle.title");
	}

}