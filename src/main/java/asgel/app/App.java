package asgel.app;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import asgel.app.bundle.BundleLoadingPanel;
import asgel.app.model.ModelHolder;
import asgel.app.model.OBJTreeRenderer;
import asgel.app.model.OBJTreeTransferHandler;
import asgel.app.model.PopupUtils;
import asgel.core.bundle.Bundle;
import asgel.core.model.BundleRegistry;
import asgel.core.model.BundleRegistry.ObjectEntry;
import asgel.core.model.GlobalRegistry;
import asgel.core.model.IParametersRequester;
import asgel.core.model.Model;
import asgel.core.model.ModelTab;

/**
 * @author Florent Guille
 **/
public class App {

	// GFX
	private JFrame frame;

	// Model
	private JTabbedPane holderTabs;
	private ModelHolder previous;

	// Global Registry
	private GlobalRegistry registry;

	// Object registry Tree
	private JTree registryTree;
	private JTextField searchBar;
	private OBJTreeRenderer registryTreeRend;
	private DefaultMutableTreeNode root;
	private HashMap<String, DefaultMutableTreeNode> tabs;
	private HashMap<String, DefaultMutableTreeNode> entries;

	// Objects Tree
	private JTree objectsTree;

	// App Menu Bar
	private AppMenuBar menubar;

	// Parameters requester
	private ParametersRequester requester;

	// Working dir
	private File workingDir;

	// Icon
	public static final BufferedImage ICON = Utils
			.loadImage(App.class.getClassLoader().getResourceAsStream("logo.png"));

	public App() {

	}

	public void start(LaunchConfig config) {
		App instance = this;
		config.loadConfig();
		Logger log = Logger.INSTANCE.derivateLogger("[LAUNCH]");
		log.setVisible(true);
		registry = new GlobalRegistry(config);
		registry.loadAppTextAtlases();
		requester = new ParametersRequester(this);
		log.log("Created Global Registry");

		LoadingFrame loadFrame = new LoadingFrame(this);
		WorkingDirPanel dirPanel = new WorkingDirPanel(loadFrame, config);
		BundleLoadingPanel bundlePanel = new BundleLoadingPanel(config, log, this);
		loadFrame.build(dirPanel, bundlePanel);
		dirPanel.update();
		loadFrame.showDialog();

		workingDir = dirPanel.getWorkingDir();
		log.log("Selected working dir: " + workingDir);
		dirPanel.storeDirs(config);

		File objects = new File(workingDir.getAbsolutePath() + "/models");
		objects.mkdir();

		loadBundles(bundlePanel.getBundles(), requester);

		frame = new JFrame("AsgelLogicSimulator origins");
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setIconImage(ICON);

		// Objects Tree
		objectsTree = new JTree((DefaultMutableTreeNode) null);
		objectsTree.setPreferredSize(new Dimension(400, 500));
		objectsTree.setTransferHandler(new ObjectsTreeTransferHandler(this));
		objectsTree.setDragEnabled(true);
		objectsTree.addKeyListener(new ObjectsTreeKeyListener(this));
		objectsTree.addMouseListener(new MouseAdapter() {

			@Override
			public void mousePressed(MouseEvent e) {
				if (SwingUtilities.isRightMouseButton(e)) {
					Object node = objectsTree.getLastSelectedPathComponent();
					if (node == null)
						return;
					Object target = ((DefaultMutableTreeNode) node).getUserObject();
					if (target instanceof ObjectCategory obj) {
						PopupUtils.forCat(previous, obj.toString(), instance).show(e.getComponent(), e.getX(),
								e.getY());
					}
				}
			}
		});
		objectsTree.setCellRenderer(new ObjectTreeRenderer(this));
		JScrollPane objectsScroll = new JScrollPane(objectsTree);

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

		searchPanel.setBorder(BorderFactory.createTitledBorder(registry.getAppAtlas().getValue("rightmenu.search")));

		right.add(searchPanel);

		// Registry Tree
		buildTree();
		registryTree.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		registryTree.setDragEnabled(true);
		registryTree.setTransferHandler(new OBJTreeTransferHandler(this));
		ToolTipManager.sharedInstance().registerComponent(registryTree);
		registryTree.setCellRenderer(registryTreeRend = new OBJTreeRenderer());

		JScrollPane scroll = new JScrollPane(registryTree);
		right.add(scroll);

		// Search Bar Listener
		searchBar.addActionListener(e -> {
			ArrayList<ObjectEntry> searchResult = new ArrayList<ObjectEntry>();
			if (searchBar.getText() != null && !searchBar.getText().equals(""))
				for (Bundle bundle : registry.getBundles().values()) {
					BundleRegistry regis = bundle.getBundleRegistry();
					for (ObjectEntry entry : regis.OBJECT_REGISTRY.values()) {
						if (entry.getFullID().contains(searchBar.getText())
								|| entry.getName().contains(searchBar.getText())) {
							searchResult.add(entry);
						}
					}
				}
			registryTreeRend.setSearchResult(searchResult);
			registryTree.clearSelection();
			for (ObjectEntry entry : searchResult) {
				registryTree.addSelectionPath(new TreePath(
						new Object[] { root, tabs.get(entry.getFullTab()), entries.get(entry.getFullID()) }));
			}
			registryTree.revalidate();
			registryTree.repaint();
		});

		frame.add(right, BorderLayout.EAST);

		// MenuBar
		menubar = new AppMenuBar(this);
		menubar.init();
		frame.setJMenuBar(menubar);

		holderTabs = new JTabbedPane();
		holderTabs.addChangeListener(e -> {
			if (previous != null) {
				previous.stop();
			}
			previous = (ModelHolder) holderTabs.getSelectedComponent();
			if (previous != null) {
				previous.start();
				((DefaultTreeModel) objectsTree.getModel()).setRoot(previous.getNodeRepresentation());
			}
			menubar.updateOnChange();
		});

		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, objectsScroll, holderTabs);
		splitPane.setDividerLocation(250);
		frame.add(splitPane, BorderLayout.CENTER);

