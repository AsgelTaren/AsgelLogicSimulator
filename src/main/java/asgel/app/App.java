package asgel.app;

import java.awt.BorderLayout;
import java.awt.dnd.DropTarget;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.ToolTipManager;
import javax.swing.tree.DefaultMutableTreeNode;

import asgel.app.model.ModelHolder;
import asgel.app.model.OBJTreeDropTarget;
import asgel.app.model.OBJTreeRenderer;
import asgel.app.model.OBJTreeTransferHandler;
import asgel.core.gfx.Renderer;
import asgel.core.model.Model;
import asgel.core.model.ModelRegistry;
import asgel.core.model.ModelRegistry.ObjectEntry;
import asgel.core.model.ModelTab;

public class App implements Runnable, MouseListener, MouseMotionListener, MouseWheelListener {

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

	// Model Registry
	private ModelRegistry registry;

	// Object Tree
	private JTree tree;

	public App() {

	}

	private void init() {
		registry = new ModelRegistry();
		loadBundles();

		frame = new JFrame("AsgelLogicSimulator origins");
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		renderer = new Renderer(1600, 900, 10);
		frame.add(renderer);
		renderer.create();
		renderer.addMouseListener(this);
		renderer.addMouseMotionListener(this);
		renderer.addMouseWheelListener(this);

		// Tree
		buildTree();
		tree.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tree.setDragEnabled(true);
		tree.setTransferHandler(new OBJTreeTransferHandler(this));

		@SuppressWarnings("unused")
		DropTarget target = new DropTarget(renderer, new OBJTreeDropTarget(this));

		JScrollPane scroll = new JScrollPane(tree);
		ToolTipManager.sharedInstance().registerComponent(tree);
		tree.setCellRenderer(new OBJTreeRenderer());
		frame.add(scroll, BorderLayout.EAST);

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

	private void loadBundles() {
		bundles = new ArrayList<Bundle>();

		File root = new File(System.getenv("APPDATA") + "/AsgelLogicSim/bundles/");
		for (File f : root.listFiles()) {
			if (f.getName().endsWith(".jar")) {
				bundles.add(new Bundle(f));
			}
		}

		for (Bundle bundle : bundles) {
			try {
				bundle.loadDetails();
				System.out.println("[BUNDLES] Loaded details for " + bundle.getID() + " : " + bundle.getName());
			} catch (Exception e) {
				e.printStackTrace();
			}

			try {
				bundle.load(registry);
				System.out.println("[BUNDLES] Loaded " + bundle.getID());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		System.out.println("[REGISTRY] List of all registered object entires");
		for (ObjectEntry entry : registry.OBJECT_REGISTRY.values()) {
			try {
				entry.matchTab();
			} catch (Exception e) {
				e.printStackTrace();
			}
			System.out
					.println("[REGISTRY] -->" + entry.getId() + " from " + entry.getBundle() + " : " + entry.getName());
		}
	}

	private void buildTree() {
		DefaultMutableTreeNode root = new DefaultMutableTreeNode("AsgelLogicSimulator");

		HashMap<String, DefaultMutableTreeNode> tabs = new HashMap<String, DefaultMutableTreeNode>();

		for (Entry<String, ModelTab> entry : registry.TAB_REGISTRY.entrySet()) {
			DefaultMutableTreeNode node = new DefaultMutableTreeNode(entry.getValue());
			tabs.put(entry.getKey(), node);
			root.add(node);
		}

		for (ObjectEntry reg : registry.OBJECT_REGISTRY.values()) {
			tabs.get(reg.getTab().getID()).add(new DefaultMutableTreeNode(reg));
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

}