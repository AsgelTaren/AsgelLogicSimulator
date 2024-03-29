package asgel.app.model;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DropTarget;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;

import asgel.app.App;
import asgel.app.Logger;
import asgel.app.ObjectCategory;
import asgel.app.Utils;
import asgel.core.gfx.Point;
import asgel.core.gfx.Renderer;
import asgel.core.model.BundleRegistry.ObjectEntry;
import asgel.core.model.Clickable;
import asgel.core.model.Link;
import asgel.core.model.Model;
import asgel.core.model.ModelOBJ;
import asgel.core.model.Pin;

/**
 * @author Florent Guille
 **/

@SuppressWarnings("serial")
public class ModelHolder extends JPanel
		implements MouseMotionListener, MouseListener, MouseWheelListener, KeyListener, Runnable {

	// Thread
	private Thread thread;
	private boolean running = false;

	// OBJ Flavor
	public static final DataFlavor OBJFLAVOR = new DataFlavor(ObjectEntry.class, "Flavor for object registry");

	// Model reference
	private Model model;

	// Camera pos
	private float camx, camy, zoom = 1.0f;
	private Point old;

	// App reference
	private App app;

	// Highlighted
	private ModelOBJ highOBJ;
	private Pin highPin, anchor;
	private Point delta;

	// Link path
	private ArrayList<Point> linkPath;
	private Link targetLink;
	private Pin targetPin;
	private Point nextToPath;
	private boolean horizontal;

	// Mouse
	private Point mouseInModel = new Point(0, 0);

	// To Add
	private ArrayList<ModelOBJ> objToAdd;

	// Renderer
	private Renderer renderer;

	// Name
	private String name;

	// Node representation
	private DefaultMutableTreeNode root, def, cats;
	private HashMap<String, DefaultMutableTreeNode> nodes;
	private HashMap<ModelOBJ, DefaultMutableTreeNode> objNodes;
	private HashMap<String, ImageIcon> catIconsInstances;

	public ModelHolder(Model model, App app, String name) {
		super();
		this.model = model;
		this.app = app;
		this.name = name;
		objToAdd = new ArrayList<ModelOBJ>();

		setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = gbc.weighty = 1;

		renderer = new Renderer(18);
		add(renderer, gbc);

		// Adding drop target
		@SuppressWarnings("unused")
		DropTarget target = new DropTarget(renderer, new OBJTreeDropTarget(this));

		// Adding listeners
		renderer.addMouseListener(this);
		renderer.addMouseMotionListener(this);
		renderer.addMouseWheelListener(this);
		renderer.addKeyListener(this);

		// Node representation
		reloadCatIcons();
		createNodeRepresentation();
	}

	@Override
	public void run() {

		double FPS = 60.0;
		double time = 1_000_000_000 / FPS;
		double delta = 0;
		long last = System.nanoTime(), now;
		while (running) {
			now = System.nanoTime();
			delta += (now - last) / time;
			if (delta >= 1) {
				delta--;
				render();
			}
			last = now;
		}
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

	public void render() {
		try {
			if (!renderer.begin()) {
				System.out.println("Error while trying to begin rendering for " + name);
				return;
			}
			renderer.begin();
			if (objToAdd.size() > 0) {
				model.getObjects().addAll(objToAdd);
				objToAdd = new ArrayList<ModelOBJ>();
			}
			if (mouseInModel == null)
				return;
			renderer.push();
			renderer.center();
			renderer.translate(-camx, -camy);
			renderer.scale(zoom);

			for (ModelOBJ obj : model.getObjects()) {
				obj.render(renderer, highOBJ, highPin, anchor);
			}

			renderer.setStroke(3);
			for (Link l : model.getLinks()) {
				if (l != targetLink)
					l.render(renderer);
			}
			if (targetLink != null) {
				for (int i = 0; i < linkPath.size() - 1; i++) {
					renderer.drawLine(linkPath.get(i), linkPath.get(i + 1), Color.BLACK);
				}
				renderer.drawLine(linkPath.get(linkPath.size() - 1), nextToPath, Color.BLACK);
			}

			if (anchor != null) {
				renderer.drawLine(anchor.posInModel(),
						(highPin != null && highPin.getLink() == null && highPin.isInput() != anchor.isInput()
								&& highPin.getSize() == anchor.getSize()) ? highPin.posInModel() : mouseInModel,
						Color.BLACK);
			}

			renderer.pop();

			renderer.drawString("Camera: " + camx + ", " + camy, 15, 15, Color.BLACK);
			renderer.drawString("Zoom: " + zoom, 15, 30, Color.BLACK);
			renderer.drawString("Mouse: " + mouseInModel.x + ", " + mouseInModel.y, 15, 45, Color.BLACK);
			renderer.drawString(
					"Model: " + model.getObjects().size() + " objects and " + model.getLinks().size() + " links", 15,
					60, Color.BLACK);

			if (highOBJ != null) {
				renderer.drawString(highOBJ.toString(), 15, 75, Color.BLACK);
			}

			if (highPin != null) {
				renderer.drawString("Pin: " + highPin.toString() + ", " + highPin.getRotation() + " | " + highPin.getX()
						+ ", " + highPin.getY(), 15, 90, Color.BLACK);
			}
			renderer.end();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public synchronized void addObject(ObjectEntry entry, int mousex, int mousey) {
		Point mouse = fromCameraToModel(new Point(mousex, mousey));
		ModelOBJ obj = entry.getProvider().apply(mouse);
		if (obj != null) {
			objToAdd.add(obj);
			obj.update();
			def.add(new DefaultMutableTreeNode(obj));
			app.updateObjectsTree();
		}
	}

	public Point fromCameraToModel(Point p) {
		return new Point((p.x + camx - (renderer.getWidth() >> 1)) / zoom,
				(p.y + camy - (renderer.getHeight() >> 1)) / zoom);
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		float oldzoom = zoom;
		zoom *= (float) Math.pow(1.15, e.getWheelRotation());
		camx = (renderer.getWidth() >> 1) - e.getX() + zoom / oldzoom * (e.getX() + camx - (renderer.getWidth() >> 1));
		camy = (renderer.getHeight() >> 1) - e.getY()
				+ zoom / oldzoom * (e.getY() + camy - (renderer.getHeight() >> 1));
		refreshHigh();
	}

	@Override
	public void mouseClicked(MouseEvent e) {

	}

	@Override
	public void mousePressed(MouseEvent e) {
		if (SwingUtilities.isLeftMouseButton(e)) {
			if (targetLink != null) {
				linkPath.add(nextToPath);
				nextToPath = new Point(mouseInModel);
				horizontal = !horizontal;
				if (highPin == targetPin) {
					linkPath.add(highPin.posInModel());
					targetLink.setPath(linkPath);
					targetLink = null;
					linkPath = null;
					targetPin = null;
					nextToPath = null;
					delta = null;
				}
				return;
			}
			if (highOBJ != null && highOBJ.isMoveable()) {
				delta = highOBJ.getPos().sub(mouseInModel);
				for (Pin p : highOBJ.getPins()) {
					if (p.getLink() != null) {
						p.getLink().setPath(null);
					}
				}
				return;
			} else if (highPin != null && highPin.getLink() == null) {
				anchor = highPin;

			} else {
				old = new Point(e.getX(), e.getY());
			}
			delta = null;
		} else if (SwingUtilities.isRightMouseButton(e)) {
			if (highOBJ != null) {
				PopupUtils.forModelOBJ(this, highOBJ, app).show(e.getComponent(), e.getX(), e.getY());
			} else if (highPin != null) {
				PopupUtils.forPin(this, highPin, app).show(e.getComponent(), e.getX(), e.getY());
			}
		} else if (SwingUtilities.isMiddleMouseButton(e)) {
			if (highPin != null && highPin.getLink() != null && !highPin.getModelOBJ().isMoveable()
					&& targetLink == null && !highPin.getLink().getOther(highPin).getModelOBJ().isMoveable()) {
				targetLink = highPin.getLink();
				linkPath = new ArrayList<Point>();
				linkPath.add(highPin.posInModel());
				nextToPath = highPin.posInModel();
				horizontal = ((highPin.getRealRotation().ordinal() & 1) == 0);
				targetPin = targetLink.getOther(highPin);
			}
		}

	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if (anchor != null && highPin != null && highPin.getLink() == null && highPin.getSize() == anchor.getSize()
				&& highPin.isInput() != anchor.isInput()) {
			Link l = Link.createAndApply(anchor, highPin);
			model.getLinks().add(l);
			ArrayList<ModelOBJ> temp = new ArrayList<ModelOBJ>();
			temp.add(l.getStart().getModelOBJ());
			model.refresh(temp);
		}
		anchor = null;
		delta = null;
		old = null;
	}

	@Override
	public void mouseEntered(MouseEvent e) {

	}

	@Override
	public void mouseExited(MouseEvent e) {

	}

	@Override
	public void mouseDragged(MouseEvent e) {
		mouseInModel = fromCameraToModel(new Point(e.getX(), e.getY()));
		if (SwingUtilities.isLeftMouseButton(e)) {
			if (highOBJ != null && anchor == null && delta != null) {
				highOBJ.setPos(delta.add(mouseInModel));
				if (e.isControlDown()) {
					highOBJ.setX(highOBJ.getX() / 5 * 5);
					highOBJ.setY(highOBJ.getY() / 5 * 5);
				}
			} else if (anchor != null) {
				refreshHigh();
				if (highPin == anchor) {
					highPin = null;
				}
			} else if (old != null) {
				Point n = new Point(e.getX(), e.getY());
				Point temp = old.sub(n);
				camx += temp.x;
				camy += temp.y;
				old = n;
			}
		}
		if (SwingUtilities.isMiddleMouseButton(e)) {

		}
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		mouseInModel = fromCameraToModel(new Point(e.getX(), e.getY()));
		refreshHigh();

		if (targetLink != null) {
			if (highPin == targetPin) {
				if (horizontal) {
					nextToPath.x = highPin.posInModel().x;
				} else {
					nextToPath.y = highPin.posInModel().y;
				}
			} else {
				if (horizontal) {
					nextToPath.x = mouseInModel.x;
				} else {
					nextToPath.y = mouseInModel.y;
				}
			}
		}
	}

	private void refreshHigh() {
		highOBJ = null;
		highPin = null;

		for (ModelOBJ obj : model.getObjects()) {
			Point point = obj.fromModelToObject(mouseInModel);
			if (obj.containsFromObject(point, 5)) {
				for (Pin p : obj.getPins()) {
					if (p.getPos().distSQ(point) < 64) {
						highPin = p;
						return;
					}
				}
				if (obj.containsFromObject(point, 0)) {
					highOBJ = obj;
					highOBJ.updateMousePos(point);
					return;
				}
			}
		}
	}

	private void createNodeRepresentation() {
		root = new DefaultMutableTreeNode(this);
		def = new DefaultMutableTreeNode("Default");
		cats = new DefaultMutableTreeNode("Categories");
		root.add(def);
		root.add(cats);
		nodes = new HashMap<>();
		objNodes = new HashMap<>();
		rebuildNodeRepresentation();
	}

	public void rebuildNodeRepresentation() {
		def.removeAllChildren();
		cats.removeAllChildren();
		for (ModelOBJ object : model.getObjects()) {
			DefaultMutableTreeNode objNode = objNodes.get(object);
			if (objNode == null) {
				objNode = new DefaultMutableTreeNode(object);
				objNodes.put(object, objNode);
			}
			if (object.getCategory() == null || "".equals(object.getCategory())) {
				def.add(objNode);
			} else {
				DefaultMutableTreeNode cat = nodes.get(object.getCategory());
				if (cat == null) {
					cat = new DefaultMutableTreeNode(new ObjectCategory(object.getCategory()));
					nodes.put(object.getCategory(), cat);
				}
				if (cat.getParent() == null) {
					cats.add(cat);
				}
				cat.add(objNode);
			}
		}
	}

	public void reloadCatIcons() {
		catIconsInstances = new HashMap<>();
		for (String val : model.getCatIcons().values()) {
			try {
				File target = Utils.resolvePath(model.getFile(), app.getWorkingDir(), val);
				ImageIcon result = Utils.loadIcon(new FileInputStream(target), 16);
				catIconsInstances.put(val, result);
			} catch (Exception e) {
				e.printStackTrace();
				Logger.INSTANCE.log("Error while trying to load icons under id: " + val);
			}
		}
		System.out.println(model.getCatIcons());
		System.out.println(catIconsInstances);
	}

	public DefaultMutableTreeNode getNodeRepresentation() {
		return root;
	}

	@Override
	public void keyTyped(KeyEvent e) {

	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_C) {
			if (highOBJ != null && highOBJ instanceof Clickable c) {
				if (e.isControlDown()) {
					c.reset();
				} else {
					c.onClick();
				}
				ArrayList<ModelOBJ> ref = new ArrayList<ModelOBJ>();
				ref.add(highOBJ);
				model.refresh(ref);
			}
		}
		if (e.getKeyCode() == KeyEvent.VK_R && highOBJ != null && highOBJ.isMoveable()) {
			highOBJ.setRotation(highOBJ.getRotation().next());
			for (Pin p : highOBJ.getPins()) {
				if (p.getLink() != null) {
					p.getLink().setPath(null);
				}
			}
		}
		if (e.getKeyCode() == KeyEvent.VK_S && e.isControlDown()) {
			if (model.getFile() == null) {
				app.getMenuBar().saveAs();
			} else {
				app.getMenuBar().save();
			}
		}

	}

	@Override
	public void keyReleased(KeyEvent e) {

	}

	public App getApp() {
		return app;
	}

	public HashMap<String, ImageIcon> getCatIconsInstances() {
		return catIconsInstances;
	}

	public Renderer getRenderer() {
		return renderer;
	}

	public Model getModel() {
		return model;
	}

	public File getFile() {
		return model.getFile();
	}

	public void setFile(File f) {
		this.model.setFile(f);
	}

	@Override
	public String toString() {
		return model.getFile() != null ? model.getFile().getName() : name;
	}

}