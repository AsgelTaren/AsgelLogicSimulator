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

import asgel.app.LaunchConfig;

/**
 * @author Florent Guille
 **/

@SuppressWarnings("serial")
public class BundleLoadingPanel extends JPanel {

	private ArrayList<BundleHolder> bundles;

	private File dir;

	public BundleLoadingPanel(LaunchConfig config) {
		super();

		dir = config.getBundleDir();

		setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(5, 5, 5, 5);
		gbc.gridwidth = 2;
		gbc.gridheight = 1;

		JTable table = new JTable(new BundleLoadingModel(this));
		table.setPreferredScrollableViewportSize(new Dimension(600, 400));
		table.setDefaultRenderer(Bundle.class, new BundleCellRenderer());
		table.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		add(new JScrollPane(table), gbc);

		JTextArea area = new JTextArea();
		area.setBorder(BorderFactory.createTitledBorder("Bundle Desc"));
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
		loc.setBorder(BorderFactory.createTitledBorder("Location"));

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
		JButton browse = new JButton("Browse");
		browse.addActionListener(e -> {
			JFileChooser chooser = new JFileChooser();
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			chooser.setCurrentDirectory(dir);
			int choice = chooser.showDialog(this, "Select");
			if (choice == JFileChooser.APPROVE_OPTION) {
				dir = chooser.getSelectedFile();
				load(dir);
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

		load(dir);
	}

	public ArrayList<BundleHolder> getBundles() {
		return bundles;
	}

	private void load(File dir) {
		bundles = new ArrayList<BundleHolder>();
		for (File f : dir.listFiles()) {
			if (f.getName().endsWith(".jar")) {
				Bundle b = new Bundle(f);
				try {
					b.loadDetails();
					bundles.add(new BundleHolder(b, true));
				} catch (Exception e) {
					System.out.println("Wrong file found at " + f);
				}
			}
		}
		revalidate();
		repaint();
	}

	private class BundleLoadingModel extends AbstractTableModel {

		private BundleLoadingPanel load;

		private BundleLoadingModel(BundleLoadingPanel load) {
			super();
			this.load = load;
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
				return "Used";
			case 1:
				return "Bundle";
			case 2:
				return "ID";
			case 3:
				return "Desc";
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
		return "Bundles";
	}

}