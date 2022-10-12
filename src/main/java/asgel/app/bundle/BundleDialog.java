package asgel.app.bundle;

import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;

import asgel.app.App;

@SuppressWarnings("serial")
public class BundleDialog extends JDialog {

	// Table
	private JTable table;
	private BundleTableModel model;

	public BundleDialog(JFrame frame, App app) {
		super(frame, "Bundles", true);

		model = new BundleTableModel(app);
		table = new JTable(model);
		table.setPreferredScrollableViewportSize(new Dimension(600, 200));
		table.setMinimumSize(new Dimension(600, 200));
		table.setMaximumSize(new Dimension(600, 200));
		table.setPreferredSize(new Dimension(600, 200));
		table.setDefaultRenderer(Bundle.class, new BundleCellRenderer());
		table.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		JPanel tabPanel = new JPanel();
		tabPanel.setBorder(BorderFactory.createTitledBorder("Bundles"));

		tabPanel.add(new JScrollPane(table));

		add(tabPanel);
		pack();

		setResizable(false);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
	}

	public void showDialog() {
		setVisible(true);
	}

}
