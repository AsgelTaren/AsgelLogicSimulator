package asgel.app;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.ToolTipManager;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import asgel.app.bundle.Bundle;
import asgel.app.bundle.BundleLoadingPanel;
import asgel.app.model.ModelHolder;
import asgel.app.model.OBJTreeRenderer;
import asgel.app.model.OBJTreeTransferHandler;
import asgel.core.app.IApp;
import asgel.core.model.BundleRegistry;
import asgel.core.model.BundleRegistry.ObjectEntry;
import asgel.core.model.GlobalRegistry;
import asgel.core.model.IParametersRequester;
import asgel.core.model.Model;
import asgel.core.model.ModelTab;

public class App implements IApp {

	// GFX
	private JFrame frame;

	// Model
	private JTabbedPane holderTabs;
	private ModelHolder previous;

	// Bundles
	private ArrayList<Bundle> bundles;

	// Global Registry
	private GlobalRegistry registry;

	// Object Tree
	private JTree tree;
	private JTextField searchBar;
	private OBJTreeRenderer treeRend;
	private DefaultMutableTreeNode root;
	private HashMap<String, DefaultMutableTreeNode> tabs;
	private HashMap<String, DefaultMutableTreeNode> entries;

	// App Menu Bar
	private AppMenuBar menubar;

	// Parameters requester
	private ParametersRequester requester;

	public App() {

	}

	public void start() {
		registry = new GlobalRegistry();

		BundleLoadingPanel bundlePanel = new BundleLoadingPanel();
		LoadingFrame loadFrame = new LoadingFrame(bundlePanel);
		loadFrame.showDialog();

		requester = new ParametersRequester(this);
		bundlePanel.showFrame();
		loadBundles(bundlePanel.getBundles(), requester);

		frame = new JFrame("AsgelLogicSimulator origins");
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Right-side panel
		JPanel right = new JPanel();
		right.setLayout(new BoxLayout(right, BoxLayout.PAGE_AXIS));

		searchBar = new JTextField();
		searchBar.setPreferredSize(new Dimension(200, 25));
		searchBar.setMaximumSize(new Dimension(200, 25));
		searchBar.setMinimumSize(new Dimension(200, 25));

		JPanel searchPanel = new JPanel();
		searchPanel.setLayout(new BoxLayout(searchPanel, BoxLayout.PAGE_AXIS));
		searchPanel.add(searchBar);

		searchPanel.setBorder(BorderFactory.createTitledBorder("Search"));

		right.add(searchPanel);

		// Tree
		buildTree();
		tree.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tree.setDragEnabled(true);
		tree.setTransferHandler(new OBJTreeTransferHandler(this));
		ToolTipManager.sharedInstance().registerComponent(tree);
		tree.setCellRenderer(treeRend = new OBJTreeRenderer());

		JScrollPane scroll = new JScrollPane(tree);
		right.add(scroll);

		// Search Bar Listener
		searchBar.addActionListener(e -> {
			ArrayList<ObjectEntry> searchResult = new ArrayList<ObjectEntry>();
			if (searchBar.getText() != null && !searchBar.getText().equals(""))
				for (BundleRegistry regis : registry.getRegistries().values()) {
					for (ObjectEntry entry : regis.OBJECT_REGISTRY.values()) {
						if (entry.getFullID().contains(searchBar.getText())
								|| entry.getName().contains(searchBar.getText())) {
							searchResult.add(entry);
						}
					}
				}
			treeRend.setSearchResult(searchResult);
			tree.clearSelection();
			for (ObjectEntry entry : searchResult) {
				tree.addSelectionPath(new TreePath(
						new Object[] { root, tabs.get(entry.getFullTab()), entries.get(entry.getFullID()) }));
			}
			tree.revalidate();
			tree.repaint();
		});

		frame.add(right, BorderLayout.EAST);

		holderTabs = new JTabbedPane();
		holderTabs.setPreferredSize(new Dimension(1650, 950));
		holderTabs.addChangeListener(e -> {
			if (previous != null) {
				previous.stop();
			}
			previous = (ModelHolder) holderTabs.getSelectedComponent();
			previous.start();
		});
		frame.add(holderTabs, BorderLayout.CENTER);

		// MenuBar
		menubar = new AppMenuBar(this);
		menubar.init();
		frame.setJMenuBar(menubar);

		frame.pack();
		frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
	}

	public JTree getTree() {
		return tree;
	}

	public JFrame getJFrame() {
		return frame;
	}

	public ArrayList<Bundle> getBundles() {
		return bundles;
	}

	public GlobalRegistry getGlobalRegistry() {
		return registry;
	}

	private void loadBundles(ArrayList<BundleLoadingPanel.BundleHolder> temp, IParametersRequester req) {
		bundles = new ArrayList<Bundle>();

		for (BundleLoadingPanel.BundleHolder bundleHolder : temp) {
			if (!bundleHolder.used) {
				continue;
			}
			Bundle bundle = bundleHolder.b;
			bundles.add(bundle);
			try {
				bundle.loadDetails();
				System.out.println("[BUNDLES] Loaded details for " + bundle.getID() + " : " + bundle.getName());
			} catch (Exception e) {
				e.printStackTrace();
			}
			BundleRegistry reg = new BundleRegistry(bundle.getID());
			registry.getRegistries().put(bundle.getID(), reg);
			try {
				bundle.load(reg, req, registry);
				System.out.println("[BUNDLES] Loaded " + bundle.getID());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		System.out.println("[REGISTRY] List of all registered object entires");
		for (BundleRegistry regis : registry.getRegistries().values()) {
			for (ObjectEntry entry : regis.OBJECT_REGISTRY.values()) {
				System.out.println("[REGISTRY] -->" + entry.getFullID() + " : " + entry.getName());
			}
		}
	}

	private void buildTree() {
		root = new DefaultMutableTreeNode("AsgelLogicSimulator");

		tabs = new HashMap<String, DefaultMutableTreeNode>();
		entries = new HashMap<String, DefaultMutableTreeNode>();
		for (BundleRegistry regis : registry.getRegistries().values()) {
			for (ModelTab tab : regis.TAB_REGISTRY.values()) {
				DefaultMutableTreeNode node = new DefaultMutableTreeNode(tab);
				tabs.put(tab.getFullID(), node);
				root.add(node);
			}
		}

		for (BundleRegistry regis : registry.getRegistries().values()) {
			for (ObjectEntry entry : regis.OBJECT_REGISTRY.values()) {
				DefaultMutableTreeNode node = new DefaultMutableTreeNode(entry);
				tabs.get(entry.getFullTab()).add(node);
				entries.put(entry.getFullID(), node);
			}
		}
		tree = new JTree(root);
	}

	public ModelHolder getSelectedModelHolder() {
		return (ModelHolder) holderTabs.getSelectedComponent();
	}

	public void setModel(Model m, File f) {
		ModelHolder holder = new ModelHolder(m, this, "<Untitled " + holderTabs.getComponentCount() + ">", f);
		holderTabs.add(holder.toString(), holder);
		holderTabs.setSelectedComponent(holder);
		holderTabs.repaint();
	}

}