package asgel.app.bundle;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.ToolTipManager;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import asgel.app.App;
import asgel.app.model.OBJTreeRenderer;
import asgel.core.bundle.Bundle;
import asgel.core.model.BundleRegistry.ObjectEntry;
import asgel.core.model.ModelTab;

/**
 * @author Florent Guille
 **/

@SuppressWarnings("serial")
public class BundleDialog extends JDialog {

	// Table
	private JTable table;
	private BundleTableModel model;

	// Content Tree
	private JTree contTree;
	private HashMap<String, DefaultMutableTreeNode> tabs, entries;

	public BundleDialog(JFrame frame, App app) {
		super(frame, app.getText("bundlesdialog.title"), true);

		this.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();

		model = new BundleTableModel(app);
		table = new JTable(model);
		table.setPreferredScrollableViewportSize(new Dimension(600, 200));
		table.setMinimumSize(new Dimension(600, 200));
		table.setMaximumSize(new Dimension(600, 200));
		table.setPreferredSize(new Dimension(600, 200));
		table.setDefaultRenderer(Bundle.class, new BundleCellRenderer());
		table.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		JPanel tabPanel = new JPanel();
		tabPanel.setBorder(BorderFactory.createTitledBorder(app.getText("bundlesdialog.title")));
		tabPanel.add(new JScrollPane(table));

		add(tabPanel, gbc);

		JPanel contPanel = new JPanel();
		contPanel.setBorder(BorderFactory.createTitledBorder(app.getText("bundlesdialog.content")));

		contTree = new JTree(new DefaultMutableTreeNode(app.getText("bundlesdialog.nobundle")));
		contTree.setPreferredSize(new Dimension(600, 300));
		contTree.setMaximumSize(new Dimension(600, 300));
		contTree.setMinimumSize(new Dimension(600, 300));
		contTree.setCellRenderer(new OBJTreeRenderer());
		ToolTipManager.sharedInstance().registerComponent(contTree);
		contPanel.add(new JScrollPane(contTree));

		table.getSelectionModel().addListSelectionListener(e -> {
			if (table.getSelectedRow() < 0 || !e.getValueIsAdjusting())
				return;
			Bundle bundle = (Bundle) table.getModel().getValueAt(table.getSelectedRow(), 0);
			updateTree(bundle);
		});

		gbc.gridy = 1;
		add(contPanel, gbc);
		pack();
		setLocationRelativeTo(null);

		setResizable(false);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
	}

	public void showDialog() {
		setVisible(true);
	}

	private void updateTree(Bundle bundle) {

		// Creating the new tree structure
		DefaultMutableTreeNode root = new DefaultMutableTreeNode(bundle);

		tabs = new HashMap<String, DefaultMutableTreeNode>();
		entries = new HashMap<String, DefaultMutableTreeNode>();

		for (ModelTab tab : bundle.getBundleRegistry().TAB_REGISTRY.values()) {
			DefaultMutableTreeNode node = new DefaultMutableTreeNode(tab);
			tabs.put(tab.getID(), node);
			root.add(node);
		}

		for (ObjectEntry entry : bundle.getBundleRegistry().OBJECT_REGISTRY.values()) {
			DefaultMutableTreeNode node = new DefaultMutableTreeNode(entry);
			entries.put(entry.getId(), node);
			tabs.get(entry.getTab()).add(node);
		}

		// Updating the tree
		((DefaultTreeModel) contTree.getModel()).setRoot(root);
		contTree.treeDidChange();
		contTree.revalidate();
		contTree.repaint();
	}

}
