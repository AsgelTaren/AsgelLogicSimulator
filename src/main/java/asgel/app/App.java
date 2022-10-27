package asgel.app;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.dnd.DropTarget;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.ToolTipManager;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import asgel.app.bundle.Bundle;
import asgel.app.bundle.BundleLoadingPanel;
import asgel.app.model.ModelHolder;
import asgel.app.model.OBJTreeDropTarget;
import asgel.app.model.OBJTreeRenderer;
import asgel.app.model.OBJTreeTransferHandler;
import asgel.core.gfx.Renderer;
import asgel.core.model.BundleRegistry;
import asgel.core.model.BundleRegistry.ObjectEntry;
import asgel.core.model.GlobalRegistry;
import asgel.core.model.IParametersRequester;
import asgel.core.model.Model;
import asgel.core.model.ModelTab;

public class App implements Runnable, MouseListener, MouseMotionListener, MouseWheelListener, KeyListener {

	// Thread
	private Thread thread;
	private boolean running = false;

	// GFX
	private JFrame frame;
	private Renderer renderer;

	// Model
	private ModelHolder holder;

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

	private void init() {
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

		renderer = new Renderer(1600, 900, 18);
		frame.add(renderer);
		renderer.create();
		renderer.addMouseListener(this);
		renderer.addMouseMotionListener(this);
		renderer.addMouseWheelListener(this);
		renderer.addKeyListener(this);

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

		@SuppressWarnings("unused")
		DropTarget target = new DropTarget(renderer, new OBJTreeDropTarget(this));

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

		// MenuBar
		menubar = new AppMenuBar(this);
		menubar.init();
		frame.setJMenuBar(menubar);

		frame.pack();

		holder = new ModelHolder(new Model(), this);
	}

	public void run() {
		init();

		double FPS = 60.0;
		double time = 1_000_000_000 / FPS;
		double delta = 0;
		long last = System.nanoTime(), now;
		while (running) {
			now = System.nanoTime();
			delta += (now - last) / time;
			if (delta >= 1) {
				delta--;
				renderer.begin();
				holder.render(renderer);
				renderer.end();
			}
			now = last;
		}
	}

	public Renderer getRenderer() {
		return renderer;
	}

	public ModelHolder getModelHolder() {
		return holder;
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

	public GlobalRegistry getRegistry() {
		return registry;
	}

	public synchronized void start() {
		if (running)
			return;
		running = true;
		thread = new Thread(this);
		thread.start();
	}

	public synchronized void stop() {
		if (!running)
			return;
		running = false;
		try {
			thread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
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

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		holder.mouseWheelMoved(e);
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		holder.mouseDragged(e);
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		holder.mouseMoved(e);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		holder.mouseClicked(e);
	}

	@Override
	public void mousePressed(MouseEvent e) {
		holder.mousePressed(e);
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		holder.mouseReleased(e);
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		holder.mouseEntered(e);
	}

	@Override
	public void mouseExited(MouseEvent e) {
		holder.mouseExited(e);
	}

	@Override
	public void keyTyped(KeyEvent e) {
		holder.keyTyped(e);
	}

	@Override
	public void keyPressed(KeyEvent e) {
		holder.keyPressed(e);
	}

	@Override
	public void keyReleased(KeyEvent e) {
		holder.keyReleased(e);
	}

	public void setModel(Model m) {
		holder = new ModelHolder(m, this);
	}

}