		frame.pack();
		frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		menubar.updateOnChange();
	}

	public JTree getRegistryTree() {
		return registryTree;
	}

	public JTree getObjectsTree() {
		return objectsTree;
	}

	public void updateObjectsTree() {
		Enumeration<TreePath> expanded = objectsTree
				.getExpandedDescendants(new TreePath(new Object[] { objectsTree.getModel().getRoot() }));
		((DefaultTreeModel) objectsTree.getModel()).reload();
		Iterator<TreePath> it = expanded.asIterator();
		while (it.hasNext()) {
			objectsTree.expandPath(it.next());
		}
	}

	public JFrame getJFrame() {
		return frame;
	}

	public GlobalRegistry getGlobalRegistry() {
		return registry;
	}

	private void loadBundles(ArrayList<BundleLoadingPanel.BundleHolder> temp, IParametersRequester req) {
		Logger log = Logger.INSTANCE.derivateLogger("[BUNDLES]");
		for (BundleLoadingPanel.BundleHolder bundleHolder : temp) {
			if (!bundleHolder.used) {
				continue;
			}
			Bundle bundle = bundleHolder.b;
			registry.addBundle(bundle);
			try {
				bundle.loadTextAtlas(registry.getCurrentLanguage(), log);
				bundle.onLoad();
				bundle.getBundleRegistry().linkTabInstances();
				log.log("Loaded " + bundle.getID());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		Logger reg = log.derivateLogger("[REGISTRY]");

		reg.log("List of all registered object entires");
		for (Bundle bundle : registry.getBundles().values()) {
			for (ObjectEntry entry : bundle.getBundleRegistry().OBJECT_REGISTRY.values()) {
				reg.log(" -->" + entry.getFullID() + " : " + entry.getName());
			}
		}
	}

	private void buildTree() {
		root = new DefaultMutableTreeNode("AsgelLogicSimulator");

		tabs = new HashMap<String, DefaultMutableTreeNode>();
		entries = new HashMap<String, DefaultMutableTreeNode>();

		for (Bundle bundle : registry.getBundles().values()) {
			for (ModelTab tab : bundle.getBundleRegistry().TAB_REGISTRY.values()) {
				DefaultMutableTreeNode node = new DefaultMutableTreeNode(tab);
				tabs.put(tab.getFullID(), node);
				root.add(node);
			}
		}

		for (Bundle bundle : registry.getBundles().values()) {
			for (ObjectEntry entry : bundle.getBundleRegistry().OBJECT_REGISTRY.values()) {
				DefaultMutableTreeNode node = new DefaultMutableTreeNode(entry);
				tabs.get(entry.getFullTab()).add(node);
				entries.put(entry.getFullID(), node);
			}
		}
		registryTree = new JTree(root);
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

	public JTabbedPane getHolderTabs() {
		return holderTabs;
	}

	public File getWorkingDir() {
		return workingDir;
	}

	public AppMenuBar getMenuBar() {
		return menubar;
	}

	public IParametersRequester getParametersRequester() {
		return requester;
	}

	public String getText(String id) {
		return registry.getAppAtlas().getValue(id);
	}

}