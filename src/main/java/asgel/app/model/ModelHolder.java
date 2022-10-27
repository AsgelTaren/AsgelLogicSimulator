package asgel.app.model;

import java.awt.Color;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;

import javax.swing.SwingUtilities;

import asgel.app.App;
import asgel.core.gfx.Point;
import asgel.core.gfx.Renderer;
import asgel.core.model.BundleRegistry.ObjectEntry;
import asgel.core.model.Clickable;
import asgel.core.model.Link;
import asgel.core.model.Model;
import asgel.core.model.ModelOBJ;
import asgel.core.model.Pin;

public class ModelHolder implements MouseMotionListener, MouseListener, MouseWheelListener, KeyListener {

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
	private Point mouseInModel;

	// To Add
	private ArrayList<ModelOBJ> objToAdd;

	public ModelHolder(Model model, App app) {
		this.model = model;
		this.app = app;

		objToAdd = new ArrayList<ModelOBJ>();
	}

	public void render(Renderer renderer) {
		try {
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
		}
	}

	public Point fromCameraToModel(Point p) {
		return new Point((p.x + camx - (app.getRenderer().getWidth() >> 1)) / zoom,
				(p.y + camy - (app.getRenderer().getHeight() >> 1)) / zoom);
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		float oldzoom = zoom;
		zoom *= (float) Math.pow(1.15, e.getWheelRotation());
		camx = (app.getRenderer().getWidth() >> 1) - e.getX()
				+ zoom / oldzoom * (e.getX() + camx - (app.getRenderer().getWidth() >> 1));
		camy = (app.getRenderer().getHeight() >> 1) - e.getY()
				+ zoom / oldzoom * (e.getY() + camy - (app.getRenderer().getHeight() >> 1));
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
				PopupUtils.forModelOBJ(this, highOBJ).show(e.getComponent(), e.getX(), e.getY());
			} else if (highPin != null) {
				PopupUtils.forPin(this, highPin).show(e.getComponent(), e.getX(), e.getY());
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
			} else {
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

	@Override
	public void keyTyped(KeyEvent e) {

	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_C) {
			if (highOBJ != null && highOBJ instanceof Clickable c) {
				c.onClick();
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

	}

	@Override
	public void keyReleased(KeyEvent e) {

	}

	public App getApp() {
		return app;
	}

	public Model getModel() {
		return model;
	}

}